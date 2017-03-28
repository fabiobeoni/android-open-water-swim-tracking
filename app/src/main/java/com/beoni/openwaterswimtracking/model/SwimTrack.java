package com.beoni.openwaterswimtracking.model;


import android.content.Context;

import com.beoni.openwaterswimtracking.R;
import com.beoni.openwaterswimtracking.utils.DateUtils;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;

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
                ctx.getResources().getString(R.string.new_swim_title),
                "",
                ctx.getResources().getString(R.string.new_swim_location),
                DateUtils.stringToDate("01/01/2017",DateUtils.SHORT_FORMAT)
                ,60,3000,1,1,1
        );
    }

    public static String formatDuration(Context ctx, int duration){
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

    private SwimTrack(String title, String notes, String location, Date date, int duration, int length, int perceivedTemperature, int waves, int flow)
    {
        this.title = title;
        this.notes = notes;
        this.location = location;
        this.date = date;
        this.duration = duration;
        this.length = length;
        this.perceivedTemperature = perceivedTemperature;
        this.waves = waves;
        this.flow = flow;
    }

    public SwimTrack()
    {
    }

    public boolean isValid(){
        return (isDateValid() && isLocationValid() && isTitleValid());
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isTitleValid(){
        return (this.title!=null && this.title.trim().length()>0);
    }

    private String title;

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

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

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

    private String notes;
    private String location;
    private Date date;
    //in minutes
    private int duration = 0;
    //measure type agnostic number
    private int length = 0;
    private int perceivedTemperature = 0;
    private int waves = 0;
    private int flow = 0;

}
