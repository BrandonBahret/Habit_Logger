package com.example.brandon.habitlogger.HabitActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NewEntryForm extends DialogFragment {
    private static final String ENTRY = "ENTRY";
    private OnFinishedListener onFinishedListener;

    private View content;
    EditText note, startingTime, date, hours, minutes, seconds;

    private SessionEntry entry;
    private boolean modify = false;

    public void setOnFinishedListener(OnFinishedListener listener){
        onFinishedListener = listener;
    }

    public interface OnFinishedListener{
        void onFinishedWithResult(SessionEntry entry);

        void onDeleteClicked(SessionEntry entry);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.entry = new SessionEntry(SessionManager.getCurrentTime(), 0, "");

        if (getArguments() != null) {
            if(getArguments().containsKey(ENTRY)) {
                this.entry = (SessionEntry) getArguments().getSerializable(ENTRY);
                this.modify = true;
            }
        }
    }

    public static NewEntryForm newInstance(SessionEntry entry){
        NewEntryForm form = new NewEntryForm();
        Bundle args = new Bundle();
        args.putSerializable(ENTRY, entry);
        form.setArguments(args);

        return form;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);

        View dialogView = View.inflate(getContext(), R.layout.modify_entry_layout, null);
        note = (EditText)dialogView.findViewById(R.id.entry_note);
        startingTime = (EditText)dialogView.findViewById(R.id.entry_time);
        date = (EditText)dialogView.findViewById(R.id.entry_date);
        hours = (EditText)dialogView.findViewById(R.id.entry_hours);
        minutes = (EditText)dialogView.findViewById(R.id.entry_minutes);
        seconds = (EditText)dialogView.findViewById(R.id.entry_seconds);


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
        } else {
            builder.setTitle("Modify Entry");

            builder.setPositiveButton("Update Entry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SessionEntry entry = getEntry();

                    if(onFinishedListener != null) {
                        onFinishedListener.onFinishedWithResult(entry);
                    }

                    dismiss();
                }
            });

            builder.setNegativeButton("Delete Entry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(onFinishedListener != null) {
                        onFinishedListener.onDeleteClicked(entry);
                    }
                }
            });
        }

        note.setText(entry.getNote());
        startingTime.setText(entry.getStartTimeAsString("h:mm a"));
        date.setText(entry.getStartTimeAsString(new PreferenceChecker(getContext()).stringGetDateFormat()));

        SessionManager.TimeDisplay time = new SessionManager.TimeDisplay(entry.getDuration());
        hours.setText(String.valueOf(time.hours));
        minutes.setText(String.valueOf(time.minutes));
        seconds.setText(String.valueOf(time.seconds));

        date.setOnClickListener(new View.OnClickListener() {
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
                        date.setText(dateString);
                    }
                });
                dialog.show(getFragmentManager(), "get-date");
            }
        });

        startingTime.setOnClickListener(new View.OnClickListener() {
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

                        startingTime.setText(entry.getStartTimeAsString("h:mm a"));
                    }
                });
                dialog.show(getFragmentManager(), "get-date");
            }
        });

        builder.setView(dialogView);

        return builder.create();
    }

    private SessionEntry getEntry() {
        long duration = 0;

        duration += Integer.parseInt(hours.getText().toString()) * 60 * 60 * 1000;
        duration += Integer.parseInt(minutes.getText().toString()) * 60 * 1000;
        duration += Integer.parseInt(seconds.getText().toString()) * 1000;
        entry.setDuration(duration);

        String note = this.note.getText().toString();
        entry.setNote(note);

        return entry;
    }
}
