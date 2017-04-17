package com.example.brandon.habitlogger.ui.Activities.OverviewActivity;

import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.CategoryDataCollection;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.Habit;

import java.io.Serializable;

/**
 * Created by Brandon on 3/2/2017.
 * Interface to communicate with fragments within DataOverviewActivity
 */

public interface IDataOverviewCallback {

    interface IEntriesFragment {
        void onUpdateEntries(SessionEntryCollection dataSample);

        void onNotifyEntryRemoved(int adapterPosition);

        void onNotifyEntryAdded(int adapterPosition);

        void onNotifyEntryUpdated(int oldPosition, int newPosition);

        void onTabReselected();
    }

    void setEntriesFragmentCallback(IEntriesFragment callback);

    interface ICalendarFragment {
        void onUpdateEntries(SessionEntryCollection dataSample);

        void onTabReselected();
    }

    void setCalendarFragmentCallback(ICalendarFragment callback);


    interface IStatisticsFragment {
        void onUpdateEntries(SessionEntryCollection dataSample);

        void onTabReselected();
    }

    void setStatisticsFragmentCallback(IStatisticsFragment callback);

    interface IUpdateEntries extends Serializable {
        SessionEntryCollection getSessionEntries();

    }
    interface IUpdateCategoryData extends Serializable {
        CategoryDataCollection getCategoryDataSample();
    }

    SessionEntryCollection getSessionEntries();

}
