package com.beoni.openwaterswimtracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.beoni.openwaterswimtracking.model.RssItemSimplified;
import com.beoni.openwaterswimtracking.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

//TODO: change implementation of the all adapter by using Android Annotations https://github.com/androidannotations/androidannotations/wiki/Adapters-and-lists
/**
 * Adapter class to bind Rss item data to the view.
 */
public class RssListAdapter extends ArrayAdapter
{
    private Context ctx;

    private int layoutId;

    /**
     * Initialize the adapter by passing the
     * list of rss items to be displayed on view.
     * @param ctx
     * @param layoutId
     * @param rssItems
     */
    public RssListAdapter(Context ctx, int layoutId, ArrayList<RssItemSimplified> rssItems) {
        super(ctx, layoutId, rssItems);
        this.layoutId = layoutId;
        this.ctx = ctx;
    }


    @Override
    public View getView(int position, View viewItem, ViewGroup viewGroup) {
        final RssItemView holder;

        if (viewItem == null)
        {
            LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
            viewItem = inflater.inflate(layoutId, viewGroup, false);
            holder = new RssItemView();
            holder.btnKnowMore = (Button) viewItem.findViewById(R.id.btn_know_more);
            holder.titleView = (TextView) viewItem.findViewById(R.id.rss_title);
            holder.dateView = (TextView) viewItem.findViewById(R.id.rss_date);
            holder.descriptionView = (TextView) viewItem.findViewById(R.id.rss_text);
            holder.imageView = (ImageView) viewItem.findViewById(R.id.rss_image);
            viewItem.setTag(holder);
        }
        else
            holder = (RssItemView) viewItem.getTag();

        //the single Rss item
        final RssItemSimplified rssItemSimplified = (RssItemSimplified) this.getItem(position);

        //rss item usually has a main image...
        //when not available displays a place holder
        if(rssItemSimplified.getImageUrl()!=null)
            Picasso.with(getContext()).load(rssItemSimplified.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imageView);

        holder.titleView.setText(rssItemSimplified.getTitle());
        holder.dateView.setText(DateUtils.dateToString(rssItemSimplified.getDate(),DateUtils.SHORT_FORMAT));
        holder.descriptionView.setText(rssItemSimplified.getDescription());

        //clicking of the "Know More" button
        //moves the user to the browser to read
        //the full Rss post.
        holder.btnKnowMore.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String link = rssItemSimplified.getLink();
                Uri uri = Uri.parse(link);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
                getContext().startActivity(intent);
            }
        });

        return viewItem;
    }

    static class RssItemView
    {
        Button btnKnowMore;
        TextView titleView;
        TextView dateView;
        TextView descriptionView;
        ImageView imageView;
    }
}
