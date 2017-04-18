package com.example.brandon.habitlogger.data.DataModels.DataCollections;

import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 3/2/2017.
 * A class to structure CategoryDataSample objects.
 */

public class HabitDataCollection extends MyDataCollectionBase<CategoryDataCollection> {

    //region (Member Attributes)
    private long mDateFrom, mDateTo;
    private boolean mDateFromIsInvalid, mDateToIsInvalid;

    private SessionEntryCollection mAllSessionEntries;
    private boolean mSessionEntriesListIsInvalid;

    private Long mTotalDuration;
    private boolean mTotalDurationIsInvalid;

    private int[] mColors;
    private boolean mColorsArrayIsInvalid;
    //endregion

    //region Constructors {}
    public HabitDataCollection(List<CategoryDataCollection> data, long dateFrom, long dateTo) {
        super(data);
        mDateFrom = dateFrom != -1 ? dateFrom : getDateFrom();
        mDateTo = dateTo != -1 ? dateTo : getDateTo();
    }

    public HabitDataCollection(List<CategoryDataCollection> data) {
        this(data, -1, -1);
    }

    public HabitDataCollection() {
        this(new ArrayList<CategoryDataCollection>(), -1, -1);
    }

    public HabitDataCollection(SessionEntryCollection sessionEntries){
        super(sessionEntries.buildHabitDataCollection());
        mDateFrom = sessionEntries.getDateFromTime();
        mDateTo = sessionEntries.getDateToTime();
    }
    //endregion -- end --

    /**
     * @param timestamp Epoch timestamp for a certain date to search for.
     */
    public HabitDataCollection getDataSampleForDate(long timestamp) {
        List<CategoryDataCollection> categoryDataSamples = new ArrayList<>(this.size());

        for (CategoryDataCollection dataSample : this)
            categoryDataSamples.add(dataSample.getDataSampleForDate(timestamp));

        return new HabitDataCollection(categoryDataSamples, timestamp, timestamp);
    }

    //region Methods With One Time Calculations {}
    @Override
    void invalidate() {
        mDateFromIsInvalid = true;
        mDateToIsInvalid = true;
        mSessionEntriesListIsInvalid = true;
        mTotalDurationIsInvalid = true;
        mColorsArrayIsInvalid = true;
    }

    public SessionEntryCollection buildSessionEntriesList() {
        if (mAllSessionEntries == null || mSessionEntriesListIsInvalid) {
            List<SessionEntry> sessionEntries = MyCollectionUtils.collectLists(this,
                    new MyCollectionUtils.IGetList<CategoryDataCollection, SessionEntry>() {
                        @Override
                        public List<SessionEntry> getList(CategoryDataCollection dataCollection) {return dataCollection.buildSessionEntriesList();}
                    }
            );

            mAllSessionEntries = new SessionEntryCollection(sessionEntries);
            Collections.sort(mAllSessionEntries, SessionEntry.ICompareStartingTimes);

            mSessionEntriesListIsInvalid = false;
            mTotalDurationIsInvalid = true;
            mDateToIsInvalid = true;
            mDateFromIsInvalid = true;
        }

        return mAllSessionEntries;
    }

    public long calculateTotalDuration() {
        if (mTotalDuration == null || mTotalDurationIsInvalid) {
            mTotalDuration = (long) MyCollectionUtils.sum(this, CategoryDataCollection.IGetEntriesDuration);
            mTotalDurationIsInvalid = false;
        }

        return mTotalDuration;
    }

    public int[] buildColorsArray() {
        if (mColors == null || mColorsArrayIsInvalid) {
            mColors = new int[this.size()];

            for (int i = 0; i < this.size(); i++)
                mColors[i] = this.get(i).getCategory().getColorAsInt();

            mColorsArrayIsInvalid = false;
        }

        return mColors;
    }
    //endregion

    //region Getters {}
    public List<CategoryDataCollection> getData() {
        return this;
    }

    public long getDateFrom() {
        if (mDateFromIsInvalid) {
            SessionEntryCollection sessionEntries = buildSessionEntriesList();
            if (!sessionEntries.isEmpty()) {
                mDateFrom = Collections.min(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            }
            else {
                mDateFrom = -1;
            }
            mDateFromIsInvalid = false;
        }
        return mDateFrom;
    }

    public long getDateTo() {
        if (mDateToIsInvalid) {
            SessionEntryCollection sessionEntries = buildSessionEntriesList();
            if (!sessionEntries.isEmpty()) {
                mDateTo = Collections.max(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            }
            else {
                mDateTo = -1;
            }
            mDateToIsInvalid = false;
        }
        return mDateTo;
    }

    public long getMinimumTime() {
        return buildSessionEntriesList().getMinimumTime();
    }

    public long getMaximumTime() {
        return buildSessionEntriesList().getMaximumTime();
    }
    //endregion

    //region CRUD (Create, Read, Update, Delete) {}

    //endregion -- end --

}
