package com.example.brandon.habitlogger.HabitActivity;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;

import java.util.List;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to update entries on fragments
 */

public interface UpdateEntriesInterface {
    void updateEntries(List<SessionEntry> sessionEntries, long dateFrom, long dateTo);
}
