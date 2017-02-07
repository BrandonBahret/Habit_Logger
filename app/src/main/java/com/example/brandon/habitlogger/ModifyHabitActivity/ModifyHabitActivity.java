package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;

import java.io.Serializable;

/**
 * Created by Brandon on 12/6/2016.
 */

public class ModifyHabitActivity extends AppCompatActivity {

    public static final int NEW_HABIT_RESULT_CODE = 100;
    public static final int EDIT_HABIT_RESULT_CODE = 101;
    public int color = 0xFF9E9E9E;

    EditText habitName, habitDescription, habitCategory;
    ImageButton colorPicker, iconPicker;
    Spinner categorySpinner;

    Habit oldHabit = null;

    HabitDatabase habitDatabase;

    boolean isEditMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_habit_activity);

        habitDatabase = new HabitDatabase(this, null, false);

        isEditMode = getIntent().hasExtra("edit");

        categorySpinner  = (Spinner)     findViewById(R.id.category_spinner);
        habitName        = (EditText)    findViewById(R.id.habit_name);
        habitDescription = (EditText)    findViewById(R.id.habit_description);
        habitCategory    = (EditText)    findViewById(R.id.habit_category);
        colorPicker      = (ImageButton) findViewById(R.id.colorPicker);
        iconPicker       = (ImageButton) findViewById(R.id.icon_picker);

        CharSequence data[] = new CharSequence[]{}; //TODO habitDatabase.getCategories();
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        setColorOfColorPickerButton(color);
        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeColorDialogFragment dialog = new ChangeColorDialogFragment();
                dialog.setOnFinishedListener(new ChangeColorDialogFragment.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(int color) {
                        setColorOfColorPickerButton(color);
                    }
                });

                dialog.show(getSupportFragmentManager(), "color-picker");
            }
        });

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null){
            if(isEditMode) {
                toolbar.setTitle(R.string.edit_habit_title);
                setupEditMode();
            }
            else{
                toolbar.setTitle(R.string.new_habit_title);
            }
        }

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
                data.putExtra("new_habit", (Serializable)getHabit());
                setResult(RESULT_OK, data);
                finish();
            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setupEditMode() {
        oldHabit = (Habit)getIntent().getSerializableExtra("habit");

        habitName.setText(oldHabit.getName());
        habitDescription.setText(oldHabit.getDescription());
        habitCategory.setText(oldHabit.getCategory().getName());
        setColorOfColorPickerButton(oldHabit.getCategory().getColorAsInt());
    }

    public void setColorOfColorPickerButton(int color){
        this.color = color;

        ImageButton colorPicker = (ImageButton)findViewById(R.id.colorPicker);
        Drawable background = getDrawable(R.drawable.circle_background);
        if(background == null){
            throw new Error("Failed to get background drawable @ line 97 ModifyHabitActivity.java");
        }

        background.setColorFilter(color, PorterDuff.Mode.SRC);
        colorPicker.setBackground(background);
    }

    public Habit getHabit() {
        String name = habitName.getText().toString();
        String description = habitDescription.getText().toString();
        String categoryName = habitCategory.getText().toString();

        if (oldHabit == null) {
            return new Habit(name, description,
                    new HabitCategory(this.color, categoryName),
                    null, "icon res");
        } else {
            oldHabit.setName(name);
            oldHabit.setDescription(description);
            oldHabit.setCategory(new HabitCategory(this.color, categoryName));
            // oldHabit.setIconResId("icon res");

            return oldHabit;
        }
    }
}
