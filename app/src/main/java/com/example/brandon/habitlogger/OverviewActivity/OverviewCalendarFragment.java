package com.example.brandon.habitlogger.OverviewActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;

public class OverviewCalendarFragment extends Fragment {

    public OverviewCalendarFragment() {
        // Required empty public constructor
    }

    public static OverviewCalendarFragment newInstance() {
        return new OverviewCalendarFragment();
    }

    //region // Methods responsible for handling the fragment lifecycle.

    //region // On create methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_overview_calendar, container, false);
    }
    //endregion

    //region // Attach - Detach
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    //endregion

    //endregion

}