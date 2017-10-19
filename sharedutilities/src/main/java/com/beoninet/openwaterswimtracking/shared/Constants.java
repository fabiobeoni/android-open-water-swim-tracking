package com.beoninet.openwaterswimtracking.shared;

public class Constants
{
    public static final String MSG_SWIM_DATA_AVAILABLE = Constants.class.getPackage().getName()+".MSG_SWIM_DATA_AVAILABLE";
    public static final String MSG_SWIM_MESSAGE_RECEIVED = Constants.class.getPackage().getName()+".MSG_SWIM_MESSAGE_RECEIVED";
    public static final String EXTRA_SWIM_GPS_DATA = "EXTRA_SWIM_GPS_DATA";

    /**
     * Intent key checked by this fragment to
     display a specific swim track to edit.
     The key must return a JSON serialized
     swim track.
     */
    public static final String INTENT_SWIM_ITEM = "INTENT_SWIM_ITEM";

    /**
     * Intent key checked by this fragment
     to know the index of the current editing
     swim track (the one on SWIM_ITEM_KEY)
     */
    public static final String INTENT_SWIM_ITEM_INDEX = "INTENT_SWIM_ITEM_INDEX";
    public static final String INTENT_UPDATE_LIST = "INTENT_UPDATE_LIST";
    public static final String INTENT_REQUEST_SELECTED_TAB_KEY = "INTENT_REQUEST_SELECTED_TAB_KEY";
}
