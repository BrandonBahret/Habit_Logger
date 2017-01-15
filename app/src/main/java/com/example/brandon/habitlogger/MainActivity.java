package com.example.brandon.habitlogger;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.habitlogger.DataExportHelpers.GoogleDriveDataExportManager;
import com.example.brandon.habitlogger.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.HabitActivity.HabitActivity;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseCache;
import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.ActiveSessionsActivity;
import com.example.brandon.habitlogger.HabitSessions.SessionActivity;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ModifyHabitActivity.ModifyHabitActivity;
import com.example.brandon.habitlogger.OverallStatistics.OverallStatisticsActivity;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.Preferences.SettingsActivity;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.HabitViewAdapter;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.support.design.R.styleable.RecyclerView;
import static android.widget.Toast.makeText;
import static com.example.brandon.habitlogger.R.menu.main;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    PreferenceChecker preferenceChecker;

    SessionManager sessionManager;
    CardView currentSession;

    HabitDatabase habitDatabase;
    LocalDataExportManager exportManager;
    GoogleDriveDataExportManager googleDrive;

    List<Habit> habitList = new ArrayList<>();
    Runnable updateCards;
    Handler handler = new Handler();

    RecyclerView habitCardContainer;
    HabitViewAdapter habitAdapter;

    final int NO_ARCHIVED_HABITS = 0, ONLY_ARCHIVED_HABITS = 1;
    int habitDisplayMode = NO_ARCHIVED_HABITS;

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
                startNewHabitActivity();
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

        habitDatabase = new HabitDatabase(MainActivity.this, cache, true);
        sessionManager = new SessionManager(this);
        sessionManager.setSessionChangedListener(new SessionManager.SessionChangeListener() {
            @Override
            public void sessionPauseStateChanged(long habitId, boolean isPaused) {

            }

            @Override
            public void sessionEnded(long habitId, boolean wasCanceled) {
                for(int i = 0; i < habitList.size(); i++) {
                    if(habitList.get(i).getDatabaseId() == habitId){
                        View item = habitCardContainer.getChildAt(i);
                        if(item != null) {
                            TextView timeTextView = (TextView) item.findViewById(R.id.habit_card_time_display);
                            timeTextView.setText(getString(R.string.time_display_placeholder));

                            ImageButton pauseButton = (ImageButton) item.findViewById(R.id.session_control_button);
                            pauseButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                        }
                        break;
                    }
                }
            }
        });


        exportManager = new LocalDataExportManager(MainActivity.this);
        googleDrive = new GoogleDriveDataExportManager(MainActivity.this);
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

        currentSession = (CardView) findViewById(R.id.current_sessions_card);
        habitCardContainer = (RecyclerView) findViewById(R.id.habit_recycler_view);

        HabitViewAdapter.MenuItemClickListener menuItemClickListener = new HabitViewAdapter.MenuItemClickListener() {
            @Override
            public void onEditClick(long habitId) {
                startModifyHabitActivity(habitDatabase.getHabit(habitId));
            }

            @Override
            public void onDeleteClick(long habitId) {
                if (sessionManager.isSessionActive(habitId)) {
                    sessionManager.cancelSession(habitId);
                    updateCurrentSessionCard();
                }

                habitDatabase.deleteHabit(habitId);

                int position = getItemPosition(habitId);
                habitList.remove(position);
                habitAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onExportClick(long habitId) {
                Habit habit = habitDatabase.getHabit(habitId);
                String filepath = exportManager.exportHabit(habit, true);
                Toast.makeText(MainActivity.this, "Habit exported to: " + filepath, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onArchiveClick(long habitId) {
                boolean archivedState = !habitDatabase.getIsHabitArchived(habitId);
                habitDatabase.updateHabitIsArchived(habitId, archivedState);

                int position = getItemPosition(habitId);
                habitList.remove(position);
                habitAdapter.notifyItemRemoved(position);
            }
        };

        HabitViewAdapter.ButtonClickListener buttonClickListener = new HabitViewAdapter.ButtonClickListener() {
            @Override
            public void onPlayButtonClicked(long habitId) {
                if(!sessionManager.isSessionActive(habitId)) {
                    startSession(habitDatabase.getHabit(habitId));
                }
                else{
                    boolean isPaused = sessionManager.getIsPaused(habitId);
                    sessionManager.setPauseState(habitId, !isPaused);
                    handler.post(updateCards);
                }
            }

            @Override
            public void onPlayButtonLongClicked(long habitId) {
                startSession(habitDatabase.getHabit(habitId));
            }

            @Override
            public void onCardClicked(long habitId) {
                startHabitActivity(habitId);
            }
        };

        updateCards = new Runnable() {
            @Override
            public void run() {
                int size = habitList.size();

                for(int i = 0; i < size; i++) {
                    long habitId = habitList.get(i).getDatabaseId();

                    View item = habitCardContainer.getChildAt(i);
                    if(item != null) {
                        TextView timeTextView = (TextView) item.findViewById(R.id.habit_card_time_display);
                        ImageButton pauseButton = (ImageButton) item.findViewById(R.id.session_control_button);

                        if (sessionManager.isSessionActive(habitId)) {
                            SessionEntry entry = sessionManager.getSession(habitId);

                            timeTextView.setText(
                                    new SessionManager.TimeDisplay(entry.getDuration()).toString());

                            pauseButton.setImageResource(
                                    sessionManager.getResourceIdForPauseButton(sessionManager.getIsPaused(habitId)));
                        }
                    }
                }

                handler.postDelayed(updateCards, 1000);
            }
        };
        handler.post(updateCards);

        habitAdapter = new HabitViewAdapter(habitList, menuItemClickListener, buttonClickListener);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        habitCardContainer.setLayoutManager(layoutManager);
        habitCardContainer.setItemAnimator(new DefaultItemAnimator());
        habitCardContainer.setAdapter(habitAdapter);

        currentSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActiveSessionsActivity();
            }
        });

        preferenceChecker = new PreferenceChecker(this);
        updateCurrentSessionCard();
        showDatabase();
    }

    public int getItemPosition(long habitId){
        int position;

        for (position = 0; position < habitList.size(); position++) {
            Habit habit = habitList.get(position);
            if (habit.getDatabaseId() == habitId)
                break;
        }

        return position;
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

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
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
        switch (id) {
            case (R.id.menu_database_export): {
                exportManager.exportDatabase(true);
            }
            break;

            case (R.id.menu_database_restore): {
                exportManager.importDatabase(true);

                habitDatabase.loadAllContents();
                habitDatabase.notifyChange();
            }break;

            case (R.id.menu_export_database_as_csv):{
                String filepath = exportManager.exportDatabaseAsCsv();
                Toast.makeText(this, "Database exported to: " + filepath, Toast.LENGTH_LONG).show();
            }break;

            case (R.id.menu_settings): {
                startSettingsActivity();
            }
            break;

            case (R.id.menu_about): {
                startAboutActivity();
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case (R.id.home_nav): {
                setInitialFragment();
            }break;

            case (R.id.running_habits_nav): {
                startActiveSessionsActivity();
            }break;

            case (R.id.archived_habits): {
                startArchivedHabitsActivity();
            }break;

            case (R.id.overall_stats_nav): {
                startOverallStatisticsActivity();
            }
            break;

            case (R.id.settings_nav): {
                startSettingsActivity();
            }
            break;

            case (R.id.about_nav): {
                startAboutActivity();
            }
            break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ModifyHabitActivity.NEW_HABIT_RESULT_CODE: {
                    Habit newHabit = (Habit) data.getSerializableExtra("new_habit");
                    habitDatabase.addHabitAndCategory(newHabit);
                    showDatabase();
                }break;

                case ModifyHabitActivity.EDIT_HABIT_RESULT_CODE: {
                    Habit editHabit = (Habit) data.getSerializableExtra("new_habit");
                    habitDatabase.updateHabit(editHabit.getDatabaseId(), editHabit);

                    int position = getHabitPositionInList(editHabit.getDatabaseId());
                    habitList.set(position, editHabit);
                    habitAdapter.notifyItemChanged(position);
                }break;

                case 1: {
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
                    exportManager.importDatabase(true);
                    showDatabase();
                }break;

                case 2: {
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    exportManager.importHabit(filePath, true);
                    showDatabase();
                }break;

                case GoogleDriveDataExportManager.REQUEST_CODE_RESOLUTION: {
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
        habitDatabase.dataCache = (DatabaseCache) savedInstanceState.getSerializable("dataCache");
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(preferenceChecker.doShowNotificationsAutomatically()) {
            sessionManager.createAllSessionNotifications();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentSessionCard();
        showDatabase();
    }



    public void processUserQuery(String query) {
        if (query.length() != 0) {
            Set<Long> ids = habitDatabase.queryDatabaseByTheUser(query);

            habitList.clear();

            for (long id : ids) {
                Habit habit = habitDatabase.getHabit(id);

                if(habit != null) {
                    if (habitDisplayMode == ONLY_ARCHIVED_HABITS && habit.getIsArchived())
                        habitList.add(habit);

                    else if(habitDisplayMode == NO_ARCHIVED_HABITS && !habit.getIsArchived())
                        habitList.add(habit);
                }
            }

            habitAdapter.notifyDataSetChanged();
        } else {
            showDatabase();
        }
    }

    public void setInitialFragment(){
        updateCurrentSessionCard();
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.app_name));
        }

        habitDisplayMode = NO_ARCHIVED_HABITS;
        habitList.clear();
        habitCardContainer.removeAllViews();
        showDatabase();
    }

    public void startSession(Habit habit) {
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
            makeText(this, R.string.cannot_open_active_sessions_activity, Toast.LENGTH_SHORT).show();
        }
    }

    private void startArchivedHabitsActivity() {

        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle("Archive");
        }

        habitDisplayMode = ONLY_ARCHIVED_HABITS;
        showDatabase();
    }

    public void startAboutActivity() {
        Intent startAbout = new Intent(this, AboutActivity.class);
        startActivity(startAbout);
    }

    public void startSettingsActivity() {
        Intent startSettings = new Intent(this, SettingsActivity.class);
        startActivity(startSettings);
    }

    public void startNewHabitActivity() {
        Intent newHabit = new Intent(MainActivity.this, ModifyHabitActivity.class);
        startActivityForResult(newHabit, ModifyHabitActivity.NEW_HABIT_RESULT_CODE);
    }

    public void startModifyHabitActivity(Habit habit){
        Intent editHabit = new Intent(MainActivity.this, ModifyHabitActivity.class);
        editHabit.putExtra("edit", true);
        editHabit.putExtra("habit", habit);
        startActivityForResult(editHabit, ModifyHabitActivity.EDIT_HABIT_RESULT_CODE);
    }

    public void startHabitActivity(long habitId){
        Intent startTargetActivity = new Intent(MainActivity.this, HabitActivity.class);
        startTargetActivity.putExtra("habitId", habitId);
        startActivity(startTargetActivity);
    }

    private void startOverallStatisticsActivity() {
        Intent startTargetActivity = new Intent(MainActivity.this, OverallStatisticsActivity.class);
        startActivity(startTargetActivity);
    }

    public void updateCurrentSessionCard() {
        TextView countText = (TextView) currentSession.findViewById(R.id.active_session_value_text);
        TextView countLabelText = (TextView) currentSession.findViewById(R.id.active_session_description_text);

        int count = sessionManager.getSessionCount();
        currentSession.setAlpha(count == 0? 0.5f : 1.0f);

        if((count != 0 || preferenceChecker.doAlwaysShowCurrentSessions()) && preferenceChecker.doShowCurrentSessions()
                && (currentSession.getVisibility() == View.GONE)) {
            currentSession.setVisibility(View.VISIBLE);
        } else if (!preferenceChecker.doShowCurrentSessions()) {
            currentSession.setVisibility(View.GONE);
        }

        switch(count){
            case(0): {
                countText.setText(R.string.no);
                countLabelText.setText(R.string.active_sessions);

                if(!preferenceChecker.doAlwaysShowCurrentSessions()){
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

    public class addJunkData extends AsyncTask<Void, Void, Void>{
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

        // TODO make this proper

        habitList.clear();
        habitCardContainer.removeAllViews();

        for(int categoryInd = 0; categoryInd < habitDatabase.getNumberOfCategories(); categoryInd++) {

            long categoryId = habitDatabase.getCategoryIdFromIndex(categoryInd);

            for (int habitInd = 0; habitInd < habitDatabase.getNumberOfHabits(categoryId); habitInd++) {
                long habitId = habitDatabase.getHabitIdFromIndex(categoryId, habitInd);
                Habit habit = habitDatabase.getHabit(habitId);

                if(habit != null) {
                    if (habitDisplayMode == ONLY_ARCHIVED_HABITS && habit.getIsArchived()) {
                        habitList.add(habit);
                    } else if (habitDisplayMode == NO_ARCHIVED_HABITS && !habit.getIsArchived()) {
                        habitList.add(habit);
                    }
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

    public int getHabitPositionInList(long habitId){
        int position = 0;

        for(Habit habit : habitList){
            if(habit.getDatabaseId() == habitId){
                return position;
            }
            position++;
        }

        return -1;
    }
}
