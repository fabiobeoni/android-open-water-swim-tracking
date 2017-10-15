package com.beoninet.openwaterswimtracking.shared;


import android.location.Location;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class LocationSerializer
{
    public String serialize(Location location){
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

    public Location parse(String locationData){
        String[] locationArr = locationData.split(",");

        Location location = new Location("");
        location.setTime(Long.parseLong(locationArr[0]));
        location.setSpeed(Float.parseFloat(locationArr[1]));
        location.setLatitude(Double.parseDouble(locationArr[2]));
        location.setLongitude(Double.parseDouble(locationArr[3]));
        location.setAltitude(Double.parseDouble(locationArr[4]));

        return location;
    }

    public List<Location> parseMany(String manyLocationsData){
        String[] locationRows = manyLocationsData.split(System.lineSeparator());
        List<Location> locations = new ArrayList<>();
        for (String locationData:locationRows)
            locations.add(parse(locationData));

        return locations;
    }
}
