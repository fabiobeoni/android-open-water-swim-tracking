package com.beoni.openwaterswimtracking;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.beoni.openwaterswimtracking.bll.RssManager;
import com.beoni.openwaterswimtracking.model.RssItemSimplified;
import com.beoni.openwaterswimtracking.utils.ConnectivityUtils;
import com.beoni.openwaterswimtracking.utils.LLog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * The fragment presents RSS data in a list view,
 * performs RSS restoreFromFireDatabase and local cache by the
 * RssManager, handles the navigation to the swim
 * list activity.
 */
@EFragment(R.layout.fragment_main)
public class RssFragment extends Fragment {

    //interface to communicate with the hosting
    //activity and request a tab change, if any.
    public interface ITabSelectionRequest{
        void onSelectTab(int index);
    }

    //============== UI STATES ==============/

    /**
     * App is getting Rss data from web site.
     * Progress should be displayed to the user.
     */
    private static final int UISTATE_GETTING_DATA = 0;

    /**
     * App cannot get Rss data since the divice
     * is not connected. Alert the user.
     */
    private static final int UISTATE_OFFLINE = 1;

    /**
     * Add has completed the download of Rss data
     * and is showing them on the view.
     */
    private static final int UISTATE_VIEW_DATA = 2;


    //keep track of Rss loading on progress
    private static boolean isLoadingData = false;

    //list of Rss items presented on view
    @InstanceState
    ArrayList<RssItemSimplified> mRssItems = null;

    //class that actually performs the Rss restoreFromFireDatabase and cache
    @Bean
    RssManager mRssManager;

    //TODO:change adapter implementation to use AndroidAnnotations framework
    //adapt class for this view
    RssListAdapter rssListAdapter;

    //list view presenting the Rss data
    @ViewById(R.id.rss_list)
    ListView mRssList;

    @ViewById(R.id.progress_bar)
    ProgressBar mProgressBar;

    //group of ui elements to display
    //a message to the user when
    //device is offline
    @ViewById(R.id.rss_message_panel)
    LinearLayout mMessagePanel;

    /**
     * Perform download of Rss data
     * if local cache is empty and connection
     * becomes available.
     */
    @Receiver(
        actions = ConnectivityManager.CONNECTIVITY_ACTION,
        registerAt = Receiver.RegisterAt.OnResumeOnPause
    )
    void onConnectivityChange() {
        if(
            ConnectivityUtils.isDeviceConnected(getContext()) &&
            mRssItems==null &&
            !isLoadingData
        )
            loadData();
    }

    // Required empty public constructor
    public RssFragment() {}

    /**
     * Requests data when not saved
     * on instance state
     */
    @AfterViews
    void viewCreated() {
        //mSwimTracksList is saved in instance state
        //so can be reused
        if(mRssItems==null){
            //updates the UI showing progress bar
            //for background tasks
            setUIState(UISTATE_GETTING_DATA);

            //gets data from the web or from cached
            loadData();
        }
        else //just proceed with UI update
            onDataLoadCompleted();
    }

    /**
     * Request to the hosting activity
     * to display the swim tracks list
     * tab.
     */
    @Click(R.id.btn_show_swim)
    void onBtnShowSwimClick(){
        ((ITabSelectionRequest)getActivity()).onSelectTab(1);
    }

    /**
     * Async load rss data from cache or from the web
     * when cache is not valid anymore and network
     * is available, then request list view update
     */
    @Background
    void loadData() {
        isLoadingData = true;
        //Performs data load at once, when starting the app
        //and your local data are obsolete (see RssManager).
        //Otherwise loads from local cache.
        mRssItems = mRssManager.getRssItems();
        onDataLoadCompleted();
    }

    /**
     Performs list view adapter refresh
     with loaded rss data (if any), otherwise
     displays the swim list activity
     */
    @UiThread
    void onDataLoadCompleted() {
        //updates the list adapter to display the data
        if(mRssItems!=null && mRssItems.size()>0){
            rssListAdapter = new RssListAdapter(getContext(), R.layout.rss_item, mRssItems);
            mRssList.setAdapter(rssListAdapter);

            //hides the progress bar since the
            //background process is completed
            //and displays the data
            setUIState(UISTATE_VIEW_DATA);
        }
        else
            //hides the progress bar since the
            //background process is completed
            //and shows a message
            setUIState(UISTATE_OFFLINE);

        isLoadingData = false;
    }

    /**
     * Manages view controls according
     * to the current UI state.
     * @param state see list of available states in this class.
     */
    private void setUIState(int state){
        switch (state){
            case UISTATE_GETTING_DATA:
                mProgressBar.setVisibility(View.VISIBLE);
                mMessagePanel.setVisibility(View.GONE);
                mRssList.setVisibility(View.GONE);
                break;
            case UISTATE_OFFLINE:
                mProgressBar.setVisibility(View.GONE);
                mMessagePanel.setVisibility(View.VISIBLE);
                mRssList.setVisibility(View.GONE);
                break;
            case UISTATE_VIEW_DATA:
                mProgressBar.setVisibility(View.GONE);
                mMessagePanel.setVisibility(View.GONE);
                mRssList.setVisibility(View.VISIBLE);
                break;
            default:
                LLog.e(new Exception("Missing UI state definition"));
                break;
        }
    }

}
