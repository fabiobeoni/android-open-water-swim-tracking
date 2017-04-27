package com.beoni.openwaterswimtracking;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.beoni.openwaterswimtracking.utils.LLog;

import java.util.Map;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter
{
    //list of all available tabs titles
    //that will be presented when a tab
    //is selected
    private Map<Integer,String> mTabsTitles;

    //list of available tabs fragments
    //that will be presented when a tab
    //is selected
    private Map<Integer,Class> mTabsFragments;


    public TabsPagerAdapter(FragmentManager fm, Map<Integer, String> tabTitles, Map<Integer, Class> tabsFragments)
    {
        super(fm);
        mTabsFragments = tabsFragments;
        mTabsTitles = tabTitles;
    }

    @Override
    public Fragment getItem(int position)
    {
        Class fragmentClass = mTabsFragments.get(position);
        try
        {
            return (Fragment) fragmentClass.newInstance();
        } catch (InstantiationException e)
        {
            e.printStackTrace();
            LLog.e(e);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
            LLog.e(e);
        }

        return null;
    }

    @Override
    public int getCount()
    {
        return mTabsFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return mTabsTitles.get(position);
    }
}
