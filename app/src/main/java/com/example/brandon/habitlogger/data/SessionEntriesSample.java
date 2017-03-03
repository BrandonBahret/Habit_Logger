package com.example.brandon.habitlogger.data;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;

import java.util.List;

/**
 * Created by Brandon on 3/2/2017.
 *
 */

public class SessionEntriesSample{
    public List<SessionEntry> sessionEntries;
    public long dateFromTime;
    public long dateToTime;

    public SessionEntriesSample(List<SessionEntry> sessionEntries, long dateFromTime, long dateToTime){
        this.sessionEntries = sessionEntries;
        this.dateFromTime = dateFromTime;
        this.dateToTime = dateToTime;
    }
}