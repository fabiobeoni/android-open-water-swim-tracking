package com.beoni.openwaterswimtracking.model;

import android.content.Context;
import android.location.Location;

import com.beoni.openwaterswimtracking.R;
import com.beoni.openwaterswimtracking.utils.DateUtils;
import com.beoninet.openwaterswimtracking.shared.LocationSerializer;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//TODO: replace validation with annotations based library
/**
 * Model class to host tracked swimming data.
 */
public class SwimTrack implements Serializable
{
    /**
     * Utility to fill the new swim form with some helper data.
     * @param ctx
     * @return
     */
    public static SwimTrack createNewEmptySwim(Context ctx){
        return new SwimTrack(
                ctx.getResources().getString(R.string.new_swim_location),
                DateUtils.stringToDate("01/01/2017",DateUtils.SHORT_FORMAT)
                ,0,0,1,1,1,
                ""
        );
    }

    public static String formatDuration(Context ctx, float duration){
        //"i" are minutes
        double h = ((double)duration/60.00);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        NumberFormat format = new DecimalFormat("#.##",symbols);
        String full = format.format(h);
        String hours = "0";
        String minutes = "0";
        if(full.contains("."))
        {
            hours = full.split("[.]")[0];
            minutes = full.split("[.]")[1];
        }
        String hoursLabel = ctx.getResources().getString(R.string.swim_duration_label_hours);
        String minutesLabel = ctx.getResources().getString(R.string.swim_duration_label_minutes);
        return (hours+ hoursLabel +minutes+ minutesLabel);
    }

    private SwimTrack(String location, Date date, int duration, int length, int perceivedTemperature, int waves, int flow, String notes)
    {
        this.notes = notes;
        this.location = location;
        this.date = date;
        this.duration = duration;
        this.length = length;
        this.perceivedTemperature = perceivedTemperature;
        this.waves = waves;
        this.flow = flow;

        this.ID = UUID.randomUUID().toString();
    }

    public SwimTrack()
    {
    }

    public String getID(){
        return ID;
    }

    public String getMapPreviewImageFileName(){
        return getID()+".png";
    }

    public boolean isValid(){

        return (
                isDateValid() && isLocationValid() &&
                        isDurationValid() && isLengthValid()
        );
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public boolean isLocationValid(){
        return (this.location!=null && this.location.trim().length()>0);
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public boolean isDateValid(){
        return (this.date!=null);
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public boolean isDurationValid(){
        return duration>0;
    }

    public long getLength()
    {
        return length;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

    public boolean isLengthValid(){ return length>0;}

    public int getPerceivedTemperature()
    {
        return perceivedTemperature;
    }

    public void setPerceivedTemperature(int perceivedTemperature)
    {
        this.perceivedTemperature = perceivedTemperature;
    }

    public int getWaves()
    {
        return waves;
    }

    public void setWaves(int waves)
    {
        this.waves = waves;
    }

    public int getFlow()
    {
        return flow;
    }

    public void setFlow(int flow)
    {
        this.flow = flow;
    }

    public List<Location> getGpsLocations(LocationSerializer serializer)
    {
        if(gpsLocationsAsString!=null)
            return serializer.parseMany(gpsLocationsAsString);
        else
            return null;
    }

    public void setGpsLocations(LocationSerializer serializer, List<Location> gpsLocations)
    {
        this.setGpsLocationsAsString(serializer.serializeMany(gpsLocations));
    }

    public String getGpsLocationsAsString()
    {
        return gpsLocationsAsString;
    }

    public void setGpsLocationsAsString(String gpsLocationsAsString)
    {
        this.gpsLocationsAsString = gpsLocationsAsString;
    }

    public void setMapPreviewFullFileName(String path)
    {
        mapPreviewFullFileName = path;
    }

    public String getMapPreviewFullFileName()
    {
        return mapPreviewFullFileName;
    }

    public void setID(String ID)
    {
        this.ID = ID;
    }

    private String ID;
    private String notes;
    private String location;
    private Date date;
    //in milliseconds
    private long duration = 0;
    //in meters
    private long length = 0;
    private int perceivedTemperature = 0;
    private int waves = 0;
    private int flow = 0;
    private String mapPreviewFullFileName;
    private String gpsLocationsAsString;


}
