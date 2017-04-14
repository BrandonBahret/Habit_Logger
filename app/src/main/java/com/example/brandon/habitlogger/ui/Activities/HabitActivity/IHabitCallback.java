package com.example.brandon.habitlogger.ui.Activities.HabitActivity;

import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to communicate with fragments within HabitActivity
 */

public interface IHabitCallback {

    interface IUpdateEntries {
        void updateEntries(SessionEntriesCollection dataSample);
    }

    interface IUpdateCategorySample {
        void updateCategoryDataSample(CategoryDataSample dataSample);
    }

    interface IUpdateColor {
        void updateColor(int color);
    }

    interface IOnTabReselected {
        void onTabReselected(int position);
    }

    Habit getHabit();

    //region Color callbacks
    int getDefaultColor();

    void addUpdateColorCallback(IUpdateColor callback);

    void removeUpdateColorCallback(IUpdateColor callback);
    //endregion

    //region Session entries callbacks
    SessionEntriesCollection getSessionEntries();

    void addUpdateEntriesCallback(IUpdateEntries callback);

    void removeUpdateEntriesCallback(IUpdateEntries callback);
    //endregion

    //region Category data callbacks
    CategoryDataSample getCategoryDataSample();

    void addUpdateCategoryDataSampleCallback(IUpdateCategorySample callback);

    void removeUpdateCategoryDataSampleCallback(IUpdateCategorySample callback);
    //endregion

    //region Event callbacks
    void addOnTabReselectedCallback(IOnTabReselected callback);

    void removeOnTabReselectedCallback(IOnTabReselected callback);
    //endregion

}