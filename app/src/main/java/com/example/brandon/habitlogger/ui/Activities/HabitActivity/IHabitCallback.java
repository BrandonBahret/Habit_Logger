package com.example.brandon.habitlogger.ui.Activities.HabitActivity;

import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to communicate with fragments within HabitActivity
 */

public interface IHabitCallback {

    interface IUpdateEntries {
        void updateEntries(SessionEntriesSample dataSample);
    }

    interface IUpdateCategorySample {
        void updateCategoryDataSample(CategoryDataSample dataSample);
    }

    interface IOnTabReselected{
        void onTabReselected(int position);
    }

    int getDefaultColor();
    Habit getHabit();

    SessionEntriesSample getSessionEntries();
    void addUpdateEntriesCallback(IUpdateEntries callback);
    void removeUpdateEntriesCallback(IUpdateEntries callback);

    CategoryDataSample getCategoryDataSample();
    void addUpdateCategoryDataSampleCallback(IUpdateCategorySample callback);
    void removeUpdateCategoryDataSampleCallback(IUpdateCategorySample callback);

    void addOnTabReselectedCallback(IOnTabReselected callback);

    void removeOnTabReselectedCallback(IOnTabReselected callback);

}