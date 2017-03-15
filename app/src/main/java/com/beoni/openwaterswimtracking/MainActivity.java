package com.beoni.openwaterswimtracking;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Map;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements RssFragment.ITabSelectionRequest
{
    public static final String REQUEST_SELECTED_TAB_KEY = "REQUEST_SELECTED_TAB_KEY";

    private TabsPagerAdapter mTabsPagerAdapter;

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


    @AfterViews
    void viewCreated(){
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
    }

    @Override
    public void onSelectTab(int index)
    {
        mTabLayout.getTabAt(index).select();
    }
}
