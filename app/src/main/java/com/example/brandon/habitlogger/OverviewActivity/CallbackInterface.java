package com.example.brandon.habitlogger.OverviewActivity;

import com.example.brandon.habitlogger.data.HabitDataSample;

/**
 * Created by Brandon on 3/2/2017.
 *
 */

public interface CallbackInterface {
    void addCallback(UpdateHabitDataSampleInterface callback);
    HabitDataSample getDataSample();

    void removeCallback(UpdateHabitDataSampleInterface callback);
}
