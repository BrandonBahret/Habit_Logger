package com.example.brandon.habitlogger.HabitActivity;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

import java.util.List;

/**
 * Created by Brandon on 2/9/2017.
 */

public interface UpdateEntriesInterface {
    void updateEntries(List<SessionEntry> sessionEntries, long dateFrom, long dateTo);
}
