package com.beoni.openwaterswimtracking.bll;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.beoni.openwaterswimtracking.R;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@EBean
public class MapManager
{
    private static final String TAG = MapManager.class.getSimpleName();
    private static final String MAPS_IMG_DIRECTORY = "maps";

    private LatLngBounds.Builder mMapBoundsBuilder;
    private GoogleMap mGoogleMap;

    @RootContext
    Context mContext;

    public MapManager(){
        mMapBoundsBuilder = new LatLngBounds.Builder();
    }


    public void drawSwimmingPath(GoogleMap googleMap, List<Location> mLocations, boolean displayMarkers){
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
            Marker mapMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title(mContext.getString(R.string.map_marker_default_position)));
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

    public void createMapPreviewAsync(final SwimTrack swimTrack){
         mGoogleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
             @Override
             public void onSnapshotReady(Bitmap bitmap)
             {
                 storeMapPreviewAsync(swimTrack,bitmap);
             }
         });
    }

    @Background
    public void storeMapPreviewAsync(SwimTrack swimTrack, Bitmap bitmap)
    {
        final File imageFile = getFile(swimTrack);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            swimTrack.setMapPreviewFullFileName(imageFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG,mContext.getString(R.string.error_storing_map_image), e);
        } finally {
            try {
                if(fos!=null)
                    fos.close();
            } catch (IOException e) {
                Log.e(TAG,mContext.getString(R.string.error_storing_map_image), e);
            }
        }
    }

    @Background
    public void deleteMapPreviewAsync(SwimTrack swimTrack){
        getFile(swimTrack).delete();
    }

    @NonNull
    private File getFile(SwimTrack swimTrack)
    {
        final ContextWrapper contextWrapper = new ContextWrapper(mContext);
        final File dir = contextWrapper.getDir(MAPS_IMG_DIRECTORY, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        return new File(dir, swimTrack.getMapPreviewImageFileName());
    }

}
