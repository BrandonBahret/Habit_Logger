package com.example.brandon.habitlogger.HabitActivity;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.util.ArrayList;

/**
 * Created by Brandon on 1/16/2017.
 * Dialog to get time.
 */

public class StartingTimeDialog extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener {

    private ArrayList<OnFinishedListener> onFinishedListeners = new ArrayList<>();

    public void setOnFinishedListener(OnFinishedListener listener){
        onFinishedListeners.add(listener);
    }

    public static StartingTimeDialog newInstance(int hours, int minutes) {
        StartingTimeDialog dialog = new StartingTimeDialog();

        Bundle args = new Bundle();
        args.putInt("hours", hours);
        args.putInt("minutes", minutes);
        dialog.setArguments(args);

        return dialog;
    }

    public interface OnFinishedListener{
        void onFinishedWithResult(int hours, int minutes);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Bundle args = getArguments();

        int hours = args.getInt("hours", 0);
        int minutes = args.getInt("minutes", 0);

        // Create a new instance of DatePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hours, minutes, false);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
        for(OnFinishedListener listener: onFinishedListeners) {
            listener.onFinishedWithResult(hours, minutes);
        }
    }
}