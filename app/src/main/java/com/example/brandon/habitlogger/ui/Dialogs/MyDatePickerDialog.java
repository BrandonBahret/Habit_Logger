package com.example.brandon.habitlogger.ui.Dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.widget.DatePicker;

import com.example.brandon.habitlogger.R;

import java.util.Calendar;

/**
 * Created by Brandon on 1/16/2017.
 * Dialog to set dates
 */

public class MyDatePickerDialog extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {

    public static final String KEY_DATE_MIN = "date_min";
    public static final String KEY_DATE_MAX = "date_max";
    public static final String KEY_DATE_MILLIS = "date_in_milliseconds";
    public static final String KEY_COLOR_ACCENT = "color_accent";

    //region (Member attributes)
    private int mHour;
    private int mMinute;
    private int mSecond;

    private Integer mAccentColor = null;
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

    //region Methods to get instances of the dialog
    public static MyDatePickerDialog newInstance(long dateMin, long dateMax, long dateInMillis) {
        MyDatePickerDialog dialog = new MyDatePickerDialog();

        Bundle args = new Bundle();
        args.putLong(KEY_DATE_MIN, Math.max(dateMin, DateUtils.DAY_IN_MILLIS));
        args.putLong(KEY_DATE_MAX, dateMax);
        args.putLong(KEY_DATE_MILLIS, dateInMillis);
        dialog.setArguments(args);

        return dialog;
    }

    public static MyDatePickerDialog newInstance(long dateMin, long dateInMillis) {
        MyDatePickerDialog dialog = new MyDatePickerDialog();

        Bundle args = new Bundle();
        args.putLong(KEY_DATE_MIN, Math.max(dateMin, DateUtils.DAY_IN_MILLIS));
        args.putLong(KEY_DATE_MAX, System.currentTimeMillis());
        args.putLong(KEY_DATE_MILLIS, dateInMillis);
        dialog.setArguments(args);

        return dialog;
    }

    public static MyDatePickerDialog newInstance(long dateMin, long dateMax, int accentColor, long dateInMillis) {
        MyDatePickerDialog dialog = new MyDatePickerDialog();

        Bundle args = new Bundle();
        args.putLong(KEY_DATE_MIN, Math.max(dateMin, DateUtils.DAY_IN_MILLIS));
        args.putLong(KEY_DATE_MAX, dateMax);
        args.putLong(KEY_DATE_MILLIS, dateInMillis);
        args.putInt(KEY_COLOR_ACCENT, accentColor);
        dialog.setArguments(args);

        return dialog;
    }

    public static MyDatePickerDialog newInstance(long dateMin, int accentColor, long dateInMillis) {
        MyDatePickerDialog dialog = new MyDatePickerDialog();

        Bundle args = new Bundle();
        args.putLong(KEY_DATE_MIN, Math.max(dateMin, DateUtils.DAY_IN_MILLIS));
        args.putLong(KEY_DATE_MAX, System.currentTimeMillis());
        args.putLong(KEY_DATE_MILLIS, dateInMillis);
        args.putInt(KEY_COLOR_ACCENT, accentColor);
        dialog.setArguments(args);

        return dialog;
    }
    //endregion -- end --

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Bundle args = getArguments();

        long minTime = args.getLong(KEY_DATE_MIN, -1);
        long maxTime = args.getLong(KEY_DATE_MAX, -1);
        long time = args.getLong(KEY_DATE_MILLIS, minTime);
        mAccentColor = args.getInt(KEY_COLOR_ACCENT);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mSecond = c.get(Calendar.SECOND);

        // Create a new instance of DatePickerDialog and return it
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        final DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), R.style.MyDatePickerDialogTheme, this, mYear, mMonth, mDay);

//        pickerDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                pickerDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mAccentColor);
//                pickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mAccentColor);
//            }
//        });

        if (minTime != -1)
            pickerDialog.getDatePicker().setMinDate(minTime);

        if (maxTime != -1)
            pickerDialog.getDatePicker().setMaxDate(maxTime);

        return pickerDialog;
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