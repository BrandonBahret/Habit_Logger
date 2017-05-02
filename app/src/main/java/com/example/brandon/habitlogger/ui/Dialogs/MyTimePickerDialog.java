package com.example.brandon.habitlogger.ui.Dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.example.brandon.habitlogger.R;

/**
 * Created by Brandon on 1/16/2017.
 * Dialog to get time.
 */

public class MyTimePickerDialog extends DialogFragment {

    //region (Member attributes)
    public static final String KEY_HOURS = "KEY_HOURS";
    public static final String KEY_MINUTES = "KEY_MINUTES";
    //endregion -- end --

    //region Code responsible for providing an interface
    private TimePickerDialog.OnTimeSetListener mOnTimeSetListener;

    public void setOnFinishedListener(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        mOnTimeSetListener = onTimeSetListener;
    }
    //endregion -- end --

    //region Methods for creating new instances
    public static MyTimePickerDialog newInstance(int hours, int minutes) {
        MyTimePickerDialog dialog = new MyTimePickerDialog();

        Bundle args = new Bundle();
        args.putInt(KEY_HOURS, hours);
        args.putInt(KEY_MINUTES, minutes);
        dialog.setArguments(args);

        return dialog;
    }
    //endregion -- end --

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Bundle args = getArguments();
        int hours = args.getInt(KEY_HOURS, 0);
        int minutes = args.getInt(KEY_MINUTES, 0);

        // Create a new instance of DatePickerDialog and return it
        return new TimePickerDialog(getActivity(), R.style.MyTimePickerDialogTheme, mOnTimeSetListener, hours, minutes, false);
    }

}