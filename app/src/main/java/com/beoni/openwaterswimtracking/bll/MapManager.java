package com.beoni.openwaterswimtracking.bll;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.androidannotations.annotations.EBean;

import java.util.List;

@EBean
public class MapManager
{
    LatLngBounds.Builder mMapBoundsBuilder;
    private List<Location> mLocations;
    private GoogleMap mGoogleMap;

    public MapManager(){
        mMapBoundsBuilder = new LatLngBounds.Builder();
    }


    public void drawSwimmingPath(GoogleMap googleMap, List<Location> mLocations, boolean displayMarkers){
        this.mLocations = mLocations;
        this.mGoogleMap = googleMap;

        googleMap.addPolyline(new PolylineOptions()
                .add(new LatLng(0, 0), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.RED));


        if(mLocations!=null)
        {
            for (Location location : mLocations)
            {
                LatLng mapPoint = new LatLng(location.getLatitude(), location.getLongitude());
                Marker mapMarker = googleMap.addMarker(new MarkerOptions().position(mapPoint).title("GPS Point: " + location.getTime()));

                mMapBoundsBuilder.include(mapMarker.getPosition());
            }
        }
        else
        {
            Marker mapMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("GPS Point"));
            mMapBoundsBuilder.include(mapMarker.getPosition());
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getMapBounds(),10));
    }

    public void addStartStopMarkers(){

    }


    public LatLngBounds getMapBounds(){
        return mMapBoundsBuilder.build();
    }

    public void getMapAsBitmap(GoogleMap.SnapshotReadyCallback snapshotReadyCallback){
         mGoogleMap.snapshot(snapshotReadyCallback);
    }
}
