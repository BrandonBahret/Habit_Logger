package com.example.brandon.habitlogger.OverviewActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitActivity.CalendarFragment;
import com.example.brandon.habitlogger.OverviewActivity.CalendarView.CalendarViewAdapter;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.data.SessionEntriesSample;


public class OverviewCalendarFragment extends Fragment implements UpdateHabitDataSampleInterface {

    private CalendarFragment.Listener listener;
    CallbackInterface callbackInterface;


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


        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        RecyclerView calendarViewContainer = (RecyclerView) v.findViewById(R.id.calendar_view_container);

        SessionEntriesSample sample = callbackInterface.getDataSample().getSessionEntriesSample();
        int defaultColor = ContextCompat.getColor(getContext(), R.color.colorAccent);

        CalendarViewAdapter mAdapter = new CalendarViewAdapter(sample, defaultColor, getContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        calendarViewContainer.setLayoutManager(layoutManager);
        calendarViewContainer.setItemAnimator(new DefaultItemAnimator());
        calendarViewContainer.setAdapter(mAdapter);

        return v;
    }
    //endregion

    //region // Attach - onStop
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callbackInterface = (CallbackInterface)context;
        callbackInterface.addCallback(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        callbackInterface.removeCallback(this);
    }
    //endregion

    //endregion

    @Override
    public void updateDataSample(HabitDataSample data) {

    }

}