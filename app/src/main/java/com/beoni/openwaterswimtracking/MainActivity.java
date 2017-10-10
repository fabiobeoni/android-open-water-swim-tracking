package com.beoni.openwaterswimtracking;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.beoninet.android.easymessage.EasyMessageManager;
import com.google.android.gms.wearable.MessageEvent;

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

    //these two paths must be defined on wear module too
    public static final String MSG_SWIM_DATA_AVAILABLE = MainActivity.class.getPackage().getName()+".MSG_SWIM_DATA_AVAILABLE";
    public static final String MSG_SWIM_DATA_RECEIVED = MainActivity.class.getPackage().getName()+".MSG_SWIM_DATA_RECEIVED";

    private TabsPagerAdapter mTabsPagerAdapter;

    private EasyMessageManager easyMessageManager;


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

        easyMessageManager = new EasyMessageManager(ctx){
            @Override
            public void onMessageReceived(MessageEvent messageEvent)
            {
                if(messageEvent.getPath().equals(MSG_SWIM_DATA_AVAILABLE))
                {
                    Toast.makeText(ctx, (new String(messageEvent.getData())), Toast.LENGTH_LONG).show();

                    //process data, then send back the answer...

                    easyMessageManager.sendMessage(MSG_SWIM_DATA_RECEIVED, String.valueOf(true));
                }
            }
        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        easyMessageManager.connect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        easyMessageManager.disconnect();
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
