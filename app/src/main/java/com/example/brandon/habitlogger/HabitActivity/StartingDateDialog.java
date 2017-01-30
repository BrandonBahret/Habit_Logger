package com.example.brandon.habitlogger.HabitActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Brandon on 1/16/2017.
 */

public class StartingDateDialog extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {

    private ArrayList<OnFinishedListener> onFinishedListeners = new ArrayList<OnFinishedListener>();

    public void setOnFinishedListener(OnFinishedListener listener){
        onFinishedListeners.add(listener);
    }

    public interface OnFinishedListener{
        void onFinishedWithResult(String monthName, int day, int year, long time);
    }

    int year, month, day, hour, minute, second;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Bundle args = getArguments();

        long minTime = args.getLong("date_min", -1);
        long maxTime = args.getLong("date_max", -1);
        long time = args.getLong("date_in_milliseconds", minTime);

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);

        this.year   = c.get(Calendar.YEAR);
        this.month  = c.get(Calendar.MONTH);
        this.day    = c.get(Calendar.DAY_OF_MONTH);
        this.hour   = c.get(Calendar.HOUR_OF_DAY);
        this.minute = c.get(Calendar.MINUTE);
        this.second = c.get(Calendar.SECOND);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);

        if(minTime > 0)
            dialog.getDatePicker().setMinDate(minTime);

        if(maxTime > 0)
            dialog.getDatePicker().setMaxDate(maxTime);

        return dialog;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        for(OnFinishedListener listener: onFinishedListeners){
            String monthString = String.valueOf(month);//getResources().getStringArray(R.array.month_names)[month];

            Calendar c = Calendar.getInstance();
            c.set(year, month, day);

            c.set(Calendar.HOUR_OF_DAY, this.hour);
            c.set(Calendar.MINUTE, this.minute);
            c.set(Calendar.SECOND, this.second);
            c.set(Calendar.MILLISECOND, 0);

            long time = c.getTimeInMillis();

            listener.onFinishedWithResult(monthString, day, year, time);
        }
    }
}