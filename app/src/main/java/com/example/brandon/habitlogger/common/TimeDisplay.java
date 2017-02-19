package com.example.brandon.habitlogger.common;

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
        millis /= 1000;

        this.hours = (millis - (millis % 3600)) / 3600;
        millis -= this.hours * 3600;

        this.minutes = (millis - (millis % 60)) / 60;
        millis -= this.minutes * 60;

        this.seconds = millis;
    }

    public static String getDisplay(long duration) {
        return new TimeDisplay(duration).toString();
    }
}
