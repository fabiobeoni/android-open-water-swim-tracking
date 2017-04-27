package com.beoni.openwaterswimtracking.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utility to check if device is connected from anywhere in the app.
 */
public class ConnectivityUtils
{
    public static boolean isDeviceConnected(Context _ctx) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) _ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
