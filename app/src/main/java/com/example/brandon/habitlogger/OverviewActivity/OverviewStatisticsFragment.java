package com.example.brandon.habitlogger.OverviewActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;

public class OverviewStatisticsFragment extends Fragment {

    private static View view;

    public OverviewStatisticsFragment() {
        // Required empty public constructor
    }

    public static OverviewStatisticsFragment newInstance() {
        return new OverviewStatisticsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_overall_statistics, container, false);
        } catch (InflateException e) {
            // empty stub
        }
        return view;
    }

}
