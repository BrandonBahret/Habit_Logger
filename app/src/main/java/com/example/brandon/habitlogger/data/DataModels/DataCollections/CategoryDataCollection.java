package com.example.brandon.habitlogger.data.DataModels.DataCollections;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.internal.util.Predicate;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;

import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 2/28/2017.
 * This is a class to structure a sample of session entries on the categorical level.
 */

public class CategoryDataCollection extends MyDataCollectionBase<Habit> implements Parcelable {

    //region (Member Attributes)
    private final HabitCategory mCategory;
    private long mDateFrom, mDateTo;
    private boolean mDateFromIsInvalid, mDateToIsInvalid;

    private SessionEntryCollection mAllSessionEntries;
    private boolean mAllSessionEntriesIsInvalid;

    private Long mTotalDuration;
    private boolean mTotalDurationInvalid;
    //endregion

    //region Static helper interface methods
    public static Predicate<? super CategoryDataCollection> IFilterEmptySamples = new Predicate<CategoryDataCollection>() {
        @Override
        public boolean apply(CategoryDataCollection dataSample) {
            return dataSample.hasHabits();
        }
    };

    public static MyCollectionUtils.IGetKey<CategoryDataCollection, Long> IGetEntriesDuration = new MyCollectionUtils.IGetKey<CategoryDataCollection, Long>() {
        @Override
        public Long get(CategoryDataCollection dataSample) {
            return dataSample.calculateTotalDuration();
        }
    };
    //endregion -- end --

    public CategoryDataCollection(HabitCategory category, List<Habit> habits,
                                  long dateFromTime, long dateToTime) {
        super(habits);
        Collections.sort(this, Habit.ICompareDuration);
        mCategory = category;
        mDateFrom = dateFromTime;
        mDateTo = dateToTime;
    }

    //region Implement Parcelable
    protected CategoryDataCollection(Parcel in) {
        CategoryDataCollection sample = in.readParcelable(CategoryDataCollection.class.getClassLoader());
        this.mCategory = sample.getCategory();
        this.clear();
        this.addAll(sample.getHabits());
        this.mDateFrom = sample.getDateFrom();
        this.mDateTo = sample.getDateTo();
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
    //endregion -- end --

    //region Methods With One Time Calculations {}
    @Override
    void invalidate() {
        mAllSessionEntriesIsInvalid = true;
        mTotalDurationInvalid = true;
    }

    public long calculateTotalDuration() {
        if (mTotalDuration == null || mTotalDurationInvalid) {
            mTotalDuration = (long) MyCollectionUtils.sum(this, Habit.IGetEntriesDuration);
            mTotalDurationInvalid = false;
        }

        return mTotalDuration;
    }

    public SessionEntryCollection buildSessionEntriesList() {

        if (mAllSessionEntries == null || mAllSessionEntriesIsInvalid) {
            mAllSessionEntries = new SessionEntryCollection(
                    MyCollectionUtils.collectLists(this, new MyCollectionUtils.IGetList<Habit, SessionEntry>() {
                        @Override
                        public List<SessionEntry> getList(Habit habit) {
                            return habit.getEntries();
                        }
                    })
            );
            mAllSessionEntriesIsInvalid = false;
        }

        return mAllSessionEntries;
    }
    //endregion -- end --

    /**
     * This method only works when the entries have their mHabit field set, otherwise the app
     * will crash.
     */
    public static CategoryDataCollection buildCategoryDataCollection(SessionEntryCollection entries) {

        HabitCategory category = entries.get(0).getHabit().getCategory();
        long dateFrom = entries.getDateFromTime();
        long dateTo = entries.getDateToTime();

        Collections.sort(entries, SessionEntry.ICompareHabitIds);

        List<List<SessionEntry>> habitEntries = MyCollectionUtils.split(entries, new MyCollectionUtils.IGetKey<SessionEntry, Long>() {
            @Override
            public Long get(SessionEntry entry) {
                return entry.getHabit().getDatabaseId();
            }
        });

        List<Habit> habits = MyCollectionUtils.map(habitEntries, new MyCollectionUtils.IMapValue<List<SessionEntry>, Habit>() {
            @Override
            public Habit apply(List<SessionEntry> sessionEntries) {
                return new Habit().setEntries(new SessionEntryCollection(sessionEntries));
            }
        });

        return new CategoryDataCollection(category, habits, dateFrom, dateTo);
    }


    /**
     * @param timestamp Epoch timestamp for a certain date to search for.
     */
    public CategoryDataCollection getDataSampleForDate(final long timestamp) {

        List<Habit> habits = MyCollectionUtils.collect(this, new MyCollectionUtils.IGetKey<Habit, Habit>() {
            @Override
            public Habit get(Habit habit) {
                habit = Habit.duplicate(habit);
                SessionEntryCollection entriesForDate = habit.findEntriesWithDate(timestamp);
                return habit.setEntries(entriesForDate);
            }
        });

        return new CategoryDataCollection(mCategory, habits, timestamp, timestamp);

    }

    //region Getters {}
    public long getDateFrom() {
        if(mDateFromIsInvalid){
            SessionEntryCollection sessionEntries = buildSessionEntriesList();
            if (!sessionEntries.isEmpty()) {
                mDateFrom = Collections.min(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            }
            else{
                mDateFrom = -1;
            }
            mDateFromIsInvalid = false;
        }
        return mDateFrom;
    }

    public long getDateTo() {
        if(mDateToIsInvalid){
            SessionEntryCollection sessionEntries = buildSessionEntriesList();
            if (!sessionEntries.isEmpty()) {
                mDateTo = Collections.max(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            }
            else{
                mDateTo = -1;
            }
            mDateToIsInvalid = false;
        }
        return mDateTo;
    }

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

    //region CRUD (Create, Read, Update, Delete) {}

    //endregion -- end --

}
