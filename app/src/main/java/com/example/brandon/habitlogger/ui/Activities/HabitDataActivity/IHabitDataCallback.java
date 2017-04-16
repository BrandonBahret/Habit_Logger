package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity;

import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to communicate with fragments within HabitActivity
 */

public interface IHabitDataCallback {

    interface IEntriesFragment {
        void onUpdateEntries(SessionEntriesCollection dataSample);

        void onRemoveEntry(SessionEntry removedEntry);

        void onNotifyEntryAdded(int adapterPosition);

        void onUpdateEntry(long databaseId, SessionEntry oldEntry, SessionEntry newEntry);

        void onUpdateColorPalette(ThemeColorPalette palette);

        void onTabReselected();
    }

    void setEntriesFragmentCallback(IEntriesFragment callback);

    interface ICalendarFragment {
        void onUpdateEntries(SessionEntriesCollection dataSample);

        void onUpdateColorPalette(ThemeColorPalette palette);

        void onTabReselected();

    }

    void setCalendarFragmentCallback(ICalendarFragment callback);


    interface IStatisticsFragment {
        void onUpdateEntries(SessionEntriesCollection dataSample);

        void onUpdateCategoryDataSample(CategoryDataSample dataSample);

        void onUpdateColorPalette(ThemeColorPalette palette);

        void onTabReselected();
    }

    void setStatisticsFragmentCallback(IStatisticsFragment callback);

    Habit getHabit();

    ThemeColorPalette getColorPalette();

    SessionEntriesCollection getSessionEntries();

    CategoryDataSample getCategoryDataSample();

}