package com.beoni.openwaterswimtracking.rubricrequired;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;

import java.util.ArrayList;

public class AAsyncTaskLoader extends AsyncTaskLoader<ArrayList<SwimTrack>>
{
    private Context mCtx;

    public AAsyncTaskLoader(Context ctx){
        super(ctx);
        this.mCtx = ctx;
    }

    @Override
    public ArrayList<SwimTrack> loadInBackground()
    {
        SwimTrackManager mng = new SwimTrackManager(mCtx);
        return mng.getSwimTracks(true);
    }
}