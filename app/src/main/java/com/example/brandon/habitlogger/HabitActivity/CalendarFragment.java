package com.example.brandon.habitlogger.HabitActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.ui.CalendarView.CalendarViewAdapter;

public class CalendarFragment extends Fragment implements CallbackInterface.IUpdateEntries {

    //region (Member attributes)
    CallbackInterface callbackInterface;

    private RecyclerView mCalendarViewContainer;
    private CalendarViewAdapter mAdapter;
    private int mDefaultColor;
    //endregion

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    //region Methods responsible for handling fragment lifecycle
    //region (onAttach - onDestroy)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callbackInterface = (CallbackInterface)context;
        callbackInterface.addUpdateEntriesCallback(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackInterface.removeUpdateEntriesCallback(this);
    }
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        mDefaultColor = callbackInterface.getDefaultColor();
        mCalendarViewContainer = (RecyclerView) v.findViewById(R.id.calendar_view_container);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCalendarViewContainer.setLayoutManager(layoutManager);
        mCalendarViewContainer.setItemAnimator(new DefaultItemAnimator());
        updateEntries(callbackInterface.getSessionEntries());

        return v;
    }
    //endregion

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {
        if (!dataSample.isEmpty()) {
            mAdapter = new CalendarViewAdapter(callbackInterface.getSessionEntries(), mDefaultColor, getContext());
            mCalendarViewContainer.setAdapter(mAdapter);
        }
    }
}