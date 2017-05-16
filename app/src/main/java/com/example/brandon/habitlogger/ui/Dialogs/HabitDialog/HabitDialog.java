package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.databinding.DialogHabitFormBinding;
import com.example.brandon.habitlogger.ui.Events.IStateContainer;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog.CategoryDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog.CategorySpinnerAdapter;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog.SelectCategoryDialog;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Brandon on 5/12/2017.
 * Dialog used for manipulating Habits in the database.
 */

public class HabitDialog extends DialogFragment {

    private class DialogState implements IStateContainer {
        int accentColor = 0;
        String title = "Habit Dialog";
        String positiveText = "Confirm", negativeText = "Cancel";
        Habit habit = null;
        public Habit initHabit;

        @Override
        public void saveState(@NonNull Bundle outState) {
            outState.putString("title", title);
            outState.putString("positiveText", positiveText);
            outState.putString("negativeText", negativeText);
            outState.putInt("accentColor", accentColor);
            outState.putSerializable("habit", habit);
            outState.putSerializable("initHabit", initHabit);
        }

        @Override
        public void restoreState(@NonNull Bundle savedInstanceState) {
            title = savedInstanceState.getString("title");
            positiveText = savedInstanceState.getString("positiveText");
            negativeText = savedInstanceState.getString("negativeText");
            accentColor = savedInstanceState.getInt("accentColor");

            Serializable serializedHabit = savedInstanceState.getSerializable("habit");
            if (serializedHabit != null) habit = (Habit) serializedHabit;

            serializedHabit = savedInstanceState.getSerializable("initHabit");
            if (serializedHabit != null) initHabit = (Habit) serializedHabit;
        }
    }

    public interface DialogResult {
        void onResult(Habit initHabit, Habit habit);
    }

    //region (Member attributes)
    DialogState mDialogState = new DialogState();
    DialogHabitFormBinding ui;
    DialogInterface.OnClickListener onPositiveClicked;
    DialogInterface.OnClickListener onNegativeClicked;
    //endregion -- end --

    //region Methods responsible for handling the dialog lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetDialogListeners();

        if (savedInstanceState != null) {
            mDialogState.restoreState(savedInstanceState);
        }

        Fragment fragment = getFragmentManager().findFragmentByTag("category-selector");
        if (fragment != null) {
            SelectCategoryDialog dialog = (SelectCategoryDialog) fragment;
            dialog.setCallbackInterface(spinnerCallback);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        ui = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_habit_form, null, false);

        List<HabitCategory> categories = new HabitDatabase(getContext()).getCategories();
        CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(getContext(), categories);

//        ui.spinnerCategorySelector.setAccentColor(mDialogState.accentColor);
        ui.spinnerCategorySelector.setAdapter(adapter);

        ui.spinnerCategorySelector.setClickListener(onSpinnerClickListener);
//        ui.spinnerCategorySelector.setFragmentManager(getFragmentManager());

        if (mDialogState.habit != null) {
            ui.habitName.setText(mDialogState.habit.getName());
            int categoryPosition = adapter.getItemPosition(mDialogState.habit.getCategory());
            ui.spinnerCategorySelector.setSelection(categoryPosition);
        }

        final AlertDialog myDialog = new AlertDialog.Builder(getContext())
                .setTitle(mDialogState.title)
                .setPositiveButton(mDialogState.positiveText, onPositiveClicked)
                .setNegativeButton(mDialogState.negativeText, onNegativeClicked)
                .setView(ui.getRoot())
                .create();

        if (mDialogState.accentColor != 0) {
            myDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
//                    int color = ContextCompat.getColor(getContext(), R.color.textColorContrastBackground);
                    myDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mDialogState.accentColor);
                    myDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mDialogState.accentColor);
                }
            });
        }

        return myDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDialogState.habit != null) mDialogState.habit = getHabit();
        mDialogState.saveState(outState);
    }
    //endregion -- end --

    //region Methods responsible for handling events
    private CategoryDialog.DialogResult onCategoryDialogFinished = new CategoryDialog.DialogResult() {
        @Override
        public void onResult(HabitCategory initCategory, HabitCategory category) {
            new HabitDatabase(getContext()).addCategoryIfNotExists(category);
            ((CategorySpinnerAdapter) ui.spinnerCategorySelector.getAdapter()).addCategory(category);
            SelectCategoryDialog dialog = (SelectCategoryDialog) getFragmentManager().findFragmentByTag("category-selector");
            dialog.addCategory(category);
        }
    };

    SelectCategoryDialog.DialogCallback spinnerCallback = new SelectCategoryDialog.DialogCallback() {
        @Override
        public void onCategoryListItemClick(int adapterPosition) {
            ui.spinnerCategorySelector.setSelection(adapterPosition);
        }

        @Override
        public void onNewCategoryButtonClick() {
            new CategoryDialog()
                    .setTitle("New Category")
                    .setAccentColor(mDialogState.accentColor)
                    .setPositiveButton("Create", onCategoryDialogFinished)
                    .show(getFragmentManager(), "create-category");
        }
    };

    View.OnClickListener onSpinnerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            List<HabitCategory> categories = new HabitDatabase(getContext()).getCategories();

            SelectCategoryDialog dialog = new SelectCategoryDialog()
                    .setCategories(categories);

            if (mDialogState.accentColor != 0)
                dialog.setAccentColor(mDialogState.accentColor);

            dialog.setCallbackInterface(spinnerCallback);
            dialog.show(getFragmentManager(), "category-selector");
        }
    };
    //endregion -- end --

    //region Getters {}
    public Habit getHabit() {
        String name = ui.habitName.getText().toString();
        HabitCategory category = (HabitCategory) ui.spinnerCategorySelector.getSelectedItem();

        if (mDialogState.habit == null) mDialogState.habit = new Habit();

        mDialogState.habit.setName(name);
        mDialogState.habit.setCategory(category);

        return mDialogState.habit;
    }
    //endregion

    //region Setters {}
    public HabitDialog setTitle(String title) {
        mDialogState.title = title;
        return this;
    }

    public HabitDialog setInitHabit(Habit habit) {
        mDialogState.initHabit = Habit.duplicate(habit);
        mDialogState.habit = habit;
        return this;
    }

    public HabitDialog setAccentColor(@NonNull Integer color) {
        mDialogState.accentColor = color;
        return this;
    }

    public HabitDialog setPositiveButton(String title, final DialogResult listener) {
        if (title != null)
            mDialogState.positiveText = title;

        onPositiveClicked = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null)
                    listener.onResult(mDialogState.initHabit, getHabit());
            }
        };
        return this;
    }

    public HabitDialog setNegativeButton(String title, final DialogResult listener) {
        if (title != null)
            mDialogState.negativeText = title;

        onNegativeClicked = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null)
                    listener.onResult(mDialogState.initHabit, getHabit());
            }
        };
        return this;
    }

    private void resetDialogListeners() {
        Fragment dialog = getFragmentManager().findFragmentByTag("create-category");

        if (dialog != null) {
            CategoryDialog categoryDialog = (CategoryDialog) dialog;
            categoryDialog.setPositiveButton(null, onCategoryDialogFinished);
        }
    }
    //endregion -- end --

}
