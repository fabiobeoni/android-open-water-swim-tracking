package com.beoni.openwaterswimtracking;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
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

import java.lang.ref.WeakReference;


public class TrackingActivity extends WearableActivity
{
    private static final String TAG = "OWST.Wear.TrackingActivity";

    /**
     * Custom 'what' for Message sent to Handler.
     */
    private static final int MSG_UPDATE_SCREEN = 0;

    /**
     * Milliseconds between updates based on state.
     */
    private static final long ACTIVE_INTERVAL_MS = (1000);  //1 second
    //TODO:consider moving app settings to let the user decide...
    private static final long AMBIENT_INTERVAL_MS = (2 * 60 * 1000); //2 minutes
    private static final long MIN_MINUTES = (0); //0 milli-seconds
    private static final long MIN_METERS = 100; //100 meters
    private static final int AMBIENT_INT_REQ_CODE = 0;

    /**
     * Since the handler (used in active mode) can't wake up the processor when the device is in
     * ambient mode and undocked, we use an Alarm to cover ambient mode updates when we need them
     * more frequently than every minute. Remember, if getting updates once a minute in ambient
     * mode is enough, you can do away with the Alarm code and just rely on the onUpdateAmbient()
     * callback.
     */
    private AlarmManager mAmbientStateAlarmManager;
    private PendingIntent mAmbientStatePendingIntent;

    /**
     * This custom handler is used for updates in "Active" mode. We use a separate static class to
     * help us avoid memory leaks.
     */
    private final Handler mActiveModeUpdateHandler = new UpdateHandler(this);

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastLocation;
    private float mTotalDistance = 0;

