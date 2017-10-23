package com.beoni.openwaterswimtracking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.text.format.DateUtils;
import android.text.style.TtsSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beoninet.android.easymessage.EasyMessageManager;
import com.beoninet.android.easymessage.INodeConnection;
import com.beoninet.openwaterswimtracking.shared.Constants;
import com.google.android.gms.wearable.MessageEvent;

import java.text.DecimalFormat;
import java.util.List;
import java.util.StringTokenizer;


public class WearMainActivity extends WearableActivity
{
    private static final String TAG = WearMainActivity.class.getSimpleName();

    private TextView mHoursTxw;
    private TextView mDistanceTxw;
    private ImageView mSyncImg;
    private ImageButton mStartSwimTrackBtn;
    private ImageButton mSendDataToDeviceBtn;

    private EasyMessageManager easyMessageManager;
    private SwimmingTrackStorage mSwimmingTrackStorage;

    private List<Location> mLocations;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);

        SharedPreferences mSharedPref = getSharedPreferences(getString(R.string.locations_list_pref), Context.MODE_PRIVATE);

        mSwimmingTrackStorage = SwimmingTrackStorage.get(mSharedPref);

        mHoursTxw = findViewById(R.id.hoursTxw);
        mDistanceTxw = findViewById(R.id.distanceTxw);
        mSyncImg = findViewById(R.id.syncStatusImg);
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
        bindData();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        easyMessageManager.disconnect();
    }

    private void bindData()
    {
        hasData(); //populates mLocations list used below

        int totalDistance = calculateTotalDistance(mLocations);
        mDistanceTxw.setText(getString(R.string.last_track_distance, String.valueOf(totalDistance)));

        String duration = calculateDuration(mLocations);
        mHoursTxw.setText(getString(R.string.last_track_time_length, String.valueOf(duration)));
    }

    private String calculateDuration(List<Location> locations)
    {
        String result = "0.00";
        if(hasData())
        {
            long startTime = locations.get(0).getTime();
            long endTime = locations.get(locations.size() - 1).getTime();
            double duration = (double)((endTime - startTime) / 1000 / 60); //in hours
            DecimalFormat df = new DecimalFormat("#.##");
            result = df.format(duration);
        }

        return result;
    }

    private int calculateTotalDistance(List<Location> locations)
    {
        int totalDistance = 0;

        if(hasData()){
            Location lastLocation = null;
            for (Location location:locations)
            {
                if(lastLocation==null)
                    lastLocation = location;

                totalDistance += location.distanceTo(lastLocation);
            }
        }

        return totalDistance;
    }

    private boolean hasData(){
        mLocations = mSwimmingTrackStorage.getAllLocations();
        return mLocations.size()>0;
    }

    private void navigateToSwimTrack()
    {
        Intent intent = new Intent(WearMainActivity.this, TrackingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //to update the activity intent and related data
        startActivity(intent);
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
                {
                    SharedPreferences mSharedPref = getSharedPreferences(getString(R.string.locations_list_pref), Context.MODE_PRIVATE);
                    mSwimmingTrackStorage = SwimmingTrackStorage.get(mSharedPref);

                    easyMessageManager.sendMessage(
                            Constants.MSG_SWIM_DATA_AVAILABLE,
                            mSwimmingTrackStorage.getAllLocationsAsString()
                    );
                }
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

    public void btnStartTrackingOnClick(View view)
    {
        if(hasData())
            new AlertDialog.Builder(this)
                .setMessage(R.string.override_track_question)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        navigateToSwimTrack();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
        else
            navigateToSwimTrack();
    }
}
