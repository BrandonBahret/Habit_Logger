package com.example.brandon.habitlogger.HabitActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

public class CalendarFragment extends Fragment implements UpdateEntriesInterface {
    private Listener listener;
    private int mMenuRes = R.menu.menu_habit;

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

//        CalendarView calendar = (CalendarView) v.findViewById(R.id.calendarView);
//        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//            @Override
//            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
//                if(listener != null)
//                    listener.onDateClicked(year, month, dayOfMonth);
//            }
//        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        CallbackInterface callbackInterface = (CallbackInterface)context;
        callbackInterface.addCallback(this);
    }

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {

    }
}