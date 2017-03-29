package com.example.brandon.habitlogger.data;

import android.os.Parcel;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Brandon on 2/28/2017.
 * This is a class to structure a sample of session entries on the categorical level.
 */

public final class CategoryDataSample extends ExpandableGroup<Habit> {

    //region (Member Attributes)
    private final HabitCategory mCategory;
    private final Habit[] mHabits;
    private final long mDateFromTime, mDateToTime;
    private List<SessionEntry> mSessionEntries;
    private Long mDuration;
    //endregion

    public CategoryDataSample(HabitCategory category, Habit[] habits,
                              long dateFromTime, long dateToTime) {

        super(category.getName(), Arrays.asList(habits));
        mCategory = category;
        mHabits = habits;
        Arrays.sort(mHabits, Habit.DurationComparator);
        mDateFromTime = dateFromTime;
        mDateToTime = dateToTime;
    }

    //region Implement Parcelable
    protected CategoryDataSample(Parcel in) {
        super(in);
        CategoryDataSample sample = in.readParcelable(CategoryDataSample.class.getClassLoader());

        this.mCategory = sample.getCategory();
        this.mHabits = sample.getHabits();
        this.mDateFromTime = sample.getDateFromTime();
        this.mDateToTime = sample.getDateToTime();
        this.mDuration = sample.calculateTotalDuration();
        this.mSessionEntries = sample.buildSessionEntriesList();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this, flags);
    }

    public static final Creator<CategoryDataSample> CREATOR = new Creator<CategoryDataSample>() {
        @Override
        public CategoryDataSample createFromParcel(Parcel in) {
            return new CategoryDataSample(in);
        }

        @Override
        public CategoryDataSample[] newArray(int size) {
            return new CategoryDataSample[size];
        }
    };
    //endregion

    /**
     * @param timestamp Epoch timestamp for a certain date to search for.
     */
    public CategoryDataSample getDataSampleForDate(long timestamp) {

        Habit habits[] = new Habit[mHabits.length];

        for (int i = 0; i < mHabits.length; i++) {
            habits[i] = Habit.duplicate(mHabits[i]);
            List<SessionEntry> entriesForDate = habits[i].getEntriesForDate(timestamp);
            habits[i].setEntries(entriesForDate);
        }

        return new CategoryDataSample(mCategory, habits, timestamp, timestamp);
    }

    //region Methods With One Time Calculations {}
    public long calculateTotalDuration() {

        if (mDuration == null)
            mDuration = (long) MyCollectionUtils.sum(mHabits, Habit.IGetEntriesDuration);

        return mDuration;
    }

    public List<SessionEntry> buildSessionEntriesList() {

        if (mSessionEntries == null) {
            mSessionEntries = new ArrayList<>();

            for (Habit habit : mHabits) {
                List<SessionEntry> entries = habit.getEntriesAsList();
                if (entries != null)
                    mSessionEntries.addAll(entries);
            }
        }

        return mSessionEntries;
    }
    //endregion

    //region Getters {}
    public static MyCollectionUtils.IGetKey<CategoryDataSample, Long> IGetEntriesDuration = new MyCollectionUtils.IGetKey<CategoryDataSample, Long>() {
        @Override
        public Long get(CategoryDataSample dataSample) {
            return dataSample.calculateTotalDuration();
        }
    };

    public String getCategoryName() {
        return mCategory.getName();
    }

    public HabitCategory getCategory() {
        return mCategory;
    }

    public final int getNumberOfHabits() {
        return mHabits.length;
    }

    public long getDateFromTime() {
        return mDateFromTime;
    }

    public long getDateToTime() {
        return mDateToTime;
    }

    public long getHabitDuration(int i) {
        return mHabits[i].getEntriesDuration();
    }

    public Habit getHabit(int i) {
        return mHabits[i];
    }

    public Habit[] getHabits() {
        return mHabits;
    }
    //endregion

}
