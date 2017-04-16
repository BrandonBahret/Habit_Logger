package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity;

import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;

import java.io.Serializable;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to communicate with fragments within HabitActivity
 */

public interface IHabitDataCallback {

    interface IEntriesFragment {
        void onUpdateEntries(SessionEntriesCollection dataSample);

        void onNotifyEntryRemoved(int adapterPosition);

        void onNotifyEntryAdded(int adapterPosition);

        void onNotifyEntryUpdated(int oldPosition, int newPosition);

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

    interface IUpdateEntries extends Serializable {
        SessionEntriesCollection getSessionEntries();
    }

    interface IUpdateCategoryData extends Serializable {
        CategoryDataSample getCategoryDataSample();
    }

    void setStatisticsFragmentCallback(IStatisticsFragment callback);

    Habit getHabit();

    ThemeColorPalette getColorPalette();

    SessionEntriesCollection getSessionEntries();

    CategoryDataSample getCategoryDataSample();

}