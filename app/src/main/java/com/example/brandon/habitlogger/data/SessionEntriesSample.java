package com.example.brandon.habitlogger.data;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;

import java.util.List;

/**
 * Created by Brandon on 3/2/2017.
 *
 */

public class SessionEntriesSample{
    private final List<SessionEntry> sessionEntries;
    public final long dateFromTime;
    public final long dateToTime;
    private long mDuration = -1;

    public SessionEntriesSample(List<SessionEntry> sessionEntries, long dateFromTime, long dateToTime){
        this.sessionEntries = sessionEntries;
        this.dateFromTime = dateFromTime;
        this.dateToTime = dateToTime;
    }

    public SessionEntriesSample(List<SessionEntry> sessionEntries) {
        this.sessionEntries = sessionEntries;
        this.dateFromTime = sessionEntries.get(0).getStartingTimeDate();
        this.dateToTime = sessionEntries.get(sessionEntries.size() - 1).getStartingTimeDate();
    }

    public List<SessionEntry> getSessionEntries(){
        return sessionEntries;
    }

    public long calculateDuration(){
        if(mDuration == -1){
            mDuration = 0;
            for(SessionEntry entry : sessionEntries){
                mDuration += entry.getDuration();
            }
        }

        return mDuration;
    }

    public boolean isEmpty() {
        return sessionEntries.isEmpty();
    }
}