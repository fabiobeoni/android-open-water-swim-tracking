package com.beoni.openwaterswimtracking;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RemoteViews;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;

/**
 * Implementation of App Widget functionality.
 */
public class SwimminhGraphWidget extends AppWidgetProvider
{
    private static SwimTrackManager swimTrackManager;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {
        Bitmap graphBitmap;
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.swimminh_graph_widget);

        if(swimTrackManager==null)
            swimTrackManager = new SwimTrackManager(context);

        if(swimTrackManager.getSwimTracks(false).size()>0)
        {
            int h = appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight;
            int w = appWidgetManager.getAppWidgetInfo(appWidgetId).minWidth;
            graphBitmap = swimTrackManager.createSwimmingGraph(w, h);
            views.setImageViewBitmap(R.id.img_graph,graphBitmap);
            views.setViewVisibility(R.id.txt_message, View.GONE);
            views.setViewVisibility(R.id.img_graph, View.VISIBLE);
        }
        else
        {
            views.setViewVisibility(R.id.txt_message, View.VISIBLE);
            views.setViewVisibility(R.id.img_graph, View.GONE);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }
}

