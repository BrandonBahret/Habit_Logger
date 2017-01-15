package com.example.brandon.habitlogger.HabitActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;

public class NewEntryForm extends DialogFragment {
    private static final String ENTRY = "ENTRY";
    private OnFinishedListener onFinishedListener;

    private View content;

    private SessionEntry entry;

    public void setOnFinishedListener(OnFinishedListener listener){
        onFinishedListener = listener;
    }

    public interface OnFinishedListener{
        void onFinishedWithResult(SessionEntry entry);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if(getArguments().containsKey(ENTRY)) {
                this.entry = (SessionEntry) getArguments().getSerializable(ENTRY);
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View dialogView = View.inflate(getContext(), R.layout.modify_entry_layout, null);
        builder.setCancelable(true);

        if (entry == null) {
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
                    onFinishedListener.onFinishedWithResult(entry);
                    dismiss();
                }
            });

            builder.setNegativeButton("Delete Entry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // onFinishedListener.onFinishedWithResult(null);
                }
            });

            EditText note = (EditText)dialogView.findViewById(R.id.entry_note);
            note.setText(entry.getNote());

            EditText startingTime = (EditText)dialogView.findViewById(R.id.entry_time);
            startingTime.setText(entry.getStartTimeAsString("h:mm a"));

            EditText date = (EditText)dialogView.findViewById(R.id.entry_date);
            date.setText(entry.getStartTimeAsString(new PreferenceChecker(getContext()).stringGetDateFormat()));

            SessionManager.TimeDisplay time = new SessionManager.TimeDisplay(entry.getDuration());

            EditText hours = (EditText)dialogView.findViewById(R.id.entry_hours);
            hours.setText(String.valueOf(time.hours));

            EditText minutes = (EditText)dialogView.findViewById(R.id.entry_minutes);
            minutes.setText(String.valueOf(time.minutes));

            EditText seconds = (EditText)dialogView.findViewById(R.id.entry_seconds);
            seconds.setText(String.valueOf(time.seconds));
        }

        builder.setView(dialogView);

        return builder.create();
    }

    private SessionEntry getEntry() {
        return null;
    }
}
