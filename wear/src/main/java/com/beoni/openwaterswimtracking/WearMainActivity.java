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
import android.widget.TextView;
import android.widget.Toast;

import com.beoninet.android.easymessage.EasyMessageManager;
import com.beoninet.android.easymessage.INodeConnection;
import com.beoninet.openwaterswimtracking.shared.Constants;
import com.beoninet.openwaterswimtracking.shared.ICallback;
import com.beoninet.openwaterswimtracking.shared.SwimTrackCalculator;
import com.google.android.gms.wearable.MessageEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Starting activity, shows UI controls to:
 * 1. start a new recording session of GPS locations,
 * 2. display last swimming track duration and distance,
 * 3. start sending track data to handled device
 */
public class WearMainActivity extends WearableActivity
{
    private static final String TAG = WearMainActivity.class.getSimpleName();

    /**
     * No swimming track data available yet to display, first time access only.
     * User can start a new tracking, sync button is disabled instead.
     */
    private static final int UI_STATE_NO_SWIM = 10;

    /**
     * Last track about swimming are stored on wear
     * and can be displayed to the user.
     * User can start a new tracking, sync feature is also on.
     */
    private static final int UI_STATE_HAS_SWIM = 20;

    /**
     * Sending swimming track data to the handled
     * device, that's a really short time and you
     * may not notice it while running the app.
     * User can not start a new tracking, sync button
     * is disabled instead during data transfer.
     */
    private static final int UI_STATE_SYNC_PROGRESS = 30;

    /**
     * Data have been sent to the handled device,
     * User can start a new tracking, sync feature is also on.
     */
    private static final int UI_STATE_SYNC_COMPLETED = 40;

    //All UI controls
    private TextView mSwimDurationTxw;
    private TextView mSwimDistanceTxw;
    private TextView mStatusMessageTxw;
    private ImageButton mStartSwimTrackBtn;
    private ImageButton mSendDataToDeviceBtn;

    /**
     * Instance of the library utility class
     * to quickly send and receive messages
     * by the Message API channel from/to
     * wear/phone
     */
    private EasyMessageManager easyMessageManager;

