package com.beoni.openwaterswimtracking;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beoninet.openwaterswimtracking.shared.ICallback;
import com.beoninet.openwaterswimtracking.shared.SwimTrackCalculator;
import com.thanosfisherman.mayi.Mayi;
import com.thanosfisherman.mayi.PermissionBean;
import com.thanosfisherman.mayi.PermissionToken;
import com.thanosfisherman.mayi.listeners.MayiErrorListener;
import com.thanosfisherman.mayi.listeners.multi.PermissionResultMultiListener;
import com.thanosfisherman.mayi.listeners.multi.RationaleMultiListener;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class TrackingActivity extends WearableActivity
{
    private static final String TAG = TrackingActivity.class.getSimpleName();

    /**
     * Custom 'what' for Message sent to Handler.
     */
    private static final int MSG_UPDATE_SCREEN = 0;

    /**
     * Milliseconds between updates based on state.
     */
    private static final long ACTIVE_MODE_UPDATE_INTERVAL_MS = (1000);  //1 second

    //TODO:consider moving app settings to let the user decide...
    private static final long AMBIENT_MODE_UPDATE_INTERVAL_MS = (2 * 60 * 1000); //2 minutes
    private static final long TRACK_MIN_TIME_DIFF_MINUTES = (0); //0 milli-seconds
    private static final long TRACK_MIN_DISTANCE_METERS = 50; //100 meters
    private static final int AMBIENT_INT_REQ_CODE = 0;

    public static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");

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

    /**
     * Instance of class that keep stores all
     * the GPS locations recorded by this activity.
     */
    private SwimmingTrackStorage mSwimmingTrackStorage;

    /**
     * Last location recorded before the current one.
     * Needed to calculate distance between track
     * points.
     */
    private Location mLastLocation;

    /**
     * First GPS location recorded.
     * Needed to calculate the total
     * time from begin to end of the track.
     */
    private Location mStartLocation;

    /**
     * Total swim distance, sum
     * of multiple segments defined
     * by GPS points.
     */
    private float mTotalDistance;

    private TextView mTotalDistanceTxw;
    private TextView mHoursTxw;
    private TextView mCurrentTimeTxw;
    private View mContainerView;
    private ImageButton mStopBtn;

    private SwimmingTrackStorage getSwimmingTrackStorage()
    {
        if(mSwimmingTrackStorage==null)
            mSwimmingTrackStorage = new SwimmingTrackStorage(this);

        return mSwimmingTrackStorage;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tracking);

        mContainerView = findViewById(R.id.container);
        mTotalDistanceTxw = findViewById(R.id.distanceTxw);
        mHoursTxw = findViewById(R.id.hoursTxw);
        mCurrentTimeTxw = findViewById(R.id.currentTimeTxw);
        mStopBtn = findViewById(R.id.stopTrackingBtn);

        setAmbientEnabled();

        //set the alarm needed to call display updates when
        //ambient state is enabled on watch.
        //The listener of this trigger also reads GPS coordinates.
        mAmbientStateAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent ambientStateIntent = new Intent(getApplicationContext(), TrackingActivity.class);

        mAmbientStatePendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                AMBIENT_INT_REQ_CODE,
                ambientStateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //sets default values
        resetTotalDistance();

        //gets GPS location, updates the UI
        //and updates teh alarm for next update
        refreshDisplayAndSetNextUpdate();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        displayTrackData(0,0);

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
                        else
                            getSwimmingTrackStorage();

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

        resetTotalDistance();
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

        mTotalDistanceTxw.setTag(mTotalDistanceTxw.getCurrentTextColor());
        mTotalDistanceTxw.setTextColor(Color.WHITE);
        mTotalDistanceTxw.getPaint().setAntiAlias(false);

        mHoursTxw.setTag(mHoursTxw.getCurrentTextColor());
        mHoursTxw.setTextColor(Color.WHITE);
        mHoursTxw.getPaint().setAntiAlias(false);

        mCurrentTimeTxw.setTag(mCurrentTimeTxw.getCurrentTextColor());
        mCurrentTimeTxw.setTextColor(Color.WHITE);
        mCurrentTimeTxw.getPaint().setAntiAlias(false);

        mStopBtn.setTag(((ColorDrawable) mStopBtn.getBackground()).getColor());
        mStopBtn.setBackgroundColor(Color.BLACK);

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

        //this event is critical because
        //it open the window to get data
        //and update UI based on them
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

        mContainerView.setBackgroundColor(Color.WHITE);

        mTotalDistanceTxw.setTextColor((int) mTotalDistanceTxw.getTag());
        mTotalDistanceTxw.getPaint().setAntiAlias(true);

        mHoursTxw.setTextColor((int) mHoursTxw.getTag());
        mHoursTxw.setTextColor((int) mHoursTxw.getTag());
        mHoursTxw.getPaint().setAntiAlias(true);

        mCurrentTimeTxw.setTextColor((int) mCurrentTimeTxw.getTag());
        mCurrentTimeTxw.getPaint().setAntiAlias(true);

        mStopBtn.setBackgroundColor((int)mStopBtn.getTag());

        refreshDisplayAndSetNextUpdate();
    }

    /**
     * Loads data/updates screen (via method), but most importantly, sets up the next refresh
     * (active mode = Handler and ambient mode = Alarm).
     */
    private void refreshDisplayAndSetNextUpdate()
    {
        getGPSLocationAndUpdate();

        long currentTimeMillis = System.currentTimeMillis();

        if (isAmbient())
        {
            /* Calculate next trigger time (based on state). */
            long delayMs = AMBIENT_MODE_UPDATE_INTERVAL_MS - (currentTimeMillis % AMBIENT_MODE_UPDATE_INTERVAL_MS);
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
            long delayMs = ACTIVE_MODE_UPDATE_INTERVAL_MS - (currentTimeMillis % ACTIVE_MODE_UPDATE_INTERVAL_MS);

            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            mActiveModeUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SCREEN, delayMs);
        }
    }

    /**
     * Updates display based on Ambient state. If you need to pull data, you should do it here.
     */
    private void getGPSLocationAndUpdate()
    {
        if (mLocationManager != null && mLocationListener == null)
        {
            mLocationListener = new LocationListener()
            {
                @Override
                public void onLocationChanged(final Location location)
                {
                    //keep track locally of first
                    //registered location as track
                    //starting point (for other calculations
                    // like total time spent)
                    if(mStartLocation==null)
                        mStartLocation = location;

                    if(mLastLocation==null)
                        mLastLocation = location;

                    calculateTrackData(location);

                    storeNewLocation(location);

                    //updates to be ready for next coming
                    //location and calculate the distance again
                    mLastLocation = location;

                    if(mLocationListener!=null)
                    {
                        mLocationManager.removeUpdates(mLocationListener);
                        mLocationListener = null;
                    }
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
                        TRACK_MIN_TIME_DIFF_MINUTES,
                        TRACK_MIN_DISTANCE_METERS,
                        mLocationListener
                );

            } catch (SecurityException ex)
            {
                Log.e(TAG, ex.getMessage(),ex);
            }
        }
    }

    /**
     * Add the new GPS location to the
     * track on local storage.
     * @param location
     */
    private void storeNewLocation(final Location location)
    {
        //reloading from data source is for some reasons
        //needed to avoid to play with cached data in
        //mSwimmingTrackStorage when the addAsync()
        //methods adds to the locations collection.
        //If the activity is resuming that collection
        //not empty even if cleared with deletion
        //method. It has something to do with the
        //way the Handler stores the activity reference.
        //So here you refresh the list to make sure
        //the will be empty (if deleted before.. new track)
        getSwimmingTrackStorage().getAllLocationsAsync(true, new ICallback<List<Location>>()
        {
            @Override
            public void completed(List<Location> isCompleted)
            {
                //stores the location on the wear
                //doesn't look for result because
                //even in case a single location
                //is not stored, many others should
                //be. This method is invoked on a time
                //bases, so you gonna have many locations
                //anyway.
                mSwimmingTrackStorage.addAsync(location, null);
            }
        });
    }

    /**
     * Calculate distance and time from last
     * tracked location and updates teh screen.
     * @param location
     */
    private void calculateTrackData(Location location)
    {
        long duration = SwimTrackCalculator.calculateDuration(mStartLocation,location);
        mTotalDistance += SwimTrackCalculator.calculateDistance(Arrays.asList(mLastLocation,location),true);

        displayTrackData(duration,mTotalDistance);
    }

    /**
     * Updates the UI to display swim duration and total distance.
     * @param duration
     * @param totalDistance
     */
    private void displayTrackData(long duration, float totalDistance){
        mHoursTxw.setText(getString(R.string.last_track_time_length,
                String.valueOf(TimeUnit.MILLISECONDS.toHours(duration)),
                String.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)),
                String.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration))
        ));
        mTotalDistanceTxw.setText(getString(R.string.last_track_distance, String.valueOf(totalDistance)));
        mCurrentTimeTxw.setText(mSimpleDateFormat.format(new Date()));
    }

    /**
     * Clears the swim track values
     * to display on UI when the user
     * stops tracking, or new intent
     * comes.
     */
    private void resetTotalDistance()
    {
        mLastLocation = null;
        mStartLocation = null;
        mTotalDistance = 0;
    }

    public void btnStopTrackingOnClick(View button){
        resetTotalDistance();
        finish();
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
            TrackingActivity trackingActivity = mTrackingActivityWeakReference.get();

            if (trackingActivity != null)
            {
                switch (message.what)
                {
                    case MSG_UPDATE_SCREEN:
                        trackingActivity.refreshDisplayAndSetNextUpdate();
                        break;
                }
            }
        }
    }
}
