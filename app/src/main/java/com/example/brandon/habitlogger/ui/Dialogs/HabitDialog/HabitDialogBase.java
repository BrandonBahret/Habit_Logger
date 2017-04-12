package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.databinding.DialogHabitFormBinding;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog.CategorySpinnerAdapter;

import java.util.List;

/**
 * Created by Brandon on 3/31/2017.
 * Base class for habit new/edit dialogs
 */

public abstract class HabitDialogBase extends DialogFragment {

    //region (Member attributes)
    protected static final String KEY_HABIT = "KEY_HABIT";
    protected static final String KEY_COLOR = "KEY_COLOR";

    protected Habit mHabit;
    Integer mAccentColor = 0;
    DialogHabitFormBinding ui;
    protected CategorySpinnerAdapter mAdapter;
    //endregion

    //region Code responsible for providing an interface
    private OnFinishedListener onFinishedListener;

    public interface OnFinishedListener {
        void onFinishedWithResult(Habit habit);
    }

    public void setOnFinishedListener(OnFinishedListener listener) {
        onFinishedListener = listener;
    }
    //endregion

    public abstract Habit getInitialHabit();

    protected abstract String getPositiveButtonText();

    protected abstract String getTitle();

    //region Methods responsible for creating the dialog


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            mAccentColor = getArguments().getInt(KEY_COLOR, 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mHabit = getInitialHabit();

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        ui = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_habit_form, null, false);

        List<HabitCategory> categories = new HabitDatabase(getContext()).getCategories();
        mAdapter = new CategorySpinnerAdapter(getContext(), categories);
        ui.spinnerCategorySelector.setAccentColor(mAccentColor);
        ui.spinnerCategorySelector.setAdapter(mAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setTitle(getTitle())
                .setPositiveButton(getPositiveButtonText(), getOnPositiveButtonClickListener())
                .setNegativeButton("Cancel", null);

        ui.habitName.setText(mHabit.getName());
        int categoryPosition = mAdapter.getItemPosition(mHabit.getCategory());
        ui.spinnerCategorySelector.setSelection(categoryPosition);
        ui.habitDescription.setText(mHabit.getDescription());

        builder.setView(ui.getRoot());

        final AlertDialog habitDialog = builder.create();

        if (mAccentColor != 0) {
            habitDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
//                    int color = ContextCompat.getColor(getContext(), R.color.textColorContrastBackground);
                    habitDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mAccentColor);
                    habitDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mAccentColor);
                }
            });
        }

        return habitDialog;
    }

    protected DialogInterface.OnClickListener getOnPositiveButtonClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Habit habit = getHabitFromDialog();
                onFinishedListener.onFinishedWithResult(habit);
            }
        };
    }
    //endregion

    public void setAccentColor(int color) {
        mAccentColor = color;
    }

    public Habit getHabitFromDialog() {
        String name = ui.habitName.getText().toString();
        String description = ui.habitDescription.getText().toString();
        HabitCategory category = (HabitCategory) ui.spinnerCategorySelector.getSelectedItem();

        mHabit.setName(name);
        mHabit.setDescription(description);
        mHabit.setCategory(category);
        return mHabit;
    }
}
