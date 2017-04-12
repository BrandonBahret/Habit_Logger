package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;

/**
 * Created by Brandon on 3/6/2017.
 * Dialog to edit habits
 */

public class EditHabitDialog extends HabitDialogBase {

    //region (Member attributes)
    private Habit mEditHabit;
    //endregion

    public static EditHabitDialog newInstance(OnFinishedListener listener, Habit editHabit) {
        EditHabitDialog dialog = new EditHabitDialog();

        Bundle args = new Bundle(1);
        args.putSerializable(KEY_HABIT, editHabit);
        dialog.setArguments(args);

        dialog.setOnFinishedListener(listener);
        return dialog;
    }

    public static EditHabitDialog newInstance(OnFinishedListener listener, int accentColor, Habit editHabit) {
        EditHabitDialog dialog = new EditHabitDialog();

        Bundle args = new Bundle(2);
        args.putSerializable(KEY_HABIT, editHabit);
        args.putInt(KEY_COLOR, accentColor);
        dialog.setArguments(args);

        dialog.setOnFinishedListener(listener);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditHabit = (Habit) getArguments().getSerializable(KEY_HABIT);
    }

    @Override
    public Habit getInitialHabit() {
        return mEditHabit;
    }

    @Override
    protected String getPositiveButtonText() {
        return "Update";
    }

    @Override
    protected String getTitle() {
        return "Edit Habit";
    }

}
