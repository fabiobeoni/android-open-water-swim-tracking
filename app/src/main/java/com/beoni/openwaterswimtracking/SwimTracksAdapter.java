package com.beoni.openwaterswimtracking;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.utils.FormattingUtils;

import java.util.ArrayList;

//TODO: change implementation of the all adapter by using Android Annotations https://github.com/androidannotations/androidannotations/wiki/Adapters-and-lists
public class SwimTracksAdapter extends ArrayAdapter
{

    public static final String DRAWABLE = "drawable";
    private Context ctx;
    private int layoutId;
    private String[] tempColors;
    private String[] tempValues;
    private String[] flowIconNames;
    private String[] wavesIconNames;
    private Resources resources;


    public SwimTracksAdapter(Context ctx, int layoutId, ArrayList<SwimTrack> swimTracks) {
        super(ctx, layoutId, swimTracks);
        this.layoutId = layoutId;
        this.ctx = ctx;
        resources = ctx.getResources();
        tempColors = resources.getStringArray(R.array.temperature_colors);
        tempValues = resources.getStringArray(R.array.temperature_values);
        flowIconNames = resources.getStringArray(R.array.flow_icons);
        wavesIconNames = resources.getStringArray(R.array.waves_icons);
    }


    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final String packageName = getContext().getPackageName();
        final ItemView itemView;
        int temperatureColor;
        int resourceID;
        String resourceName;

        if (view == null)
        {
            LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
            view = inflater.inflate(layoutId, viewGroup, false);
            itemView = new ItemView();

            itemView.wavesImgVw = (ImageView) view.findViewById(R.id.icon_swim_temperature);
            itemView.flowImgVw = (ImageView) view.findViewById(R.id.icon_swim_flow);
            itemView.titleTvw = (TextView) view.findViewById(R.id.swim_title);
            itemView.locationTvw = (TextView) view.findViewById(R.id.swim_location);
            itemView.lengthTvw = (TextView) view.findViewById(R.id.swim_length);
            itemView.durationTvw = (TextView) view.findViewById(R.id.swim_duration);
            view.setTag(itemView);
        }
        else
            itemView = (ItemView) view.getTag();


        SwimTrack swimTrack = (SwimTrack) this.getItem(position);

        itemView.titleTvw.setText(swimTrack.getTitle());
        itemView.locationTvw.setText(swimTrack.getLocation());
        itemView.durationTvw.setText(FormattingUtils.formatDuration(getContext(),swimTrack.getDuration()));
        itemView.lengthTvw.setText(String.valueOf(swimTrack.getLength())+resources.getString(R.string.swim_length_label));

        temperatureColor = Color.parseColor(tempColors[swimTrack.getPerceivedTemperature()]);
        itemView.wavesImgVw.setColorFilter(temperatureColor);
        itemView.flowImgVw.setColorFilter(temperatureColor);

        resourceName = wavesIconNames[swimTrack.getWaves()];
        resourceID = resources.getIdentifier(resourceName, DRAWABLE, packageName);
        itemView.wavesImgVw.setImageResource(resourceID);

        resourceName = wavesIconNames[swimTrack.getWaves()];
        resourceID = resources.getIdentifier(resourceName, DRAWABLE, packageName);
        itemView.flowImgVw.setImageResource(resourceID);

        return view;
    }

    static class ItemView
    {
        TextView locationTvw;
        TextView titleTvw;
        TextView lengthTvw;
        TextView durationTvw;
        ImageView wavesImgVw;
        ImageView flowImgVw;
    }
}
