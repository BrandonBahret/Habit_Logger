package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.databinding.DialogNewHabitBinding;

import java.util.List;

/**
 * Created by Brandon on 3/6/2017.
 */

public class NewHabitDialog extends DialogFragment implements DialogInterface.OnClickListener {

    DialogNewHabitBinding ui;

    Habit habitResult;
    protected CategorySpinnerAdapter adapter;

    //region // Get a result from the dialog.
    private OnFinishedListener onFinishedListener;
    private Habit resultHabit;

    public void setOnFinishedListener(OnFinishedListener listener) {
        onFinishedListener = listener;
    }

    public interface OnFinishedListener {
        void onFinishedWithResult(Habit habit);
    }
    //endregion

    //region // Methods to create the dialog.
    public static NewHabitDialog newInstance(OnFinishedListener listener) {
        NewHabitDialog dialog = new NewHabitDialog();
        dialog.setOnFinishedListener(listener);
        return dialog;
    }

    public Habit getWorkingHabit() {
        HabitCategory category = new HabitCategory("#ff000000", getString(R.string.uncategorized));
        return new Habit("", category);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        habitResult = getWorkingHabit();

        LayoutInflater layoutInflater = LayoutInflater.from(getContext()); //getLayoutInflater(savedInstanceState);
        ui = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_new_habit, null, false);

        List<HabitCategory> categories = new HabitDatabase(getContext()).getCategories();
        adapter = new CategorySpinnerAdapter(getContext(), categories);
        ui.spinnerCategorySelector.setAdapter(adapter);

        AlertDialog.Builder builder = getAlertDialogBuilder();

        onBeforeSetView();

        builder.setView(ui.getRoot());
        return builder.create();
    }

    protected AlertDialog.Builder getAlertDialogBuilder() {
        return new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setTitle("New Habit")
                .setPositiveButton("Create", this)
                .setNegativeButton("Cancel", null);
    }


    public void onBeforeSetView() {
        // Empty stub
    }

    //endregion

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Habit habit = getHabitFromDialog();
        onFinishedListener.onFinishedWithResult(habit);
    }

    public Habit getHabitFromDialog() {
        String name = ui.habitName.getText().toString();
        String description = ui.habitDescription.getText().toString();
        HabitCategory category = (HabitCategory) ui.spinnerCategorySelector.getSelectedItem();

        habitResult.setName(name);
        habitResult.setDescription(description);
        habitResult.setCategory(category);
        return habitResult;
    }
}
