package com.example.brandon.habitlogger.ui;

import android.content.DialogInterface;
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
import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDialog.EditHabitDialog;
import com.example.brandon.habitlogger.HabitDialog.NewHabitDialog;
import com.example.brandon.habitlogger.HabitSessions.ActiveSessionsActivity;
import com.example.brandon.habitlogger.HabitSessions.SessionActivity;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.HabitSessions.SessionNotificationManager;
import com.example.brandon.habitlogger.OverviewActivity.DataOverviewActivity;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.Preferences.SettingsActivity;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerViewAdapters.CategoryCardAdapter;
import com.example.brandon.habitlogger.RecyclerViewAdapters.GroupDecoration;
import com.example.brandon.habitlogger.RecyclerViewAdapters.HabitViewAdapter;
import com.example.brandon.habitlogger.RecyclerViewAdapters.SpaceOffsetDecoration;
import com.example.brandon.habitlogger.common.ConfirmationDialog;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.databinding.ActivityMainBinding;
import com.github.clans.fab.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //region (Member attributes)
    final int NO_ARCHIVED_HABITS = 0, ONLY_ARCHIVED_HABITS = 1;
    private int mHabitDisplayMode = NO_ARCHIVED_HABITS;

    private List<Habit> mHabitList = new ArrayList<>();
    private HabitDatabase mHabitDatabase;
    private SessionManager mSessionManager;
    private SessionNotificationManager mSessionNotificationManager;
    private PreferenceChecker mPreferenceChecker;
    private LocalDataExportManager mLocalExportManager;
    private GoogleDriveDataExportManager mGoogleDriveExportManager;
    private CurrentSessionCardManager mCurrentSessionCard;

    ActivityMainBinding ui;
    private RecyclerView mHabitCardContainer;

    private Handler mUpdateHandler = new Handler();
    private GroupDecoration mGroupDecoration;
    private SpaceOffsetDecoration mSpaceDecoration;
    private HabitViewAdapter mHabitAdapter;
    private CategoryCardAdapter mCategoryAdapter;
    //endregion

    //region [ ---- Methods responsible for handling the lifecycle of the activity ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivityTheme();

        super.onCreate(savedInstanceState);

        ui = DataBindingUtil.setContentView(this, R.layout.activity_main);

        gatherDependencies();

        setListeners();

        setUpActivity();

    }

    private void setActivityTheme() {
        mPreferenceChecker = new PreferenceChecker(this);
        int themeMode = mPreferenceChecker.isNightMode() ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void setUpActivity() {
        setSupportActionBar(ui.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, ui.drawerLayout, ui.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        ui.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mHabitCardContainer.setLayoutManager(layoutManager);
        mHabitCardContainer.setItemAnimator(new DefaultItemAnimator());

        if (mPreferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_CARDS) {
            List<CategoryDataSample> categoryContainers = mHabitDatabase.getAllData();
            MyCollectionUtils.filter(categoryContainers, CategoryDataSample.IFilterEmptySamples);
            mCategoryAdapter = new CategoryCardAdapter(categoryContainers, getMenuItemClickListener(), getHabitButtonClickCallback());
            mHabitCardContainer.setAdapter(mCategoryAdapter);
        }
        else {
            mHabitAdapter = new HabitViewAdapter(mHabitList, getMenuItemClickListener(), getHabitButtonClickCallback());
            mHabitCardContainer.setAdapter(mHabitAdapter);
            showDatabase();
        }

        applyItemDecorationToRecyclerView();
        updateCurrentSessionCard();
    }

    private View.OnClickListener getOnSessionsCardClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActiveSessionsActivity();
            }
        };
    }

    private void setListeners() {
        mSessionManager.addSessionChangedCallback(getOnSessionChangedListener());
        ui.mainInclude.fab.setOnClickListener(getOnNewHabitButtonClicked());
        ui.navView.setNavigationItemSelectedListener(this);
        mHabitCardContainer.addOnScrollListener(getOnScrollListener());
        mCurrentSessionCard.setOnClickListener(getOnSessionsCardClickListener());
    }

    private RecyclerViewScrollObserver getOnScrollListener() {
        return new RecyclerViewScrollObserver() {
            @Override
            public void onScrollUp() {
                FloatingActionButton fab = ui.mainInclude.fab;

                // Scroll Up
                if (mPreferenceChecker.hideFabOnScroll() && !fab.isShown()) {
                    fab.show(true);
                }

                if (mPreferenceChecker.doHideCurrentSessionCard()) {
                    mCurrentSessionCard.showView();
                }
            }

            @Override
            public void onScrollDown() {
                FloatingActionButton fab = ui.mainInclude.fab;

                // Scroll Down
                if (mPreferenceChecker.hideFabOnScroll() && fab.isShown())
                    fab.hide(true);

                if (mPreferenceChecker.doHideCurrentSessionCard()) {
                    mCurrentSessionCard.hideView();
                }
            }
        };
    }

    private void gatherDependencies() {
        mCurrentSessionCard = new CurrentSessionCardManager(ui.mainInclude.currentSessionsCard.itemRoot);
        mHabitCardContainer = ui.mainInclude.habitRecyclerView;
        mSessionNotificationManager = new SessionNotificationManager(this);
        mHabitDatabase = new HabitDatabase(this);
        mSessionManager = new SessionManager(this);
        mLocalExportManager = new LocalDataExportManager(this);
        mGoogleDriveExportManager = new GoogleDriveDataExportManager(this);
    }

    private View.OnClickListener getOnNewHabitButtonClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewHabitDialog dialog = NewHabitDialog.newInstance(new NewHabitDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(Habit habit) {
                        mHabitDatabase.addHabit(habit);
                        showDatabase();
                    }
                });

                dialog.show(getSupportFragmentManager(), "new-habit");
            }
        };
    }

    private SessionManager.SessionChangeListeners getOnSessionChangedListener() {
        return new SessionManager.SessionChangeListeners() {
            @Override
            public void onSessionPauseStateChanged(long habitId, boolean isPaused) {}

            @Override
            public void beforeSessionEnded(long habitId, boolean wasCanceled) {
                mSessionNotificationManager.cancel((int) habitId);

                for (int i = 0; i < mHabitList.size(); i++) {
                    if (mHabitList.get(i).getDatabaseId() == habitId) {
                        View item = mHabitCardContainer.getChildAt(i);
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
            public void afterSessionEnded(long habitId, boolean wasCanceled) {}

            @Override
            public void onSessionStarted(long habitId) {
                if (mPreferenceChecker.doShowNotificationsAutomatically() && mPreferenceChecker.doShowNotifications()) {
                    Habit habit = mHabitDatabase.getHabit(habitId);
                    mSessionNotificationManager.updateNotification(habit);
                }

                updateCurrentSessionCard();
                applySpaceItemDecorator();
            }
        };
    }

    private HabitViewAdapter.MenuItemClickListener getMenuItemClickListener() {
        return new HabitViewAdapter.MenuItemClickListener() {
            @Override
            public void onEditClick(long habitId) {
                onHabitMenuEditClick(habitId);
            }

            @Override
            public void onResetClick(final long habitId) {
                onHabitMenuResetClick(habitId);
            }

            @Override
            public void onDeleteClick(final long habitId) {
                onHabitMenuDeleteClick(habitId);
            }

            @Override
            public void onExportClick(long habitId) {
                onHabitMenuExportClick(habitId);
            }

            @Override
            public void onArchiveClick(final long habitId) {
                onHabitMenuArchiveClick(habitId);
            }

            @Override
            public void onStartSession(long habitId) {
                startSession(habitId);
            }
        };
    }

    private void onHabitMenuExportClick(long habitId) {
        Habit habit = mHabitDatabase.getHabit(habitId);
        mLocalExportManager.shareExportHabit(habit);
    }

    private void onHabitMenuArchiveClick(final long habitId) {
        String habitName = mHabitDatabase.getHabitName(habitId);
        final boolean archivedState = mHabitDatabase.getIsHabitArchived(habitId);
        String actionName = archivedState ? "Unarchive" : "Archive";
        String actionNameLower = archivedState ? "unarchive" : "archive";

        new ConfirmationDialog(MainActivity.this)
                .setTitle("Confirm " + actionName)
                .setMessage("Do you really want to " + actionNameLower + " '" + habitName + "'? ")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHabitDatabase.updateHabitIsArchived(habitId, !archivedState);

                        int position = getItemPosition(habitId);
                        mHabitList.remove(position);
                        mHabitAdapter.notifyItemRemoved(position);
                    }
                })
                .show();
    }

    private void onHabitMenuDeleteClick(final long habitId) {
        String habitName = mHabitDatabase.getHabitName(habitId);
        new ConfirmationDialog(MainActivity.this)
                .setTitle("Confirm Delete")
                .setMessage("Do you really want to delete '" + habitName + "'?")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mSessionManager.getIsSessionActive(habitId)) {
                            mSessionManager.cancelSession(habitId);
                            updateCurrentSessionCard();
                        }

                        mHabitDatabase.deleteHabit(habitId);

                        int position = getItemPosition(habitId);
                        mHabitList.remove(position);
                        mHabitAdapter.notifyItemRemoved(position);
                    }
                })
                .show();
    }

    private void onHabitMenuResetClick(final long habitId) {
        String habitName = mHabitDatabase.getHabitName(habitId);
        new ConfirmationDialog(MainActivity.this)
                .setTitle("Confirm Data Reset")
                .setMessage("Do you really want to delete all entries for '" + habitName + "'?")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHabitDatabase.deleteEntriesForHabit(habitId);
                    }
                })
                .show();
    }

    private void onHabitMenuEditClick(long habitId) {
        Habit habit = mHabitDatabase.getHabit(habitId);
        EditHabitDialog dialog = EditHabitDialog.newInstance(new EditHabitDialog.OnFinishedListener() {
            @Override
            public void onFinishedWithResult(Habit habit) {
                mHabitDatabase.updateHabit(habit.getDatabaseId(), habit);

                int position = getHabitPositionInList(habit.getDatabaseId());
                mHabitList.set(position, habit);
                mHabitAdapter.notifyItemChanged(position);
            }
        }, habit);

        dialog.show(getSupportFragmentManager(), "edit-habit");
    }
    //endregion

    //region visible lifetime (onStart - onStop)
    @Override
    protected void onStart() {
        super.onStart();

        if (mPreferenceChecker.doShowNotificationsAutomatically()) {
            mSessionNotificationManager.launchNotificationsForAllActiveSessions();
        }

    }
    //endregion

    //region foreground lifetime (onResume - onPause)
    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra("LAST_POSITION")) {
            RecyclerView rv = ui.mainInclude.habitRecyclerView;
            rv.getLayoutManager().onRestoreInstanceState(getIntent().getExtras().getParcelable("LAST_POSITION"));
//            int position = getIntent().getExtras().getInt("LAST_POSITION", 0);
//            rv.scrollToPosition(position);
        }

        updateCurrentSessionCard();
        showDatabase();
        startRepeatingTask();

        updateActivityContent();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopRepeatingTask();
        RecyclerView rv = ui.mainInclude.habitRecyclerView;

        getIntent().putExtra("LAST_POSITION", rv.getLayoutManager().onSaveInstanceState());
    }
    //endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setOnQueryTextListener(getSearchListener());

        return super.onCreateOptionsMenu(menu);
    }

    //endregion [ ---------------- end ---------------- ]

    //region Methods responsible for maintaining state changes
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCategoryAdapter != null) {
            mCategoryAdapter.onSaveInstanceState(outState);
        }

        RecyclerView rv = ui.mainInclude.habitRecyclerView;
        outState.putParcelable("LAST_POSITION", rv.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (mCategoryAdapter != null) {
            mCategoryAdapter.onRestoreInstanceState(savedInstanceState);
        }
    }
    //endregion -- end --

    //region Code responsible to update habit card views with active sessions
    private Runnable updateCards = new Runnable() {
        @Override
        public void run() {
            updateActivityContent();

            mUpdateHandler.postDelayed(updateCards, 1000);
        }
    };

    void startRepeatingTask() {
        updateCards.run();
    }

    void stopRepeatingTask() {
        mUpdateHandler.removeCallbacks(updateCards);
    }
    //endregion -- end --

    //region Methods responsible for applying item deoration to the recycler view
    private void applyItemDecorationToRecyclerView() {
        switch (mPreferenceChecker.howToDisplayCategories()) {
            case PreferenceChecker.AS_CARDS: {
                mHabitCardContainer.removeItemDecoration(mGroupDecoration);
            }
            break;

            case PreferenceChecker.AS_SECTIONS: {
                mGroupDecoration = new GroupDecoration(this, R.dimen.category_section_text_size, new GroupDecoration.Callback() {
                    @Override
                    public long getGroupId(int position) {
                        if (position >= 0 && position < mHabitList.size()) {
                            return mHabitList.get(position).getCategory().getDatabaseId();
                        }
                        else {
                            return -1;
                        }
                    }

                    @Override
                    @NonNull
                    public String getGroupFirstLine(int position) {
                        if (position >= 0 && position < mHabitList.size()) {
                            return mHabitList.get(position).getCategory().getName();
                        }
                        else {
                            return "";
                        }
                    }
                });
                mHabitCardContainer.addItemDecoration(mGroupDecoration);

            }
            break;

            case PreferenceChecker.WITHOUT_CATEGORIES: {
                mHabitCardContainer.removeItemDecoration(mGroupDecoration);
            }
            break;
        }

        applySpaceItemDecorator();
    }

    public void applySpaceItemDecorator() {
        int sessionCount = (int) mSessionManager.getNumberOfActiveSessions();
        boolean useLargeOffset =
                (mPreferenceChecker.doShowCurrentSessions() && sessionCount > 0) ||
                        (sessionCount >= 0 && mPreferenceChecker.doAlwaysShowCurrentSessions() && mPreferenceChecker.doShowCurrentSessions());

        int topOffset = useLargeOffset ? (int) getResources().getDimension(R.dimen.large_top_offset_dp) : (int) getResources().getDimension(R.dimen.top_offset_dp);

        if (useLargeOffset && mPreferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_SECTIONS)
            topOffset += (int) getResources().getDimension(R.dimen.sections_top_offset_dp);

        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);

        if (mSpaceDecoration != null)
            mHabitCardContainer.removeItemDecoration(mSpaceDecoration);

        mSpaceDecoration = new SpaceOffsetDecoration(bottomOffset, topOffset);
        mHabitCardContainer.addItemDecoration(mSpaceDecoration);
    }
    //endregion -- end --

    //region Methods responsible for handling events
    @Override
    public void onBackPressed() {
        if (ui.drawerLayout.isDrawerOpen(GravityCompat.START))
            ui.drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    private SearchView.OnQueryTextListener getSearchListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processUserQuery(newText);
                return true;
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();
        switch (id) {
            case (R.id.menu_database_export): {
                mLocalExportManager.exportDatabase(true);
                if (mGoogleDriveExportManager.isConnected()) {
                    mGoogleDriveExportManager.backupDatabase();
                }
            }
            break;

            case (R.id.menu_database_restore): {
                mLocalExportManager.importDatabase(true);
                showDatabase();
            }
            break;

            case (R.id.menu_export_database_as_csv): {
                String filepath = mLocalExportManager.exportDatabaseAsCsv();
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
        // Handle navigation rootView item clicks here.
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

        if (requestCode == RequestCodes.SETTINGS_ACTIVITY) {
            recreate();
        }
        else if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RequestCodes.GOOGLE_DRIVE_REQUEST_CODE: {
                    mGoogleDriveExportManager.connect();
                }
                break;
            }
        }
    }

    public void processUserQuery(String query) {
        if (query.length() != 0) {
            Set<Long> ids = mHabitDatabase.searchHabitDatabase(query);

            List<Habit> allHabits = mHabitDatabase.getHabits();

            mHabitList.clear();

            for (Habit habit : allHabits) {
                if (ids.contains(habit.getDatabaseId())) {
                    if (mHabitDisplayMode == ONLY_ARCHIVED_HABITS && habit.getIsArchived())
                        mHabitList.add(habit);

                    else if (mHabitDisplayMode == NO_ARCHIVED_HABITS && !habit.getIsArchived())
                        mHabitList.add(habit);
                }
            }

            mHabitAdapter.notifyDataSetChanged();
        }
        else {
            showDatabase();
        }

    }

    private HabitViewAdapter.ButtonClickCallback getHabitButtonClickCallback() {
        return new HabitViewAdapter.ButtonClickCallback() {

            @Override
            public View.OnClickListener getPlayButtonClickedListener(final long habitId) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mSessionManager.getIsSessionActive(habitId)) {
                            startSession(mHabitDatabase.getHabit(habitId));
                        }
                        else {
                            boolean isPaused = mSessionManager.getIsPaused(habitId);
                            mSessionManager.setPauseState(habitId, !isPaused);
                            updateCards.run();
                        }
                    }
                };
            }

            @Override
            public View.OnLongClickListener getPlayButtonLongClickedListener(final long habitId) {
                return new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        startSession(mHabitDatabase.getHabit(habitId));
                        return true;
                    }
                };
            }

            @Override
            public View.OnClickListener getHabitViewClickedListener(final long habitId) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startHabitActivity(habitId);
                    }
                };
            }

        };
    }

    public int getItemPosition(long habitId) {
        int position;

        for (position = 0; position < mHabitList.size(); position++) {
            Habit habit = mHabitList.get(position);
            if (habit.getDatabaseId() == habitId)
                break;
        }

        return position;
    }
    //endregion

    //region Methods responsible for starting activities
    public void startSession(long habitId) {
        Habit habit = mHabitDatabase.getHabit(habitId);
        startSession(habit);
    }

    public void startSession(Habit habit) {
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) habit);
        startActivity(startSession);
    }

    public void startActiveSessionsActivity() {
        long count = mSessionManager.getNumberOfActiveSessions();
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

        mHabitDisplayMode = ONLY_ARCHIVED_HABITS;
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
        startTargetActivity.putExtra(HabitActivity.HABIT_ID, habitId);
        startActivity(startTargetActivity);
    }

    private void startOverallStatisticsActivity() {
        Intent startTargetActivity = new Intent(MainActivity.this, DataOverviewActivity.class);
        startActivity(startTargetActivity);
    }
    //endregion -- end --

    //region Methods responsible for updating the ui
    public void updateCurrentSessionCard() {


//        mCurrentSession.setAlpha(count == 0 ? 0.5f : 1.0f);
        int sessionCount = (int) mSessionManager.getNumberOfActiveSessions();
//        CurrentSessionsCardViewHolder cardViewHolder = new CurrentSessionsCardViewHolder(mCurrentSession);
        mCurrentSessionCard.updateColor(sessionCount);

        if ((sessionCount != 0 || mPreferenceChecker.doAlwaysShowCurrentSessions()) && mPreferenceChecker.doShowCurrentSessions()
                && (mCurrentSessionCard.getVisibility() == View.GONE)) {
            mCurrentSessionCard.setVisibility(View.VISIBLE);
        }
        else if (!mPreferenceChecker.doShowCurrentSessions()) {
            mCurrentSessionCard.setVisibility(View.GONE);
        }

        if (sessionCount == 0) {
            mCurrentSessionCard.getViewHolder().captionValue.setText(R.string.no);
            mCurrentSessionCard.getViewHolder().captionDescription.setText(R.string.active_sessions);

            if (!mPreferenceChecker.doAlwaysShowCurrentSessions())
                mCurrentSessionCard.setVisibility(View.GONE);
        }
        else if (sessionCount == 1) {
            mCurrentSessionCard.getViewHolder().captionValue.setText(R.string.one);
            mCurrentSessionCard.getViewHolder().captionDescription.setText(R.string.active_session);
        }
        else {
            mCurrentSessionCard.getViewHolder().captionValue.setText(String.valueOf(sessionCount));
            mCurrentSessionCard.getViewHolder().captionDescription.setText(R.string.active_sessions);
        }
    }

    public void showDatabase() {
        if (mPreferenceChecker.howToDisplayCategories() != PreferenceChecker.AS_CARDS) {
            mHabitList = mHabitDatabase.getHabits();


            mHabitAdapter = new HabitViewAdapter(mHabitList, getMenuItemClickListener(), getHabitButtonClickCallback());

            if (mHabitDisplayMode == ONLY_ARCHIVED_HABITS)
                MyCollectionUtils.filter(mHabitList, Habit.ICheckIfIsNotArchived);

            else if (mHabitDisplayMode == NO_ARCHIVED_HABITS)
                MyCollectionUtils.filter(mHabitList, Habit.ICheckIfIsArchived);

            Collections.sort(mHabitList, Habit.ICompareCategoryName);

            mHabitCardContainer.setAdapter(mHabitAdapter);
        }
    }

    public int getHabitPositionInList(long habitId) {
        int position = 0;

        for (Habit habit : mHabitList) {
            if (habit.getDatabaseId() == habitId) {
                return position;
            }
            position++;
        }

        return -1;
    }

    private void updateActivityContent() {
        List<SessionEntry> entries = mSessionManager.getActiveSessionList();

        if (mHabitAdapter != null)
            mHabitAdapter.updateHabitViews(entries);
    }

    public void setInitialFragment() {
        updateCurrentSessionCard();
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.app_name));
        }

        mHabitDisplayMode = NO_ARCHIVED_HABITS;
        mHabitList.clear();
        mHabitCardContainer.removeAllViews();
        showDatabase();
    }
    //endregion -- end --

}