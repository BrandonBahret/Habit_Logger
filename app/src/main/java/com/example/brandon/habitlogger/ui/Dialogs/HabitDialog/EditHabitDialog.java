package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.brandon.habitlogger.data.DataModels.Habit;

/**
 * Created by Brandon on 3/6/2017.
 * Dialog to edit habits
 */

public class EditHabitDialog extends HabitDialogBase {

    //region (Member attributes)
    private Habit mEditHabit;
    private Habit mOldHabit;
    //endregion

    //region Code responsible for providing an interface
    private OnFinishedListener onFinishedListener;

    public interface OnFinishedListener {
        void onUpdateHabit(Habit oldHabit, Habit newHabit);
    }
    //endregion

    //region Methods responsible for getting instances of the dialog
    public static EditHabitDialog newInstance(Habit editHabit) {
        EditHabitDialog dialog = new EditHabitDialog();

        Bundle args = new Bundle(1);
        args.putSerializable(KEY_HABIT, editHabit);
        dialog.setArguments(args);

        return dialog;
    }

    public static EditHabitDialog newInstance(int accentColor, Habit editHabit) {
        EditHabitDialog dialog = new EditHabitDialog();

        Bundle args = new Bundle(2);
        args.putSerializable(KEY_HABIT, editHabit);
        args.putInt(KEY_COLOR, accentColor);
        dialog.setArguments(args);

        return dialog;
    }
    //endregion -- end --

    //region [ ---- Methods responsible for handling the dialog lifecycle ---- ]

    //region (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditHabitDialog.OnFinishedListener) {
            onFinishedListener = (EditHabitDialog.OnFinishedListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement EditHabitDialog.OnFinishedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFinishedListener = null;
    }
    //endregion -- end --

    //region Methods responsible for creating the dialog
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditHabit = (Habit) getArguments().getSerializable(KEY_HABIT);
        mOldHabit = Habit.duplicate(mEditHabit);
    }
    //endregion -- end --

    //endregion [ -------- end -------- ]

    @Override
    void onHabitDialogFinished(Habit newHabit) {
        onFinishedListener.onUpdateHabit(mOldHabit, newHabit);
    }

}
