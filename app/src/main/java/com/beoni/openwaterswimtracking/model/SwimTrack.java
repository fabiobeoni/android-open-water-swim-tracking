package com.beoni.openwaterswimtracking.model;


import java.io.Serializable;
import java.util.Date;

//TODO: replace validation with annotation based one if you can find one specifically designed for android that works on model classes instead of forms
public class SwimTrack implements Serializable
{
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
