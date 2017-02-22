package com.beoni.openwaterswimtracking.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by fabio on 22/02/17.
 */

public class DateUtils
{
    private final static String FORMAT = "MM/dd/yyyy HH:mm:ss";

    public static String dateToString(Date date){
        DateFormat df = new SimpleDateFormat(FORMAT);
        return df.format(date);
    }

    /**
     * Returns null if parsing error occurs.
     * @param dateStr
     * @return
     */
    public static Date stringToDate(String dateStr){
        DateFormat formatter = new SimpleDateFormat(FORMAT);
        try
        {
            return formatter.parse(dateStr);
        } catch (ParseException e)
        {
            LLog.e(e);
            return null;
        }
    }

    public static long dateDiff(Date startdate, Date endDate){

        long diff = endDate.getTime() - startdate.getTime();

        diff = (diff / (1000 * 60 * 60 * 24));

        return diff;
    }
}
