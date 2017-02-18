package com.beoni.openwaterswimtracking;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RssFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RssFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RssFragment extends Fragment {

    public RssFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rss, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rss_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //TODO: find a better way to handle those cases
        //when the settings button Filter gets clicked
        //sets the filter for the movie list to display
        /*
        switch (id){
            case R.id.menu_filter_popular:
                onMenuPopularClick();
                break;
            case R.id.menu_reload:
                onMenuPopularClick();
                break;
            case R.id.menu_filter_rated:
                onMenuTopRatedClick();
                break;
            case R.id.menu_filter_favorites:
                onMenuOrderByFavoritesClick();
                break;
            case R.id.menu_clear_favorites:
                onMenuClearFavoritesClick();
                break;
        }
        */

        return super.onOptionsItemSelected(item);
    }

}
