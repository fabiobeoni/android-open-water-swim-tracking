package com.beoni.openwaterswimtracking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.beoninet.android.easymessage.EasyMessageManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

//todo: the service doesn't survive to application kills, so use a broadcast receiver to get notified of bluetooth connection established => start this service to listen for messages from wear.
public class WearMessageListener extends Service
{
    private static final String MESSAGE_EVENT = "com.google.android.gms.wearable.MESSAGE_RECEIVED";

    private EasyMessageManager mEasyMessageManager;

    public WearMessageListener()
    {

    }

    public static void startService(Context ctx){
        Intent intent = new Intent(ctx, WearMessageListener.class);
        intent.setAction(MESSAGE_EVENT);
        ctx.startService(intent);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mEasyMessageManager = new EasyMessageManager(getBaseContext()){
            @Override
            public void onMessageReceived(MessageEvent messageEvent)
            {
                String data = new String(messageEvent.getData());

                data = "just-for-test";

                //TODO: 1. start swim edit activity and pass GPS data to it
                Intent showActivityInt = new Intent(getContext(),MainActivity_.class);
                showActivityInt.putExtra(MainActivity.WEAR_DATA_KEY,data);
                showActivityInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(showActivityInt);
            }
        };
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        mEasyMessageManager.connect();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mEasyMessageManager.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
