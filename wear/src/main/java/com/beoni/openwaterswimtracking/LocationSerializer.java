package com.beoni.openwaterswimtracking;


import android.location.Location;
import android.text.TextUtils;

public class LocationSerializer
{
    String serialize(Location location){
        String time = String.valueOf(location.getTime());

        String locationStr = TextUtils.join(",", new String[]{
                time,
                String.valueOf(location.getSpeed()),
                String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude()),
                String.valueOf(location.getAltitude())
        });

        return locationStr;
    }

    Location parse(String locationData){
        String[] locationArr = locationData.split(",");

        Location location = new Location("");
        location.setTime(Long.parseLong(locationArr[0]));
        location.setSpeed(Float.parseFloat(locationArr[1]));
        location.setLatitude(Double.parseDouble(locationArr[2]));
        location.setLongitude(Double.parseDouble(locationArr[3]));
        location.setAltitude(Double.parseDouble(locationArr[4]));

        return location;
    }
}
