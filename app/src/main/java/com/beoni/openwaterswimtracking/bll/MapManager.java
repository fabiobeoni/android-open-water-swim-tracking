package com.beoni.openwaterswimtracking.bll;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;

import com.beoni.openwaterswimtracking.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.text.SimpleDateFormat;
import java.util.List;

@EBean
public class MapManager
{
    LatLngBounds.Builder mMapBoundsBuilder;
    private List<Location> mLocations;
    private GoogleMap mGoogleMap;

    @RootContext
    Context mContext;

    public MapManager(){
        mMapBoundsBuilder = new LatLngBounds.Builder();
    }


    public void drawSwimmingPath(GoogleMap googleMap, List<Location> mLocations, boolean displayMarkers){
        this.mLocations = mLocations;
        this.mGoogleMap = googleMap;

        if(mLocations!=null)
        {
            float totalDistance = 0;
            Location lastLocation = null;

            if(mLocations.size()>0)
                lastLocation = mLocations.get(0);

            for (int i=0;i<mLocations.size();i++)
            {
                Location location = mLocations.get(i);

                totalDistance += Math.round(location.distanceTo(lastLocation));

                LatLng mapPoint = new LatLng(location.getLatitude(), location.getLongitude());
                Marker mapMarker = addMarker(i,mapPoint, location, mLocations.size(),totalDistance);

                //...but includes all markers to calculate map bounds
                mMapBoundsBuilder.include(mapMarker.getPosition());

                //draws the path
                drawsPath(lastLocation, location);

                lastLocation = location;
            }
        }
        else
        {
            Marker mapMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Default Location (0,0)"));
            mMapBoundsBuilder.include(mapMarker.getPosition());
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getMapBounds(),100));
    }

    private void drawsPath(Location locationA, Location locationB)
    {
        mGoogleMap.addPolyline(new PolylineOptions()
                .add(
                        new LatLng(locationA.getLatitude(), locationA.getLongitude()),
                        new LatLng(locationB.getLatitude(), locationB.getLongitude())
                )
                .width(5)
                .color(Color.GREEN));
    }

    private Marker addMarker(int index, LatLng mapPoint, Location location, int totalLocations, float totalDistance)
    {
        Marker mapMarker = mGoogleMap.addMarker(new MarkerOptions().position(mapPoint));

        String title = mContext.getString(R.string.swim_path_total_distance);
        title = String.format(title,String.valueOf(totalDistance), new SimpleDateFormat("HH:mm:ss").format(location.getTime()));

        //set the title label displayed when the marker is clicked
        mapMarker.setTitle(title);

        //display first and last points only as markers...
        if(index==0)
            mapMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.swim));
        else if(index==totalLocations-1)
            mapMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
        else
            mapMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.point));

        return mapMarker;
    }

    public LatLngBounds getMapBounds(){
        return mMapBoundsBuilder.build();
    }

    public void getMapAsBitmap(GoogleMap.SnapshotReadyCallback snapshotReadyCallback){
         mGoogleMap.snapshot(snapshotReadyCallback);
    }
}
