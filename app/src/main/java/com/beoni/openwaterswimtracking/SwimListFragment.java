package com.beoni.openwaterswimtracking;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.rubricrequired.AAsyncTaskLoader;
import com.beoni.openwaterswimtracking.utils.ConnectivityUtils;
import com.beoni.openwaterswimtracking.utils.LLog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * The fragment presents swim tracking data in a list view,
 * performs swim reading from the storage by the
 * SwimManager.
 */
@EFragment(R.layout.fragment_swim_list)
@OptionsMenu(R.menu.menu_swim_list)
public class SwimListFragment extends Fragment implements LoaderManager.LoaderCallbacks
{
    //============ AVAILABLE UI STATES ===============/

    private static final int UISTATE_GETTING_DATA = 0;
    private static final int UISTATE_VIEW_DATA = 1;


    //list of swim track items presented on view
    @InstanceState
    ArrayList<SwimTrack> mSwimTracksList = null;

    //class that actually performs the swim CRUD actions
    @Bean
    SwimTrackManager mSwimTrackManager;

    //adapt class for this view
    SwimTracksAdapter mSwimTracksAdapter;

    //list view presenting the swim tracks data
    @ViewById(R.id.swim_list)
    ListView mSwimListView;

    @ViewById(R.id.progress_bar)
    ProgressBar mProgressBar;

    //group of ui elements to display
    //a message to the user when
    //device is offline
    @ViewById(R.id.swim_message_panel)
    LinearLayout mMessagePanel;

    @OptionsMenuItem(R.id.menu_backup)
    MenuItem menuItemBackup;


    //Required empty public constructor
    public SwimListFragment() {}

    //initialization
    @AfterViews
    void viewCreated() {
        //TODO: for project approval using here AsyncTaskLoader instead of annotation. Restore commented code after.
        //mSwimTracksList is saved in instance state
        //so can be reused
        if(mSwimTracksList ==null){
            //updates the UI showing progress bar
            //for background tasks
            setUIState(UISTATE_GETTING_DATA);

            //gets data from the web or from cached
            ////loadData();
            getActivity().getSupportLoaderManager().initLoader(1, null, this).forceLoad();
        }
        else //just proceed with UI update
            onDataLoadCompleted();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //menuItemBackup.setEnabled(ConnectivityUtils.isDeviceConnected(getContext()));
    }

    //creates a loader to load list of swim track
    @Override
    public Loader onCreateLoader(int id, Bundle args)
    {
        return new AAsyncTaskLoader(getActivity());
    }

    //populate the list of swim tracks and updates the ui
    @Override
    public void onLoadFinished(Loader loader, Object data)
    {
        mSwimTracksList = (ArrayList<SwimTrack>) data;
        onDataLoadCompleted();
    }

    @Override
    public void onLoaderReset(Loader loader)
    {
        //do nothing, list is empty already
    }

    //TODO: for project approval using here AsyncTaskLoader instead of annotation. Restore commented code after.
    ////@Background
    ////void loadData() {
        ////mSwimTracksList = mSwimTrackMng.getSwimTracks();
        ////onDataLoadCompleted();
    ////}
    //Updates the UI to display loaded swim tracks
    //@UiThread
    void onDataLoadCompleted() {
        //updates the list adapter to display the data
        if(mSwimTracksList !=null && mSwimTracksList.size()>0){
            mSwimTracksAdapter = new SwimTracksAdapter(getContext(), R.layout.swim_track_item, mSwimTracksList);
            mSwimListView.setAdapter(mSwimTracksAdapter);
        }

        //hides the progress bar since the
        //background process is completed
        //and displays the data
        setUIState(UISTATE_VIEW_DATA);
    }

    private void setUIState(int state){
        switch (state){
            case UISTATE_GETTING_DATA:
                mProgressBar.setVisibility(View.VISIBLE);
                mMessagePanel.setVisibility(View.GONE);
                mSwimListView.setVisibility(View.GONE);
                break;
            case UISTATE_VIEW_DATA:
                mProgressBar.setVisibility(View.GONE);
                mMessagePanel.setVisibility(View.GONE);
                mSwimListView.setVisibility(View.VISIBLE);
                break;
            default:
                LLog.e(new Exception("Missing UI state definition"));
                break;
        }
    }


    //=============== USER ACTIONS ================/d

    //when the menu item Add Swim is clicked
    //display the SwimEditActivity to add new
    //a new swim track
    @OptionsItem(R.id.menu_add_swim)
    void displayEditSwimActivity(){
        Intent displayIntent = new Intent(getActivity(), SwimEditActivity_.class);
        startActivity(displayIntent);
    }

    @OptionsItem(R.id.menu_backup)
    void displayGoogleLoginActivity(){
        Intent displayIntent = new Intent(getActivity(), BackupActivity_.class);
        startActivity(displayIntent);
    }

}
