package com.example.brandon.habitlogger.HabitActivity.StatisticsFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitActivity.CallbackInterface;
import com.example.brandon.habitlogger.HabitActivity.UpdateEntriesInterface;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeAverages extends Fragment implements UpdateEntriesInterface {

    private TextView hoursPerMonth, hoursPerWeek, hoursPerDay, habitFrequency;
    CallbackInterface callbackInterface;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_averages, container, false);

        hoursPerMonth  = (TextView) view.findViewById(R.id.hours_per_month_text);
        hoursPerWeek   = (TextView) view.findViewById(R.id.hours_per_week_text);
        hoursPerDay    = (TextView) view.findViewById(R.id.hours_per_day_text);
        habitFrequency = (TextView) view.findViewById(R.id.habit_frequency_text);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callbackInterface = (CallbackInterface)context;
        callbackInterface.addCallback(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        CallbackInterface.SessionEntriesSample sample = callbackInterface.getSessionEntries();
        updateEntries(sample.sessionEntries, sample.dateFromTime, sample.dateToTime);
    }

    public List<StreaksFragment.Streak> getWeekStreaks(List<SessionEntry> sessionEntries) {
        List<StreaksFragment.Streak> streaks = new ArrayList<>();

        if(!sessionEntries.isEmpty()) {
            Collections.sort(sessionEntries, SessionEntry.StartingTimeComparator);

            long initialDate = sessionEntries.get(0).getStartingTimeDate();
            long currentDate = initialDate;
            long previousDate = currentDate;
            long endOfWeek = currentDate + DateUtils.WEEK_IN_MILLIS;

            StreaksFragment.Streak currentStreak = new StreaksFragment.Streak(initialDate);

            for(SessionEntry entry : sessionEntries){
                currentDate = entry.getStartingTimeDate();

                if(currentDate < endOfWeek && currentDate != previousDate){
                    currentStreak.streakLength++;
                    previousDate = currentDate;
                }
                else if(currentDate >= endOfWeek && currentDate != previousDate){
                    currentStreak.streakLength++;
                    previousDate = currentDate;
                    currentStreak.streakEnd = currentDate;
                    streaks.add(currentStreak);
                }
            }

        }

        return streaks;
    }

    @Override
    public void updateEntries(List<SessionEntry> sessionEntries, long dateFrom, long dateTo){

        if(!sessionEntries.isEmpty()){
            List<StreaksFragment.Streak> streaks = StreaksFragment.getWeekStreaks(sessionEntries);

            int amount = 0;
            for(StreaksFragment.Streak streak : streaks){
                amount += streak.streakLength;
            }

            if(streaks.size() != 0) {// Don't divide by zero
                amount /= streaks.size();
            }

            this.habitFrequency.setText(String.valueOf(amount));


            long totalDuration = 0;
            for (SessionEntry entry : sessionEntries) {
                totalDuration += entry.getDuration();
            }

            long totalTime = dateTo - dateFrom;

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
