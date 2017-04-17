package com.example.brandon.habitlogger.data.DataModels.DataCollections;

import android.text.format.DateUtils;

import com.android.internal.util.Predicate;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 3/2/2017.
 * A class to structure SessionEntry objects.
 */

public class SessionEntryCollection extends MyDataCollectionBase<SessionEntry> {

    //region (Member Attributes)
    private long mDateFromTime, mDateToTime;
    private Long mDuration;
    private boolean mDurationIsInvalid;
    private Integer mTotalDaysLength;
    private boolean mTotalDaysLengthIsInvalid;
    //endregion

    //region Constructors {}
    public SessionEntryCollection(List<SessionEntry> sessionEntries, long dateFromTime, long dateToTime) {
        super(sessionEntries);
        mDateFromTime = dateFromTime;
        mDateToTime = dateToTime;
    }

    public SessionEntryCollection(List<SessionEntry> sessionEntries) {
        super(sessionEntries);
        updateDateFromToWithEntries(this);
    }

    public SessionEntryCollection(int initialCapacity) {
        this(new ArrayList<SessionEntry>(initialCapacity));
    }

    public SessionEntryCollection() {
        this(new ArrayList<SessionEntry>());
    }
    //endregion -- end --

    public SessionEntryCollection findEntriesWithDate(final long timestamp) {
        SessionEntryCollection entries = new SessionEntryCollection(this);

        MyCollectionUtils.filter(entries, new Predicate<SessionEntry>() {
            @Override
            public boolean apply(SessionEntry sessionEntry) {
                return !(sessionEntry.getStartingTimeIgnoreTimeOfDay() == timestamp);
            }
        });

        return entries;
    }

    //region Methods With One Time Calculations {}
    @Override
    void invalidate() {
        mTotalDaysLengthIsInvalid = true;
        mDurationIsInvalid = true;
    }

    public long calculateDuration() {
        if (mDuration == null || mDurationIsInvalid) {
            mDuration = (long) MyCollectionUtils.sum(this, SessionEntry.IGetSessionDuration);
            mDurationIsInvalid = false;
        }

        return mDuration;
    }

    public int calculateTotalDaysLength() {
        if (mTotalDaysLength == null || mTotalDaysLengthIsInvalid) {
            long totalTime = getDateToTime() - getDateFromTime();
            mTotalDaysLength = (int) Math.ceil(totalTime / DateUtils.DAY_IN_MILLIS);
            mTotalDaysLengthIsInvalid = false;
        }

        return mTotalDaysLength;
    }
    //endregion

    //region Getters {}
    public List<SessionEntry> asList() {
        return this;
    }

    public long getDateFromTime() {
        return mDateFromTime;
    }

    public long getDateToTime() {
        return mDateToTime;
    }

    public long getMinimumTime() {
        if (!this.isEmpty())
            return Collections.min(this, SessionEntry.ICompareStartingTimes).getStartingTime();
        else return -1;
    }

    public long getMaximumTime() {
        if (!this.isEmpty())
            return Collections.max(this, SessionEntry.ICompareStartingTimes).getStartingTime();
        else return -1;
    }
    //endregion

    //region CRUD (Create, Read, Update, Delete) {}
    public void updateSessionEntries(List<SessionEntry> entries) {
        this.clear();
        this.addAll(entries);
        updateDateFromToWithEntries(this);
    }

    private void updateDateFromToWithEntries(List<SessionEntry> sessionEntries) {
        if (!sessionEntries.isEmpty()) {
            mDateFromTime = Collections.min(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            mDateToTime = Collections.max(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
        }
        else {
            mDateFromTime = -1;
            mDateToTime = -1;
        }
    }

    public boolean hasEntryFor(long targetDate) {
        targetDate = MyTimeUtils.setTimePortion(targetDate, true, 0, 0, 0, 0);
        return MyCollectionUtils.binarySearch(this, targetDate, new MyCollectionUtils.ICompareKey() {
            @Override
            public int compare(Object element, Object key) {
                long startingTimeIgnoreTimeOfDay = ((SessionEntry) element).getStartingTimeIgnoreTimeOfDay();
                return Long.compare(startingTimeIgnoreTimeOfDay, (Long) key);
            }
        }) >= 0;
    }

    public int addEntry(SessionEntry entry) {
        int pos = MyCollectionUtils.binarySearchForInsertPosition(this, entry.getStartingTime(), SessionEntry.IKeyCompareStartingTime);
        this.add(pos, entry);
        return pos;
    }

    public int removeEntry(SessionEntry entry) {
        int pos = this.indexOf(entry);
        this.remove(pos);
        return pos;
    }

    public int updateEntry(SessionEntry oldEntry, SessionEntry newEntry) {
        this.removeEntry(oldEntry);
        return this.addEntry(newEntry);
    }

    public void setDateFrom(long dateFrom) {
        mDateFromTime = dateFrom;
    }

    public void setDateTo(long dateTo) {
        mDateToTime = dateTo;
    }
    //endregion

}