    /**
     * Instance of class storing GPS locations
     * recorded from the wear into h memory.
     */
    private SwimmingTrackStorage mSwimmingTrackStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);

        mSwimmingTrackStorage = new SwimmingTrackStorage(this);

        mSwimDurationTxw = findViewById(R.id.swimDurationTxw);
        mSwimDistanceTxw = findViewById(R.id.swimDistanceTxw);
        mStatusMessageTxw = findViewById(R.id.statusMessageTxw);
        mStartSwimTrackBtn = findViewById(R.id.startTrackingBtn);
        mSendDataToDeviceBtn = findViewById(R.id.sendDataToDeviceBtn);

        //Registers message listeners to communicate
        //with the handled device.
        //MessageManager is connected on Start and disconnected
        //on Stop
        easyMessageManager = new EasyMessageManager(this){
            @Override
            public void onMessageReceived(MessageEvent messageEvent)
            {
                if(messageEvent.getPath().equals(Constants.MSG_SWIM_MESSAGE_RECEIVED))
                    Toast.makeText(getContext(),new String(messageEvent.getData()),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMessageDelivered(final boolean result)
            {
                ((Activity)getContext()).runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(result)
                        {
                            setUIState(UI_STATE_SYNC_COMPLETED);
                            Toast.makeText(getContext(), R.string.data_delivered, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            setUIState(UI_STATE_HAS_SWIM);
                            Toast.makeText(getContext(), R.string.error_on_data_sync, Toast.LENGTH_LONG).show();
                        }
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

        //gets all available GPS locations and
        // updates the UI according to available data
        mSwimmingTrackStorage.getAllLocationsAsync(true, new ICallback<List<Location>>(){
            @Override
            public void completed(final List<Location> locations)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(locations!=null && locations.size()>0)
                            setUIState(UI_STATE_HAS_SWIM);
                        else
                            setUIState(UI_STATE_NO_SWIM);

                        displayTrackData(locations);
                    }
                });
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        easyMessageManager.disconnect();
    }

    /**
     * Set all UI controls status
     * according to data availability.
     * @param state
     */
    private void setUIState(int state){
        switch (state){
            case UI_STATE_NO_SWIM:
                mStartSwimTrackBtn.setEnabled(true);
                mStartSwimTrackBtn.setBackgroundColor(getResources().getColor(R.color.image_button_dark));
                mSendDataToDeviceBtn.setEnabled(false);
                mSendDataToDeviceBtn.setBackgroundColor(getResources().getColor(R.color.image_button_disabled));
                mStatusMessageTxw.setText(R.string.no_track);
                break;

            case UI_STATE_HAS_SWIM:
                mStartSwimTrackBtn.setEnabled(true);
                mStartSwimTrackBtn.setBackgroundColor(getResources().getColor(R.color.image_button_dark));
                mSendDataToDeviceBtn.setEnabled(true);
                mSendDataToDeviceBtn.setBackgroundColor(getResources().getColor(R.color.image_button_light));
                mStatusMessageTxw.setText(R.string.to_be_sync);
                break;

            case UI_STATE_SYNC_PROGRESS:
                mStartSwimTrackBtn.setEnabled(false);
                mStartSwimTrackBtn.setBackgroundColor(getResources().getColor(R.color.image_button_disabled));
                mSendDataToDeviceBtn.setEnabled(false);
                mSendDataToDeviceBtn.setBackgroundColor(getResources().getColor(R.color.image_button_disabled));
                mStatusMessageTxw.setText(R.string.sync_in_progress);
                break;

            case UI_STATE_SYNC_COMPLETED:
                mStartSwimTrackBtn.setEnabled(true);
                mStartSwimTrackBtn.setBackgroundColor(getResources().getColor(R.color.image_button_dark));
                mSendDataToDeviceBtn.setEnabled(true);
                mSendDataToDeviceBtn.setBackgroundColor(getResources().getColor(R.color.image_button_light));
                mStatusMessageTxw.setText(R.string.sync_completed);
                break;
        }
    }

    /**
     * Shows swimming track distance and duration
     * on UI with needed formatting.
     * @param locations
     */
    private void displayTrackData(final List<Location> locations)
    {
        float totalDistance = SwimTrackCalculator.calculateDistance(locations,true);
        mSwimDistanceTxw.setText(getString(R.string.last_track_distance, String.valueOf(totalDistance)));

        long duration = 0;
        if(locations!=null && locations.size()>0)
            duration = SwimTrackCalculator.calculateDuration(locations.get(0),locations.get(locations.size()-1));

        mSwimDurationTxw.setText(getString(R.string.last_track_time_length,
                String.valueOf(TimeUnit.MILLISECONDS.toHours(duration)),
                String.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)),
                String.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration))
        ));
    }

    /**
     * Deletes current existing swimming
     * track data and redirect user to
     * tracking activity when done.
     */
    private void resetTrackAndStartNewOne()
    {
        mSwimmingTrackStorage.deleteAllAsync(new ICallback<Boolean>()
        {
            //TODO: add check about deleting result.
            @Override
            public void completed(Boolean isCompleted)
            {
                Intent intent = new Intent(WearMainActivity.this, TrackingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //to update the activity intent and related data
                startActivity(intent);
            }
        });
    }

    /**
     * When user wants to start a new tracking
     * checks if a track is store and ask to
     * confirm before proceeding to override.
     * @param view
     */
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

    /**
     * Starts sending data to the handled device.
     * Connected device is needed, data are sent
     * using the Message API channel and serialized
     * as text.
     * @param view
     */
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
                    //no need to use async task since here your are already in other context
                    easyMessageManager.sendMessage(
                            Constants.MSG_SWIM_DATA_AVAILABLE,
                            mSwimmingTrackStorage.loadLocationsAsString()
                    );

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setUIState(UI_STATE_SYNC_PROGRESS);
                        }
                    });
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
}
