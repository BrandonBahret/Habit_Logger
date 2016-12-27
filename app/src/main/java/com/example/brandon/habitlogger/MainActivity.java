package com.example.brandon.habitlogger;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.habitlogger.DataExportHelpers.GoogleDriveDataExportManager;
import com.example.brandon.habitlogger.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseCache;
import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.HabitViewAdapter;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.example.brandon.habitlogger.R.menu.main;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    
    HabitDatabase habitDatabase;
    LocalDataExportManager exportManager;
    GoogleDriveDataExportManager googleDrive;

    List<Habit> habitList = new ArrayList<>();;
    RecyclerView recyclerView;
    HabitViewAdapter habitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newHabit = new Intent(MainActivity.this, NewHabitActivity.class);
                startActivityForResult(newHabit, NewHabitActivity.NEW_HABIT_RESULT_CODE);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = (RecyclerView)findViewById(R.id.habit_recycler_view);
        habitAdapter = new HabitViewAdapter(habitList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(habitAdapter);

        Serializable cache = null;
        if (savedInstanceState != null) {
            cache = savedInstanceState.getSerializable("dataCache");
        }

        habitDatabase = new HabitDatabase(MainActivity.this, cache, true);
        habitDatabase.setOnDatabaseChangeListener(new HabitDatabase.OnDatabaseChange() {
            @Override
            public void onDatabaseChanged() {
                showDatabase();
                Toast.makeText(MainActivity.this, "restored database", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDatabaseClear() {
                habitList.clear();
                habitAdapter.notifyDataSetChanged();
            }
        });

        exportManager = new LocalDataExportManager(MainActivity.this);
        googleDrive = new GoogleDriveDataExportManager(MainActivity.this);
        googleDrive.connect();

        showDatabase();
    }

    private class addJunkData extends AsyncTask<Void, Void, Void>{
        int numberOfCategories, numberOfEntries, numberOfHabits;

        addJunkData(int numberOfCategories, int numberOfEntries, int numberOfHabits){
            this.numberOfCategories = numberOfCategories;
            this.numberOfEntries = numberOfEntries;
            this.numberOfHabits = numberOfHabits;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HabitCategory categories[] = new HabitCategory[numberOfCategories];
            SessionEntry  entries[]    = new SessionEntry[numberOfEntries];
            Habit         habits[]     = new Habit[numberOfHabits];

            int intColors[] = getResources().getIntArray(R.array.colors);
            for(Integer i = 0; i < categories.length; i++){
                categories[i] = new HabitCategory(intColors[i % intColors.length], i.toString());
            }
            habitDatabase.addCategories(categories);

            SessionEntry entry = new SessionEntry(0, 0, "note");
            for(int i = 0; i < entries.length; i++){
                entries[i] = entry;
            }

            for(int i = 0; i < habits.length; i++){
                habits[i] = new Habit(String.valueOf(i), String.valueOf(i),
                        categories[i % numberOfCategories], entries, "");
                habitDatabase.addHabit(habits[i]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            habitDatabase.loadAllContents();
            habitDatabase.notifyChange();
        }
    }

    public void addJunkData(int numberOfCategories, int numberOfEntries, int numberOfHabits){
        new addJunkData(numberOfCategories, numberOfEntries, numberOfHabits).execute();
    }

    public void showDatabase(){
        for(int categoryInd = 0; categoryInd < habitDatabase.getNumberOfCategories(); categoryInd++) {

            long categoryId = habitDatabase.getCategoryIdFromIndex(categoryInd);

            for (int habitInd = 0; habitInd < habitDatabase.getNumberOfHabits(categoryId); habitInd++) {
                long habitId = habitDatabase.getHabitIdFromIndex(categoryId, habitInd);
                Habit habit = habitDatabase.getHabit(habitId);

                habitList.add(habit);
            }
        }

        habitAdapter.notifyDataSetChanged();


//        TextView main = (TextView)findViewById(R.id.text);
//        String dataString = habitDatabase.toString3();
//        main.setText(dataString);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();
        switch(id){
            case(R.id.auth_google_drive):
            {
                googleDrive.connect();
            }break;

            case(R.id.show_license):
            {
                String license = GoogleApiAvailability.getInstance()
                        .getOpenSourceSoftwareLicenseInfo(MainActivity.this);

                TextView mainText = (TextView)findViewById(R.id.text);
                mainText.setText(license);
            }break;

            case(R.id.reset_database):
            {
                habitDatabase.resetDatabase();
            }break;

            case(R.id.save_to_sd):
            {
                exportManager.exportDatabase(true);
                Toast.makeText(this, "Saved database", Toast.LENGTH_SHORT).show();
            }break;

            case(R.id.backup_to_drive):
            {
                googleDrive.backupDatabase();
            }break;

            case(R.id.restore_from_drive):
            {
                googleDrive.restoreDatabase();
            }break;

            case(R.id.add_junk):
            {
                addJunkData(10, 20, 5);
                showDatabase();
            }break;

            case(R.id.add_mound_junk):
            {
                addJunkData(4, 50, 15);
                showDatabase();
            }break;

            case(R.id.add_ton_junk):
            {
                addJunkData(7, 365, 25);
                showDatabase();
            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id){
            case(R.id.home_nav):{
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            }break;

            case(R.id.running_habits_nav):{
                Toast.makeText(this, "Running Habit", Toast.LENGTH_SHORT).show();
            }break;

            case(R.id.overall_stats_nav):{
                Toast.makeText(this, "Overall stats", Toast.LENGTH_SHORT).show();
            }break;

            case(R.id.settings_nav):{
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
            }break;

            case(R.id.about_nav):{
                Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
            }break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case NewHabitActivity.NEW_HABIT_RESULT_CODE:
                {
                    Habit newHabit = (Habit)data.getSerializableExtra("habit");
                    habitDatabase.addHabitAndCategory(newHabit);
                    showDatabase();
                }break;

                case 1: {
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
                    exportManager.importDatabase(true);
                    showDatabase();
                }break;

                case 2:{
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    exportManager.importHabit(filePath, true);
                    showDatabase();
                }break;

                case GoogleDriveDataExportManager.REQUEST_CODE_RESOLUTION:{
                    googleDrive.connect();
                }break;
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("dataCache", habitDatabase.dataCache);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        habitDatabase.dataCache = (DatabaseCache)savedInstanceState.getSerializable("dataCache");
    }
}
