package com.example.brandon.habitlogger.OverviewActivity;

import com.example.brandon.habitlogger.data.HabitDataSample;

/**
 * Created by Brandon on 3/2/2017.
 * Interface to communicate with fragments within DataOverviewActivity
 */

public interface IDataOverviewCallback {

    interface IUpdateHabitSample {
        void updateHabitDataSample(HabitDataSample dataSample);
    }

    HabitDataSample getDataSample();

    void addCallback(IUpdateHabitSample callback);

    void removeCallback(IUpdateHabitSample callback);

}
