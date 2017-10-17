package com.beoni.openwaterswimtracking;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beoni.openwaterswimtracking.bll.MapManager;
import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.utils.DateUtils;
import com.beoninet.openwaterswimtracking.shared.Constants;
import com.beoninet.openwaterswimtracking.shared.LocationSerializer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.res.StringArrayRes;

import java.util.List;

/**
 * Fragment hosting the form to edit
 * swim track data like location, duration,
 * length, temperature, etc.
 */
@EFragment(R.layout.fragment_swim_edit)
@OptionsMenu(R.menu.menu_edit_swim)
public class SwimEditFragment extends Fragment
{

    private static final String TAG = SwimEditFragment.class.getCanonicalName();

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    /**
     * Intent key checked by this fragment to
     * display a specific swim track to edit.
     * The key must return a JSON serialized
     * swim track.
     */
    public static final String SWIM_ITEM_KEY = "SWIM_ITEM_KEY";

    /**
     * Intent key checked by this fragment
     * to know the index of the current editing
     * swim track (the one on SWIM_ITEM_KEY)
     */
    public static final String SWIM_ITEM_INDEX = "SWIM_ITEM_INDEX";

    //keep track of editing swim track status
    private boolean isNewSwim;

    //keep track of editing swim index (if not new swim)
    private int mSwimIndex;

    @InstanceState
    SwimTrack mSwimTrack;

    //class that actually performs the swim CRUD actions
    @Bean
    SwimTrackManager mSwimTrackManager;

    //swim track waves options
    @StringArrayRes
    String[] wavesValues;

    //swim track temperature options
    @StringArrayRes
    String[] temperatureValues;

    @StringRes(R.string.swim_length_label)
    String mLengthLabel;

    //swim track flow options
    @StringArrayRes
    String[] flowValues;

    @StringRes(R.string.swim_title_validate)
    String mTitleValidation;

    @StringRes(R.string.swim_date_validate)
    String mDateValidation;

    @StringRes(R.string.swim_location_validate)
    String mLocationValidation;

    //swim track duration input control
    @ViewById(R.id.swim_duration)
    SeekBar mDurationSkb;

    @ViewById(R.id.swim_duration_label)
    TextView mDurationLabelTvw;

    //swim track length input control
    @ViewById(R.id.swim_length)
    SeekBar mLengthSkb;

    @ViewById(R.id.swim_length_label)
    TextView mLengthLabelTvw;

    //swim track temperature input control
    @ViewById(R.id.swim_temperature)
    SeekBar mTemperatureSkb;

    @ViewById(R.id.swim_temperature_label)
    TextView mTemperatureTvw;

    //swim track waves input control
    @ViewById(R.id.swim_waves)
    SeekBar mWavesSkb;

    @ViewById(R.id.swim_waves_label)
    TextView mWavesTvw;

    //swim track flow input control
    @ViewById(R.id.swim_flow)
    SeekBar mFlowSkb;

    @ViewById(R.id.swim_flow_label)
    TextView mFlowTvw;

    //swim track title input control
    @ViewById(R.id.swim_title)
    EditText mTitleTxt;

    //swim track date input control
    @ViewById(R.id.swim_date)
    EditText mDateTxt;

    //swim track location input control
    @ViewById(R.id.swim_location)
    EditText mLocationTxt;

    //swim track notes input control
    @ViewById(R.id.swim_notes)
    EditText mNotesTxt;

    @ViewById(R.id.map_view)
    MapView mapVw;

    @Bean
    MapManager mMapManager;

    private LocationSerializer mLocationSerializer;

    private Bundle mSavedInstanceState;


    // Required empty public constructor
    public SwimEditFragment() {
        mLocationSerializer = new LocationSerializer();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        readSwimTrackToEdit();
        bindData();
    }


