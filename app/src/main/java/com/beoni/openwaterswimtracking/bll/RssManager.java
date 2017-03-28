package com.beoni.openwaterswimtracking.bll;

import android.content.Context;
import android.content.SharedPreferences;

import com.beoni.openwaterswimtracking.R;
import com.beoni.openwaterswimtracking.model.RssItemSimplified;
import com.beoni.openwaterswimtracking.data.LocalFileStorage;
import com.beoni.openwaterswimtracking.utils.ConnectivityUtils;
import com.beoni.openwaterswimtracking.utils.DateUtils;
import com.beoni.openwaterswimtracking.utils.LLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.EBean;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import com.beoni.openwaterswimtracking.saxRssReader.RssFeed;
import com.beoni.openwaterswimtracking.saxRssReader.RssItem;
import com.beoni.openwaterswimtracking.saxRssReader.RssReader;

/**
 * The class manages the reading of the RSS data
 * from remote address, as well as storing and reading
 * the local cached version.
 * The manager also takes care of updating the local
 * cache when RSS data are too old (1 day).
 */
@EBean
public class RssManager
{
    /**
     * Name of the file where local cached file get stored
     */
    private static final String RSS_FILE_NAME = "OWSTRss.txt";

    /**
     * Preference key that stores the date about the latest
     * download performed. Needed to check if the local cached
     * file is too old and needs to be updated.
     */
    private static final String PREF_LAST_DOWNLOAD_DATE = "LAST_DOWNLOAD_DATE";

    private Context mContext;
    private SharedPreferences mPreferences;

    //instance of DAO class to manage read/write
    // files to the local disk
    private LocalFileStorage mStorage;

    public RssManager(Context ctx){
        mContext = ctx;
        mStorage = LocalFileStorage.get(ctx);
        mPreferences = mContext.getSharedPreferences(ctx.getString(R.string.preferences_file), android.content.Context.MODE_PRIVATE);
    }

    /**
     * ASYNC INVOCATION REQUIRED WHEN INVOKED FROM UI THREAD
     * This methods returns the list of rss items.
     * Rss items are downloaded from the web and stored
     * locally. The cache becomes invalid after one day.
     * When cache is still valid or network is missing
     * the locally cached rss are returned to the client.
     * @return list of RssItemSimplified items. Empty list if either network connection AND cached data are NOT available.
     */
    public ArrayList<RssItemSimplified> getRssItems()
    {
        ArrayList<RssItemSimplified> rssItems = new ArrayList<>();

        if(ConnectivityUtils.isDeviceConnected(mContext) && isDownloadedRssObsolete()){
            rssItems = downloadRss();
            writeRssFile(rssItems);
        }
        else
            rssItems = readRssFile();

        return rssItems;
    }

    /**
     * ASYNC INVOCATION REQUIRED WHEN INVOKED FROM UI THREAD
     * Performs the request of RSS data to the remote address
     * and returns a list of objects from parsing.
     * @return list of RssItemSimplified items. Empty list when exception occurs.
     */
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
            LLog.e(e);
        }

        return rssItemsSimp;
    }

    /**
     * ASYNC INVOCATION REQUIRED WHEN INVOKED FROM UI THREAD
     * Performs read of cached rss text file and returns typed
     * list of rss items.
     * @return list of RssItemSimplified items.
     */
    private ArrayList<RssItemSimplified> readRssFile(){
        Type listType = new TypeToken<ArrayList<RssItemSimplified>>(){}.getType();
        ArrayList<RssItemSimplified> rssItems = new ArrayList<RssItemSimplified>();
        String result = mStorage.readTextFile(RSS_FILE_NAME);

        if(result.length()>0)
            rssItems = new Gson().fromJson(result, listType);

        return rssItems;
    }

    /**
     * ASYNC INVOCATION REQUIRED WHEN INVOKED FROM UI THREAD
     * Performs writing of rss data to a local text file for caching,
     * and updates shared preference with the current cache date time.
     * @param rssItemsSimp
     */
    private void writeRssFile(ArrayList<RssItemSimplified> rssItemsSimp){
        String rssAsString = new Gson().toJson(rssItemsSimp);
        mStorage.writeTextFile(RSS_FILE_NAME, rssAsString);

        //stores the date about the current restoreFromFireDatabase
        Date today = new Date();
        mPreferences.edit().putString(PREF_LAST_DOWNLOAD_DATE, DateUtils.dateToString(today)).apply();
    }

    /**
     * This method host the logic to invalidate rss cached data.
     * When the rss file is older than 1 day, cache is invalid.
     * The last restoreFromFireDatabase date of the cached data is stored in
     * a shared preference of easy retrying.
     * @return true|false according to cache valid status.
     */
    private boolean isDownloadedRssObsolete(){
        long diff = 1;
        Date today = new Date();
        String lastDownloadDateStr = mPreferences.getString(PREF_LAST_DOWNLOAD_DATE,"");

        //gets the last data restoreFromFireDatabase date
        if(!lastDownloadDateStr.equals("")){
            Date lastDownloadDate = DateUtils.stringToDate(lastDownloadDateStr,DateUtils.FORMAT);
            diff = DateUtils.dateDiff(lastDownloadDate,today);
        }

        return (diff>=1);
    }

}
