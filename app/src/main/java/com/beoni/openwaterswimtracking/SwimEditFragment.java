package com.beoni.openwaterswimtracking;
import android.content.Intent;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.utils.DateUtils;
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


@EFragment(R.layout.fragment_swim_edit)
@OptionsMenu(R.menu.menu_edit_swim)
public class SwimEditFragment extends Fragment {

    public static final String SWIM_ITEM_KEY = "SWIM_ITEM_KEY";
    public static final String SWIM_ITEM_INDEX = "SWIM_ITEM_INDEX";

    private boolean isNewSwim;

    private int mSwimIndex;

    //list of swim track items presented on view
    @InstanceState
    SwimTrack mSwimTrack;

    //class that actually performs the swim CRUD actions
    @Bean
    SwimTrackManager mSwimTrackManager;

    @StringArrayRes
    String[] wavesValues;

    @StringArrayRes
    String[] temperatureValues;

    @StringRes(R.string.swim_length_label)
    String mLengthLabel;

    @StringArrayRes
    String[] flowValues;

    @StringRes(R.string.swim_title_validate)
    String mTitleValidation;

    @StringRes(R.string.swim_date_validate)
    String mDateValidation;

    @StringRes(R.string.swim_location_validate)
    String mLocationValidation;

    @ViewById(R.id.swim_duration)
    SeekBar mDurationSkb;

    @ViewById(R.id.swim_duration_label)
    TextView mDurationLabelTvw;

    @ViewById(R.id.swim_length)
    SeekBar mLengthSkb;

    @ViewById(R.id.swim_length_label)
    TextView mLengthLabelTvw;

    @ViewById(R.id.swim_temperature)
    SeekBar mTemperatureSkb;

    @ViewById(R.id.swim_temperature_label)
    TextView mTemperatureTvw;

    @ViewById(R.id.swim_waves)
    SeekBar mWavesSkb;

    @ViewById(R.id.swim_waves_label)
    TextView mWavesTvw;

    @ViewById(R.id.swim_flow)
    SeekBar mFlowSkb;

    @ViewById(R.id.swim_flow_label)
    TextView mFlowTvw;

    @ViewById(R.id.swim_title)
    EditText mTitleTxt;

    @ViewById(R.id.swim_date)
    EditText mDateTxt;

    @ViewById(R.id.swim_location)
    EditText mLocationTxt;

    @ViewById(R.id.swim_notes)
    EditText mNotesTxt;


    // Required empty public constructor
    public SwimEditFragment() {}


    @AfterViews
    void viewCreated(){
        //initialize the swim instance to edit
        String mSwimTrackSerialized = getActivity().getIntent().getStringExtra(SWIM_ITEM_KEY);
        mSwimIndex = getActivity().getIntent().getIntExtra(SWIM_ITEM_INDEX,-1);
        isNewSwim = (mSwimTrackSerialized ==null);

        if(isNewSwim)
            mSwimTrack = SwimTrack.createNewEmptySwim(getContext());
        else
            mSwimTrack = new Gson().fromJson(mSwimTrackSerialized,SwimTrack.class);


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
    }

    @Background
    void performAsyncSaving(){
        mSwimTrackManager.save();
        displaySwimList();
    }

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

    @OptionsItem(R.id.menu_delete_swim)
    void deleteSwim(){
        if(!isNewSwim)
            mSwimTrackManager.deleteSwimTrack(mSwimIndex);
        else
            Toast.makeText(getContext(),R.string.cannot_delete_new_swim, Toast.LENGTH_SHORT).show();

        performAsyncSaving();
    }
}