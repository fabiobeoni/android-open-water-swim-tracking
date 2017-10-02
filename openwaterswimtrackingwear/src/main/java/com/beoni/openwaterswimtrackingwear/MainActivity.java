package com.beoni.openwaterswimtrackingwear;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.thanosfisherman.mayi.Mayi;
import com.thanosfisherman.mayi.PermissionBean;
import com.thanosfisherman.mayi.PermissionToken;
import com.thanosfisherman.mayi.listeners.MayiErrorListener;
import com.thanosfisherman.mayi.listeners.multi.PermissionResultMultiListener;
import com.thanosfisherman.mayi.listeners.multi.RationaleMultiListener;

import java.util.Arrays;

public class MainActivity extends Activity
{

    private static final String TAG = "OWSTW.MainActivity";
    private TextView mTextView;
    private Button mBtnStart;
    private Button mBtnStop;

    private GPSLocationChangeReceiver gpsReceiver;
    private IntentFilter receiverIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub)
            {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mBtnStart = (Button) stub.findViewById(R.id.btnStartGPS);
                mBtnStop = (Button) stub.findViewById(R.id.btnStopGPS);
                mBtnStart.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Mayi.withActivity(MainActivity.this)
                                .withPermissions(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                                .onRationale(new RationaleMultiListener(){
                                    @Override
                                    public void onRationale(@NonNull PermissionBean[] permissions, @NonNull PermissionToken token)
                                    {
                                        Toast.makeText(MainActivity.this, "Please allow GPS tracking. " + Arrays.deepToString(permissions), Toast.LENGTH_LONG).show();
                                        token.continuePermissionRequest(); //request again previously denied permission.
                                    }
                                })
                                .onResult(new PermissionResultMultiListener(){
                                    @Override
                                    public void permissionResults(@NonNull PermissionBean[] permissions)
                                    {
                                        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                                        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                            startService(new Intent(MainActivity.this,GPSTrackingIntService.class));
                                        else
                                            Toast.makeText(MainActivity.this, "Please enable GPS in settings.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .onErrorListener(new MayiErrorListener(){
                                    @Override
                                    public void onError(Exception e)
                                    {
                                        Toast.makeText(MainActivity.this, "Error " + e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .check();
                    }
                });
            }
        });

        receiverIntent = new IntentFilter(GPSTrackingIntService.GPS_LOCATION_CHANGED);

        gpsReceiver = new GPSLocationChangeReceiver(){
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent!=null && intent.getAction().equals(GPSTrackingIntService.GPS_LOCATION_CHANGED))
                    if(mTextView!=null) //just to stay safe
                        mTextView.setText(intent.getStringExtra(GPSTrackingIntService.GPS_LOCATION_KEY));
            }
        };
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(gpsReceiver, receiverIntent);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(gpsReceiver);
    }
}
