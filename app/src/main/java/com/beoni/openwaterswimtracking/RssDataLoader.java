package com.beoni.openwaterswimtracking;

import android.content.Context;

import com.beoni.openwaterswimtracking.bll.RssManager;
import com.beoni.openwaterswimtracking.model.RssItemSimplified;
import com.beoni.openwaterswimtracking.utils.AsyncTaskLoaderEx;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;

public class RssDataLoader extends AsyncTaskLoaderEx<ArrayList<RssItemSimplified>>
{
    //class that actually performs the Rss restoreFromFireDatabase and cache
    public RssManager mRssManager;

    public RssDataLoader(Context context) {
        super(context);
        mRssManager = new RssManager(context);
    }

    @Override
    public ArrayList<RssItemSimplified> loadInBackground() {
        return mRssManager.getRssItems();
    }
}
