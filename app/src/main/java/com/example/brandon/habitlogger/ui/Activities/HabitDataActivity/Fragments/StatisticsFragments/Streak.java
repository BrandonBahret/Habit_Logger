package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments;

import com.example.brandon.habitlogger.common.MyTimeUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by Brandon on 3/27/2017.
 */

public class Streak {
    public long streakStart = -1, streakEnd = -1;
    public int streakLength = 1;

    public Streak(long streakStart, long streakEnd, int streakLength) {
        this.streakStart = streakStart;
        this.streakEnd = streakEnd;
        this.streakLength = streakLength;
    }

    public Streak(long streakStart) {
        this.streakStart = streakStart;
        this.streakEnd = streakStart;
    }

    @Override
    public String toString() {
        String format = "%s [%d] %s";
        String startDate = MyTimeUtils.stringifyTimestamp(streakStart, "MMMM-d-yyyy");
        String endDate = MyTimeUtils.stringifyTimestamp(streakEnd, "MMMM-d-yyyy");

        return String.format(Locale.US, format, startDate, streakLength, endDate);
    }

    public static String listToString(List<Streak> streaks) {
        String result = "";
        for (Streak streak : streaks) {
            result += streak.toString() + '\n';
        }
        return result;
    }
}