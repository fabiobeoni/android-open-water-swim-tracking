package com.beoni.openwaterswimtracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.beoni.openwaterswimtracking.utils.LLog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_swim_edit)
public class SwimEditActivity extends AppCompatActivity
{
    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @AfterViews
    void viewCreated(){
        setSupportActionBar(mToolbar);
    }
}
