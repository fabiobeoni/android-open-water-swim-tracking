package com.beoni.openwaterswimtracking;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;

/**
 * Implementation of App Widget functionality.
 */
public class SwimminhGraphWidget extends AppWidgetProvider
{

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {
        int h = appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight;
        int w = appWidgetManager.getAppWidgetInfo(appWidgetId).minWidth;

        Bitmap bitmap = SwimTrackManager.createSwimmingGraph(context,w,h);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.swimminh_graph_widget);
        views.setImageViewBitmap(R.id.img_graph,bitmap);

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

