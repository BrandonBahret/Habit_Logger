package com.example.brandon.habitlogger.ModifyHabitDialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;


/**
 * Created by Brandon on 3/6/2017.
 * Dialog to edit habits
 */

public class EditHabitDialog extends NewHabitDialog {

    //region (Member attributes)
    private static final String SERIALIZED_HABIT = "SERIALIZED_HABIT";
    private Habit mEditHabit;
    //endregion

    public static EditHabitDialog newInstance(OnFinishedListener listener, Habit editHabit) {
        EditHabitDialog dialog = new EditHabitDialog();

        Bundle args = new Bundle(1);
        args.putSerializable(SERIALIZED_HABIT, editHabit);
        dialog.setArguments(args);

        dialog.setOnFinishedListener(listener);
        return dialog;
    }

    //region Methods responsible for creating the dialog
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mEditHabit = (Habit) getArguments().getSerializable(SERIALIZED_HABIT);
    }

    @Override
    public void onBeforeSetView() {
        Habit edit = mHabit;

        this.ui.habitName.setText(edit.getName());
        this.ui.spinnerCategorySelector.setSelection
                (this.adapter.getItemPosition(edit.getCategory()));
        this.ui.habitDescription.setText(edit.getDescription());
    }

    @Override
    protected AlertDialog.Builder getAlertDialogBuilder() {
        return new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setTitle("Edit Habit")
                .setPositiveButton("Update", this.OnCreateHabitButtonClicked)
                .setNegativeButton("Cancel", null);
    }

    @Override
    public Habit getWorkingHabit() {
        return this.mEditHabit;
    }
    //endregion -- end --

}
