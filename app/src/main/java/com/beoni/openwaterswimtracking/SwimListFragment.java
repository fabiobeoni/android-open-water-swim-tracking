package com.beoni.openwaterswimtracking;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.utils.ConnectivityUtils;
import com.beoni.openwaterswimtracking.utils.LLog;
import com.beoninet.openwaterswimtracking.shared.Constants;
import com.google.gson.Gson;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;

import java.util.ArrayList;

/**
 * The fragment presents swim tracking data in a list view,
 * performs swim reading from the storage by the
 * SwimManager.
 */
@EFragment(R.layout.fragment_swim_list)
@OptionsMenu(R.menu.menu_swim_list)
public class SwimListFragment extends Fragment
{
    public static final String TASK_LOAD_DATA_ASYNC = "TASK_LOAD_DATA_ASYNC";

    //============ AVAILABLE UI STATES ===============/

    /**
     * App is loading data from locally stored file.
     * User sees a loading progress.
     */
    private static final int UISTATE_GETTING_DATA = 0;

    /**
     * App has completed the loading of
     * data from the storing file and
     * a list with all available swim
     * tracks is visible to the user.
     */
    private static final int UISTATE_VIEW_DATA = 1;

    /**
     * App has completed the load of the file
     * hosting swim tracks data, but the list
     * is empty. App presents UI controls to
     * inform the user and to move to swim editing.
     */
    private static final int UISTATE_NO_DATA = 2;


    //list of swim track items presented on view
    @InstanceState
    ArrayList<SwimTrack> mSwimTracksList = null;

    //class that actually performs the swim CRUD actions
    @Bean
    SwimTrackManager mSwimTrackManager;

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

    //Required empty public constructor
    public SwimListFragment() {}

    @Override
    public void onResume()
    {
        super.onResume();

        //the hosting activity can handle requests
        //to refresh this list
        boolean forceListReload = getActivity().getIntent().getBooleanExtra(Constants.INTENT_UPDATE_LIST,false);

        //mSwimTracksList is saved in instance state
        //so can be reused
        if (mSwimTracksList == null || forceListReload)
        {
            //updates the UI showing progress bar
            //for background tasks
            setUIState(UISTATE_GETTING_DATA);

            //gets data from the web or from cached
            loadDataAsync();
        }
        else //just proceed with UI update
            onDataLoadCompleted();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        BackgroundExecutor.cancelAll(TASK_LOAD_DATA_ASYNC,true);
    }

    /**
     * Performs asynchronous loading
     * of swim tracks data from the
     * locally stored file.
     */
    @Background(id=TASK_LOAD_DATA_ASYNC)
    void loadDataAsync() {
        mSwimTracksList = mSwimTrackManager.getSwimTracks(true);
        onDataLoadCompleted();
    }

    /**
     * Updates the UI to display loaded swim tracks,
     * or just a message to user when no swim tracks
     * are available.
     */
    @UiThread
    void onDataLoadCompleted() {
        //updates the list adapter to display the data
        if(mSwimTracksList !=null && mSwimTracksList.size()>0){
            SwimTracksAdapter mSwimTracksAdapter = new SwimTracksAdapter(getContext(), R.layout.swim_track_item, mSwimTracksList);
            mSwimListView.setAdapter(mSwimTracksAdapter);
            //hides the progress bar since the
            //background process is completed
            //and displays the data
            setUIState(UISTATE_VIEW_DATA);
        }
        else
            setUIState(UISTATE_NO_DATA);
    }

    /**
     * Updates view controls visibility
     * according to the state.
     * @param state
     */
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
            case UISTATE_NO_DATA:
                mProgressBar.setVisibility(View.GONE);
                mMessagePanel.setVisibility(View.VISIBLE);
                mSwimListView.setVisibility(View.GONE);
                break;
            default:
                LLog.e(new Exception("Missing UI state definition"));
                break;
        }
    }


    //=============== USER ACTIONS ================/d

    /**
     * When the menu item Add Swim is clicked
     * display the SwimEditActivity to add new
     * a new swim track
     */
    @Click(R.id.btn_add_swim)
    void displayEditSwimActivity(){
        Intent intent = new Intent(getActivity(), SwimEditActivity_.class);
        //these two flags make sure your receiving activity will get new data from getIntent() instead of reusing first once
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.INTENT_SWIM_ITEM, "");
        startActivity(intent);
    }

    /**
     * If Internet connection is available
     * redirects the user to the Google Sign-in
     * activity.
     */
    @OptionsItem(R.id.menu_backup)
    void displayGoogleLoginActivity(){
        if(!ConnectivityUtils.isDeviceConnected(getContext()))
        {
            Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_LONG).show();
            return;
        }

        Intent displayIntent = new Intent(getActivity(), BackupActivity_.class);
            startActivity(displayIntent);
    }

    /**
     * When the user clicks on a swim track
     * item, the data related to that item
     * are serialized and sent to the editing
     * swim activity.
     * @param swimTrack
     */
    @ItemClick(R.id.swim_list)
    void onSwimListItemClick(SwimTrack swimTrack){
        String swimJson = new Gson().toJson(swimTrack);
        Intent intent = new Intent(getContext(), SwimEditActivity_.class);
        //these two flags make sure your receiving activity will get new data from getIntent() instead of reusing first once
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.INTENT_SWIM_ITEM, swimJson);
        intent.putExtra(Constants.INTENT_SWIM_ITEM_INDEX, mSwimTracksList.indexOf(swimTrack));
        startActivity(intent);
    }

}
