package com.beoni.openwaterswimtracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beoninet.android.easymessage.EasyMessageManager;
import com.google.android.gms.wearable.MessageEvent;


public class WearMainActivity extends WearableActivity
{
    private static final String TAG = "OWST.WearMainActivity";

    //these two paths must be defined on phone module too
    public static final String MSG_SWIM_DATA_AVAILABLE = WearMainActivity.class.getPackage().getName()+".MSG_SWIM_DATA_AVAILABLE";
    public static final String MSG_SWIM_DATA_RECEIVED = WearMainActivity.class.getPackage().getName()+".MSG_SWIM_DATA_RECEIVED";

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
                if(messageEvent.getPath().equals(MSG_SWIM_DATA_RECEIVED))
                    Toast.makeText(getContext(),R.string.swim_data_downlaoded,Toast.LENGTH_LONG).show();
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
    protected void onStop()
    {
        super.onStop();
        easyMessageManager.disconnect();
    }

    public void sendMessage(View v){
        if(easyMessageManager.isConnected())
        {
            SharedPreferences mSharedPref = getSharedPreferences(getString(R.string.locations_list_pref), Context.MODE_PRIVATE);
            mSwimmingTrackStorage = SwimmingTrackStorage.get(mSharedPref);

            easyMessageManager.sendMessage(MSG_SWIM_DATA_AVAILABLE, mSwimmingTrackStorage.getAllAsString());
        }
        else
            Toast.makeText(this,R.string.missing_device_connection,Toast.LENGTH_LONG).show();
    }

}
