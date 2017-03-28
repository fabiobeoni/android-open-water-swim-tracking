package com.beoni.openwaterswimtracking.model;

import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.beoni.openwaterswimtracking.saxRssReader.RssItem;

/**
 * Class to host UI-ready RSS data to be displayed.
 * Original RSS data get some kind of adaptation
 * (like images or HTML processing).
 */
public class RssItemSimplified implements Serializable
{
    public RssItemSimplified()
    {
    }

    /**
     * Utility, takes a regular RssItem object and returns
     * a simplified version of it with only needed
     * data on the UI by parsing some contents too.
     * @param rssItem
     * @return
     */
    public static RssItemSimplified factory(RssItem rssItem)
    {
        RssItemSimplified item = new RssItemSimplified();
        item.setDate(rssItem.getPubDate());
        item.setTitle(rssItem.getTitle());
        item.setLink(rssItem.getLink());

        //description field can contain multiple medias....
        //to keep it simple here you extract the first image
        //available to use it has the main image of the rss item
        //on UI. Works quite well, most of the original RSS items
        //have the first image quite big and RSS context related.
        ArrayList<String> imagesUrls = getImagesURL(rssItem.getDescription());
        if (imagesUrls.size() > 0)
            item.setImageUrl(imagesUrls.get(0));

        //since the description holds HTML code, here you make
        //sure that it will look good on a TextView
        String description = "";
        if (rssItem.getDescription() != null)
        {
            Spanned spanned = removeImageSpanObjects(rssItem.getDescription());

            if (Build.VERSION.SDK_INT < 24)
                description = Html.fromHtml(spanned.toString()).toString();
            else
                description = Html.fromHtml(spanned.toString(), Html.FROM_HTML_MODE_LEGACY).toString();
        }

        item.setDescription(description);

        return item;
    }

    /**
     * See .factory() method.
     * @param rssItems
     * @return
     */
    public static ArrayList<RssItemSimplified> simplify(ArrayList<RssItem> rssItems)
    {
        ArrayList<RssItemSimplified> items = new ArrayList<>();
        for (RssItem rssItem : rssItems)
            items.add(RssItemSimplified.factory(rssItem));

        return items;
    }

    /**
     * Extract image URL from image HTML tag.
     * @param rssHTMLContent
     * @return
     */
    private static ArrayList<String> getImagesURL(String rssHTMLContent){
        Pattern p = Pattern.compile("src=\"(.*?)\"");
        Matcher m = p.matcher(rssHTMLContent);
        ArrayList<String> urls = new ArrayList<>();
        while(m.find())
            //Displaying the url
            urls.add(m.group(1));

        return urls;
    }

    private static Spanned removeImageSpanObjects(String inStr) {
        SpannableStringBuilder spannedStr = (SpannableStringBuilder) Html
                .fromHtml(inStr.trim());
        Object[] spannedObjects = spannedStr.getSpans(0, spannedStr.length(),
                Object.class);
        for (int i = 0; i < spannedObjects.length; i++) {
            if (spannedObjects[i] instanceof ImageSpan) {
                ImageSpan imageSpan = (ImageSpan) spannedObjects[i];
                spannedStr.replace(spannedStr.getSpanStart(imageSpan),
                        spannedStr.getSpanEnd(imageSpan), "");
            }
        }
        return spannedStr;
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
