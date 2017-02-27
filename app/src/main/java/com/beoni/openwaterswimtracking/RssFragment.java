package com.beoni.openwaterswimtracking;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.beoni.openwaterswimtracking.bll.RssManager;
import com.beoni.openwaterswimtracking.model.RssItemSimplified;
import com.beoni.openwaterswimtracking.utils.LLog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * The fragment presents RSS data in a list view,
 * performs RSS download and local cache by the
 * RssManager, handles the navigation to the swim
 * list activity.
 */
@EFragment(R.layout.fragment_rss)
public class RssFragment extends Fragment {

    //list of Rss items presented on view
    @InstanceState
    ArrayList<RssItemSimplified> mRssItems = null;

    //class that actually performs the Rss download and cache
    @Bean
    RssManager mRssManager;

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
            setUIState(UIStates.GETTING_DATA);

            //gets data from the web or from cached
            loadData();
        }
        else //just proceed with UI update
            onDataLoadCompleted();
    }

    /**
     * Async load rss data from cache or from the web
     * when cache is not valid anymore and network
     * is available, then request list view update
     */
    @Background
    void loadData() {
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
            setUIState(UIStates.VIEW_DATA);
        }
        else
            //hides the progress bar since the
            //background process is completed
            //and shows a message
            setUIState(UIStates.OFFLINE);
    }


    //========== USER INTERACTION ===========/

    //handler for Rss list view item
    //click event that starts the browsers
    //and displays the RSS web page
    @ItemClick(R.id.rss_list)
    void viewRssOnBrowser(int position){
        String link = mRssItems.get(position).getLink();
        Uri uri = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        startActivity(intent);
    }


    //============== UI STATES ==============/

    private enum UIStates { GETTING_DATA, OFFLINE, VIEW_DATA}

    private void setUIState(UIStates state){
        switch (state){
            case GETTING_DATA:
                mProgressBar.setVisibility(View.VISIBLE);
                mMessagePanel.setVisibility(View.GONE);
                mRssList.setVisibility(View.GONE);
                break;
            case OFFLINE:
                mProgressBar.setVisibility(View.GONE);
                mMessagePanel.setVisibility(View.VISIBLE);
                mRssList.setVisibility(View.GONE);
                break;
            case VIEW_DATA:
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
