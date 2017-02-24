package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Brandon on 12/6/2016.
 * An activity to create/modify habit objects
 */

public class ModifyHabitActivity extends AppCompatActivity {

    public static final int NEW_HABIT_RESULT_CODE = 100;
    public static final int EDIT_HABIT_RESULT_CODE = 101;

    Habit oldHabit = null;
    CategorySpinnerAdapter adapter;

    ViewHolder vh;

    class ViewHolder{
        public EditText habitName, habitDescription;
        public Spinner categorySpinner;

        ViewHolder(){
            categorySpinner = (Spinner) findViewById(R.id.spinner_category_selector);
            habitName = (EditText) findViewById(R.id.habit_name);
            habitDescription = (EditText) findViewById(R.id.habit_description);
        }

        void setAdapter(CategorySpinnerAdapter adapter){
            categorySpinner.setAdapter(adapter);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_habit);

        if(getSupportActionBar() != null){
            boolean isEditMode = getIntent().hasExtra("edit");
            getSupportActionBar().setTitle(isEditMode ? R.string.edit_habit_title : R.string.new_habit_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        List<HabitCategory> categories = new HabitDatabase(this).getCategories();
        adapter = new CategorySpinnerAdapter(this, categories);

        vh = new ViewHolder();
        vh.setAdapter(adapter);
        fillInFields(vh);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_modify_habit_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case (R.id.new_habit_confirm): {
                Intent data = new Intent();
                data.putExtra("new_habit", (Serializable) getHabit());
                setResult(RESULT_OK, data);
                finish();
            }break;

            case(android.R.id.home):{
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void fillInFields(ViewHolder vh) {
        if(getIntent().hasExtra("habit")) {
            oldHabit = (Habit) getIntent().getSerializableExtra("habit");
            vh.habitName.setText(oldHabit.getName());
            vh.habitDescription.setText(oldHabit.getDescription());

            vh.categorySpinner.setSelection(adapter.getItemPosition(oldHabit.getCategory()));
        }
    }

    public Habit getHabit() {
        String name = vh.habitName.getText().toString();
        String description = vh.habitDescription.getText().toString();
        HabitCategory category = (HabitCategory) vh.categorySpinner.getSelectedItem();

        if (oldHabit == null) {
            return new Habit(name, description, category, "icon res", null);
        }
        else {
            oldHabit.setName(name);
            oldHabit.setDescription(description);
            oldHabit.setCategory(category);

            return oldHabit;
        }
    }
}