    private void readSwimTrackToEdit()
    {
        //initialize the swim instance to edit
        if(mSwimTrack==null) //nothing in instance state, looks somewhere else...
        {
            //swim track available in intent from MainActivity?
            String mSwimTrackSerialized = getEditingTrackFromIntent();
            if(mSwimTrackSerialized!=null)
            {
                isNewSwim = false; //keep track to perform insert v.s. update
                mSwimTrack = new Gson().fromJson(mSwimTrackSerialized, SwimTrack.class);
            }
            else
            {
                isNewSwim = true; //keep track to perform insert v.s. update
                mSwimTrack = SwimTrack.createNewEmptySwim(getContext());
                mSwimTrack.setGpsLocations(mLocationSerializer, getGPSLocationsFromIntent());
            }
        }

        //at this point mSwimTrack must have a value!
        Log.i(TAG, "SwimTrack to edit found: " + String.valueOf((mSwimTrack!=null)));
    }

    private String getEditingTrackFromIntent()
    {
        String mSwimTrackSerialized = getActivity().getIntent().getStringExtra(SWIM_ITEM_KEY);
        mSwimIndex = getActivity().getIntent().getIntExtra(SWIM_ITEM_INDEX,-1);
        return mSwimTrackSerialized;
    }

    private List<Location> getGPSLocationsFromIntent()
    {
        List<Location> locations = null;
        String swimGPSData  = getActivity().getIntent().getStringExtra(Constants.EXTRA_SWIM_GPS_DATA);
        if(swimGPSData!=null) locations = new LocationSerializer().parseMany(swimGPSData);

        return locations;
    }

    private void bindData()
    {
        //binds data to UI
        mTitleTxt.setText(mSwimTrack.getTitle());
        mLocationTxt.setText(mSwimTrack.getLocation());
        mDateTxt.setText(DateUtils.dateToString(mSwimTrack.getDate(),DateUtils.SHORT_FORMAT));
        mNotesTxt.setText(mSwimTrack.getNotes());
        mDurationSkb.setProgress(mSwimTrack.getDuration());
        mLengthSkb.setProgress(mSwimTrack.getLength()/100);
        mTemperatureSkb.setProgress(mSwimTrack.getPerceivedTemperature());
        mTemperatureTvw.setText(temperatureValues[mSwimTrack.getPerceivedTemperature()]);
        mWavesSkb.setProgress(mSwimTrack.getWaves());
        mWavesTvw.setText(wavesValues[mSwimTrack.getWaves()]);
        mFlowSkb.setProgress(mSwimTrack.getFlow());
        mFlowTvw.setText(flowValues[mSwimTrack.getFlow()]);

        //prepares map to show the swimming path
        mapVw.onCreate(mSavedInstanceState);
        mapVw.getMapAsync(new OnMapReadyCallback(){
            @Override
            public void onMapReady(GoogleMap googleMap)
            {
                List<Location> locations = mSwimTrack.getGpsLocations(mLocationSerializer);
                if(locations!=null)
                {
                    mapVw.setVisibility(View.VISIBLE);

                    mMapManager.drawSwimmingPath(googleMap, locations, false);
                    mMapManager.addStartStopMarkers();
                    mMapManager.getMapAsBitmap(new GoogleMap.SnapshotReadyCallback()
                    {
                        @Override
                        public void onSnapshotReady(Bitmap mapBitmap)
                        {
                            mSwimTrack.setMapPreview(mapBitmap);
                        }
                    });
                }
                else
                    mapVw.setVisibility(View.GONE);
            }
        });
    }


    /**
     * Stores swim data from the form
     * and sends back the user to
     * the list of swim tracks.
     */
    //todo: replace with Loader
    @Background
    void performAsyncSaving(){
        mSwimTrackManager.save();
        displaySwimList();
    }

