package com.example.brandon.habitlogger.HabitActivity.StatisticsFragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitActivity.CallbackInterface;
import com.example.brandon.habitlogger.HabitActivity.UpdateEntriesInterface;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;

import java.util.Collections;
import java.util.List;


public class PieGraphCompletion extends Fragment implements UpdateEntriesInterface {

    private TextView title, value;
    CallbackInterface callbackInterface;

    public PieGraphCompletion() {
        // Required empty public constructor
    }

    public static PieGraphCompletion newInstance(List<SessionEntry> sessionEntries) {
        return new PieGraphCompletion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pie_graph_completion, container, false);

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
        float ratio = 0.0f;

        if(!sessionEntries.isEmpty()) {
            Collections.sort(sessionEntries, SessionEntry.StartingTimeComparator);
            final long DAY_IN_MILLI = 86400000L;

            long totalTime = dateTo - dateFrom;
            int totalDays = (int)(totalTime / DAY_IN_MILLI);

            if(totalDays > 0) {
                long targetDate = dateFrom;
                int dayCount = 0;

                for (SessionEntry entry : sessionEntries) {
                    long currentDate = entry.getStartingTimeDate();

                    if (currentDate == targetDate) {
                        dayCount++;
                        targetDate += DAY_IN_MILLI;
                    } else if (currentDate > targetDate) {
                        dayCount++;
                        targetDate = currentDate + DAY_IN_MILLI;
                    }
                }

                ratio = dayCount / (float)totalDays;

            } else{
                ratio = 1;
            }
        }

        value.setText(String.valueOf(ratio));
    }
}
