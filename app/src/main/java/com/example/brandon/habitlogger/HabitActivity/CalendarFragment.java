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

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.ui.CalendarView.CalendarViewAdapter;

import java.util.List;

public class CalendarFragment extends Fragment implements UpdateEntriesInterface {
    private Listener listener;
    CallbackInterface callbackInterface;
    private int mMenuRes = R.menu.menu_habit;

    private RecyclerView calendarViewContainer;

    private List<SessionEntry> mSessionEntries;
    private CalendarViewAdapter mAdapter;

    public CalendarFragment() {
        // Required empty public constructor
    }

    //region // Create communication with the parent
    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    public interface Listener{
        void onDateClicked(int year, int month, int dayOfMonth);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarViewContainer = (RecyclerView) v.findViewById(R.id.calendar_view_container);

        mSessionEntries = callbackInterface.getSessionEntries().getSessionEntries();
        mAdapter = new CalendarViewAdapter(callbackInterface.getSessionEntries(), getContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        calendarViewContainer.setLayoutManager(layoutManager);
        calendarViewContainer.setItemAnimator(new DefaultItemAnimator());
        calendarViewContainer.setAdapter(mAdapter);

        return v;
    }

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

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {
        if (calendarViewContainer != null) {
            mSessionEntries = dataSample.getSessionEntries();
            mAdapter = new CalendarViewAdapter(callbackInterface.getSessionEntries(), getContext());
            calendarViewContainer.setAdapter(mAdapter);
        }
    }
}