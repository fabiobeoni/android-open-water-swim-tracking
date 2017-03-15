package com.beoni.openwaterswimtracking.rubricrequired;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;

import java.util.ArrayList;

public class AAsyncTaskLoader extends AsyncTaskLoader<Object>
{
    public AAsyncTaskLoader(Context ctx){
        super(ctx);
    }

    @Override
    public Object loadInBackground()
    {
        return null;
    }
}