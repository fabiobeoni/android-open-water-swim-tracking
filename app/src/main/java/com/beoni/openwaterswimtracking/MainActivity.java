package com.beoni.openwaterswimtracking;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.beoninet.android.easymessage.EasyMessageManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Map;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements
        RssFragment.ITabSelectionRequest
{
    public static final String REQUEST_SELECTED_TAB_KEY = "REQUEST_SELECTED_TAB_KEY";
    public static final String WEAR_DATA_KEY = "WEAR_DATA_KEY";

    //these two paths must be defined on wear module too
    public static final String MSG_SWIM_DATA_AVAILABLE = MainActivity.class.getPackage().getName()+".MSG_SWIM_DATA_AVAILABLE";
    public static final String MSG_SWIM_DATA_RECEIVED = MainActivity.class.getPackage().getName()+".MSG_SWIM_DATA_RECEIVED";

    private TabsPagerAdapter mTabsPagerAdapter;


    //================== UI CONTROLS ===============//

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.tab_container)
    ViewPager mViewPager;

    @ViewById(R.id.tabs)
    TabLayout mTabLayout;

    //set by the SwimEditActivity before
    //sending the user back to this activity
    //to display the swim tab instead of
    //the news one (default)
    @Extra(REQUEST_SELECTED_TAB_KEY)
    int mSelectedTab;

    @Extra(WEAR_DATA_KEY)
    String mWearData;

    //list of all available tabs titles
    //that will be presented when a tab
    //is selected
    Map<Integer,String> mTabsTitles;

    //list of available tabs fragments
    //that will be presented when a tab
    //is selected
    Map<Integer,Class> mTabsFragments;

    //basic UI initialization
    @AfterViews
    void viewCreated(){
        final Context ctx = this;

        setSupportActionBar(mToolbar);

        //starts the service who listens for messages coming
        //from the wear
        WearMessageListener.startService(this);

        //populates the list of titles. See SectionsPagerAdapter
        mTabsTitles = new HashMap<Integer, String>(){{
            put(0,getString(R.string.rss_activity_label));
            put(1,getString(R.string.swim_activity_label));
        }};

        //populates the list of fragments. See SectionsPagerAdapter
        mTabsFragments = new HashMap<Integer, Class>(){{
            put(0,RssFragment_.class);
            put(1,SwimListFragment_.class);
        }};

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mTabsPagerAdapter = new TabsPagerAdapter(
                getSupportFragmentManager(),
                mTabsTitles,
                mTabsFragments
        );

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mTabsPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

        //default 0
        mTabLayout.getTabAt(mSelectedTab).select();
    }

    @AfterExtras
    void onStartedByIntentFromWear(){
        if(mWearData!=null && !mWearData.isEmpty())
            Toast.makeText(this,"Got data",Toast.LENGTH_LONG).show();
    }

    /**
     * By invoking this method a child fragment can
     * request to this activity to select a specific tab.
     * Is normally invoked when the user add, removes or
     * restores the list of swimming tracks and gets redirected
     * to this activity. Once redirected, the user will see
     * the selected tab (swim tracks).
     * @param index index of the tab to select an make visible
     */
    @Override
    public void onSelectTab(int index)
    {
        mTabLayout.getTabAt(index).select();
    }

}
