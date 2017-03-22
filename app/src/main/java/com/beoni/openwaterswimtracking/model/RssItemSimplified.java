package com.beoni.openwaterswimtracking.model;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import com.beoni.openwaterswimtracking.utils.LLog;
import com.beoni.openwaterswimtracking.utils.RssUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.beoni.openwaterswimtracking.saxRssReader.RssItem;

public class RssItemSimplified implements Serializable
{
    public RssItemSimplified()
    {
    }

    public static RssItemSimplified factory(RssItem rssItem){
        RssItemSimplified item = new RssItemSimplified();
        item.setDate(rssItem.getPubDate());
        item.setTitle(rssItem.getTitle());
        item.setLink(rssItem.getLink());

        ArrayList<String> imagesUrls = RssUtils.getImagesURL(rssItem.getDescription());
        if(imagesUrls.size()>0)
            item.setImageUrl(imagesUrls.get(0));

        String description = "";
        if(rssItem.getDescription()!=null)
        {
            Spanned spanned = RssUtils.removeImageSpanObjects(rssItem.getDescription());

            if (Build.VERSION.SDK_INT < 24)
                description = Html.fromHtml(spanned.toString()).toString();
            else
                description = Html.fromHtml(spanned.toString(), Html.FROM_HTML_MODE_LEGACY).toString();
        }

        item.setDescription(description);

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
    private String imageUrl;
    private String description;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }



    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }



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
