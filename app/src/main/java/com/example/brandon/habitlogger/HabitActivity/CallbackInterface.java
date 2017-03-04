package com.example.brandon.habitlogger.HabitActivity;

import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to communicate with fragments in the habit activity
 */

public interface CallbackInterface {

    void addCallback(UpdateEntriesInterface callback);
    void removeCallback(UpdateEntriesInterface callback);

    void addOnNewCategoryDataSampleCallback(UpdateCategorySampleInterface callback);

    SessionEntriesSample getSessionEntries();
    CategoryDataSample getCategoryDataSample();

    int getDefaultColor();
}