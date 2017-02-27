package com.beoni.openwaterswimtracking.utils;

import android.util.Log;

import java.util.Arrays;

/**
 * Utility to easily print errors.
 */
public class LLog
{
    private static final String ERROR_TAG = "OWST.error";
    private static final String WARN_TAG = "OWST.warn";
    private static final String INFO_TAG = "OWST.info";

    public static Exception e(Exception ex){
        ex.printStackTrace();
        Log.e(ERROR_TAG, Arrays.toString(ex.getStackTrace()));
        return ex;
    }

    public static Exception w(Exception ex){
        ex.printStackTrace();
        Log.e(WARN_TAG, Arrays.toString(ex.getStackTrace()));
        return ex;
    }

    public static void w(String msg){
        Log.i(WARN_TAG, msg);
    }

    public static void i(String msg){
        Log.i(INFO_TAG, msg);
    }
}
