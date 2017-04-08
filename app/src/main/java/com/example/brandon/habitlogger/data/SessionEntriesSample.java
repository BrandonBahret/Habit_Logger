package com.example.brandon.habitlogger.data;

import android.text.format.DateUtils;

import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;

import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 3/2/2017.
 * A class to structure SessionEntry objects.
 */

public class SessionEntriesSample {

    //region (Member Attributes)
    private final List<SessionEntry> mSessionEntries;
    private final long mDateFromTime, mDateToTime;
    private Long mDuration;
    private Integer mTotalDaysLength;
    //endregion

    public SessionEntriesSample(List<SessionEntry> sessionEntries, long dateFromTime, long dateToTime) {
        mSessionEntries = sessionEntries;
        mDateFromTime = dateFromTime;
        mDateToTime = dateToTime;
    }

    public SessionEntriesSample(List<SessionEntry> sessionEntries) {
        mSessionEntries = sessionEntries;
        if (!mSessionEntries.isEmpty()) {
            mDateFromTime = Collections.min(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            mDateToTime = Collections.max(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
        }else{
            mDateFromTime = -1;
            mDateToTime = -1;
        }
    }

    //region Methods With One Time Calculations {}
    public long calculateDuration() {
        if (mDuration == null)
            mDuration = (long) MyCollectionUtils.sum(mSessionEntries, SessionEntry.IGetSessionDuration);

        return mDuration;
    }

    public int calculateTotalDaysLength() {
        if (mTotalDaysLength == null) {
            long totalTime = getDateToTime() - getDateFromTime();
            mTotalDaysLength = (int) Math.ceil(totalTime / DateUtils.DAY_IN_MILLIS);
        }

        return mTotalDaysLength;
    }
    //endregion

    //region Getters {}
    public List<SessionEntry> getSessionEntries() {
        return mSessionEntries;
    }

    public boolean isEmpty() {
        return mSessionEntries.isEmpty();
    }

    public long getDateFromTime() {
        return mDateFromTime;
    }

    public long getDateToTime() {
        return mDateToTime;
    }

    public long getMinimumTime(){
        if (!mSessionEntries.isEmpty())
            return Collections.min(mSessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
        else return -1;
    }

    public long getMaximumTime(){
        if (!mSessionEntries.isEmpty())
            return Collections.max(mSessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
        else return -1;
    }
    //endregion
}