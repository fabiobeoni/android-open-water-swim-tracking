package com.beoni.openwaterswimtracking.bll;

import android.content.Context;

import com.beoni.openwaterswimtracking.data.LocalFileStorage;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.EBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class performing CRUD operations over swimming tracks.
 * Works with the LocalStorage class to store data in
 * a local text file (JSON format). Exposes the local
 * copy in string format to make it available for backup.
 */
@EBean
public class SwimTrackManager
{
    //file where user swim tracks are stored as JSON string
    private static final String FILE_NAME = "OWSTSwimTrack.txt";

    //instance of class performing file reading/writing
    //on device hard-drive
    private LocalFileStorage mStorage;

    //in memory list of swim tracks to provide
    //to the client
    private ArrayList<SwimTrack> mSwimTracks;

    private Context mContext;


    public SwimTrackManager(Context ctx)
    {
        mContext = ctx;
        mStorage = LocalFileStorage.get();
    }


    /**
     * Loads the swim tracks from a text json
     * file locally stored. Loading is performed
     * at first invocation, then a in memory cache
     * is served unless the client requires a new
     * load from disk.
     *
     * @param forceReload
     * @return
     */
    public ArrayList<SwimTrack> getSwimTracks(boolean forceReload)
    {
        if (mSwimTracks == null || forceReload)
            mSwimTracks = readFile();

        return mSwimTracks;
    }

    public boolean isNewSwim(SwimTrack swimTrack){
        boolean result = true;

        getSwimTracks(false);

        for (SwimTrack t:mSwimTracks)
            if (t.getID().equals(swimTrack.getID())){
                result = false;
                break;
            }

        return result;
    }

    /**
     * Add a new swim track to the in-memory
     * cached list of swimming. To store changes
     * you must invoke .save() method.
     *
     * @param swim new swim to add
     */
    public void addNewSwimTrack(SwimTrack swim)
    {
        getSwimTracks(false).add(swim);
    }

    /**
     * Update the given swim track in the in-memory
     * cached list of swimming. To store changes
     * you must invoke .save() method.
     *
     * @param index       index of the swim to update in the list of swim tracks
     * @param currentSwim the updated swim track
     */
    public void updateSwimTrack(int index, SwimTrack currentSwim)
    {
        deleteSwimTrack(index).add(index, currentSwim);
    }

    /**
     * Delete the given swim track from the in-memory
     * cached list of swimming. To store changes
     * you must invoke .save() method.
     *
     * @param index index of the swim to delete in the list of swim tracks
     * @return list of remaining swim tracks
     */
    public ArrayList<SwimTrack> deleteSwimTrack(int index)
    {
        getSwimTracks(false).remove(index);
        return mSwimTracks;
    }

    /**
     * Performs saving to text file of cached
     * list of swim tracks
     */
    public void save()
    {
        writeFile(mSwimTracks);
    }

    /**
     * Load, serialize and return the swim list.
     * @return serialized swim tracks list as json string
     */
    public String getLocalDataForBackup()
    {
        return mStorage.readTextFile(mContext,FILE_NAME);
    }

    /**
     * Deserialize the given json string
     * and store the list of swim tracks
     * on text file overriding any existing
     * swim track list on local device.
     * @param data backup serialized data
     */
    public void restoreLocalDataFromBackup(String data)
    {
        mStorage.writeTextFile(mContext, FILE_NAME, data);
    }

    /**
     * Perform reading from json text file
     * the list of swim tracks, the deserialize
     * it and return it to the client.
     * @return list of swim tracks
     */
    private ArrayList<SwimTrack> readFile()
    {
        Type listType = new TypeToken<ArrayList<SwimTrack>>(){}.getType();
        ArrayList<SwimTrack> items = new ArrayList<SwimTrack>();
        String result = mStorage.readTextFile(mContext,FILE_NAME);

        if (result != "" && result != "[]")
            items = new Gson().fromJson(result, listType);

        return items;
    }

    /**
     * Perform serialization and writing to
     * a local json text file the list of
     * swim tracks.
     * @param swimTracks list of swim tracks to be stored locally
     */
    private void writeFile(ArrayList<SwimTrack> swimTracks)
    {
        if (swimTracks != null)
        {
            String itemsAsString = new Gson().toJson(swimTracks);
            mStorage.writeTextFile(mContext, FILE_NAME, itemsAsString);
        }
    }

}
