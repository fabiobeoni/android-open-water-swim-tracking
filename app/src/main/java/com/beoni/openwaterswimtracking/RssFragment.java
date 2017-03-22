package com.beoni.openwaterswimtracking;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
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
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.Receiver;
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

    private static final int UISTATE_GETTING_DATA = 0;
    private static final int UISTATE_OFFLINE = 1;
    private static final int UISTATE_VIEW_DATA = 2;


    //list of Rss items presented on view
    @InstanceState
    ArrayList<RssItemSimplified> mRssItems = null;

    //class that actually performs the Rss restoreFromFireDatabase and cache
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

    @Receiver(actions = ConnectivityManager.CONNECTIVITY_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void onConnectivityChange() {
        if(ConnectivityUtils.isDeviceConnected(getContext()) && mRssItems==null)
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

    @Click(R.id.btn_show_swim)
    void onBtnShowSwimClick(){
        ((ITabSelectionRequest)getActivity()).onSelectTab(1);
    }

    /**
     * Async load rss data from cache or from the web
     * when cache is not valid anymore and network
     * is available, then request list view update
     */
    //TODO: as requested by the rubric: using here AsyncTask for atomic one time request.
    //@Background
    void loadData() {
        //Performs data load at once, when starting the app
        //and your local data are obsolete (see RssManager).
        //Otherwise loads from local cache.
        new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] objects)
            {
                RssManager mng = new RssManager(getContext());
                return mng.getRssItems();
            }

            @Override
            protected void onPostExecute(Object o)
            {
                mRssItems = (ArrayList<RssItemSimplified>)o;
                onDataLoadCompleted();
            }
        }.execute(getContext());

        //mRssItems = mRssManager.getRssItems();
        //onDataLoadCompleted();
    }

    /**
     Performs list view adapter refresh
     with loaded rss data (if any), otherwise
     displays the swim list activity
     */
    //TODO: for project approval. Restore commented code after.
    //@UiThread
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
    }


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
