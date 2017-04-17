package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity;

import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.CategoryDataCollection;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.Habit;

import java.io.Serializable;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to communicate with fragments within HabitActivity
 */

public interface IHabitDataCallback {

    interface IEntriesFragment {
        void onUpdateEntries(SessionEntryCollection dataSample);

        void onNotifyEntryRemoved(int adapterPosition);

        void onNotifyEntryAdded(int adapterPosition);

        void onNotifyEntryUpdated(int oldPosition, int newPosition);

        void onUpdateColorPalette(ThemeColorPalette palette);

        void onTabReselected();
    }

    void setEntriesFragmentCallback(IEntriesFragment callback);

    interface ICalendarFragment {
        void onUpdateEntries(SessionEntryCollection dataSample);

        void onUpdateColorPalette(ThemeColorPalette palette);

        void onTabReselected();

    }

    void setCalendarFragmentCallback(ICalendarFragment callback);


    interface IStatisticsFragment {
        void onUpdateEntries(SessionEntryCollection dataSample);

        void onUpdateCategoryDataSample(CategoryDataCollection dataSample);

        void onUpdateColorPalette(ThemeColorPalette palette);

        void onTabReselected();
    }

    interface IUpdateEntries extends Serializable {
        SessionEntryCollection getSessionEntries();
    }

    interface IUpdateCategoryData extends Serializable {
        CategoryDataCollection getCategoryDataSample();
    }

    void setStatisticsFragmentCallback(IStatisticsFragment callback);

    Habit getHabit();

    ThemeColorPalette getColorPalette();

    SessionEntryCollection getSessionEntries();

    CategoryDataCollection getCategoryDataSample();

}