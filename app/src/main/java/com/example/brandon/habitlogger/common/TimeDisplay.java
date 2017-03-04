package com.example.brandon.habitlogger.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Brandon on 2/18/2017.
 * Class to format duration fields in milliseconds into a human-readable string.
 */

public class TimeDisplay {
    public long hours, minutes, seconds;

    public TimeDisplay(long time) {
        updateTime(time);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%02d:%02d:%02d", this.hours, this.minutes, this.seconds);
    }

    public void updateTime(long millis) {
        int[] timeComponents = getTimeComponents(millis);
        this.hours = timeComponents[0];
        this.minutes = timeComponents[1];
        this.seconds = timeComponents[2];
    }

    public static int[] getTimeComponents(long millis){
        millis /= 1000;

        int hours = (int)((millis - (millis % 3600)) / 3600);
        millis -= hours * 3600;

        int minutes = (int)((millis - (millis % 60)) / 60);
        millis -= minutes * 60;

        int seconds = (int)millis;

        return new int[]{hours, minutes, seconds};
    }

    public static String getDisplay(long duration) {
        return new TimeDisplay(duration).toString();
    }

    /**
     * @param timestampInMillis Ex: (1:00 AM in an integer representation)
     * @return Ex: (timestampInMinutes = 0) returns 1:00 AM
     */
    public static String getTimeAsString(long timestampInMillis, String timeformat) {
        int[] timeComponents = getTimeComponents(timestampInMillis);

        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, timeComponents[0]);
        c.set(Calendar.MINUTE, timeComponents[1]);
        c.set(Calendar.SECOND, timeComponents[2]);

        SimpleDateFormat formatter = new SimpleDateFormat(timeformat, Locale.getDefault());
        return formatter.format(new Date(c.getTimeInMillis()));
    }
}
