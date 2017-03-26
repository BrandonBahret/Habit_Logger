package com.example.brandon.habitlogger.DataExportHelpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;

/**
 * Created by Brandon on 1/23/2017.
 * Empty activity to process broadcast receiver commands to export data via intent picker.
 */

public class LocalExportActivity extends AppCompatActivity {
    LocalDataExportManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();

        if (getIntent() != null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
            String receivedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            manager = new LocalDataExportManager(this);
            manager.exportHabit(Habit.fromCSV(receivedText), true);
        }
    }
}