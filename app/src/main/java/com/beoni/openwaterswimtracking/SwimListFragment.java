package com.beoni.openwaterswimtracking;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.utils.LLog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * The fragment presents swim tracking data in a list view,
 * performs swim reading from the storage by the
 * SwimManager.
 */
@EFragment(R.layout.fragment_swim_list)
@OptionsMenu(R.menu.menu_swim)
public class SwimListFragment extends Fragment {

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

    // Required empty public constructor
    public SwimListFragment() {}

    @Override
    public void onPause()
    {
        super.onPause();
        saveDataOnLocalFile();
    }

    /**
     * Requests data when not saved
     * on instance state
     */
    @AfterViews
    void viewCreated() {
        //mSwimTracksList is saved in instance state
        //so can be reused
        if(mSwimTracksList ==null){
            //updates the UI showing progress bar
            //for background tasks
            setUIState(UIStates.GETTING_DATA);

            //gets data from the web or from cached
            loadData();
        }
        else //just proceed with UI update
            onDataLoadCompleted();
    }


    @Background
    void loadData() {
        mSwimTracksList = mSwimTrackManager.getSwimTracks();
        onDataLoadCompleted();
    }

    @UiThread
    void onDataLoadCompleted() {
        //updates the list adapter to display the data
        if(mSwimTracksList !=null && mSwimTracksList.size()>0){
            mSwimTracksAdapter = new SwimTracksAdapter(getContext(), R.layout.swim_track_item, mSwimTracksList);
            mSwimListView.setAdapter(mSwimTracksAdapter);

            //hides the progress bar since the
            //background process is completed
            //and displays the data
            setUIState(UIStates.VIEW_DATA);
        }
        else
            //hides the progress bar since the
            //background process is completed
            //and shows a message
            setUIState(UIStates.OFFLINE);
    }

    @Background
    void saveDataOnLocalFile(){
        mSwimTrackManager.save();
    }


    //============== UI STATES ==============/

    //TODO: avoid enums!!!!
    private enum UIStates { GETTING_DATA, OFFLINE, VIEW_DATA}

    private void setUIState(UIStates state){
        switch (state){
            case GETTING_DATA:
                mProgressBar.setVisibility(View.VISIBLE);
                mMessagePanel.setVisibility(View.GONE);
                mSwimListView.setVisibility(View.GONE);
                break;
            case OFFLINE:
                mProgressBar.setVisibility(View.GONE);
                mMessagePanel.setVisibility(View.VISIBLE);
                mSwimListView.setVisibility(View.GONE);
                break;
            case VIEW_DATA:
                mProgressBar.setVisibility(View.GONE);
                mMessagePanel.setVisibility(View.GONE);
                mSwimListView.setVisibility(View.VISIBLE);
                break;
            default:
                LLog.e(new Exception("Missing UI state definition"));
                break;
        }
    }

}
