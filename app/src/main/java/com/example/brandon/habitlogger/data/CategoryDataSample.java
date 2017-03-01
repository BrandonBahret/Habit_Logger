package com.example.brandon.habitlogger.data;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;

import java.util.Arrays;

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
    private final long mTotalDuration;

    public CategoryDataSample(HabitCategory category, Habit[] habits,
                              long dateFromTime, long dateToTime) {

        mCategory = category;
        mHabits = habits;
        Arrays.sort(mHabits, Habit.DurationComparator);
        mNumberOfHabits = habits.length;
        mTotalDuration = calculateTotalDuration();
        mDateFromTime = dateFromTime;
        mDateToTime = dateToTime;
    }

    private long calculateTotalDuration() {
        long totalDuration = 0;
        for(Habit habit : mHabits){
            totalDuration += habit.getEntriesDuration();
        }
        return totalDuration;
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

    public long getTotalDuration() {
        return mTotalDuration;
    }

    public long getHabitDuration(int i){
        return mHabits[i].getEntriesDuration();
    }

    public Habit getHabit(int i) {
        return mHabits[i];
    }
    //endregion
}
