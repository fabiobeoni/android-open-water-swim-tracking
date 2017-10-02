package com.beoni.openwaterswimtrackingwear;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


public class GPSTrackingIntService extends Service implements LocationListener
{
    private static final String TAG = "OWST.GPSTrackingService";
    public static final String GPS_LOCATION_CHANGED = "com.beoni.openwaterswimtrackingwear.action.GPS_LOCATION_CHANGED";
    public static final String GPS_LOCATION_KEY = "GPS_LOCATION_KEY";

    private LocationManager locationManager;


    public GPSTrackingIntService()
    {
        super();
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId)
    {
        super.onStart(intent, startId);
        try
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,this);
        }
        catch (SecurityException se){
            Log.e(TAG,se.getMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        String loc = String.valueOf(location.getLongitude()) + " || " + String.valueOf(location.getLatitude());

        Intent intent = new Intent(GPS_LOCATION_CHANGED); //new Intent(this,MainActivity.class);
        intent.setAction(GPS_LOCATION_CHANGED);
        intent.putExtra(GPS_LOCATION_KEY,loc);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(locationManager!=null)
            locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle){/* TODO: check docs */}

    @Override
    public void onProviderEnabled(String s){/* TODO: check docs */}

    @Override
    public void onProviderDisabled(String s) {/* TODO: check docs */}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null; /* not needed */}
}
