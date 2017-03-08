package com.beoni.openwaterswimtracking.bll;

import android.content.Context;
import android.content.SharedPreferences;

import com.beoni.openwaterswimtracking.R;
import com.beoni.openwaterswimtracking.data.LocalFileStorage;
import com.beoni.openwaterswimtracking.model.RssItemSimplified;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.EBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

@EBean
public class SwimTrackManager
{
    private static final String FILE_NAME = "OWSTSwimTrack.txt";

    private Context mContext;
    private LocalFileStorage mStorage;
    private SharedPreferences mPreferences;
    private ArrayList<SwimTrack> mSwimTracks;

    public SwimTrackManager(Context ctx){
        mContext = ctx;
        mStorage = LocalFileStorage.get(ctx);
        mPreferences = mContext.getSharedPreferences(ctx.getString(R.string.preferences_file), Context.MODE_PRIVATE);
    }

    public ArrayList<SwimTrack> getSwimTracks(boolean forceReload)
    {
        if(mSwimTracks==null || forceReload)
            mSwimTracks = readFile();

        return mSwimTracks;
    }

    public void addNewSwimTrack(SwimTrack swim){
        getSwimTracks(false);
        mSwimTracks.add(swim);
    }

    public void updateSwimTrack(int index, SwimTrack currentSwim){
        getSwimTracks(false);
        mSwimTracks.remove(index);
        mSwimTracks.add(index,currentSwim);
    }

    public void deleteSwimTrack(SwimTrack swim){
        getSwimTracks(false);
        mSwimTracks.remove(swim);
    }

    public void save(){
        writeFile(mSwimTracks);
    }

    public String getFileForBackup(){
        return mStorage.readTextFile(FILE_NAME);
    }

    public void restoreFileFromBackup(String content){
        mStorage.writeTextFile(FILE_NAME, content);
    }

    private ArrayList<SwimTrack> readFile(){
        Type listType = new TypeToken<ArrayList<SwimTrack>>(){}.getType();
        ArrayList<SwimTrack> items = new ArrayList<SwimTrack>();
        String result = mStorage.readTextFile(FILE_NAME);

        if(result.length()>0)
            items = new Gson().fromJson(result, listType);

        return items;
    }

    private void writeFile(ArrayList<SwimTrack> items){
        if(items!=null){
            String itemsAsString = new Gson().toJson(items);
            mStorage.writeTextFile(FILE_NAME, itemsAsString);
        }
    }

}
