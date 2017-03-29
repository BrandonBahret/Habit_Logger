package com.example.brandon.habitlogger.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Brandon on 1/16/2017.
 * Dialog to set dates
 */

public class MyDatePickerDialog extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {

    //region (Member attributes)
    private int mHour;
    private int mMinute;
    private int mSecond;
    //endregion

    //region Code responsible for providing an interface
    private OnFinishedListener mOnFinishedListener;

    public interface OnFinishedListener {
        void onFinished(long time);
    }

    public void setOnFinishedListener(OnFinishedListener listener) {
        mOnFinishedListener = listener;
    }
    //endregion

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Bundle args = getArguments();

        long minTime = args.getLong("date_min", -1);
        long maxTime = args.getLong("date_max", -1);
        long time = args.getLong("date_in_milliseconds", minTime);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mSecond = c.get(Calendar.SECOND);

        // Create a new instance of DatePickerDialog and return it
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);

        if (minTime != -1)
            dialog.getDatePicker().setMinDate(minTime);

        if (maxTime != -1)
            dialog.getDatePicker().setMaxDate(maxTime);

        return dialog;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if (mOnFinishedListener != null) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);

            // Maintain the same time of day as provided
            c.set(Calendar.HOUR_OF_DAY, mHour);
            c.set(Calendar.MINUTE, mMinute);
            c.set(Calendar.SECOND, mSecond);

            long time = c.getTimeInMillis();
            mOnFinishedListener.onFinished(time);
        }
    }
}