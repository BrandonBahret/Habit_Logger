package com.example.brandon.habitlogger.HabitActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;

import java.util.List;

public class CalendarFragment extends Fragment implements UpdateEntriesInterface {
    private CalendarView calendar;
    private int menuRes = R.menu.menu_habit;

    private Listener listener;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public void setMenuRes(int res) {
        this.menuRes = res;
    }

    public interface Listener{
        void onDateClicked(int year, int month, int dayOfMonth);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(this.menuRes, menu);
        menu.findItem(R.id.search).setVisible(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendar = (CalendarView)v.findViewById(R.id.calendarView);

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                if(listener != null)
                    listener.onDateClicked(year, month, dayOfMonth);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        CallbackInterface callbackInterface = (CallbackInterface)context;
        callbackInterface.addCallback(this);
    }

    @Override
    public void updateEntries(List<SessionEntry> sessionEntries, long dateFrom, long dateTo) {

    }
}