package com.beoni.openwaterswimtracking.bll;

import android.content.Context;
import android.content.SharedPreferences;

import com.beoni.openwaterswimtracking.R;
import com.beoni.openwaterswimtracking.RssItemSimplified;
import com.beoni.openwaterswimtracking.data.LocalFileStorage;
import com.beoni.openwaterswimtracking.utils.ConnectivityUtils;
import com.beoni.openwaterswimtracking.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

@EBean
public class RssManager
{
    private static final String RSS_FILE_NAME = "OWSTRss.txt";
    private static final String LAST_DOWNLOAD_DATE = "LAST_DOWNLOAD_DATE";
    private static final String PREFERENCE_FILE = "com.beoni.openwaterswimtracking.preferences";

    private Context mContext;
    private LocalFileStorage storage;

    public RssManager(Context ctx){
        mContext = ctx;
        storage = LocalFileStorage.get(ctx);
    }

    public ArrayList<RssItemSimplified> getRssItems()
    {
        ArrayList<RssItemSimplified> rssItems = new ArrayList<>();

        if(ConnectivityUtils.isConnected(mContext) && isDownloadedRssObsolete()){
            rssItems = downloadRss();
            writeRssFile(rssItems);
        }
        else
            rssItems = readRssFile();

        return rssItems;
    }

    private ArrayList<RssItemSimplified> downloadRss(){
        ArrayList<RssItemSimplified> rssItemsSimp = new ArrayList<>();

        try
        {
            RssFeed feed = RssReader.read(new URL(mContext.getString(R.string.rss_url)));
            ArrayList<RssItem> rssItems = feed.getRssItems();
            rssItemsSimp = RssItemSimplified.simplify(rssItems);
        }
        catch (SAXException | IOException e)
        {
            e.printStackTrace();
        }

        return rssItemsSimp;
    }

    private ArrayList<RssItemSimplified> readRssFile(){
        Type listType = new TypeToken<ArrayList<RssItemSimplified>>(){}.getType();
        ArrayList<RssItemSimplified> rssItems = new ArrayList<RssItemSimplified>();
        String result = storage.readTextFile(RSS_FILE_NAME);

        if(result.length()>0)
            rssItems = new Gson().fromJson(result, listType);

        return rssItems;
    }

    private void writeRssFile(ArrayList<RssItemSimplified> rssItemsSimp){
        String rssAsString = new Gson().toJson(rssItemsSimp);
        storage.writeTextFile(RSS_FILE_NAME, rssAsString);
    }

    private boolean isDownloadedRssObsolete(){
        long diff = 0;
        Date today = new Date();
        SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCE_FILE, android.content.Context.MODE_PRIVATE);
        String lastDownloadDateStr = preferences.getString(LAST_DOWNLOAD_DATE,"");

        if(!lastDownloadDateStr.equals("")){
            Date lastDownloadDate = DateUtils.stringToDate(lastDownloadDateStr);
            diff = DateUtils.dateDiff(lastDownloadDate,today);
        }

        preferences.edit().putString(LAST_DOWNLOAD_DATE, DateUtils.dateToString(today)).commit();

        return (diff>=1);
    }

}
