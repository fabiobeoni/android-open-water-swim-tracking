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
    private boolean mHasNodes = false;
    private Context mContext;

    public Context getContext()
    {
        return mContext;
    }
    public boolean hasNodes() { return mHasNodes; }

    public EasyMessageManager(Context context){
        mContext = context;
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

    public boolean isConnected(){
        return mGoogleApiClient.isConnected();
    }

    public void sendMessage(final String path, final String msg){
        sendMessage(path,msg,null);
    }

    public void sendMessage(final String path, final String msg, @Nullable final String[] nodeIDs){
        if(mGoogleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(nodeIDs!=null && nodeIDs.length>0)
                        for (String nodeID:nodeIDs)
                            performSending(nodeID, path, msg);
                    else
                    {
                        List<Node>nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
                        for (Node node : nodes)
                            performSending(node.getId(), path, msg);
                    }
                }
            }).start();
        }
        else
            Log.e(TAG, GOOGLE_API_NOT_CONNECTED);
    }

    private void performSending(String nodeID, String path, String msg)
    {
        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                mGoogleApiClient,
                nodeID,
                path,
                msg.getBytes()
        ).await();

        onMessageDelivered(result.getStatus().isSuccess());
    }

    /**
     *
     * @param iNodeConnection: if null, no callback invoked
     */
    public void checkNodes(final INodeConnection iNodeConnection){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Node>nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
                mHasNodes = (nodes.size()>0);

                if(iNodeConnection!=null)
                    iNodeConnection.onNodeCheckCompleted(mHasNodes);
            }
        }).start();
    }

    public void onMessageDelivered(boolean result){}

    /**
     * Mandatory to invoke super class method
     * to check the connected nodes and invoke
     * the callback method onHasNode() check is
     * completed.
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient,this);
        checkNodes(null);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {}
}
