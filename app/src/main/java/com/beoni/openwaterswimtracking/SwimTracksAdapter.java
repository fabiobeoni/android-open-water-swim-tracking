package com.beoni.openwaterswimtracking;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.beoni.openwaterswimtracking.model.SwimTrack;

import java.util.ArrayList;

//TODO: change implementation of the all adapter by using Android Annotations https://github.com/androidannotations/androidannotations/wiki/Adapters-and-lists
public class SwimTracksAdapter extends ArrayAdapter
{

    private Context ctx;

    private int layoutId;

    public SwimTracksAdapter(Context ctx, int layoutId, ArrayList<SwimTrack> swimTracks) {
        super(ctx, layoutId, swimTracks);
        this.layoutId = layoutId;
        this.ctx = ctx;
    }


    @Override
    public View getView(int position, View viewItem, ViewGroup viewGroup) {
        final ItemView holder;

        if (viewItem == null)
        {
            LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
            viewItem = inflater.inflate(layoutId, viewGroup, false);
            holder = new ItemView();

            holder.titleView = (TextView) viewItem.findViewById(R.id.swim_title);
            viewItem.setTag(holder);
        }
        else
            holder = (ItemView) viewItem.getTag();

        SwimTrack item = (SwimTrack) this.getItem(position);

        holder.titleView.setText(item.getTitle());

        return viewItem;
    }

    static class ItemView
    {
        TextView titleView;
    }
}
