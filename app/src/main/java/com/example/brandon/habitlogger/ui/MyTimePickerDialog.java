package com.example.brandon.habitlogger.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

/**
 * Created by Brandon on 1/16/2017.
 * Dialog to get time.
 */

public class MyTimePickerDialog extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public static final String KEY_HOURS = "KEY_HOURS";
    public static final String KEY_MINUTES = "KEY_MINUTES";

    //region Code responsible for providing an interface
    private OnFinishedListener mFinishedListener;

    public interface OnFinishedListener{
        void onFinishedWithResult(int hours, int minutes);
    }

    public void setOnFinishedListener(OnFinishedListener listener){
        mFinishedListener = listener;
    }
    //endregion

    public static MyTimePickerDialog newInstance(int hours, int minutes) {
        MyTimePickerDialog dialog = new MyTimePickerDialog();

        Bundle args = new Bundle();
        args.putInt(KEY_HOURS, hours);
        args.putInt(KEY_MINUTES, minutes);
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Bundle args = getArguments();
        int hours = args.getInt(KEY_HOURS, 0);
        int minutes = args.getInt(KEY_MINUTES, 0);

        // Create a new instance of DatePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hours, minutes, false);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
        if(mFinishedListener != null)
            mFinishedListener.onFinishedWithResult(hours, minutes);
    }
}