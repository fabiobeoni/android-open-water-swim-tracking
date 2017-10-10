package com.beoninet.android.easymessage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;


public class EasyMessageManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        MessageApi.MessageListener
{
    private final static String TAG = "EasyMessageManager";
    private static final String GOOGLE_API_NOT_CONNECTED = "Google API Client is not connected and cannot send the message to nodes.";

    private GoogleApiClient mGoogleApiClient;

    public EasyMessageManager(Context context){
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect(){
        mGoogleApiClient.connect();
    }

    public void disconnect(){
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public List<Node> getNodes(){
        return Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
    }

    public void sendMessage(final String path, final String msg, @Nullable final String nodeID){

        final List<Node> nodes = getNodes();

        if(mGoogleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(nodeID!=null)
                        performSending(nodeID, path, msg);
                    else
                       for(Node node : nodes)
                           performSending(node.getId(), path, msg);
                }
            }).start();
        }
        else
            Log.e(TAG, GOOGLE_API_NOT_CONNECTED);
    }

    private boolean performSending(String nodeID, String path, String msg)
    {
        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                mGoogleApiClient,
                nodeID,
                path,
                msg.getBytes()
        ).await();

        return result.getStatus().isSuccess();
    }

    /**
     * Mandatory to invoke super class method.
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient,this);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {}
}
