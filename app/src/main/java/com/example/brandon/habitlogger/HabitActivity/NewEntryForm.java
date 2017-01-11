package com.example.brandon.habitlogger.HabitActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;

public class NewEntryForm extends DialogFragment {
    private OnFinishedListener onFinishedListener;

    private View content;

    public void setOnFinishedListener(OnFinishedListener listener){
        onFinishedListener = listener;
    }

    public interface OnFinishedListener{
        void onFinishedWithResult(SessionEntry entry);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Create Entry");
        builder.setCancelable(true);
        builder.setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SessionEntry entry = getEntry();
                onFinishedListener.onFinishedWithResult(entry);
                dismiss();
            }
        });

        builder.setView(R.layout.modify_entry_layout);

        return builder.create();
    }

    private SessionEntry getEntry() {
        return null;
    }
}
