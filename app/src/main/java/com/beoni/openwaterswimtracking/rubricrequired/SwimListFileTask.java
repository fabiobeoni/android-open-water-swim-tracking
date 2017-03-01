package com.beoni.openwaterswimtracking.rubricrequired;

import android.content.Context;
import android.os.AsyncTask;

import com.beoni.openwaterswimtracking.bll.RssManager;


public class SwimListFileTask extends AsyncTask
{
    @Override
    protected Object doInBackground(Object[] objects)
    {
        RssManager mng = new RssManager((Context) objects[0]);
        return mng.getRssItems();
    }
}
