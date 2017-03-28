package com.beoni.openwaterswimtracking;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

        int size = getSwimManager(context).getSwimTracks(true).size();
        if(size>0)
        {
            int h = appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight;
            int w = appWidgetManager.getAppWidgetInfo(appWidgetId).minWidth;
            graphBitmap = createSwimmingGraph(context, w, h);
            views.setImageViewBitmap(R.id.img_graph,graphBitmap);
            views.setViewVisibility(R.id.txt_message, View.GONE);
            views.setViewVisibility(R.id.img_graph, View.VISIBLE);
        }
        else
        {
            Toast.makeText(context,R.string.no_swim,Toast.LENGTH_LONG).show();
            views.setViewVisibility(R.id.txt_message, View.VISIBLE);
            views.setViewVisibility(R.id.img_graph, View.GONE);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static SwimTrackManager getSwimManager(Context context)
    {
        if(swimTrackManager==null)
            swimTrackManager = new SwimTrackManager(context);

        return swimTrackManager;
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

    private static Bitmap createSwimmingGraph(Context ctx, int width, int height){

        ArrayList<SwimTrack> swimTrackList = getSwimManager(ctx).getSwimTracks(false);

        //sorts by date
        Collections.sort(swimTrackList, new Comparator<SwimTrack>() {
            public int compare(SwimTrack o1, SwimTrack o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        DataPoint[] dataPointsDuration = new DataPoint[swimTrackList.size()];
        for (int i=0;i<swimTrackList.size();i++)
            dataPointsDuration[i] = new DataPoint(i, (swimTrackList.get(i).getDuration()/60));

        DataPoint[] dataPointsLength = new DataPoint[swimTrackList.size()];
        for (int i=0;i<swimTrackList.size();i++)
            dataPointsLength[i] = new DataPoint(i, (swimTrackList.get(i).getLength()/1000));

        GraphView graph = new GraphView(ctx);
        graph.setLayoutParams(new RelativeLayout.LayoutParams(width,height));


        LineGraphSeries<DataPoint> seriesDistance = new LineGraphSeries<>(dataPointsLength);
        seriesDistance.setTitle(ctx.getString(R.string.swim_length));
        seriesDistance.setThickness(8);
        seriesDistance.setDrawDataPoints(true);
        seriesDistance.setDataPointsRadius(10);
        seriesDistance.setColor(Color.BLUE);
        graph.addSeries(seriesDistance);

        LineGraphSeries<DataPoint> seriesDuration = new LineGraphSeries<>(dataPointsDuration);
        seriesDuration.setTitle(ctx.getString(R.string.swim_duration));
        seriesDuration.setColor(Color.RED);
        seriesDuration.setThickness(8);
        seriesDuration.setDrawDataPoints(true);
        seriesDuration.setDataPointsRadius(10);
        graph.getSecondScale().addSeries(seriesDuration);

        // the y bounds are always manual for second scale
        graph.getSecondScale().setMinY(0);
        graph.getSecondScale().setMaxY(10);
        graph.getSecondScale().setVerticalAxisTitle(ctx.getString(R.string.duration_houres));
        graph.getSecondScale().setVerticalAxisTitleTextSize(22);
        graph.getSecondScale().setVerticalAxisTitleColor(Color.RED);

        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();

        gridLabel.setVerticalAxisTitle(ctx.getString(R.string.swim_length));
        gridLabel.setVerticalAxisTitleTextSize(22);
        gridLabel.setVerticalAxisTitleColor(Color.BLUE);
        gridLabel.setTextSize(22);
        gridLabel.setGridColor(Color.GRAY);
        gridLabel.setVerticalLabelsColor(Color.BLUE);
        gridLabel.setVerticalLabelsSecondScaleColor(Color.RED);
        gridLabel.reloadStyles();


        Bitmap b = createBitmapFromView(graph);

        Bitmap bitmap = Bitmap.createBitmap(b);
        graph.setDrawingCacheEnabled(false); // clear drawing cache

        return bitmap;
    }

    private static Bitmap createBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b) {
            @Override
            public boolean isHardwareAccelerated() {
                return true;
            }
        };
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }
}

