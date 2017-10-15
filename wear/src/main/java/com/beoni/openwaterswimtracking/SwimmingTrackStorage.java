package com.beoni.openwaterswimtracking;


import android.content.SharedPreferences;
import android.location.Location;

import com.beoninet.openwaterswimtracking.shared.LocationSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SwimmingTrackStorage
{
    private static SwimmingTrackStorage mInstance;
    private static SharedPreferences mPreferences;
    private LocationSerializer mLocationSerializer;

    private SwimmingTrackStorage(SharedPreferences preferences){
        mPreferences = preferences;
        mLocationSerializer = new LocationSerializer();
    }

    public static SwimmingTrackStorage get(SharedPreferences preferences){
        if(mInstance==null)
            mInstance = new SwimmingTrackStorage(preferences);

        return mInstance;
    }

    public SwimmingTrackStorage newTracking(){
        deleteAll();
        return mInstance;
    }

    public void add(Location location){
        mPreferences.edit().putString(
                String.valueOf(location.getTime()),
                mLocationSerializer.serialize(location)
        ).apply();
    }

    public String getAllAsString(){
        String locations = "";

        Map<String,?> all = mPreferences.getAll();
        for (Map.Entry<String,?> entry:all.entrySet())
            locations += System.lineSeparator() + entry.getValue().toString();

        return locations;
    }

    private void deleteAll(){
        mPreferences.edit().clear().apply();
    }
}
