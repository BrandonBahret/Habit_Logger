package com.example.brandon.habitlogger.HabitActivity;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

import java.util.List;

/**
 * Created by Brandon on 2/9/2017.
 */

public interface CallbackInterface {

    void addCallback(UpdateEntriesInterface callback);

    SessionEntriesSample getSessionEntries();

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
