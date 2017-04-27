package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog;

import android.content.Context;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;

/**
 * Created by Brandon on 3/6/2017.
 * Dialog to create new habits
 */

public class NewHabitDialog extends HabitDialogBase {

    //region Code responsible for providing an interface
    private OnFinishedListener onFinishedListener;

    public interface OnFinishedListener {
        void onNewHabitCreated(Habit newHabit);
    }
    //endregion

    public static NewHabitDialog newInstance() {
        return new NewHabitDialog();
    }

    //region [ ---- Methods responsible for handling the dialog lifecycle ---- ]

    //region (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NewHabitDialog.OnFinishedListener) {
            onFinishedListener = (NewHabitDialog.OnFinishedListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFinishedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFinishedListener = null;
    }
    //endregion -- end --

    //region Methods to help create the dialog
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
    //endregion -- end --

    //endregion [ -------- end -------- ]

    @Override
    void onHabitDialogFinished(Habit newHabit) {
        onFinishedListener.onNewHabitCreated(newHabit);
    }

}
