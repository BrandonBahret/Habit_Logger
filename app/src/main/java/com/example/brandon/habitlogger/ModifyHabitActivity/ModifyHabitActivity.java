package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.databinding.ActivityModifyHabitBinding;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Brandon on 12/6/2016.
 * An activity to create/modify habit objects
 */

public class ModifyHabitActivity extends AppCompatActivity {

    Habit habitResult;

    ActivityModifyHabitBinding ui;
    private CategorySpinnerAdapter adapter;

    public static class OutputBundleKeys {
        public static final String RESULT_HABIT = "RESULT_HABIT";
    }

    public static class InputBundleKeys {
        public static final String HABIT_TO_EDIT = "HABIT_TO_EDIT";
    }

    //region // Methods responsible for handling the lifecycle of the activity.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_modify_habit);

        List<HabitCategory> categories = new HabitDatabase(this).getCategories();
        adapter = new CategorySpinnerAdapter(this, categories);
        ui.spinnerCategorySelector.setAdapter(adapter);

        if (getSupportActionBar() != null) {
            boolean isEditMode = getIntent().hasExtra(InputBundleKeys.HABIT_TO_EDIT);
            if (isEditMode) {
                habitResult = (Habit) getIntent().getSerializableExtra(InputBundleKeys.HABIT_TO_EDIT);
                updateUI(habitResult);
            }
            else {
                habitResult = new Habit(ModifyHabitActivity.this);
            }
            getSupportActionBar().setTitle(isEditMode ? R.string.edit_habit_title : R.string.new_habit_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_modify_habit_activity, menu);
        return true;
    }

    //endregion // Methods responsible for handling the lifecycle of the activity.

    //region // Methods responsible for updating the ui.

    public void updateUI(Habit habit) {
        ui.habitName.setText(habit.getName());
        ui.habitDescription.setText(habit.getDescription());
        ui.spinnerCategorySelector.setSelection(adapter.getItemPosition(habit.getCategory()));
    }

    //endregion // Methods responsible for updating the ui.

    //region // Methods responsible for handling events.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case (R.id.new_habit_confirm): {
                Intent data = new Intent();
                data.putExtra(OutputBundleKeys.RESULT_HABIT, (Serializable) getHabit());
                setResult(RESULT_OK, data);
                finish();
            }
            break;

            case (android.R.id.home): {
                finish();
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion // Methods responsible for handling events.

    private Habit getHabit() {
        String name = ui.habitName.getText().toString();
        String description = ui.habitDescription.getText().toString();
        HabitCategory category = (HabitCategory) ui.spinnerCategorySelector.getSelectedItem();

        habitResult.setName(name);
        habitResult.setDescription(description);
        habitResult.setCategory(category);
        return habitResult;
    }
}
