package com.example.brandon.habitlogger.data;

import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.common.MyCollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 3/2/2017.
 * A class to structure CategoryDataSample objects.
 */

public class HabitDataSample {

    //region (Member Attributes)
    private final List<CategoryDataSample> mData;
    private final long mDateFrom, mDateTo;
    private Long mDuration;
    private int[] mColors;
    //endregion

    public HabitDataSample(List<CategoryDataSample> data, long dateFrom, long dateTo) {
        mData = data;
        mDateFrom = dateFrom;
        mDateTo = dateTo;
    }

    public SessionEntriesSample buildSessionEntriesList() {
        List<SessionEntry> entries = new ArrayList<>();

        for (CategoryDataSample categoryDataSample : mData)
            entries.addAll(categoryDataSample.buildSessionEntriesList());

        Collections.sort(entries, SessionEntry.ICompareStartingTimes);

        return new SessionEntriesSample(entries, mDateFrom, mDateTo);
    }

    /**
     * @param timestamp Epoch timestamp for a certain date to search for.
     */
    public HabitDataSample getDataSampleForDate(long timestamp){
        List<CategoryDataSample> categoryDataSamples = new ArrayList<>(mData.size());

        for(CategoryDataSample dataSample : mData)
            categoryDataSamples.add(dataSample.getDataSampleForDate(timestamp));

        return new HabitDataSample(categoryDataSamples, timestamp, timestamp);
    }

    //region Methods With One Time Calculations {}
    public long calculateTotalDuration() {
        if (mDuration == null)
            mDuration = (long) MyCollectionUtils.sum(mData, CategoryDataSample.IGetEntriesDuration);

        return mDuration;
    }

    public int[] buildColorsArray() {
        if (mColors == null) {
            mColors = new int[mData.size()];

            for (int i = 0; i < mData.size(); i++)
                mColors[i] = mData.get(i).getCategory().getColorAsInt();
        }

        return mColors;
    }
    //endregion

    //region Getters {}
    public List<CategoryDataSample> getData() {
        return mData;
    }

    public long getDateFrom(){
        return mDateFrom;
    }

    public long getDateTo(){
        return mDateTo;
    }

    public long getMinimumTime(){
        List<SessionEntry> sessionEntries = buildSessionEntriesList().getSessionEntries();

        if (!sessionEntries.isEmpty())
            return Collections.min(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
        else return -1;
    }

    public long getMaximumTime(){
        List<SessionEntry> sessionEntries = buildSessionEntriesList().getSessionEntries();

        if (!sessionEntries.isEmpty())
            return Collections.max(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
        else return -1;
    }

    //endregion

}
