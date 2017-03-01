package com.beoni.openwaterswimtracking;
import android.content.Intent;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.model.SwimTrack;
import com.beoni.openwaterswimtracking.utils.LLog;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.res.StringArrayRes;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


@EFragment(R.layout.fragment_swim_edit)
@OptionsMenu(R.menu.menu_edit_swim)
public class SwimEditFragment extends Fragment {

    public static final String SWIM_ITEM_KEY = "SWIM_ITEM_KEY";
    public static final String SWIM_ITEM_INDEX = "SWIM_ITEM_INDEX";

    private boolean isNewSwim;

    private String mSwimTrackSerialized;
    private int mSwimIndex;

    //list of swim track items presented on view
    @InstanceState
    SwimTrack mSwimTrack = null;

    //class that actually performs the swim CRUD actions
    @Bean
    SwimTrackManager mSwimTrackManager;

    @StringRes(R.string.swim_duration_label_hours)
    String mDurationLabelHours;

    @StringRes(R.string.swim_duration_label_minutes)
    String mDurationLabelMinutes;

    @ViewById(R.id.swim_duration)
    SeekBar mDurationSkb;

    @ViewById(R.id.swim_duration_label)
    TextView mDurationLabelTvw;


    @StringRes(R.string.swim_length_label)
    String mLengthLabel;

    @ViewById(R.id.swim_length)
    SeekBar mLengthSkb;

    @ViewById(R.id.swim_length_label)
    TextView mLengthLabelTvw;


    @StringArrayRes
    String[] temperatureValues;

    @ViewById(R.id.swim_temperature)
    SeekBar mTemperatureSkb;

    @ViewById(R.id.swim_temperature_label)
    TextView mTemperatureTvw;


    @StringArrayRes
    String[] wavesValues;

    @ViewById(R.id.swim_waves)
    SeekBar mWavesSkb;

    @ViewById(R.id.swim_waves_label)
    TextView mWavesTvw;


    @StringArrayRes
    String[] flowValues;

    @ViewById(R.id.swim_flow)
    SeekBar mFlowSkb;

    @ViewById(R.id.swim_flow_label)
    TextView mFlowTvw;

    @StringRes(R.string.swim_title_validate)
    String mTitleValidation;

    @ViewById(R.id.swim_title)
    EditText mTitleTxt;

    @StringRes(R.string.swim_date_validate)
    String mDateValidation;

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
        mSwimTrackSerialized = getActivity().getIntent().getStringExtra(SWIM_ITEM_KEY);
        mSwimIndex = getActivity().getIntent().getIntExtra(SWIM_ITEM_INDEX,0);
        isNewSwim = (mSwimTrackSerialized==null);

        if(isNewSwim)
            mSwimTrack = new SwimTrack();
        else
            mSwimTrack = new Gson().fromJson(mSwimTrackSerialized,SwimTrack.class);

        mDurationLabelTvw.setText(String.valueOf(mSwimTrack.getDuration())+ mDurationLabelHours);
        mLengthLabelTvw.setText(String.valueOf(mSwimTrack.getLength())+ mLengthLabel);
        mTemperatureTvw.setText(temperatureValues[mSwimTrack.getPerceivedTemperature()]);
        mWavesTvw.setText(wavesValues[mSwimTrack.getWaves()]);
        mFlowTvw.setText(flowValues[mSwimTrack.getFlow()]);

        mDurationSkb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                //"i" are minutes
                double h = ((double)i/60.00);
                NumberFormat format = new DecimalFormat("#.##");
                String full = format.format(h);
                String hours = "0";
                String minutes = "0";
                if(full.contains("."))
                {
                    hours = full.split("[.]")[0];
                    minutes = full.split("[.]")[1];
                }
                mDurationLabelTvw.setText(hours+ mDurationLabelHours +minutes+ mDurationLabelMinutes);
                mSwimTrack.setDuration(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                //not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                //not needed
            }
        });

        mLengthSkb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                //1 "i" = 100 meters
                i=i*100;

                mLengthLabelTvw.setText(String.valueOf(i)+ mLengthLabel);
                mSwimTrack.setLength(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                //not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                //not needed
            }
        });

        mTemperatureSkb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                mTemperatureTvw.setText(temperatureValues[i]);
                mSwimTrack.setPerceivedTemperature(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        mWavesSkb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                mWavesTvw.setText(wavesValues[i]);
                mSwimTrack.setWaves(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        mFlowSkb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                mFlowTvw.setText(flowValues[i]);
                mSwimTrack.setFlow(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        mTitleTxt.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                mSwimTrack.setTitle(mTitleTxt.getText().toString());
            }
        });

        mDateTxt.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                if(mDateTxt.getText()!=null && mDateTxt.getText().length()>0)
                {
                    String formattedDate = mDateTxt.getText().toString();
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy"); //Input date format
                        java.util.Date date = dateFormat.parse(formattedDate);
                        mSwimTrack.setDate(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        LLog.e(e);
                    }
                }
            }
        });

        mLocationTxt.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                mSwimTrack.setLocation(mLocationTxt.getText().toString());
            }
        });

        mNotesTxt.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                mSwimTrack.setNotes(mNotesTxt.getText().toString());
            }
        });
    }

    @OptionsItem(R.id.menu_save_swim)
    void saveSwim(){

        //TODO: replace with model/annotations based validation.
        if(mSwimTrack.getTitle()==null || mSwimTrack.getTitle().trim().length()==0)
        {
            Toast.makeText(getContext(), mTitleValidation, Toast.LENGTH_LONG).show();
            return;
        }
        if(mSwimTrack.getDate()==null)
        {
            Toast.makeText(getContext(), mDateValidation, Toast.LENGTH_LONG).show();
            return;
        }

        if(isNewSwim)
            mSwimTrackManager.addNewSwimTrack(mSwimTrack);
        else
            mSwimTrackManager.updateSwimTrack(mSwimIndex,mSwimTrack);


        performAsyncSaving();
    }

    @Background
    void performAsyncSaving(){
        mSwimTrackManager.save();
        displaySwimList();
    }

    @UiThread
    void displaySwimList(){
        Intent displayIntent = new Intent(getActivity(), MainActivity_.class);
        startActivity(displayIntent);
    }
}