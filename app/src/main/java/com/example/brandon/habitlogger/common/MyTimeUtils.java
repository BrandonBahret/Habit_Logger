package com.example.brandon.habitlogger.common;

import java.util.Calendar;

/**
 * Created by Brandon on 3/7/2017.
 */

public class MyTimeUtils {

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

}
