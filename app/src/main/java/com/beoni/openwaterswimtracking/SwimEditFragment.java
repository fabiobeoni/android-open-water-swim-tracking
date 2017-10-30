package com.beoni.openwaterswimtracking;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.beoninet.openwaterswimtracking.shared.SwimTrackCalculator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterTextChange;
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

import java.sql.Date;
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

    private static final String TAG = SwimEditFragment.class.getSimpleName();
    private static final String TASK_SAVING_ASYNC = "TASK_SAVING_ASYNC";

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

    @StringRes(R.string.swim_date_validate)
    String mDateValidation;

    @StringRes(R.string.swim_location_validate)
    String mLocationValidation;

    //swim track duration input control
    @ViewById(R.id.swim_duration)
    EditText mDurationTxt;

    //swim track length input control
    @ViewById(R.id.swim_length)
    EditText mLengthTxt;

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

    private MenuItem mMenuItemDelete;
    private MenuItem mMenuItemSave;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        mMenuItemSave = menu.findItem(R.id.menu_save_swim);
        mMenuItemDelete = menu.findItem(R.id.menu_delete_swim);
        setCommandsEnabled(true);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getSwimTrackToEdit();
        bindData();
    }

    /**
     * Stores swim data from the form
     * and sends back the user to
     * the list of swim tracks.
     */
    @Background(id=TASK_SAVING_ASYNC)
    void performSavingAsync()
    {
        mSwimTrackManager.save();
        navigateToSwimList();
    }

    /**
     * Sends the user back to the
     * swim tracks list activity
     * and request it to update
     * and display the latest changes.
     */
    @UiThread
    void navigateToSwimList()
    {
        Intent intent = new Intent(getActivity(), MainActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //requests to display the swim tab instead of the default one
        intent.putExtra(Constants.INTENT_REQUEST_SELECTED_TAB_KEY, 1);
        intent.putExtra(Constants.INTENT_UPDATE_LIST, true);
        startActivity(intent);
    }

    private void getSwimTrackToEdit()
    {
        //initialize the swim instance to edit
        //looks first in request from wear to create a new swim
        //so any user interaction with this activity gets reset
        //to start a blank form and fill GPS data
        if(hasRequestFromWear()){
            List<Location> locations = getGPSLocationsFromIntent();
            if(locations!=null && locations.size()>0)
            {
                long duration = SwimTrackCalculator.calculateDuration(locations.get(0),locations.get(locations.size()-1));
                long distance = SwimTrackCalculator.calculateDistance(locations,true);

                mSwimTrack = SwimTrack.createNewEmptySwim(getContext());
                mSwimTrack.setGpsLocations(mLocationSerializer, locations);
                mSwimTrack.setDuration(duration);
                mSwimTrack.setLength(distance);
                mSwimTrack.setDate(new Date(locations.get(0).getTime()));
            }
        }
        else{
            //only if no data from wear then look for data
            //from somewhere else
            //User is working on phone only, so or swim data resuming
            //from instance state or creating new swim record

            //swim track available in intent from SwimListActivity?
            String mSwimTrackSerialized = getEditingTrackFromIntent();
            if(mSwimTrackSerialized!=null && mSwimTrackSerialized.length()>0)
                //yes => the user selected a swim to edit
                mSwimTrack = new Gson().fromJson(mSwimTrackSerialized, SwimTrack.class);
            else
                //let's make a new one
                mSwimTrack = SwimTrack.createNewEmptySwim(getContext());
        }

        //last chance, is using value from instance state.
        //at this point mSwimTrack must have a value!
        Log.i(TAG, "SwimTrack to edit found: " + String.valueOf((mSwimTrack!=null)));
    }

    //The intent is flagged with Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
    //so it's a new one every time this activity is invoked from parent activity
    private String getEditingTrackFromIntent()
    {
        String mSwimTrackSerialized = getActivity().getIntent().getStringExtra(Constants.INTENT_SWIM_ITEM);
        mSwimIndex = getActivity().getIntent().getIntExtra(Constants.INTENT_SWIM_ITEM_INDEX,-1);
        return mSwimTrackSerialized;
    }

    private boolean hasRequestFromWear()
    {
        return getActivity().getIntent().hasExtra(Constants.EXTRA_SWIM_GPS_DATA);
    }

    private List<Location> getGPSLocationsFromIntent()
    {
        List<Location> locations = null;
        String swimGPSData  = getActivity().getIntent().getStringExtra(Constants.EXTRA_SWIM_GPS_DATA);
        if(swimGPSData!=null)
            locations = new LocationSerializer().parseMany(swimGPSData);

        return locations;
    }

    private void bindData()
    {
        float duration = mSwimTrack.getDuration()/1000/60; //sets the value in minutes

        //binds data to UI
        mLocationTxt.setText(mSwimTrack.getLocation());
        mDateTxt.setText(DateUtils.dateToString(mSwimTrack.getDate(),DateUtils.SHORT_FORMAT));
        mNotesTxt.setText(mSwimTrack.getNotes());
        mDurationTxt.setText(String.valueOf(duration));
        mLengthTxt.setText(String.valueOf(mSwimTrack.getLength())); //sets the value in meters
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
                    mMapManager.drawSwimmingPath(googleMap, locations, false);

                    if(mSwimTrack.getMapPreviewFullFileName()==null)
                        mMapManager.createMapPreviewAsync(mSwimTrack);

                    mapVw.setVisibility(View.VISIBLE);
                }
                else
                    mapVw.setVisibility(View.GONE);
            }
        });
    }

    private void setCommandsEnabled(boolean status)
    {
        mMenuItemDelete.setEnabled(status);
        mMenuItemSave.setEnabled(status);
    }


    //======= DATA BINDING AND VALIDATION ==========/

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

    @AfterTextChange(R.id.swim_duration)
    void onDurationChange(){
        long duration = 0;
        if(mDurationTxt.getText()!=null && mDurationTxt.getText().toString().trim().length()>0)
            duration =(long) (Float.parseFloat(mDurationTxt.getText().toString())*60)*1000; //in milliseconds

        mSwimTrack.setDuration(duration);
    }

    @AfterTextChange(R.id.swim_length)
    void onLengthChange(){
        long distance = 0;
        if(mLengthTxt.getText()!=null && mLengthTxt.getText().toString().trim().length()>0)
            distance = Long.parseLong(mLengthTxt.getText().toString());

        mSwimTrack.setLength(distance);
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
        setCommandsEnabled(false);

        if(mSwimTrack.isValid()){
            if(mSwimTrackManager.isNewSwim(mSwimTrack))
                mSwimTrackManager.addNewSwimTrack(mSwimTrack);
            else
            {
                if(mSwimIndex!=-1)
                    mSwimTrackManager.updateSwimTrack(mSwimIndex, mSwimTrack);
                else
                {
                    Toast.makeText(getContext(), "Try to delete -1 item index", Toast.LENGTH_SHORT).show();
                    setCommandsEnabled(true);
                }
            }

            performSavingAsync();
        }
        else
        {
            Toast.makeText(getContext(), R.string.swim_invalid, Toast.LENGTH_LONG).show();
            setCommandsEnabled(true);
        }
    }

    /**
     * Deletes the current editing swim
     * track or displays a message to the
     * user.
     */
    @OptionsItem(R.id.menu_delete_swim)
    void deleteSwim(){

        setCommandsEnabled(false);

        if(!mSwimTrackManager.isNewSwim(mSwimTrack))
        {
            if (mSwimIndex != -1)
            {
                mSwimTrackManager.deleteSwimTrack(mSwimIndex);
                mMapManager.deleteMapPreviewAsync(mSwimTrack);
                performSavingAsync();
            }
            else
            {
                Toast.makeText(getContext(), "Try to delete -1 item index", Toast.LENGTH_SHORT).show();
                setCommandsEnabled(true);
            }
        }
        else
        {
            Toast.makeText(getContext(),R.string.cannot_delete_new_swim, Toast.LENGTH_SHORT).show();
            setCommandsEnabled(true);
        }
    }
}