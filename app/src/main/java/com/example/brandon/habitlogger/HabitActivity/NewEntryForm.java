package com.example.brandon.habitlogger.HabitActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.TimeDisplay;
import com.example.brandon.habitlogger.databinding.DialogNewEntryBinding;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NewEntryForm extends DialogFragment {
    private static final String ENTRY = "ENTRY";
    private OnFinishedListener onFinishedListener;

    DialogNewEntryBinding ui;

    private SessionEntry entry;
    private boolean modify = false;

    public void setOnFinishedListener(OnFinishedListener listener) {
        onFinishedListener = listener;
    }

    public interface OnFinishedListener {
        void onFinishedWithResult(SessionEntry entry);

        void onDeleteClicked(SessionEntry entry);
    }

    public static NewEntryForm newInstance(SessionEntry entry) {
        NewEntryForm form = new NewEntryForm();
        Bundle args = new Bundle();
        args.putSerializable(ENTRY, entry);
        form.setArguments(args);

        return form;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.entry = new SessionEntry(System.currentTimeMillis(), 0, "");

        if (getArguments() != null) {
            if (getArguments().containsKey(ENTRY)) {
                this.entry = (SessionEntry) getArguments().getSerializable(ENTRY);
                this.modify = true;
            }
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);

        ui = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_new_entry, null, false);

        if (!modify) {
            builder.setTitle("Create Entry");
            builder.setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SessionEntry entry = getEntry();
                    onFinishedListener.onFinishedWithResult(entry);
                    dismiss();
                }
            });
        }
        else {
            builder.setTitle("Modify Entry");

            builder.setPositiveButton("Update Entry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SessionEntry entry = getEntry();

                    if (onFinishedListener != null) {
                        onFinishedListener.onFinishedWithResult(entry);
                    }

                    dismiss();
                }
            });

            builder.setNegativeButton("Delete Entry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (onFinishedListener != null) {
                        onFinishedListener.onDeleteClicked(entry);
                    }
                }
            });
        }

        ui.note.setText(entry.getNote());
        ui.startingTime.setText(entry.getStartTimeAsString("h:mm a"));
        ui.sessionDate.setText(entry.getStartTimeAsString(new PreferenceChecker(getContext()).stringGetDateFormat()));

        TimeDisplay time = new TimeDisplay(entry.getDuration());

        ui.hours.setMinValue(0);
        ui.hours.setMaxValue(8760);

        ui.minutes.setMinValue(0);
        ui.minutes.setMaxValue(59);

        ui.seconds.setMinValue(0);
        ui.seconds.setMaxValue(59);

        ui.hours.setValue((int) time.hours);
        ui.minutes.setValue((int) time.minutes);
        ui.seconds.setValue((int) time.seconds);

        ui.sessionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartingDateDialog dialog = new StartingDateDialog();

                Bundle args = new Bundle();
                args.putLong("date_in_milliseconds", entry.getStartTime());
                dialog.setArguments(args);

                dialog.setOnFinishedListener(new StartingDateDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(String monthName, int day, int year, long time) {
                        entry.setStartTime(time);
                        String dateString = entry.getStartTimeAsString(new PreferenceChecker(getContext()).stringGetDateFormat());
                        ui.sessionDate.setText(dateString);
                    }
                });
                dialog.show(getFragmentManager(), "get-date");
            }
        });

        ui.startingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hours = entry.getStartingTimeHours();
                int minutes = entry.getStartingTimeMinutes();

                StartingTimeDialog dialog = StartingTimeDialog.newInstance(hours, minutes);

                dialog.setOnFinishedListener(new StartingTimeDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(int hours, int minutes) {
                        entry.setStartingHour(hours);
                        entry.setStartingMinute(minutes);

                        ui.startingTime.setText(entry.getStartTimeAsString("h:mm a"));
                    }
                });
                dialog.show(getFragmentManager(), "get-date");
            }
        });

        builder.setView(ui.getRoot());

        return builder.create();
    }

    private SessionEntry getEntry() {
        long duration = 0;

        duration += ui.hours.getValue() * DateUtils.HOUR_IN_MILLIS;
        duration += ui.minutes.getValue() * DateUtils.MINUTE_IN_MILLIS;
        duration += ui.seconds.getValue() * DateUtils.SECOND_IN_MILLIS;
        entry.setDuration(duration);

        String note = ui.note.getText().toString();
        entry.setNote(note);

        return entry;
    }
}
