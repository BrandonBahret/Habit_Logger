package com.example.brandon.habitlogger.HabitActivity;

import android.os.Bundle;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NewEntryForm extends EntryFormDialogBase {

    public static NewEntryForm newInstance(SessionEntry entry) {
        NewEntryForm form = new NewEntryForm();

        Bundle args = new Bundle();
        args.putSerializable(EntryFormDialogBase.KEY_ENTRY, entry);
        form.setArguments(args);

        return form;
    }

    @Override
    String getTitle() {
        return "New Entry";
    }

    @Override
    String getPositiveButtonText() {
        return "Add Entry";
    }

    @Override
    String getNegativeButtonText() {
        return null;
    }

}
