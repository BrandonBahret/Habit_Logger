package com.example.brandon.habitlogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;

/**
 * Created by Brandon on 12/6/2016.
 */

public class NewHabitActivity extends AppCompatActivity {

    public static final int NEW_HABIT_RESULT_CODE = 100;
    public int color = 0xFF9E9E9E;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_habit_activity);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        CharSequence data[] = new CharSequence[]{"Name", "Foo", "Bar"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, data);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        ImageButton colorPicker = (ImageButton)findViewById(R.id.colorPicker);
        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeColorDialogFragment dialog = new ChangeColorDialogFragment();
                dialog.setOnFinishedListener(new ChangeColorDialogFragment.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(int color) {
                        setColor(color);
                    }
                });

                dialog.show(getSupportFragmentManager(), "color-picker");
            }
        });

    }

    public void setColor(int color){
        this.color = color;

        ImageButton colorPicker = (ImageButton)findViewById(R.id.colorPicker);
        colorPicker.setBackgroundColor(color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_habit_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();

        switch (id) {
            case (R.id.new_habit_confirm): {
                Toast.makeText(this, "Confirm Tapped", Toast.LENGTH_SHORT).show();

                Intent data = new Intent();
                data.putExtra("habit", getHabit());
                setResult(RESULT_OK, data);
                finish();

            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    public Habit getHabit(){
        EditText nameView = (EditText) findViewById(R.id.habit_name);
        String name = nameView.getText().toString();

        EditText descriptionView = (EditText) findViewById(R.id.habit_description);
        String description = descriptionView.getText().toString();

        EditText categoryView = (EditText) findViewById(R.id.habit_category);
        String categoryName = categoryView.getText().toString();

        return new Habit(name, description, new HabitCategory(this.color, categoryName), null, "icon res");
    }
}
