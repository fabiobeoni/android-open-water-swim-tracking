package com.beoni.openwaterswimtracking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beoninet.android.easymessage.EasyMessageManager;
import com.beoninet.android.easymessage.INodeConnection;
import com.beoninet.openwaterswimtracking.shared.Constants;
import com.beoninet.openwaterswimtracking.shared.ICallback;
import com.beoninet.openwaterswimtracking.shared.SwimTrackCalculator;
import com.google.android.gms.wearable.MessageEvent;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class WearMainActivity extends WearableActivity
{
    private static final String TAG = WearMainActivity.class.getSimpleName();

    private static final int STATE_DEFAULT = 0;

    private TextView mHoursTxw;
    private TextView mDistanceTxw;
    private ImageButton mStartSwimTrackBtn;
    private ImageButton mSendDataToDeviceBtn;

    private EasyMessageManager easyMessageManager;
    private SwimmingTrackStorage mSwimmingTrackStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);

        mSwimmingTrackStorage = new SwimmingTrackStorage(this);

        mHoursTxw = findViewById(R.id.hoursTxw);
        mDistanceTxw = findViewById(R.id.distanceTxw);
        mStartSwimTrackBtn = findViewById(R.id.startTrackingBtn);
        mSendDataToDeviceBtn = findViewById(R.id.sendDataToDeviceBtn);

        easyMessageManager = new EasyMessageManager(this){
            @Override
            public void onMessageReceived(MessageEvent messageEvent)
            {
                if(messageEvent.getPath().equals(Constants.MSG_SWIM_MESSAGE_RECEIVED))
                    Toast.makeText(getContext(),new String(messageEvent.getData()),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMessageDelivered(boolean result)
            {
                ((Activity)getContext()).runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getContext(),R.string.data_delivered,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        easyMessageManager.connect();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mSwimmingTrackStorage.getAllLocationsAsync(true, new ICallback<List<Location>>(){
            @Override
            public void completed(List<Location> locations)
            {
                displayTrackData(locations);
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        easyMessageManager.disconnect();
    }

    private void displayTrackData(final List<Location> locations)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                float totalDistance = SwimTrackCalculator.calculateDistance(locations,true);
                mDistanceTxw.setText(getString(R.string.last_track_distance, String.valueOf(totalDistance)));

                long duration = 0;
                if(locations!=null && locations.size()>0)
                    duration = SwimTrackCalculator.calculateDuration(locations.get(0),locations.get(locations.size()-1));

                mHoursTxw.setText(getString(R.string.last_track_time_length,
                        String.valueOf(TimeUnit.MILLISECONDS.toHours(duration)),
                        String.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)),
                        String.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration))
                ));
            }
        });
    }

    private void resetTrackAndStartNewOne()
    {
        mSwimmingTrackStorage.deleteAllAsync(new ICallback<Boolean>()
        {
            @Override
            public void completed(Boolean isCompleted)
            {
                Intent intent = new Intent(WearMainActivity.this, TrackingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //to update the activity intent and related data
                startActivity(intent);
            }
        });
    }

    public void btnStartTrackingOnClick(View view)
    {
        mSwimmingTrackStorage.getAllLocationsAsync(false, new ICallback<List<Location>>()
        {
            @Override
            public void completed(final List<Location> locations)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(locations!=null && locations.size()>0)
                            new AlertDialog.Builder(WearMainActivity.this)
                                    .setMessage(R.string.override_track_question)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int whichButton)
                                        {
                                            resetTrackAndStartNewOne();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null).show();
                        else
                            resetTrackAndStartNewOne();
                    }
                });
            }
        });
    }

    public void btnSendDataToDeviceOnClick(final View view){ //button click on view
        easyMessageManager.checkNodes(new INodeConnection()
        {
            @Override
            public void onNodeCheckCompleted(boolean hsNodes)
            {
                //message delivered only when there are actually nodes
                //connected
                if(easyMessageManager.hasNodes())
                    //no need to use async task since here your are already in other context
                    easyMessageManager.sendMessage(
                            Constants.MSG_SWIM_DATA_AVAILABLE,
                            mSwimmingTrackStorage.loadLocationsAsString()
                    );
                else
                    ((Activity)view.getContext()).runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(view.getContext(),R.string.missing_device_connection,Toast.LENGTH_LONG).show();
                        }
                    });

            }
        });
    }
}
