package com.beoninet.openwaterswimtracking.shared;

import android.location.Location;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class SwimTrackCalculator
{
    public static long calculateDuration(Location startLocation, Location endLocation)
    {
        return (endLocation.getTime()-startLocation.getTime());
    }

    public static long calculateDistance(List<Location> locations, boolean round)
    {
        long totalDistance = 0;

        if(locations.size()>0){
            Location lastLocation = null;
            for (Location location:locations)
            {
                if(lastLocation==null)
                    lastLocation = location;

                totalDistance += location.distanceTo(lastLocation);

                lastLocation = location;
            }
        }

        if(round)
            totalDistance = Math.round(totalDistance);

        return totalDistance;
    }
}
