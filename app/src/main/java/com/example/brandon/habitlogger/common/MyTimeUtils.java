package com.example.brandon.habitlogger.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Brandon on 3/7/2017.
 * Helper class for dealing with timestamps
 */

public class MyTimeUtils {

    //region Set time of day on timestamp
    public static long setTimePortion(long timestamp, boolean isAM, int hourOfDay, int minute, int second, int millisecond) {
        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(timestamp);

        c.set(Calendar.AM_PM, isAM ? Calendar.AM : Calendar.PM);
        c.set(Calendar.HOUR, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, millisecond);

        return c.getTimeInMillis();
    }

    public static long setTimePortion(Calendar c, boolean isAM, int hourOfDay, int minute, int second, int millisecond) {
        c.set(Calendar.AM_PM, isAM ? Calendar.AM : Calendar.PM);
        c.set(Calendar.HOUR, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, millisecond);

        return c.getTimeInMillis();
    }
    //endregion

    //region Get time of day from timestamp
    /**
     * @param timestamp The duration in milliseconds
     * @return An array {hours, minutes, seconds}
     */
    public static Integer[] getTimePortion(long timestamp){

        timestamp /= 1000;

        int hours = (int)((timestamp - (timestamp % 3600)) / 3600);
        timestamp -= hours * 3600;

        int minutes = (int)((timestamp - (timestamp % 60)) / 60);
        timestamp -= minutes * 60;

        int seconds = (int)timestamp;

        return new Integer[]{hours, minutes, seconds};
    }

    /**
     * @return Time of day formatted as prescribed.
     */
    public static String stringifyTimePortion(long timestamp, String format) {
        Integer[] timeComponents = getTimePortion(timestamp);

        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, timeComponents[0]);
        c.set(Calendar.MINUTE, timeComponents[1]);
        c.set(Calendar.SECOND, timeComponents[2]);

        return new SimpleDateFormat(format, Locale.getDefault())
                .format(new Date(c.getTimeInMillis()));
    }
    //endregion

    public static boolean isSameMonthOfYear(Calendar monthOne, Calendar monthTwo) {
        int month1 = monthOne.get(Calendar.MONTH);
        int month2 = monthTwo.get(Calendar.MONTH);

        int year1 = monthOne.get(Calendar.YEAR);
        int year2 = monthTwo.get(Calendar.YEAR);

        return (month1 == month2) && (year1 == year2);
    }

    public static int getTimestampField(long timestamp, int field){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        return c.get(field);
    }

    public static long setTimestampField(long timestamp, int field, int value){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        c.set(field, value);
        return c.getTimeInMillis();
    }

    /**
     * Return date in specified format.
     *
     * @param timestamp Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    public static String stringifyTimestamp(long timestamp, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }

}
