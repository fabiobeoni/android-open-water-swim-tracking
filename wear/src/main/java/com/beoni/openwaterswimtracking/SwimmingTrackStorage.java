package com.beoni.openwaterswimtracking;


import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SwimmingTrackStorage
{
    private static SwimmingTrackStorage mInstance;
    private static SharedPreferences mPreferences;

    private SwimmingTrackStorage(SharedPreferences preferences){
        mPreferences = preferences;
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
        String time = String.valueOf(location.getTime());

        String newLocationStr = TextUtils.join(",", new String[]{
                time,
                String.valueOf(location.getSpeed()),
                String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude()),
                String.valueOf(location.getAltitude())
        });

        mPreferences.edit().putString(time,newLocationStr).apply();
    }

    public List<Location> getAll(){
        ArrayList<Location> locations = new ArrayList<>();

        Map<String,?> all = mPreferences.getAll();

        for (Map.Entry<String,?> entry:all.entrySet()){
            String[] locationArr = entry.getValue().toString().split(",");

            Location location = new Location("");
            location.setTime(Long.parseLong(locationArr[0]));
            location.setSpeed(Float.parseFloat(locationArr[1]));
            location.setLatitude(Double.parseDouble(locationArr[2]));
            location.setLongitude(Double.parseDouble(locationArr[3]));
            location.setAltitude(Double.parseDouble(locationArr[4]));

            locations.add(location);
        }

        return locations;
    }

    private void deleteAll(){
        mPreferences.edit().clear().apply();
    }
}
