package com.beoni.openwaterswimtracking;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.ListView;

import com.beoni.openwaterswimtracking.bll.RssManager;
import com.beoni.openwaterswimtracking.utils.LLog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EFragment(R.layout.fragment_rss)
@OptionsMenu(R.menu.rss_menu)
public class RssFragment extends Fragment {

    ArrayList<RssItemSimplified> mRssItems = new ArrayList<>();

    @Bean
    RssManager mRssManager;

    RssListAdapter rssListAdapter;

    @ViewById(R.id.rss_list)
    ListView mRssList;


    public RssFragment() {
        // Required empty public constructor
    }


    @OptionsItem(R.id.menu_show_swim_list)
    void startSwimActivity(){
        LLog.i("Navigating to swim activity");
    }

    @AfterViews
    void viewCreated() {
        loadRssItems();
    }

    @Background
    void loadRssItems() {
        mRssItems = mRssManager.getRssItems();
        updateAdapter();
    }

    @UiThread
    void updateAdapter() {
        rssListAdapter = new RssListAdapter(getContext(), R.layout.rss_item, mRssItems);
        mRssList.setAdapter(rssListAdapter);
    }

    @ItemClick(R.id.rss_list)
    void viewRssOnBrowser(int position){
        String link = mRssItems.get(position).getLink();
        Uri uri = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        startActivity(intent);
    }
}