    /**
     * Sends the user back to the
     * swim tracks list activity
     * and request it to update
     * and display the latest changes.
     */
    @UiThread
    void displaySwimList(){
        Intent displayIntent = new Intent(getActivity(), MainActivity_.class);
        //requests to display the swim tab instead of the default one
        displayIntent.putExtra(MainActivity.REQUEST_SELECTED_TAB_KEY, 1);
        displayIntent.putExtra(SwimListFragment.UPDATE_LIST_KEY, true);
        startActivity(displayIntent);
    }


    //======= DATA BINDING AND VALIDATION ==========/

    @AfterTextChange(R.id.swim_title)
    void onTitleChange(){
        mSwimTrack.setTitle(mTitleTxt.getText().toString());
        if(!mSwimTrack.isTitleValid())
            mTitleTxt.setError(mTitleValidation);
    }

    @AfterTextChange(R.id.swim_date)
    void onDateChange(){
        if(mDateTxt.getText()!=null){
            java.util.Date date = DateUtils.stringToDate(mDateTxt.getText().toString(),DateUtils.SHORT_FORMAT);
            mSwimTrack.setDate(date);
            if(!mSwimTrack.isDateValid())
                mDateTxt.setError(mDateValidation);
        }

    }

    @AfterTextChange(R.id.swim_location)
    void onLocationChange(){
        mSwimTrack.setLocation(mLocationTxt.getText().toString());
        if(!mSwimTrack.isLocationValid())
            mLocationTxt.setError(mLocationValidation);
    }

    @AfterTextChange(R.id.swim_notes)
    void onNotesChange(){
        mSwimTrack.setNotes(mNotesTxt.getText().toString());
    }

    @SeekBarProgressChange(R.id.swim_duration)
    void onDurationChange(SeekBar seekBar, int i, boolean b){
        mDurationLabelTvw.setText(SwimTrack.formatDuration(getContext(),i));
        mSwimTrack.setDuration(i);
    }

    @SeekBarProgressChange(R.id.swim_length)
    void onLengthChange(SeekBar seekBar, int i, boolean b){
        //1 "i" = 100 meters
        i=i*100;

        mLengthLabelTvw.setText(String.valueOf(i)+ mLengthLabel);
        mSwimTrack.setLength(i);
    }

    @SeekBarProgressChange(R.id.swim_flow)
    void onFlowChange(SeekBar seekBar, int i, boolean b){
        mFlowTvw.setText(flowValues[i]);
        mSwimTrack.setFlow(i);
    }

    @SeekBarProgressChange(R.id.swim_temperature)
    void onTemperatureChange(SeekBar seekBar, int i, boolean b){
        mTemperatureTvw.setText(temperatureValues[i]);
        mSwimTrack.setPerceivedTemperature(i);
    }

    @SeekBarProgressChange(R.id.swim_waves)
    void onWavesChange(SeekBar seekBar, int i, boolean b){
        mWavesTvw.setText(wavesValues[i]);
        mSwimTrack.setWaves(i);
    }


    //========== USER ACTIONS ===============/

    /**
     * Adds or updates the current editing
     * swim. When data are not valid,
     * displays a toast message the to user.
     */
    @OptionsItem(R.id.menu_save_swim)
    void saveSwim(){
        if(mSwimTrack.isValid()){
            if(isNewSwim)
                mSwimTrackManager.addNewSwimTrack(mSwimTrack);
            else
                mSwimTrackManager.updateSwimTrack(mSwimIndex,mSwimTrack);

            performAsyncSaving();
        }
        else
            Toast.makeText(getContext(),R.string.swim_invalid, Toast.LENGTH_LONG).show();
    }

    /**
     * Deletes the current editing swim
     * track or displays a message to the
     * user.
     */
    @OptionsItem(R.id.menu_delete_swim)
    void deleteSwim(){
        if(!isNewSwim)
            mSwimTrackManager.deleteSwimTrack(mSwimIndex);
        else
            Toast.makeText(getContext(),R.string.cannot_delete_new_swim, Toast.LENGTH_SHORT).show();

        performAsyncSaving();
    }
}