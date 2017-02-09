package com.example.brandon.habitlogger.HabitActivity.StatisticsFragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitActivity.CallbackInterface;
import com.example.brandon.habitlogger.HabitActivity.UpdateEntriesInterface;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;

import java.util.List;

public class TimeAverages extends Fragment implements UpdateEntriesInterface {

    private TextView hoursPerMonth, hoursPerWeek, hoursPerDay, habitFrequency;
    List<SessionEntry> sessionEntries;

    public TimeAverages() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimeAverages.
     */
    public static TimeAverages newInstance(List<SessionEntry> sessionEntries) {
        TimeAverages frag = new TimeAverages();

        Bundle args = new Bundle();
        if(sessionEntries != null)
            args.putParcelableArray("entries", sessionEntries.toArray(new Parcelable[sessionEntries.size()]));

        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_averages, container, false);

        hoursPerMonth  = (TextView) view.findViewById(R.id.hours_per_month_text);
        hoursPerWeek   = (TextView) view.findViewById(R.id.hours_per_week_text);
        hoursPerDay    = (TextView) view.findViewById(R.id.hours_per_day_text);
        habitFrequency = (TextView) view.findViewById(R.id.habit_frequency_text);

        if(sessionEntries != null){
            updateEntries(sessionEntries);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        CallbackInterface callbackInterface = (CallbackInterface)context;
        callbackInterface.addCallback(this);
    }

    @Override
    public void updateEntries(List<SessionEntry> entries){

        if(!entries.isEmpty()){
            long totalDuration = 0;
            for (SessionEntry entry : entries) {
                totalDuration += entry.getDuration();
            }

            long beginningTime = entries.get(0).getStartTime();
            long endingTime    = entries.get(entries.size() - 1).getStartTime();
            long totalTime     = endingTime - beginningTime;

            double months = totalTime / 2592000000L;
            double weeks  = totalTime / 604800000L;
            double days   = totalTime / 86400000L;

            double totalHours = totalDuration / 3600000L;

            double hoursPerMonthTime = months == 0 ? totalHours : totalHours / months;
            double hoursPerWeekTime  = weeks  == 0 ? totalHours : totalHours / weeks;
            double hoursPerDayTime   = days   == 0 ? totalHours : totalHours / days;

            hoursPerMonth.setText(String.valueOf(hoursPerMonthTime));
            hoursPerWeek.setText(String.valueOf(hoursPerWeekTime));
            hoursPerDay.setText(String.valueOf(hoursPerDayTime));
        }
    }
}
