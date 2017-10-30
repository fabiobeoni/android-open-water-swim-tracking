package com.beoni.openwaterswimtracking.utils;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//TODO: replace with library
/**
 * Utility to transform dates from/to strings in a easy and
 * uniform way.
 */
public class DateUtils
{
    public final static String FORMAT = "dd/MM/yyyy hh:mm:ss";
    public final static String SHORT_FORMAT = "dd/MM/yyyy";

    @NonNull
    private static DateFormat getDateFormat(String format)
    {
        DateFormat formatter =  new SimpleDateFormat(format);
        formatter.setLenient(false);
        return  formatter;
    }

    public static String dateToString(Date date){
        return getDateFormat(FORMAT).format(date);
    }

    public static String dateToString(Date date, String format){
        String t = getDateFormat(format).format(date);
        return t;
    }

    /**
     * Returns null if parsing error occurs.
     * @param dateStr
     * @return
     */
    public static Date stringToDate(String dateStr, String format){
        DateFormat formatter = getDateFormat(format);
        try
        {
            return formatter.parse(dateStr);
        } catch (ParseException e)
        {
            LLog.e(e);
            return null;
        }
    }

    /**
     * Returns the difference in days between two dates.
     * @param startdate
     * @param endDate
     * @return
     */
    public static long dateDiff(Date startdate, Date endDate){

        long diff = endDate.getTime() - startdate.getTime();

        diff = (diff / (1000 * 60 * 60 * 24));

        return diff;
    }
}
