package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;

/**
 * Created by Brandon on 3/6/2017.
 * Dialog to create new habits
 */

public class NewHabitDialog extends HabitDialogBase {

    public static NewHabitDialog newInstance() {
        return new NewHabitDialog();
    }

    @Override
    public Habit getInitialHabit() {
        HabitCategory category = new HabitCategory("#ff000000", getString(R.string.uncategorized));
        return new Habit("", category);
    }

    @Override
    protected String getPositiveButtonText() {
        return "Create";
    }

    @Override
    protected String getTitle() {
        return "New Habit";
    }
}
