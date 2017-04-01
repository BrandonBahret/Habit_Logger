package com.example.brandon.habitlogger.HabitActivity;

import android.os.Bundle;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;

/**
 * Created by Brandon on 3/29/2017.
 * Class for editing entries.
 */

public class EditEntryForm extends EntryFormDialogBase {

    public static EditEntryForm newInstance(SessionEntry entry) {
        EditEntryForm form = new EditEntryForm();

        Bundle args = new Bundle();
        args.putSerializable(EntryFormDialogBase.KEY_ENTRY, entry);
        form.setArguments(args);

        return form;
    }

    @Override
    String getTitle() {
        return "Edit Entry";
    }

    @Override
    String getPositiveButtonText() {
        return "Update Entry";
    }

    @Override
    String getNegativeButtonText() {
        return "Delete Entry";
    }

}