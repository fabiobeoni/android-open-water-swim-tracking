package com.beoni.openwaterswimtracking.bll;

import android.util.Log;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

/**
 * Created by fabio on 19/02/17.
 */

public class RSSDownloader
{
    public RSSDownloader() throws IOException, SAXException
    {
        URL url = new URL("http://example.com/feed.rss");
        RssFeed feed = RssReader.read(url);

        ArrayList<RssItem> rssItems = feed.getRssItems();
        for(RssItem rssItem : rssItems) {
            Log.i("RSS Reader", rssItem.getTitle());
        }
    }

}
