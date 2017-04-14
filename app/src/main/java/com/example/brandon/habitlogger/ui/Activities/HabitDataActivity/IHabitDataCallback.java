package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity;

import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to communicate with fragments within HabitActivity
 */

public interface IHabitDataCallback {

    interface IEntriesFragment {
        void onUpdateEntries(SessionEntriesCollection dataSample);

        void onRemoveEntry(int adapterPosition);

        void onAddEntry();

        void onUpdateEntry();

        void onUpdateColor(int color);

        void onTabReselected();
    }

    void setEntriesFragmentCallback(IEntriesFragment callback);

    interface ICalendarFragment {
        void onUpdateEntries(SessionEntriesCollection dataSample);

        void onUpdateColor(int color);

        void onTabReselected();

    }

    void setCalendarFragmentCallback(ICalendarFragment callback);


    interface IStatisticsFragment {
        void onUpdateEntries(SessionEntriesCollection dataSample);

        void onUpdateCategoryDataSample(CategoryDataSample dataSample);

        void onUpdateColor(int color);

        void onTabReselected();
    }

    void setStatisticsFragmentCallback(IStatisticsFragment callback);

    Habit getHabit();

    int getDefaultColor();

    SessionEntriesCollection getSessionDataSample();

    CategoryDataSample getCategoryDataSample();

}