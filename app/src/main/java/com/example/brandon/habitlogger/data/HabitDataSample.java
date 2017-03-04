package com.example.brandon.habitlogger.data;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 3/2/2017.
 */

public class HabitDataSample {

    private List<CategoryDataSample> mData;
    private long mDateFrom, mDateTo;
    private long mDuration = -1;

    public HabitDataSample(List<CategoryDataSample> data, long dateFrom, long dateTo) {
        mData = data;
        mDateFrom = dateFrom;
        mDateTo = dateTo;
    }

    public List<CategoryDataSample> getData() {
        return mData;
    }

    public SessionEntriesSample getSessionEntriesSample() {
        List<SessionEntry> entries = new ArrayList<>();

        for (CategoryDataSample categoryDataSample : mData) {
            entries.addAll(categoryDataSample.getSessionEntries());
        }


        Collections.sort(entries, SessionEntry.StartingTimeComparator);

        return new SessionEntriesSample(entries, mDateFrom, mDateTo);
    }

    public long calculateTotalDuration() {
        if (mDuration == -1) {
            mDuration = 0;
            for (CategoryDataSample categoryDataSample : mData) {
                mDuration += categoryDataSample.calculateTotalDuration();
            }
        }

        return mDuration;
    }

    public int[] getColors() {
        int[] colors = new int[mData.size()];

        for (int i = 0; i < mData.size(); i++) {
            colors[i] = mData.get(i).getCategory().getColorAsInt();
        }

        return colors;
    }
}
