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
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StreaksFragment extends Fragment implements UpdateEntriesInterface {

    private TextView value;
    CallbackInterface callbackInterface;

    public StreaksFragment() {
        // Required empty public constructor
    }

    public static StreaksFragment newInstance() {
        return new StreaksFragment();
    }

    //region Methods responsible for handling the fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_streaks, container, false);
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
        updateEntries(callbackInterface.getSessionEntries());
    }
    //endregion

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {
        value.setText(Streak.listToString(getStreaks(dataSample.getSessionEntries())));
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
            String startDate = MyTimeUtils.stringifyTimestamp(streakStart, "MMMM-d-yyyy");
            String endDate = MyTimeUtils.stringifyTimestamp(streakEnd, "MMMM-d-yyyy");

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