    private TextView mTotalDistanceTxt;
    private View mContainerView;
    private Button mStopBtn;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tracking);

        setAmbientEnabled();


        mContainerView = findViewById(R.id.container);
        mTotalDistanceTxt = findViewById(R.id.totalDistanceTxt);

        mAmbientStateAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent ambientStateIntent = new Intent(getApplicationContext(), TrackingActivity.class);

        mAmbientStatePendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                AMBIENT_INT_REQ_CODE,
                ambientStateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        refreshDisplayAndSetNextUpdate();
    }

    //TODO: moved from onStart()
    @Override
    protected void onResume()
    {
        super.onResume();

        Mayi.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .onRationale(new RationaleMultiListener()
                {
                    @Override
                    public void onRationale(@NonNull PermissionBean[] permissions, @NonNull PermissionToken token)
                    {
                        Toast.makeText(TrackingActivity.this, R.string.allow_location_alert, Toast.LENGTH_LONG).show();
                        token.continuePermissionRequest(); //request again previously denied permission.
                    }
                })
                .onResult(new PermissionResultMultiListener()
                {
                    @Override
                    public void permissionResults(@NonNull PermissionBean[] permissions)
                    {
                        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        {
                            Toast.makeText(TrackingActivity.this, R.string.enable_location_alert, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                })
                .onErrorListener(new MayiErrorListener()
                {
                    @Override
                    public void onError(Exception e)
                    {
                        Toast.makeText(TrackingActivity.this, (R.string.error_label + e.toString()), Toast.LENGTH_LONG).show();
                    }
                })
                .check();
    }

    /**
     * This is mostly triggered by the Alarms we set in Ambient mode and informs us we need to
     * update the screen (and process any data).
     */
    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        refreshDisplayAndSetNextUpdate();
    }

    @Override
    public void onDestroy()
    {
        mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
        mAmbientStateAlarmManager.cancel(mAmbientStatePendingIntent);

        super.onDestroy();
    }

    /**
     * Prepares UI for Ambient view.
     */
    @Override
    public void onEnterAmbient(Bundle ambientDetails)
    {
        super.onEnterAmbient(ambientDetails);

        /** Clears Handler queue (only needed for updates in active mode). */
        mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);

        /**
         * Following best practices outlined in WatchFaces API (keeping most pixels black,
         * avoiding large blocks of white pixels, using only black and white,
         * and disabling anti-aliasing anti-aliasing, etc.)
         */
        mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        mTotalDistanceTxt.setTextColor(Color.WHITE);
        mTotalDistanceTxt.getPaint().setAntiAlias(false);

        refreshDisplayAndSetNextUpdate();
    }

    /**
     * Updates UI in Ambient view (once a minute). Because we need to update UI sooner than that
     * (every ~20 seconds), we also use an Alarm. However, since the processor is awake for this
     * callback, we might as well call refreshDisplayAndSetNextUpdate() to update screen and reset
     * the Alarm.
     * <p>
     * If you are happy with just updating the screen once a minute in Ambient Mode (which will be
     * the case a majority of the time), then you can just use this method and remove all
     * references/code regarding Alarms.
     */
    @Override
    public void onUpdateAmbient()
    {
        super.onUpdateAmbient();
        refreshDisplayAndSetNextUpdate();
    }

    /**
     * Prepares UI for Active view (non-Ambient).
     */
    @Override
    public void onExitAmbient()
    {
        super.onExitAmbient();

        /** Clears out Alarms since they are only used in ambient mode. */
        mAmbientStateAlarmManager.cancel(mAmbientStatePendingIntent);

        mContainerView.setBackgroundColor(getResources().getColor(android.R.color.white));
        mTotalDistanceTxt.setTextColor(Color.GREEN);
        mTotalDistanceTxt.getPaint().setAntiAlias(true);

        refreshDisplayAndSetNextUpdate();
    }

    /**
     * Loads data/updates screen (via method), but most importantly, sets up the next refresh
     * (active mode = Handler and ambient mode = Alarm).
     */
    private void refreshDisplayAndSetNextUpdate()
    {
        getGPSLocationAndUpdateScreen();

        long currentTimeMillis = System.currentTimeMillis();

        if (isAmbient())
        {
            /* Calculate next trigger time (based on state). */
            long delayMs = AMBIENT_INTERVAL_MS - (currentTimeMillis % AMBIENT_INTERVAL_MS);
            long triggerTimeMs = currentTimeMillis + delayMs;

            /*
             * Note: Make sure you have set activity launchMode to singleInstance in the manifest.
             * Otherwise, it is easy for the AlarmManager launch intent to open a new activity
             * every time the Alarm is triggered rather than reusing this Activity.
             */
            mAmbientStateAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    mAmbientStatePendingIntent
            );

        } else
        {
            /* Calculate next trigger time (based on state). */
            long delayMs = ACTIVE_INTERVAL_MS - (currentTimeMillis % ACTIVE_INTERVAL_MS);

            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            mActiveModeUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SCREEN, delayMs);
        }
    }

    /**
     * Updates display based on Ambient state. If you need to pull data, you should do it here.
     */
    private void getGPSLocationAndUpdateScreen()
    {
        if (mLocationManager != null && mLocationListener == null)
        {
            mLocationListener = new LocationListener()
            {
                @Override
                public void onLocationChanged(Location location)
                {
                    if (mLastLocation == null) //first time only
                        mLastLocation = location;

                    mTotalDistance += location.distanceTo(mLastLocation);

                    mTotalDistanceTxt.setText(getString(R.string.distance_label, String.valueOf(mTotalDistance)));
                    mLocationManager.removeUpdates(mLocationListener);
                    mLocationListener = null;

                    //updates to be ready for next coming
                    //location and calculate the distance again
                    mLastLocation = location;
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {}

                @Override
                public void onProviderEnabled(String s) {}

                @Override
                public void onProviderDisabled(String s) {}
            };

            try
            {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_MINUTES, //5
                        MIN_METERS,  //100
                        mLocationListener
                );

            } catch (SecurityException ex)
            {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    public void stop(View button){
        
    }


    /**
     * Handler separated into static class to avoid memory leaks.
     */
    private static class UpdateHandler extends Handler
    {
        private final WeakReference<TrackingActivity> mTrackingActivityWeakReference;

        public UpdateHandler(TrackingActivity reference)
        {
            mTrackingActivityWeakReference = new WeakReference<TrackingActivity>(reference);
        }

        @Override
        public void handleMessage(Message message)
        {
            TrackingActivity mainActivity = mTrackingActivityWeakReference.get();

            if (mainActivity != null)
            {
                switch (message.what)
                {
                    case MSG_UPDATE_SCREEN:
                        mainActivity.refreshDisplayAndSetNextUpdate();
                        break;
                }
            }
        }
    }
}
