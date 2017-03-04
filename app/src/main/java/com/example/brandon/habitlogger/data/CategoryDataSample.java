package com.example.brandon.habitlogger.data;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Brandon on 2/28/2017.
 *
 */

public class CategoryDataSample {

    private final HabitCategory mCategory;
    private final Habit[] mHabits;
    private final int mNumberOfHabits;
    private final long mDateFromTime;
    private final long mDateToTime;
    private long mDuration = -1;

    public CategoryDataSample(HabitCategory category, Habit[] habits,
                              long dateFromTime, long dateToTime) {

        mCategory = category;
        mHabits = habits;
        Arrays.sort(mHabits, Habit.DurationComparator);
        mNumberOfHabits = habits.length;
        mDateFromTime = dateFromTime;
        mDateToTime = dateToTime;
    }

    public long calculateTotalDuration() {
        if (mDuration == -1) {
            mDuration = 0;
            for(Habit habit : mHabits){
                mDuration += habit.getEntriesDuration();
            }
        }

        return mDuration;
    }

    //region // Getters
    public HabitCategory getCategory() {
        return mCategory;
    }

    public final int getNumberOfHabits() {
        return mNumberOfHabits;
    }

    public long getDateFromTime() {
        return mDateFromTime;
    }

    public long getDateToTime() {
        return mDateToTime;
    }

    public long getHabitDuration(int i){
        return mHabits[i].getEntriesDuration();
    }

    public Habit getHabit(int i) {
        return mHabits[i];
    }

    public List<SessionEntry> getSessionEntries() {
        List<SessionEntry> sessionEntries = new ArrayList<>();

        for (Habit habit : mHabits) {
            SessionEntry[] entries = habit.getEntries();
            if(entries != null){
                sessionEntries.addAll(Arrays.asList(entries));
            }
        }

        return sessionEntries;
    }

    public String getName() {
        return mCategory.getName();
    }
    //endregion
}
