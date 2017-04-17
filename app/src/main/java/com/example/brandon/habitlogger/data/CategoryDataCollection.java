package com.example.brandon.habitlogger.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.internal.util.Predicate;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 2/28/2017.
 * This is a class to structure a sample of session entries on the categorical level.
 */

public final class CategoryDataCollection extends ArrayList<Habit> implements Parcelable {

    //region (Member Attributes)
    private final HabitCategory mCategory;
    private long mDateFromTime, mDateToTime;
    private SessionEntriesCollection mAllSessionEntries;
    private Long mTotalDuration;
    private boolean mTotalDurationInvalid = true;
    //endregion

    //region Static helper interface methods
    public static Predicate<? super CategoryDataCollection> IFilterEmptySamples = new Predicate<CategoryDataCollection>() {
        @Override
        public boolean apply(CategoryDataCollection dataSample) {
            return dataSample.hasHabits();
        }
    };
    //endregion -- end --

    public CategoryDataCollection(HabitCategory category, List<Habit> habits,
                                  long dateFromTime, long dateToTime) {
        super(habits);
        Collections.sort(this, Habit.ICompareDuration);
        mCategory = category;
        mDateFromTime = dateFromTime;
        mDateToTime = dateToTime;
    }

    //region Implement Parcelable
    protected CategoryDataCollection(Parcel in) {
        CategoryDataCollection sample = in.readParcelable(CategoryDataCollection.class.getClassLoader());
        this.mCategory = sample.getCategory();
        this.clear();
        this.addAll(sample.getHabits());
        this.mDateFromTime = sample.getDateFromTime();
        this.mDateToTime = sample.getDateToTime();
        this.mTotalDuration = sample.calculateTotalDuration();
        this.mAllSessionEntries = sample.buildSessionEntriesList();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this, flags);
    }

    public static final Creator<CategoryDataCollection> CREATOR = new Creator<CategoryDataCollection>() {
        @Override
        public CategoryDataCollection createFromParcel(Parcel in) {
            return new CategoryDataCollection(in);
        }

        @Override
        public CategoryDataCollection[] newArray(int size) {
            return new CategoryDataCollection[size];
        }
    };
    //endregion

    //region Methods With One Time Calculations {}
    public long calculateTotalDuration() {
        if (mTotalDuration == null || mTotalDurationInvalid) {
            mTotalDuration = (long) MyCollectionUtils.sum(this, Habit.IGetEntriesDuration);
            mTotalDurationInvalid = false;
        }

        return mTotalDuration;
    }

    public SessionEntriesCollection buildSessionEntriesList() {

        if (mAllSessionEntries == null) {
            mAllSessionEntries = new SessionEntriesCollection(
                    MyCollectionUtils.collectAsList(this, new MyCollectionUtils.IGetList<Habit, SessionEntry>() {
                        @Override
                        public List<SessionEntry> getList(Habit habit) {
                            return habit.getEntries();
                        }
                    })
            );
        }

        return mAllSessionEntries;
    }
    //endregion

    /**
     * @param timestamp Epoch timestamp for a certain date to search for.
     */
    public CategoryDataCollection getDataSampleForDate(final long timestamp) {

        List<Habit> habits = MyCollectionUtils.collect(this, new MyCollectionUtils.IGetKey<Habit, Habit>() {
            @Override
            public Habit get(Habit habit) {
                habit = Habit.duplicate(habit);
                SessionEntriesCollection entriesForDate = habit.filterEntriesForDate(timestamp);
                return habit.setEntries(entriesForDate);
            }
        });

        return new CategoryDataCollection(mCategory, habits, timestamp, timestamp);

//        List<Habit> habits = new ArrayList<>(this.size());
//
//        for (int i = 0; i < mHabits.size(); i++) {
//            habits.add(i, Habit.duplicate(mHabits.get(i)));
//            List<SessionEntry> entriesForDate = habits.get(i).filterEntriesForDate(timestamp);
//            habits.get(i).setEntries(entriesForDate);
//        }
//
//        return new CategoryDataCollection(mCategory, habits, timestamp, timestamp);
    }

    //region Getters {}
    public static MyCollectionUtils.IGetKey<CategoryDataCollection, Long> IGetEntriesDuration = new MyCollectionUtils.IGetKey<CategoryDataCollection, Long>() {
        @Override
        public Long get(CategoryDataCollection dataSample) {
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
        return this.size();
    }

    private boolean hasHabits() {
        return getNumberOfHabits() > 0;
    }

    public long getDateFromTime() {
        return mDateFromTime;
    }

    public long getDateToTime() {
        return mDateToTime;
    }

    public long getHabitDuration(int index) {
        return this.get(index).getEntriesDuration();
    }

    public Habit getHabit(int index) {
        return this.get(index);
    }

    public List<Habit> getHabits() {
        return this;
    }
    //endregion

}
