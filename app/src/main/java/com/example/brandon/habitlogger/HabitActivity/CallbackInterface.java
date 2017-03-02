package com.example.brandon.habitlogger.HabitActivity;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.CategoryDataSample;

import java.util.List;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to communicate with fragments in the habit activity
 */

public interface CallbackInterface {

    void addCallback(UpdateEntriesInterface callback);
    void addOnNewCategoryDataSampleCallback(UpdateCategorySampleInterface callback);

    SessionEntriesSample getSessionEntries();
    CategoryDataSample getCategoryDataSample();

    int getDefaultColor();

    class SessionEntriesSample{
        public List<SessionEntry> sessionEntries;
        public long dateFromTime;
        public long dateToTime;

        public SessionEntriesSample(List<SessionEntry> sessionEntries, long dateFromTime, long dateToTime){
            this.sessionEntries = sessionEntries;
            this.dateFromTime = dateFromTime;
            this.dateToTime = dateToTime;
        }
    }
}
