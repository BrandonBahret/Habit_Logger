package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;

public class TimeAverages extends Fragment {

    private TextView hoursPerMonth, hoursPerWeek, hoursPerDay, habitFrequency;
//    IHabitDataCallback callbackInterface;

    public TimeAverages() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimeAverages.
     */
    public static TimeAverages newInstance() {
        return new TimeAverages();
    }

    //region Methods to handle the fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_averages, container, false);

        hoursPerMonth = (TextView) view.findViewById(R.id.hours_per_month_text);
        hoursPerWeek = (TextView) view.findViewById(R.id.hours_per_week_text);
        hoursPerDay = (TextView) view.findViewById(R.id.hours_per_day_text);
        habitFrequency = (TextView) view.findViewById(R.id.habit_frequency_text);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        callbackInterface = (IHabitCallback) context;
//        callbackInterface.addUpdateEntriesCallback(this);
    }

    @Override
    public void onStart() {
        super.onStart();
//        updateEntries(callbackInterface.asList());
    }
    //endregion

//    @Override
    public void updateEntries(SessionEntryCollection dataSample) {

        if (!dataSample.isEmpty()) {

            double totalHours = dataSample.calculateDuration() / DateUtils.HOUR_IN_MILLIS;

            double months = dataSample.calculateTotalDaysLength() / (365.0 / 12.0);
            double hoursPerMonthTime = months == 0 ? 0 : totalHours / months;
            hoursPerMonth.setText(String.valueOf(hoursPerMonthTime));

            double weeks = dataSample.calculateTotalDaysLength() / 7.0;
            double hoursPerWeekTime = weeks == 0 ? 0 : totalHours / weeks;
            hoursPerWeek.setText(String.valueOf(hoursPerWeekTime));

            double days = dataSample.calculateTotalDaysLength();
            double hoursPerDayTime = days == 0 ? 0 : totalHours / days;
            hoursPerDay.setText(String.valueOf(hoursPerDayTime));

            double daysPerWeekOnAverage = MyCollectionUtils.collectIntoSet(dataSample.asList(), SessionEntry.IGetSessionStartDate).size() / weeks;
            this.habitFrequency.setText(String.valueOf(daysPerWeekOnAverage));
        }
    }

}
