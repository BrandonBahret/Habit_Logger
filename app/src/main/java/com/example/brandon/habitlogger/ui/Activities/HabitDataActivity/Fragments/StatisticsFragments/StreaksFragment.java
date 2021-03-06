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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class StreaksFragment extends Fragment {

    private TextView value;
//    IHabitCallback mCallbackInterface;

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

//        mCallbackInterface = (IHabitCallback) context;
//        mCallbackInterface.addUpdateEntriesCallback(this);
    }

    @Override
    public void onStart() {
        super.onStart();
//        updateEntries(mCallbackInterface.asList());
    }
    //endregion

    public void updateEntries(SessionEntryCollection dataSample) {
        value.setText(Streak.listToString(getStreaks(dataSample)));
    }

    public static List<Streak> getStreaks(SessionEntryCollection dataSample) {
        List<Streak> streaks = new ArrayList<>();

        if (!dataSample.isEmpty()) {

            Set<Long> sessionDatesSet = MyCollectionUtils.collectIntoSet(dataSample.asList(), SessionEntry.IGetSessionStartDate);
            List<Long> sessionDates = new ArrayList<>(sessionDatesSet);
            Collections.sort(sessionDates);

            long lastDate = sessionDates.get(0);
            Streak currentStreak = new Streak(lastDate, lastDate, 1);

            if(sessionDates.size() > 1) {
                ListIterator iterator = sessionDates.listIterator(1);

                while (iterator.hasNext()) {
                    long currentDate = (long) iterator.next();

                    if (currentDate - lastDate > DateUtils.DAY_IN_MILLIS) {
                        currentStreak.streakEnd = lastDate;
                        streaks.add(currentStreak);

                        currentStreak = new Streak(currentDate);
                        if (!iterator.hasNext()) {
                            streaks.add(currentStreak);
                            break;
                        }
                    }
                    else if (!iterator.hasNext()) {
                        currentStreak.streakEnd = currentDate;
                        currentStreak.streakLength++;
                        streaks.add(currentStreak);
                    }
                    else currentStreak.streakLength++;

                    lastDate = currentDate;
                }
            }
            else streaks.add(currentStreak);
        }

        return streaks;
    }

}
