package com.beoni.openwaterswimtracking.bll;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout;

import com.beoni.openwaterswimtracking.R;
import com.beoni.openwaterswimtracking.data.LocalFileStorage;
import com.beoni.openwaterswimtracking.model.RssItemSimplified;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.androidannotations.annotations.EBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

@EBean
public class SwimTrackManager
{
    private static final String FILE_NAME = "OWSTSwimTrack.txt";

    private Context mContext;
    private LocalFileStorage mStorage;
    private SharedPreferences mPreferences;
    private ArrayList<SwimTrack> mSwimTracks;

    public SwimTrackManager(Context ctx){
        mContext = ctx;
        mStorage = LocalFileStorage.get(ctx);
        mPreferences = mContext.getSharedPreferences(ctx.getString(R.string.preferences_file), Context.MODE_PRIVATE);
    }

    public Bitmap createSwimmingGraph(int w, int h){

        ArrayList<SwimTrack> swimTrackList = getSwimTracks(false);

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

        GraphView graph = new GraphView(mContext);
        graph.setLayoutParams(new RelativeLayout.LayoutParams(w,h));


        LineGraphSeries<DataPoint> seriesDistance = new LineGraphSeries<>(dataPointsLength);
        seriesDistance.setTitle(mContext.getString(R.string.swim_length));
        seriesDistance.setThickness(8);
        seriesDistance.setDrawDataPoints(true);
        seriesDistance.setDataPointsRadius(10);
        seriesDistance.setColor(Color.BLUE);
        graph.addSeries(seriesDistance);

        LineGraphSeries<DataPoint> seriesDuration = new LineGraphSeries<>(dataPointsDuration);
        seriesDuration.setTitle(mContext.getString(R.string.swim_duration));
        seriesDuration.setColor(Color.RED);
        seriesDuration.setThickness(8);
        seriesDuration.setDrawDataPoints(true);
        seriesDuration.setDataPointsRadius(10);
        graph.getSecondScale().addSeries(seriesDuration);

        // the y bounds are always manual for second scale
        graph.getSecondScale().setMinY(0);
        graph.getSecondScale().setMaxY(10);
        graph.getSecondScale().setVerticalAxisTitle(mContext.getString(R.string.duration_houres));
        graph.getSecondScale().setVerticalAxisTitleTextSize(22);
        graph.getSecondScale().setVerticalAxisTitleColor(Color.RED);

        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();

        gridLabel.setVerticalAxisTitle(mContext.getString(R.string.swim_length));
        gridLabel.setVerticalAxisTitleTextSize(22);
        gridLabel.setVerticalAxisTitleColor(Color.BLUE);
        gridLabel.setTextSize(22);
        gridLabel.setGridColor(Color.GRAY);
        gridLabel.setVerticalLabelsColor(Color.BLUE);
        gridLabel.setVerticalLabelsSecondScaleColor(Color.RED);
        gridLabel.reloadStyles();


        Bitmap b = SwimTrackManager.loadBitmapFromView(graph);

        Bitmap bitmap = Bitmap.createBitmap(b);
        graph.setDrawingCacheEnabled(false); // clear drawing cache

        return bitmap;
    }

    public static Bitmap loadBitmapFromView(View v) {
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


    public ArrayList<SwimTrack> getSwimTracks(boolean forceReload)
    {
        if(mSwimTracks==null || forceReload)
            mSwimTracks = readFile();

        return mSwimTracks;
    }

    public void addNewSwimTrack(SwimTrack swim){
        getSwimTracks(false);
        mSwimTracks.add(swim);
    }

    public void updateSwimTrack(int index, SwimTrack currentSwim){
        getSwimTracks(false);
        mSwimTracks.remove(index);
        mSwimTracks.add(index,currentSwim);
    }

    public void deleteSwimTrack(int index){
        getSwimTracks(false);
        mSwimTracks.remove(index);
    }

    public void save(){
        writeFile(mSwimTracks);
    }

    public String getLocalDataForBackup(){
        return mStorage.readTextFile(FILE_NAME);
    }

    public void restoreLocalDataFromBackup(String content){
        mStorage.writeTextFile(FILE_NAME, content);
    }

    private ArrayList<SwimTrack> readFile(){
        Type listType = new TypeToken<ArrayList<SwimTrack>>(){}.getType();
        ArrayList<SwimTrack> items = new ArrayList<SwimTrack>();
        String result = mStorage.readTextFile(FILE_NAME);

        items = new Gson().fromJson(result, listType);

        if(items.size()>0)
            items = new Gson().fromJson(result, listType);
        /*
        else{
            //sample data on the list to simplify project review

            items.add(new SwimTrack(
                "Albano Lake Swimming Maraton",
                "Valid for Italian senior swimming championship",
                "Albano, Latium - Italy",
                DateUtils.stringToDate("01/06/2000",DateUtils.SHORT_FORMAT),
                50, 3000, 1, 1, 1
            ));

            items.add(new SwimTrack(
                "Crossing Messina",
                "Valid for Italian senior swimming championship",
                "Messina, Sicily - Italy",
                DateUtils.stringToDate("10/07/2001",DateUtils.SHORT_FORMAT),
                65, 3500, 2, 3, 1
            ));

            items.add(new SwimTrack(
                "Swimming Gibilterra (Europe to Africa)",
                "",
                "Gibilterra - Spain",
                DateUtils.stringToDate("12/08/202",DateUtils.SHORT_FORMAT),
                30, 25000, 1, 2, 2
            ));
        }
        */

        return items;
    }

    private void writeFile(ArrayList<SwimTrack> items){
        if(items!=null){
            String itemsAsString = new Gson().toJson(items);
            mStorage.writeTextFile(FILE_NAME, itemsAsString);
        }
    }

}
