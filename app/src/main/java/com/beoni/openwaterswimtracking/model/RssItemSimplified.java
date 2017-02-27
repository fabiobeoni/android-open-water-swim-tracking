package com.beoni.openwaterswimtracking.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import nl.matshofman.saxrssreader.RssItem;


public class RssItemSimplified implements Serializable
{
    public static RssItemSimplified factory(RssItem rssItem){
        RssItemSimplified item = new RssItemSimplified();
        item.setDate(rssItem.getPubDate());
        item.setTitle(rssItem.getTitle());
        item.setLink(rssItem.getLink());
        return item;
    }

    public static ArrayList<RssItemSimplified> simplify(ArrayList<RssItem> rssItems){
        ArrayList<RssItemSimplified> items = new ArrayList<>();
        for (RssItem rssItem:rssItems)
            items.add(RssItemSimplified.factory(rssItem));

        return items;
    }

    private String title;
    private String link;
    private Date date;

    public Date getDate()
    {
        return date;
    }

    public String getLink()
    {
        return link;
    }

    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }
}
