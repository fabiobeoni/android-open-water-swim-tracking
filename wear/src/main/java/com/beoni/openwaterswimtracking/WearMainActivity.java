package com.beoni.openwaterswimtracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beoninet.android.easymessage.EasyMessageManager;
import com.beoninet.android.easymessage.INodeConnection;
import com.beoninet.openwaterswimtracking.shared.Constants;
import com.google.android.gms.wearable.MessageEvent;


public class WearMainActivity extends WearableActivity
{
    private static final String TAG = WearMainActivity.class.getSimpleName();

    private TextView mMessageTxt;

    private EasyMessageManager easyMessageManager;
    private SwimmingTrackStorage mSwimmingTrackStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);

        mMessageTxt = findViewById(R.id.messageTxt);

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

    public void btnSendMessageOnClick(final View view){ //button click on view

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
                            mSwimmingTrackStorage.getAllAsString()
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

    @Override
    protected void onStart()
    {
        super.onStart();
        easyMessageManager.connect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        easyMessageManager.disconnect();
    }

    public void btnStartTrackingOnClick(View view)
    {
        Intent intent = new Intent(this, TrackingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //to update the activity intent and related data
        startActivity(intent);
    }
}
