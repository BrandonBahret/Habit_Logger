package com.example.brandon.habitlogger;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import com.example.brandon.habitlogger.SessionManager.SessionManager;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.brandon.habitlogger.R.menu.main;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Preferences
    boolean showCurrentSessions, showCurrentSessionsAlways;

    SessionManager sessionManager;
    CardView currentSession;

    HabitDatabase habitDatabase;
    LocalDataExportManager exportManager;
    GoogleDriveDataExportManager googleDrive;

    List<Habit> habitList = new ArrayList<>();;
    RecyclerView habitCardContainer;
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

        Serializable cache = null;
        if (savedInstanceState != null) {
            cache = savedInstanceState.getSerializable("dataCache");
        }

        habitDatabase  = new HabitDatabase(MainActivity.this, cache, true);
        sessionManager = new SessionManager(this);
        exportManager  = new LocalDataExportManager(MainActivity.this);
        googleDrive    = new GoogleDriveDataExportManager(MainActivity.this);
        googleDrive.connect();

        habitDatabase.setOnDatabaseChangeListener(new HabitDatabase.OnDatabaseChange() {
            @Override
            public void onDatabaseChanged() {
                showDatabase();
            }

            @Override
            public void onDatabaseClear() {
                habitList.clear();
                habitAdapter.notifyDataSetChanged();
            }
        });

        currentSession      = (CardView)findViewById(R.id.current_sessions_card);
        habitCardContainer  = (RecyclerView)findViewById(R.id.habit_recycler_view);

        HabitViewAdapter.MenuItemClickListener menuItemClickListener = new HabitViewAdapter.MenuItemClickListener() {
            @Override
            public void onEditClick(long habitId) {

            }

            @Override
            public void onDeleteClick(long habitId) {
                if(sessionManager.isSessionActive(habitId)){
                    sessionManager.cancelSession(habitId);
                    updateCurrentSessionCard();
                }

                habitDatabase.deleteHabit(habitId);

                for(int position = 0; position < habitList.size(); position++){
                    Habit habit = habitList.get(position);
                    if(habit.getDatabaseId() == habitId){
                        habitList.remove(position);
                        habitAdapter.notifyItemRemoved(position);
                        break;
                    }
                }
            }

            @Override
            public void onExportClick(long habitId) {

            }

            @Override
            public void onArchiveClick(long habitId) {

            }
        };

        HabitViewAdapter.ButtonClickListener buttonClickListener = new HabitViewAdapter.ButtonClickListener() {
            @Override
            public void onPlayButtonClicked(long habitId) {
                startSession(habitDatabase.getHabit(habitId));
            }

            @Override
            public void onCardClicked(long habitId) {

            }
        };

        habitAdapter = new HabitViewAdapter(habitList, menuItemClickListener, buttonClickListener);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        habitCardContainer.setLayoutManager(layoutManager);
        habitCardContainer.setItemAnimator(new DefaultItemAnimator());
        habitCardContainer.setAdapter(habitAdapter);

//        habitCardContainer.addOnItemTouchListener(new RecyclerTouchListener(this, habitCardContainer, new RecyclerTouchListener.ClickListener() {
//            @Override
//            public void onClick(View view, int position) {
//                startSession(habitList.get(position));
//            }
//
//            @Override
//            public void onLongClick(View view, int position) {
//
//            }
//        }));

        currentSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActiveSessionsActivity();
            }
        });

        updatePreferences();
        updateCurrentSessionCard();
        showDatabase();
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

        SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        processUserQuery(query);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {
                        processUserQuery(query);

                        return false;
                    }
                }
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();
        switch(id){
            case (R.id.menu_database_export):{

            }break;

            case (R.id.menu_database_restore):{

            }break;

            case (R.id.menu_settings):{
                startSettingsActivity();
            }break;

            case (R.id.menu_about):{
                startAboutActivity();
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
                startActiveSessionsActivity();
            }break;

            case(R.id.overall_stats_nav):{
                Toast.makeText(this, "Overall stats", Toast.LENGTH_SHORT).show();
            }break;

            case(R.id.settings_nav):{
                startSettingsActivity();
            }break;

            case(R.id.about_nav):{
                startAboutActivity();
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

                case SessionActivity.RESULT_SESSION_FINISH : {
                    if(data.hasExtra("entry")) {
                        SessionEntry entry = (SessionEntry) data.getSerializableExtra("entry");
                        Toast.makeText(this, entry.toString(), Toast.LENGTH_SHORT).show();
                        habitDatabase.addEntry(entry.getHabitId(), entry);
                    }
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

    @Override
    protected void onResume() {
        super.onResume();

        updatePreferences();
        updateCurrentSessionCard();
    }

    public void processUserQuery(String query){
        if(query.length() != 0) {
            Set<Long> ids = habitDatabase.queryDatabaseByTheUser(query);

            habitList.clear();

            for (long id : ids) {
                Habit habit = habitDatabase.getHabit(id);
                habitList.add(habit);
            }

            habitAdapter.notifyDataSetChanged();
        }
        else {
            showDatabase();
        }
    }

    private void updatePreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.showCurrentSessions = preferences.getBoolean(getString(R.string.preference_display_current_sessions_card), true);
        this.showCurrentSessionsAlways = preferences.getBoolean(getString(R.string.preference_display_current_sessions_at_zero), true);
    }

    public void startSession(Habit habit){
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra("habit", habit);
        startActivityForResult(startSession, SessionActivity.RESULT_SESSION_FINISH);
    }

    public void startActiveSessionsActivity() {
        int count = sessionManager.getSessionCount();
        if (count != 0) {
            Intent startTargetActivity = new Intent(this, ActiveSessionsActivity.class);
            startActivity(startTargetActivity);
        } else {
            Toast.makeText(this, R.string.cannot_open_active_sessions_activity, Toast.LENGTH_SHORT).show();
        }
    }

    public void startAboutActivity(){
        Intent startAbout = new Intent(this, AboutActivity.class);
        startActivity(startAbout);
    }

    public void startSettingsActivity(){
        Intent startSettings = new Intent(this, SettingsActivity.class);
        startActivity(startSettings);
    }

    public void updateCurrentSessionCard() {
        TextView countText = (TextView) currentSession.findViewById(R.id.active_session_value_text);
        TextView countLabelText = (TextView) currentSession.findViewById(R.id.active_session_description_text);

        int count = sessionManager.getSessionCount();
        currentSession.setAlpha(count == 0? 0.5f : 1.0f);

        if((count != 0 || showCurrentSessionsAlways) && showCurrentSessions
                && (currentSession.getVisibility() == View.GONE)) {
            currentSession.setVisibility(View.VISIBLE);
        } else if (!showCurrentSessions) {
            currentSession.setVisibility(View.GONE);
        }

        switch(count){
            case(0): {
                countText.setText(R.string.no);
                countLabelText.setText(R.string.active_sessions);

                if(!showCurrentSessionsAlways){
                    currentSession.setVisibility(View.GONE);
                }

            }break;

            case(1):{
                countText.setText(R.string.one);
                countLabelText.setText(R.string.active_session);
            }break;

            default:{
                countText.setText(String.valueOf(count));
                countLabelText.setText(R.string.active_sessions);
            }break;
        }
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

                if (!checkListForHabitId(habitId)) {
                    habitList.add(habit);
                }
            }
        }

        habitAdapter.notifyDataSetChanged();
    }

    public boolean checkListForHabitId(long habitId){
        for(Habit habit : habitList){
            if(habit.getDatabaseId() == habitId){
                return true;
            }
        }

        return false;
    }
}
