package com.example.brandon.habitlogger.ui;

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
import com.example.brandon.habitlogger.HabitSessions.SessionNotificationManager;
import com.example.brandon.habitlogger.ModifyHabitActivity.EditHabitDialog;
import com.example.brandon.habitlogger.ModifyHabitActivity.NewHabitDialog;
import com.example.brandon.habitlogger.OverviewActivity.DataOverviewActivity;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.Preferences.SettingsActivity;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerViewAdapters.CategoryCardAdapter;
import com.example.brandon.habitlogger.RecyclerViewAdapters.ComplexDecoration;
import com.example.brandon.habitlogger.RecyclerViewAdapters.HabitViewAdapter;
import com.example.brandon.habitlogger.RecyclerViewAdapters.SpaceOffsetDecoration;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.common.ResultCodes;
import com.example.brandon.habitlogger.databinding.ActivityMainBinding;
import com.github.clans.fab.FloatingActionButton;

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
    SessionNotificationManager sessionNotificationManager;

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

    ActivityMainBinding ui;

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
        ui = DataBindingUtil.setContentView(this, R.layout.activity_main);
        currentSession = ui.mainInclude.currentSessionsCard.itemRoot;
        habitCardContainer = ui.mainInclude.habitRecyclerView;

        setSupportActionBar(ui.toolbar);

        ui.mainInclude.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewHabitDialog dialog = NewHabitDialog.newInstance(new NewHabitDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(Habit habit) {
                        habitDatabase.addHabitAndCategory(habit);
                        showDatabase();
                    }
                });

                dialog.show(getSupportFragmentManager(), "new-habit");
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, ui.drawerLayout, ui.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        ui.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ui.navView.setNavigationItemSelectedListener(this);

        habitDatabase = new HabitDatabase(MainActivity.this);
        sessionNotificationManager = new SessionNotificationManager(this);
        sessionManager = new SessionManager(this);
        sessionManager.addSessionChangedCallback(new SessionManager.SessionChangeListeners() {
            @Override
            public void onSessionPauseStateChanged(long habitId, boolean isPaused) {}

            @Override
            public void onSessionEnded(long habitId, boolean wasCanceled) {
                sessionNotificationManager.cancel((int) habitId);

                for (int i = 0; i < habitList.size(); i++) {
                    if (habitList.get(i).getDatabaseId() == habitId) {
                        View item = habitCardContainer.getChildAt(i);
                        if (item != null) {
                            TextView timeTextView = (TextView) item.findViewById(R.id.habit_card_time_display);
                            timeTextView.setText(getString(R.string.time_display_placeholder));

                            ImageButton pauseButton = (ImageButton) item.findViewById(R.id.session_control_button);
                            pauseButton.setImageResource(R.drawable.ic_play_black);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onSessionStarted(long habitId) {
                if (preferenceChecker.doShowNotificationsAutomatically() && preferenceChecker.doShowNotifications()) {
                    Habit habit = habitDatabase.getHabit(habitId);
                    sessionNotificationManager.updateNotification(habit);
                }
            }
        });


        exportManager = new LocalDataExportManager(MainActivity.this);
        googleDrive = new GoogleDriveDataExportManager(MainActivity.this);
        googleDrive.connect();

        menuItemClickListener = new HabitViewAdapter.MenuItemClickListener() {
            @Override
            public void onEditClick(long habitId) {
                Habit habit = habitDatabase.getHabit(habitId);

                EditHabitDialog dialog = EditHabitDialog.newInstance(new EditHabitDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(Habit habit) {
                        habitDatabase.updateHabit(habit.getDatabaseId(), habit);

                        int position = getHabitPositionInList(habit.getDatabaseId());
                        habitList.set(position, habit);
                        habitAdapter.notifyItemChanged(position);
                    }
                }, habit);

                dialog.show(getSupportFragmentManager(), "edit-habit");
            }

            @Override
            public void onDeleteClick(long habitId) {
                if (sessionManager.getIsSessionActive(habitId)) {
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
                if (!sessionManager.getIsSessionActive(habitId)) {
                    startSession(habitDatabase.getHabit(habitId));
                }
                else {
                    boolean isPaused = sessionManager.getIsPaused(habitId);
                    sessionManager.setPauseState(habitId, !isPaused);
//                    handler.post(updateCards);
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
//        handler.post(updateCards);

        applyItemDecorationToRecyclerView();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        habitCardContainer.setLayoutManager(layoutManager);
        habitCardContainer.setItemAnimator(new DefaultItemAnimator());


        habitCardContainer.addOnScrollListener(new RecyclerViewScrollObserver() {
            @Override
            public void onScrollUp() {
                FloatingActionButton fab = ui.mainInclude.fab;

                // Scroll Up
                if (preferenceChecker.hideFabOnScroll() && !fab.isShown()) {
                    fab.show(true);
                }

                if (preferenceChecker.doHideCurrentSessionCard()) {
                    showCurrentSessionsCard();
                }
            }

            @Override
            public void onScrollDown() {
                FloatingActionButton fab = ui.mainInclude.fab;

                // Scroll Down
                if (preferenceChecker.hideFabOnScroll() && fab.isShown())
                    fab.hide(true);

                if (preferenceChecker.doHideCurrentSessionCard()) {
                    hideCurrentSessionsCard();
                }
            }
        });

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

    private void showCurrentSessionsCard() {
        View v = ui.mainInclude.currentSessionsCard.getRoot();

        v.animate()
                .setStartDelay(0)
                .setDuration(300)
                .alpha(1)
                .translationY(0);
    }

    private void hideCurrentSessionsCard() {
        View v = ui.mainInclude.currentSessionsCard.getRoot();

        v.animate()
                .setStartDelay(0)
                .setDuration(300)
                .alpha(0)
                .translationY(-v.getHeight());
    }

    private void applyItemDecorationToRecyclerView() {
        switch (preferenceChecker.howToDisplayCategories()) {
            case PreferenceChecker.AS_CARDS: {
                habitCardContainer.removeItemDecoration(itemDecoration);
            }
            break;

            case PreferenceChecker.AS_SECTIONS: {
                itemDecoration = new ComplexDecoration(this, R.dimen.category_section_text_size, new ComplexDecoration.Callback() {
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

        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);
        int topOffset = preferenceChecker.doHideCurrentSessionCard() ? (int) getResources().getDimension(R.dimen.large_top_offset_dp) : (int) getResources().getDimension(R.dimen.top_offset_dp);
        SpaceOffsetDecoration bottomOffsetDecoration = new SpaceOffsetDecoration(bottomOffset, topOffset);
        habitCardContainer.addItemDecoration(bottomOffsetDecoration);
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
        if (ui.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            ui.drawerLayout.closeDrawer(GravityCompat.START);
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

        ui.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GoogleDriveDataExportManager.REQUEST_CODE_RESOLUTION: {
                    googleDrive.connect();
                }
                break;
            }
        }
        else if (requestCode == RequestCodes.SETTINGS_ACTIVITY && resultCode == ResultCodes.SETTINGS_CHANGED) {
            recreate();
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
            sessionNotificationManager.launchNotificationsForAllActiveSessions();
        }

//        handler.post(updateCards);
    }


    @Override
    protected void onPause() {
        super.onPause();

        RecyclerView rv = ui.mainInclude.habitRecyclerView;

        int position = ((LinearLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        getIntent().putExtra("LAST_POSITION", position);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateCurrentSessionCard();
        showDatabase();

        if (getIntent().hasExtra("LAST_POSITION")) {
            RecyclerView rv = ui.mainInclude.habitRecyclerView;
            int position = getIntent().getExtras().getInt("LAST_POSITION", 0);
            rv.scrollToPosition(position);
        }
//        handler.post(updateCards);
    }

    public void processUserQuery(String query) {
        if (query.length() != 0) {
            Set<Long> ids = habitDatabase.queryDatabaseByTheUser(query);

            List<Habit> allHabits = habitDatabase.getHabits();

            habitList.clear();

            for (Habit habit : allHabits) {
                if (ids.contains(habit.getDatabaseId())) {
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
        startSession.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) habit);
        startActivity(startSession);
    }

    public void startActiveSessionsActivity() {
        long count = sessionManager.getSessionCount();
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
        startActivityForResult(startSettings, RequestCodes.SETTINGS_ACTIVITY);
    }

    public void startHabitActivity(long habitId) {
        Intent startTargetActivity = new Intent(MainActivity.this, HabitActivity.class);
        startTargetActivity.putExtra("habitId", habitId);
        startActivity(startTargetActivity);
    }

    private void startOverallStatisticsActivity() {
        Intent startTargetActivity = new Intent(MainActivity.this, DataOverviewActivity.class);
        startActivity(startTargetActivity);
    }

    public void updateCurrentSessionCard() {

        TextView countText = ui.mainInclude.currentSessionsCard.activeSessionValueText;
        TextView countLabelText = ui.mainInclude.currentSessionsCard.activeSessionDescriptionText;

        long count = (int) sessionManager.getSessionCount();
        currentSession.setAlpha(count == 0 ? 0.5f : 1.0f);

        if ((count != 0 || preferenceChecker.doAlwaysShowCurrentSessions()) && preferenceChecker.doShowCurrentSessions()
                && (currentSession.getVisibility() == View.GONE)) {
            currentSession.setVisibility(View.VISIBLE);
        }
        else if (!preferenceChecker.doShowCurrentSessions()) {
            currentSession.setVisibility(View.GONE);
        }

        if (count == 0) {
            countText.setText(R.string.no);
            countLabelText.setText(R.string.active_sessions);

            if (!preferenceChecker.doAlwaysShowCurrentSessions())
                currentSession.setVisibility(View.GONE);
        }
        else if (count == 1) {
            countText.setText(R.string.one);
            countLabelText.setText(R.string.active_session);
        }
        else {
            countText.setText(String.valueOf(count));
            countLabelText.setText(R.string.active_sessions);
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