package com.example.brandon.habitlogger.HabitActivity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {

    private static View view;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_statistics, container, false);
        } catch (InflateException e) {
            /* fragment is already there, just return view as it is */
        }

        return view;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
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

    }

}