package com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog;

import android.os.Bundle;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NewEntryForm extends EntryFormDialogBase {

    public static NewEntryForm newInstance() {
        return new NewEntryForm();
    }

    public static NewEntryForm newInstance(int accentColor) {
        NewEntryForm form = new NewEntryForm();

        Bundle args = new Bundle();
        args.putInt(EntryFormDialogBase.KEY_COLOR, accentColor);
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
