package com.beoni.openwaterswimtracking;

import android.content.Context;
import android.location.Location;

import com.beoninet.openwaterswimtracking.shared.ICallback;
import com.beoninet.openwaterswimtracking.shared.LocationSerializer;

import java.util.List;

import needle.Needle;

public class SwimmingTrackStorage
{
    private static final String TAG = SwimmingTrackStorage.class.getSimpleName();

    private static final String TRACK_FILE_NAME = "tracks.trk";
    private static final String EMPTY = "";

    private LocationSerializer mLocationSerializer;
    private PrivateFileStorage mPrivateFileStorage;
    private List<Location> mLocations;
    private Context mContext;

    public SwimmingTrackStorage(Context ctx){
        mContext = ctx;
        mLocationSerializer = new LocationSerializer();
        mPrivateFileStorage = PrivateFileStorage.get();
        mLocations = null;
    }

    public void addAsync(final Location location, final ICallback<Boolean> cb){
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                mLocations.add(location);

                String locationsStr = mLocationSerializer.serializeMany(mLocations);
                boolean completed = mPrivateFileStorage.writeTextFile(mContext, TRACK_FILE_NAME, locationsStr);

                if(cb!=null) cb.completed(completed);
            }
        });
    }

    public void getAllLocationsAsync(final boolean refresh, final ICallback<List<Location>> cb){
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                loadLocations(refresh);

                if(cb!=null) cb.completed(mLocations);
            }
        });
    }

    public void deleteAllAsync(final ICallback<Boolean> cb){
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                boolean completed = mPrivateFileStorage.writeTextFile(mContext, TRACK_FILE_NAME, EMPTY);

                if(cb!=null) cb.completed(completed);
            }
        });
    }

    private void loadLocations(boolean refresh){
        if(mLocations==null || refresh)
            mLocations = mLocationSerializer.parseMany(loadLocationsAsString());
    }

    public String loadLocationsAsString(){
        return mPrivateFileStorage.readTextFile(mContext, TRACK_FILE_NAME);
    }
}
