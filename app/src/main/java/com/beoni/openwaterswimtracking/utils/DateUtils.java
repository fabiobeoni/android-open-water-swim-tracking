package com.beoni.openwaterswimtracking.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateUtils
{
    public final static String FORMAT = "dd/mm/yyyy hh:mm:ss";
    public final static String SHORT_FORMAT = "dd/mm/yyyy";

    public static String dateToString(Date date){
        DateFormat df = new SimpleDateFormat(FORMAT);
        return df.format(date);
    }

    /**
     * Returns null if parsing error occurs.
     * @param dateStr
     * @return
     */
    public static Date stringToDate(String dateStr, String format){
        DateFormat formatter = new SimpleDateFormat(format);
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
