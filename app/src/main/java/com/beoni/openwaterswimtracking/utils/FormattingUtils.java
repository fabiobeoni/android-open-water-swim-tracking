package com.beoni.openwaterswimtracking.utils;


import android.content.Context;

import com.beoni.openwaterswimtracking.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringRes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class FormattingUtils
{
    public static String formatDuration(Context ctx, int duration){
        //"i" are minutes
        double h = ((double)duration/60.00);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        NumberFormat format = new DecimalFormat("#.##",symbols);
        String full = format.format(h);
        String hours = "0";
        String minutes = "0";
        if(full.contains("."))
        {
            hours = full.split("[.]")[0];
            minutes = full.split("[.]")[1];
        }
        String hoursLabel = ctx.getResources().getString(R.string.swim_duration_label_hours);
        String minutesLabel = ctx.getResources().getString(R.string.swim_duration_label_minutes);
        return (hours+ hoursLabel +minutes+ minutesLabel);
    }
}
