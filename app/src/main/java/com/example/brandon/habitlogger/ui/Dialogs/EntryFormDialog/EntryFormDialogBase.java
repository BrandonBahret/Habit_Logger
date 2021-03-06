package com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.databinding.DialogNewEntryBinding;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Dialogs.MyDatePickerDialog;
import com.example.brandon.habitlogger.ui.Dialogs.MyTimePickerDialog;

/**
 * Created by Brandon on 3/29/2017.
 * Base class for creating/editing entries.
 */

public abstract class EntryFormDialogBase extends DialogFragment {

    //region (Member attributes)
    protected static final String KEY_ENTRY = "KEY_ENTRY";
    protected static final String KEY_COLOR = "KEY_COLOR";
    protected SessionEntry mEntry;
    protected Integer mAccentColor = 0;

    DialogNewEntryBinding ui;
    //endregion

    //region Methods responsible for handling the lifetime of the dialog

    abstract String getTitle();

    abstract String getPositiveButtonText();

    abstract String getNegativeButtonText();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            if (getArguments().containsKey(KEY_ENTRY))
                mEntry = (SessionEntry) getArguments().getSerializable(KEY_ENTRY);
            else
                mEntry = new SessionEntry(System.currentTimeMillis(), 0, "");

            if (getArguments().containsKey(KEY_COLOR))
                mAccentColor = getArguments().getInt(KEY_COLOR, 0);
        }

        setListenerOnTimePickerIfShown("get-time");
        setListenerOnDatePickerIfShown("get-date");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        ui = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_new_entry, null, false);
        ui.sessionDate.setOnClickListener(getStartingDateClickListener());
        ui.startingTime.setOnClickListener(getOnStartingTimeClickListener());

        setDurationNumberPickersRanges();
        setDurationNumberPickers();
        setStartingTime();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setTitle(getTitle())
                .setView(ui.getRoot());

        if (getPositiveButtonText() != null)
            builder.setPositiveButton(getPositiveButtonText(), getOnPositiveButtonClickListener());

        if (getNegativeButtonText() != null)
            builder.setNegativeButton(getNegativeButtonText(), getOnNegativeButtonClickListener());

        final AlertDialog entryDialog = builder.create();

        if (mAccentColor != 0) {
            entryDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    entryDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mAccentColor);
                    entryDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mAccentColor);
                }
            });
        }

        return entryDialog;
    }

    //endregion -- end --

    //region Methods responsible for filling in the entry form initially
    private void setDurationNumberPickersRanges() {
        ui.hours.setMinValue(0);
        ui.hours.setMaxValue(8760);

        ui.minutes.setMinValue(0);
        ui.minutes.setMaxValue(59);

        ui.seconds.setMinValue(0);
        ui.seconds.setMaxValue(59);
    }

    private void setDurationNumberPickers() {
        Integer[] time = MyTimeUtils.getTimePortion(mEntry.getDuration());
        ui.hours.setValue(time[0]);
        ui.minutes.setValue(time[1]);
        ui.seconds.setValue(time[2]);
    }

    private void setStartingTime() {
        ui.startingTime.setText(mEntry.stringifyStartingTime("h:mm a"));
        ui.note.setText(mEntry.getNote());

        String dateFormat = new PreferenceChecker(getContext()).stringGetDateFormat();
        ui.sessionDate.setText(mEntry.stringifyStartingTime(dateFormat));
    }
    //endregion

    //region Methods responsible for handling events
    public abstract void onNegativeClicked(SessionEntry removeEntry);

    private DialogInterface.OnClickListener getOnNegativeButtonClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onNegativeClicked(mEntry);
            }
        };
    }

    public abstract void onPositiveClicked(SessionEntry newEntry);

    private DialogInterface.OnClickListener getOnPositiveButtonClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SessionEntry entry = getEntry();
                onPositiveClicked(entry);

                dismiss();
            }
        };
    }

    private View.OnClickListener getOnStartingTimeClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hours = mEntry.getStartingTimeHours();
                int minutes = mEntry.getStartingTimeMinutes();

                MyTimePickerDialog dialog = MyTimePickerDialog.newInstance(hours, minutes);
                setListenerOnTimePicker(dialog);
                dialog.show(getFragmentManager(), "get-time");
            }

        };
    }

    private View.OnClickListener getStartingDateClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDatePickerDialog dialog = MyDatePickerDialog.newInstance(-1, mEntry.getStartingTime());
                setListenerOnDatePicker(dialog);
                dialog.show(getFragmentManager(), "get-date");
            }
        };
    }

    //region Methods to set listeners on dialogs
    private void setListenerOnTimePickerIfShown(String tag){
        MyTimePickerDialog dialog = (MyTimePickerDialog) getFragmentManager().findFragmentByTag(tag);
        setListenerOnTimePicker(dialog);
    }

    private void setListenerOnTimePicker(MyTimePickerDialog picker){
        if (picker != null) {
            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mEntry.setStartingHour(hourOfDay);
                    mEntry.setStartingMinute(minute);

                    ui.startingTime.setText(mEntry.stringifyStartingTime("h:mm a"));
                }
            };

            picker.setOnFinishedListener(listener);
        }
    }

    private void setListenerOnDatePickerIfShown(String tag){
        MyDatePickerDialog dialog = (MyDatePickerDialog) getFragmentManager().findFragmentByTag(tag);
        setListenerOnDatePicker(dialog);
    }

    private void setListenerOnDatePicker(MyDatePickerDialog dialog){
        if(dialog != null){
            dialog.setOnFinishedListener(new MyDatePickerDialog.OnFinishedListener() {
                @Override
                public void onFinished(long time) {
                    mEntry.setStartingTime(time);
                    String dateString = mEntry.stringifyStartingTime(new PreferenceChecker(getContext()).stringGetDateFormat());
                    ui.sessionDate.setText(dateString);
                }
            });
        }
    }
    //endregion -- end --

    //endregion -- end --

    protected SessionEntry getEntry() {
        long duration = 0;

        duration += ui.hours.getValue() * DateUtils.HOUR_IN_MILLIS;
        duration += ui.minutes.getValue() * DateUtils.MINUTE_IN_MILLIS;
        duration += ui.seconds.getValue() * DateUtils.SECOND_IN_MILLIS;
        mEntry.setDuration(duration);

        String note = ui.note.getText().toString();
        mEntry.setNote(note);

        return mEntry;
    }

}
