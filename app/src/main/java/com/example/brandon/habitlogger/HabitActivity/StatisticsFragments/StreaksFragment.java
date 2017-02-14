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
import java.util.Locale;

public class StreaksFragment extends Fragment implements UpdateEntriesInterface {

    private TextView title, value;
    CallbackInterface callbackInterface;

    public StreaksFragment() {
        // Required empty public constructor
    }

    public static StreaksFragment newInstance(String param1, String param2) {
        return new StreaksFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_streaks, container, false);

        title = (TextView) view.findViewById(R.id.title);
        value = (TextView) view.findViewById(R.id.value);

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

    @Override
    public void updateEntries(List<SessionEntry> sessionEntries, long dateFrom, long dateTo) {
        value.setText(Streak.listToString(getWeekStreaks(sessionEntries)));
    }

    public static List<StreaksFragment.Streak> getWeekStreaks(List<SessionEntry> sessionEntries) {
        List<Streak> streaks = new ArrayList<>();

        if(!sessionEntries.isEmpty()) {
            Collections.sort(sessionEntries, SessionEntry.StartingTimeComparator);

            int size = sessionEntries.size();
            long targetDate = sessionEntries.get(0).getStartingTimeDate();

            long interval = DateUtils.WEEK_IN_MILLIS;
            long endOfWeek = targetDate + interval - DateUtils.DAY_IN_MILLIS;

            Streak currentStreak = new Streak(targetDate, endOfWeek, 0);

            for (SessionEntry entry : sessionEntries) {
                long currentDate = entry.getStartingTimeDate();
                boolean endOfList = sessionEntries.indexOf(entry) == (size - 1);

                if (currentDate == targetDate) {
                    currentStreak.streakLength++;

                    if(currentDate >= endOfWeek) {
                        streaks.add(currentStreak);
                        endOfWeek = currentDate + interval;
                        currentStreak = new Streak(currentDate + DateUtils.DAY_IN_MILLIS, endOfWeek, 0);
                        targetDate = currentDate + DateUtils.DAY_IN_MILLIS;
                    }
                    else if (endOfList) {
                        streaks.add(currentStreak);
                        break;
                    }else{
                        targetDate += DateUtils.DAY_IN_MILLIS;
                    }

                } else if (currentDate > targetDate) {

                    currentStreak.streakLength++;

                    if(currentDate >= endOfWeek) {
                        streaks.add(currentStreak);
                        endOfWeek  = currentDate + interval;
                        currentStreak = new Streak(currentDate + DateUtils.DAY_IN_MILLIS, endOfWeek, 0);
                        targetDate = currentDate + DateUtils.DAY_IN_MILLIS;
                    }

                    else if (endOfList) {
                        streaks.add(currentStreak);
                        break;
                    }
                    else{
                        targetDate = currentDate + DateUtils.DAY_IN_MILLIS;
                    }
                }

                else if (endOfList) {
                    streaks.add(currentStreak);
                    break;
                }
            }
        }

        return streaks;
    }

    public static List<Streak> getStreaks(List<SessionEntry> sessionEntries) {
        List<Streak> streaks = new ArrayList<>();

        if(!sessionEntries.isEmpty()) {
            Collections.sort(sessionEntries, SessionEntry.StartingTimeComparator);

            int size = sessionEntries.size();
            long targetDate = sessionEntries.get(0).getStartingTimeDate();
            long previousDate = -1;

            Streak currentStreak = new Streak(targetDate);

            for (SessionEntry entry : sessionEntries) {
                long currentDate = entry.getStartingTimeDate();
                boolean endOfList = sessionEntries.indexOf(entry) == (size - 1);

                if (currentDate == targetDate) {
                    currentStreak.streakLength++;

                    if (endOfList) {
                        currentStreak.streakEnd = currentDate;
                        streaks.add(currentStreak);
                        break;
                    }

                    previousDate = currentDate;
                    targetDate += DateUtils.DAY_IN_MILLIS;

                } else if (currentDate > targetDate) {
                    currentStreak.streakEnd = previousDate;
                    streaks.add(currentStreak);

                    currentStreak = new Streak(currentDate, currentDate, 1);

                    if (endOfList) {
                        currentStreak.streakEnd = currentDate;
                        streaks.add(currentStreak);
                        break;
                    }

                    previousDate = currentDate;
                    targetDate = currentDate + DateUtils.DAY_IN_MILLIS;
                }

                if (endOfList) {
                    currentStreak.streakEnd = currentDate;
                    streaks.add(currentStreak);
                    break;
                }
            }
        }

        return streaks;
    }

    public static class Streak{
        public long streakStart = -1, streakEnd = -1;
        public int streakLength = 0;

        public Streak(long streakStart, long streakEnd, int streakLength) {
            this.streakStart = streakStart;
            this.streakEnd = streakEnd;
            this.streakLength = streakLength;
        }

        public Streak(long streakStart){
            this.streakStart = streakStart;
        }

        @Override
        public String toString() {
            String format = "%s [%d] %s";
            String startDate = SessionEntry.getDate(streakStart, "MMMM-d-yyyy");
            String endDate = SessionEntry.getDate(streakEnd, "MMMM-d-yyyy");

            return String.format(Locale.US, format, startDate, streakLength, endDate);
        }

        public static String listToString(List<Streak> streaks){
            String result = "";
            for (Streak streak : streaks) {
                result += streak.toString() + '\n';
            }
            return result;
        }
    }
}
