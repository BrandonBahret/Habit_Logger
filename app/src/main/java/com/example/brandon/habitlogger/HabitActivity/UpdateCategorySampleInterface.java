package com.example.brandon.habitlogger.HabitActivity;

import com.example.brandon.habitlogger.data.CategoryDataSample;

/**
 * Created by Brandon on 2/9/2017.
 * Interface to update entries on fragments
 */

public interface UpdateCategorySampleInterface {
    void updateCategoryDataSample(CategoryDataSample dataSample, long dateFrom, long dateTo);
}
