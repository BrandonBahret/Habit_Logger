package com.example.brandon.habitlogger.HabitActivity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitActivity.StatisticsFragments.TimeAverages;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {

    TimeAverages timeAverages;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);

        timeAverages = (TimeAverages)childFragment.getFragmentManager().findFragmentById(R.id.time_averages);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EntriesFragment.
     */
    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    public void updateEntries(List<SessionEntry> sessionEntries){
        if(timeAverages != null)
            timeAverages.updateEntries(sessionEntries);
    }

}
