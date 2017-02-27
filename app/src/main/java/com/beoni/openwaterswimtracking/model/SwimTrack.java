package com.beoni.openwaterswimtracking.model;


import java.io.Serializable;
import java.util.Date;

public class SwimTrack implements Serializable
{
    public SwimTrack()
    {
    }

    public SwimTrack(String title, String notes, String location, Date date, int duration, int length, int perceivedTemperature, int waves, int flow)
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

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
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

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
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
    private int duration;
    //measure type agnostic number
    private int length;
    private int perceivedTemperature;
    private int waves;
    private int flow;
}
