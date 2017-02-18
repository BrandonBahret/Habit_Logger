package com.example.brandon.habitlogger;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.habitlogger.DataExportHelpers.GoogleDriveDataExportManager;
import com.example.brandon.habitlogger.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.HabitActivity.HabitActivity;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.CategoryHabitsContainer;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitSessions.ActiveSessionsActivity;
import com.example.brandon.habitlogger.HabitSessions.SessionActivity;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ModifyHabitActivity.ModifyHabitActivity;
import com.example.brandon.habitlogger.OverallStatistics.OverallStatisticsActivity;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.Preferences.SettingsActivity;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.CategoryCardAdapter;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.HabitViewAdapter;
import com.example.brandon.habitlogger.databinding.ActivityMainBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.widget.Toast.makeText;
import static com.example.brandon.habitlogger.R.menu.main;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    PreferenceChecker preferenceChecker;
    SessionManager sessionManager;

    CardView currentSession;
    RecyclerView habitCardContainer;

    HabitDatabase habitDatabase;
    LocalDataExportManager exportManager;

    GoogleDriveDataExportManager googleDrive;
    List<Habit> habitList = new ArrayList<>();

    List<CategoryHabitsContainer> categoryContainers = new ArrayList<>();
    Runnable updateCards;
    Handler handler = new Handler();
    private ComplexDecoration itemDecoration;

    HabitViewAdapter habitAdapter;
    CategoryCardAdapter categoryAdapter;
    HabitViewAdapter.MenuItemClickListener menuItemClickListener;
    HabitViewAdapter.ButtonClickListener buttonClickListener;

    ActivityMainBinding binding;

    final int NO_ARCHIVED_HABITS = 0, ONLY_ARCHIVED_HABITS = 1;
    int habitDisplayMode = NO_ARCHIVED_HABITS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferenceChecker = new PreferenceChecker(this);
        AppCompatDelegate.setDefaultNightMode(
                preferenceChecker.isNightMode() ? AppCompatDelegate.MODE_NIGHT_YES :
                        AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        currentSession = binding.mainInclude.contentMain.currentSessionsCard.itemRoot;
        habitCardContainer = binding.mainInclude.contentMain.habitRecyclerView;

        if (preferenceChecker.isNightMode())
            binding.mainInclude.toolbar.setPopupTheme(R.style.PopupMenu);
        setSupportActionBar(binding.mainInclude.toolbar);

        binding.mainInclude.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewHabitActivity();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.mainInclude.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);

        habitDatabase = new HabitDatabase(MainActivity.this);
        sessionManager = new SessionManager(this);
        sessionManager.addSessionChangedListener(new SessionManager.SessionChangeListener() {
            @Override
            public void sessionPauseStateChanged(long habitId, boolean isPaused) {

            }

            @Override
            public void sessionEnded(long habitId, boolean wasCanceled) {
                for (int i = 0; i < habitList.size(); i++) {
                    if (habitList.get(i).getDatabaseId() == habitId) {
                        View item = habitCardContainer.getChildAt(i);
                        if (item != null) {
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

        menuItemClickListener = new HabitViewAdapter.MenuItemClickListener() {
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
                exportManager.shareExportHabit(habit);
            }

            @Override
            public void onArchiveClick(long habitId) {
                boolean archivedState = !habitDatabase.getIsHabitArchived(habitId);
                habitDatabase.updateHabitIsArchived(habitId, archivedState);

                int position = getItemPosition(habitId);
                habitList.remove(position);
                habitAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onStartSession(long habitId) {
                startSession(habitId);
            }
        };

        buttonClickListener = new HabitViewAdapter.ButtonClickListener() {
            @Override
            public void onPlayButtonClicked(long habitId) {
                if (!sessionManager.isSessionActive(habitId)) {
                    startSession(habitDatabase.getHabit(habitId));
                }
                else {
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
//                List<SessionEntry> entries = sessionManager.getActiveSessionList();
//                habitAdapter.updateHabitViews(entries);

                handler.postDelayed(updateCards, 1000);
            }
        };
        handler.post(updateCards);

        applyItemDecorationToRecyclerView();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        habitCardContainer.setLayoutManager(layoutManager);
        habitCardContainer.setItemAnimator(new DefaultItemAnimator());

        if (preferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_CARDS) {
            categoryContainers = habitDatabase.getCategoryHabitsContainers();
            categoryAdapter = new CategoryCardAdapter(categoryContainers, menuItemClickListener, buttonClickListener);

            habitCardContainer.setAdapter(categoryAdapter);
        }
        else {
            habitAdapter = new HabitViewAdapter(habitList, menuItemClickListener, buttonClickListener);
            habitCardContainer.setAdapter(habitAdapter);
            showDatabase();
        }

        currentSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActiveSessionsActivity();
            }
        });

        updateCurrentSessionCard();
    }

    private void applyItemDecorationToRecyclerView() {
        switch (preferenceChecker.howToDisplayCategories()) {
            case PreferenceChecker.AS_CARDS: {
                habitCardContainer.removeItemDecoration(itemDecoration);
            }
            break;

            case PreferenceChecker.AS_SECTIONS: {
                itemDecoration = new ComplexDecoration(this, new ComplexDecoration.Callback() {
                    @Override
                    public long getGroupId(int position) {
                        if (position >= 0 && position < habitList.size()) {
                            return habitList.get(position).getCategory().getDatabaseId();
                        }
                        else {
                            return -1;
                        }
                    }

                    @Override
                    @NonNull
                    public String getGroupFirstLine(int position) {
                        if (position >= 0 && position < habitList.size()) {
                            return habitList.get(position).getCategory().getName();
                        }
                        else {
                            return "";
                        }
                    }
                });
                habitCardContainer.addItemDecoration(itemDecoration);

            }
            break;

            case PreferenceChecker.WITHOUT_CATEGORIES: {
                habitCardContainer.removeItemDecoration(itemDecoration);
            }
            break;
        }
    }

    public int getItemPosition(long habitId) {
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
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
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
                if (googleDrive.isConnected()) {
                    googleDrive.backupDatabase();
                }
            }
            break;

            case (R.id.menu_database_restore): {
                exportManager.importDatabase(true);
                showDatabase();
            }
            break;

            case (R.id.menu_export_database_as_csv): {
                String filepath = exportManager.exportDatabaseAsCsv();
                Toast.makeText(this, "Database exported to: " + filepath, Toast.LENGTH_LONG).show();
            }
            break;

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
            }
            break;

            case (R.id.running_habits_nav): {
                startActiveSessionsActivity();
            }
            break;

            case (R.id.archived_habits): {
                startArchivedHabitsActivity();
            }
            break;

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

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SettingsActivity.REQUEST_SETTINGS) {
            applyItemDecorationToRecyclerView();
            recreate();
        }
        else if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ModifyHabitActivity.NEW_HABIT_RESULT_CODE: {
                    Habit newHabit = (Habit) data.getSerializableExtra("new_habit");
                    habitDatabase.addHabitAndCategory(newHabit);
                    showDatabase();
                }
                break;

                case ModifyHabitActivity.EDIT_HABIT_RESULT_CODE: {
                    Habit editHabit = (Habit) data.getSerializableExtra("new_habit");
                    habitDatabase.updateHabit(editHabit.getDatabaseId(), editHabit);

                    int position = getHabitPositionInList(editHabit.getDatabaseId());
                    habitList.set(position, editHabit);
                    habitAdapter.notifyItemChanged(position);
                }
                break;

                case GoogleDriveDataExportManager.REQUEST_CODE_RESOLUTION: {
                    googleDrive.connect();
                }
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (categoryAdapter != null) {
            categoryAdapter.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (categoryAdapter != null) {
            categoryAdapter.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferenceChecker.doShowNotificationsAutomatically()) {
            sessionManager.createAllSessionNotifications();
        }

        handler.post(updateCards);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateCurrentSessionCard();
        showDatabase();
        handler.post(updateCards);
    }

    public void processUserQuery(String query) {
        if (query.length() != 0) {
            Set<Long> ids = habitDatabase.queryDatabaseByTheUser(query);

            habitList.clear();

            for (long id : ids) {
                Habit habit = habitDatabase.getHabit(id);

                if (habit != null) {
                    if (habitDisplayMode == ONLY_ARCHIVED_HABITS && habit.getIsArchived())
                        habitList.add(habit);

                    else if (habitDisplayMode == NO_ARCHIVED_HABITS && !habit.getIsArchived())
                        habitList.add(habit);
                }
            }

            habitAdapter.notifyDataSetChanged();
        }
        else {
            showDatabase();
        }
    }

    public void setInitialFragment() {
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


    public void startSession(long habitId) {
        Habit habit = habitDatabase.getHabit(habitId);
        startSession(habit);
    }

    public void startSession(Habit habit) {
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra("habit", (Serializable) habit);
        startActivityForResult(startSession, SessionActivity.RESULT_SESSION_FINISH);
    }

    public void startActiveSessionsActivity() {
        int count = sessionManager.getSessionCount();
        if (count != 0) {
            Intent startTargetActivity = new Intent(this, ActiveSessionsActivity.class);
            startActivity(startTargetActivity);
        }
        else {
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
        Intent startSettings = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(startSettings, SettingsActivity.REQUEST_SETTINGS);
    }

    public void startNewHabitActivity() {
        Intent newHabit = new Intent(MainActivity.this, ModifyHabitActivity.class);
        startActivityForResult(newHabit, ModifyHabitActivity.NEW_HABIT_RESULT_CODE);
    }

    public void startModifyHabitActivity(Habit habit) {
        Intent editHabit = new Intent(MainActivity.this, ModifyHabitActivity.class);
        editHabit.putExtra("edit", true);
        editHabit.putExtra("habit", (Serializable) habit);
        startActivityForResult(editHabit, ModifyHabitActivity.EDIT_HABIT_RESULT_CODE);
    }

    public void startHabitActivity(long habitId) {
        Intent startTargetActivity = new Intent(MainActivity.this, HabitActivity.class);
        startTargetActivity.putExtra("habitId", habitId);
        startActivity(startTargetActivity);
    }

    private void startOverallStatisticsActivity() {
        Intent startTargetActivity = new Intent(MainActivity.this, OverallStatisticsActivity.class);
        startActivity(startTargetActivity);
    }

    public void updateCurrentSessionCard() {

        TextView countText = binding.mainInclude.contentMain.currentSessionsCard.activeSessionValueText;
        TextView countLabelText = binding.mainInclude.contentMain.currentSessionsCard.activeSessionDescriptionText;

        int count = sessionManager.getSessionCount();
        currentSession.setAlpha(count == 0 ? 0.5f : 1.0f);

        if ((count != 0 || preferenceChecker.doAlwaysShowCurrentSessions()) && preferenceChecker.doShowCurrentSessions()
                && (currentSession.getVisibility() == View.GONE)) {
            currentSession.setVisibility(View.VISIBLE);
        }
        else if (!preferenceChecker.doShowCurrentSessions()) {
            currentSession.setVisibility(View.GONE);
        }

        switch (count) {
            case (0): {
                countText.setText(R.string.no);
                countLabelText.setText(R.string.active_sessions);

                if (!preferenceChecker.doAlwaysShowCurrentSessions()) {
                    currentSession.setVisibility(View.GONE);
                }
            }
            break;

            case (1): {
                countText.setText(R.string.one);
                countLabelText.setText(R.string.active_session);
            }
            break;

            default: {
                countText.setText(String.valueOf(count));
                countLabelText.setText(R.string.active_sessions);
            }
            break;
        }
    }

    public void showDatabase() {
        if (preferenceChecker.howToDisplayCategories() != PreferenceChecker.AS_CARDS) {
            habitList = habitDatabase.getHabits();

            Collections.sort(habitList, Habit.CategoryNameComparator);

            habitAdapter = new HabitViewAdapter(habitList, menuItemClickListener, buttonClickListener);
            habitCardContainer.setAdapter(habitAdapter);
        }
    }

    public int getHabitPositionInList(long habitId) {
        int position = 0;

        for (Habit habit : habitList) {
            if (habit.getDatabaseId() == habitId) {
                return position;
            }
            position++;
        }

        return -1;
    }
}