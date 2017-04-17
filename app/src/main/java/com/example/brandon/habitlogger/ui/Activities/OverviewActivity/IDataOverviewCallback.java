package com.example.brandon.habitlogger.ui.Activities.OverviewActivity;

import com.example.brandon.habitlogger.data.DataModels.DataCollections.HabitDataCollection;

/**
 * Created by Brandon on 3/2/2017.
 * Interface to communicate with fragments within DataOverviewActivity
 */

public interface IDataOverviewCallback {

    interface IUpdateHabitSample {
        void updateHabitDataSample(HabitDataCollection dataSample);
    }

    interface IOnTabReselected {
        void onTabReselected(int position);
    }


    HabitDataCollection getDataSample();

    void addCallback(IUpdateHabitSample callback);

    void removeCallback(IUpdateHabitSample callback);

    void addOnTabReselectedCallback(IOnTabReselected callback);

    void removeOnTabReselectedCallback(IOnTabReselected callback);


}
