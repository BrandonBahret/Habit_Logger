package com.example.brandon.habitlogger.ui.Activities.MainActivity;

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

import com.android.internal.util.Predicate;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.DataExportHelpers.GoogleDriveDataExportManager;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.data.HabitSessions.SessionNotificationManager;
import com.example.brandon.habitlogger.databinding.ActivityMainBinding;
import com.example.brandon.habitlogger.ui.Activities.AboutActivity;
import com.example.brandon.habitlogger.ui.Activities.ActiveSessionsActivity.ActiveSessionsActivity;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.HabitActivity;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.DataOverviewActivity;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.SettingsActivity;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.RecyclerViewScrollObserver;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.EditHabitDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.NewHabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.CurrentSessionCardManager;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.GroupDecoration;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;
import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //region (Member attributes)
    private static final String RECYCLER_STATE = "RECYCLER_STATE";
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
    private SearchView mSearchView;

    private Handler mUpdateHandler = new Handler();
    private SpaceOffsetDecoration mSpaceDecoration;
    private GroupDecoration mGroupDecoration;
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

        setUpActivityUi();

    }

    private void setActivityTheme() {
        mPreferenceChecker = new PreferenceChecker(this);
        int themeMode = mPreferenceChecker.isNightMode() ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void setUpActivityUi() {
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
            mCategoryAdapter = new CategoryCardAdapter(categoryContainers, getHabitMenuItemClickListener(), getHabitButtonClickCallback());
            mHabitCardContainer.setAdapter(mCategoryAdapter);
        }
        else {
            mHabitAdapter = new HabitViewAdapter(mHabitList, getHabitMenuItemClickListener(), getHabitButtonClickCallback());
            mHabitCardContainer.setAdapter(mHabitAdapter);
            showDatabase();
        }

        applyGroupDecoration();
        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);

        if (mPreferenceChecker.doShowNotificationsAutomatically())
            mSessionNotificationManager.launchNotificationsForAllActiveSessions();
    }

    private void setListeners() {
        mSessionManager.addSessionChangedCallback(getOnSessionChangedListener());
        ui.mainInclude.fab.setOnClickListener(getOnNewHabitButtonClicked());
        ui.navView.setNavigationItemSelectedListener(this);
        mHabitCardContainer.addOnScrollListener(getOnScrollListener());
        mCurrentSessionCard.setOnClickListener(getOnSessionsCardClickListener());
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
    //endregion

    //region foreground lifetime (onResume - onPause)
    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra(RECYCLER_STATE)) {
            RecyclerView rv = ui.mainInclude.habitRecyclerView;
            rv.getLayoutManager().onRestoreInstanceState(getIntent().getExtras().getParcelable(RECYCLER_STATE));
        }

        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
        showDatabase();
        startRepeatingTask();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopRepeatingTask();
        RecyclerView rv = ui.mainInclude.habitRecyclerView;
        getIntent().putExtra(RECYCLER_STATE, rv.getLayoutManager().onSaveInstanceState());
    }
    //endregion

    //region Methods responsible for maintaining state changes
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCategoryAdapter != null)
            mCategoryAdapter.onSaveInstanceState(outState);

        RecyclerView rv = ui.mainInclude.habitRecyclerView;
        outState.putParcelable(RECYCLER_STATE, rv.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (mCategoryAdapter != null)
            mCategoryAdapter.onRestoreInstanceState(savedInstanceState);
    }
    //endregion -- end --

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem search = menu.findItem(R.id.search);
        if (search != null) {
            mSearchView = (SearchView) search.getActionView();
            mSearchView.setQueryHint(getString(R.string.filter_main_activity));
            mSearchView.setOnQueryTextListener(getSearchListener());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Code responsible for updating the ui ---- ]

    //region Code responsible to update habit card views
    private Runnable updateCards = new Runnable() {
        @Override
        public void run() {
            List<SessionEntry> entries = mSessionManager.getActiveSessionList();
            if (mHabitAdapter != null)
                mHabitAdapter.updateHabitViews(entries);

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

    //region Methods responsible for applying item decorations to the recycler view
    private GroupDecoration getGroupDecoration() {
        boolean shouldUseStickyHeaders = mPreferenceChecker.makeCategoryHeadersSticky();

        return new GroupDecoration
                (MainActivity.this, R.dimen.category_section_text_size, shouldUseStickyHeaders, new GroupDecoration.Callback() {
                    @Override
                    public long getGroupId(int position) {
                        if (position >= 0 && position < mHabitList.size())
                            return mHabitList.get(position).getCategory().getDatabaseId();
                        else return -1;
                    }

                    @Override
                    public String getGroupFirstLine(int position) {
                        if (position >= 0 && position < mHabitList.size())
                            return mHabitList.get(position).getCategory().getName();
                        else return null;
                    }
                });
    }

    private void applyGroupDecoration() {
        switch (mPreferenceChecker.howToDisplayCategories()) {
            case PreferenceChecker.AS_CARDS:
                mHabitCardContainer.removeItemDecoration(mGroupDecoration);
                break;

            case PreferenceChecker.AS_SECTIONS:
                mGroupDecoration = getGroupDecoration();

                mHabitCardContainer.addItemDecoration(mGroupDecoration);
                break;

            case PreferenceChecker.WITHOUT_CATEGORIES:
                mHabitCardContainer.removeItemDecoration(mGroupDecoration);
                break;
        }

        applySpaceDecoration();
    }

    private void applySpaceDecoration() {
        int sessionCount = (int) mSessionManager.getNumberOfActiveSessions();
        boolean useLargeTopOffset = mPreferenceChecker.shouldShowCurrentSessions(sessionCount);
        int topOffset = useLargeTopOffset ? (int) getResources().getDimension(R.dimen.large_top_offset_dp) : (int) getResources().getDimension(R.dimen.top_offset_dp);

        if (mPreferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_SECTIONS)
            topOffset += (int) getResources().getDimension(R.dimen.sections_top_offset_dp);

        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);

        if (mSpaceDecoration != null)
            mHabitCardContainer.removeItemDecoration(mSpaceDecoration);

        mSpaceDecoration = new SpaceOffsetDecoration(bottomOffset, topOffset);
        mHabitCardContainer.addItemDecoration(mSpaceDecoration);
    }
    //endregion -- end --

    public void showDatabase() {
        if (mPreferenceChecker.howToDisplayCategories() != PreferenceChecker.AS_CARDS) {
            mHabitList = mHabitDatabase.getHabits();
            mHabitAdapter = new HabitViewAdapter(mHabitList, getHabitMenuItemClickListener(), getHabitButtonClickCallback());

            if (mHabitDisplayMode == ONLY_ARCHIVED_HABITS)
                MyCollectionUtils.filter(mHabitList, Habit.ICheckIfIsNotArchived);

            else if (mHabitDisplayMode == NO_ARCHIVED_HABITS)
                MyCollectionUtils.filter(mHabitList, Habit.ICheckIfIsArchived);

            Collections.sort(mHabitList, Habit.ICompareCategoryName);
            mHabitCardContainer.setAdapter(mHabitAdapter);
        }

        checkIfHabitsAreAvailable();
    }

    public void checkIfHabitsAreAvailable() {
        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;

        mCurrentSessionCard.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
        ui.mainInclude.habitRecyclerView.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
        findViewById(R.id.no_habits_available_layout).setVisibility(habitsAvailable ? View.GONE : View.VISIBLE);
    }

    public void updateUiForSearchResult(boolean isEmpty) {
        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;
        int visibility = isEmpty && habitsAvailable ? View.VISIBLE : View.GONE;
        View emptyResultsLayout = findViewById(R.id.no_results_layout);
        if (emptyResultsLayout.getVisibility() != visibility) {

            mCurrentSessionCard.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
            ui.mainInclude.habitRecyclerView.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
            emptyResultsLayout.setVisibility(visibility);
        }
    }

    private void showArchiveDatabase() {
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) toolbar.setTitle("Archive");

        mHabitDisplayMode = ONLY_ARCHIVED_HABITS;
        showDatabase();
    }

    public void setInitialFragment() {
        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(getString(R.string.app_name));

        mHabitDisplayMode = NO_ARCHIVED_HABITS;
        mHabitList.clear();
        mHabitCardContainer.removeAllViews();
        showDatabase();
    }

    //endregion [ ------------ end ------------ ]

    //region [ ---- Methods responsible for handling events ---- ]

    //region Methods responsible for handling search events
    private SearchView.OnQueryTextListener getSearchListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                runDatabaseQuery(newText);
                return true;
            }
        };
    }

    private void runDatabaseQuery(String query) {
        if (query.length() != 0) {
            final Set<Long> databaseIds = mHabitDatabase.searchHabitDatabase(query);
            List<Habit> allHabits = mHabitDatabase.getHabits();

            MyCollectionUtils.filter(allHabits, new Predicate<Habit>() {
                @Override
                public boolean apply(Habit habit) {
                    return !databaseIds.contains(habit.getDatabaseId()) ||
                            mHabitDisplayMode == ONLY_ARCHIVED_HABITS && !habit.getIsArchived() ||
                            mHabitDisplayMode == NO_ARCHIVED_HABITS && habit.getIsArchived();
                }
            });

            mHabitList.clear();
            mHabitList.addAll(allHabits);
            mHabitAdapter.notifyDataSetChanged();

            updateUiForSearchResult(mHabitList.isEmpty());
        }
        else {
            showDatabase();
            updateUiForSearchResult(false);
        }
    }
    //endregion -- end --

    //region Methods responsible for handling habit card events
    private HabitViewAdapter.ButtonClickCallback getHabitButtonClickCallback() {
        return new HabitViewAdapter.ButtonClickCallback() {

            @Override
            public View.OnClickListener getPlayButtonClickedListener(final long habitId) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mSessionManager.getIsSessionActive(habitId))
                            SessionActivity.startActivity(MainActivity.this, mHabitDatabase.getHabit(habitId));
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
                        SessionActivity.startActivity(MainActivity.this, mHabitDatabase.getHabit(habitId));
                        return true;
                    }
                };
            }

            @Override
            public View.OnClickListener getHabitViewClickedListener(final long habitId) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HabitActivity.startActivity(MainActivity.this, habitId);
                    }
                };
            }

        };
    }

    private HabitViewAdapter.MenuItemClickListener getHabitMenuItemClickListener() {
        return new HabitViewAdapter.MenuItemClickListener() {
            @Override
            public void onHabitEditClick(long habitId) {
                Habit habit = mHabitDatabase.getHabit(habitId);
                EditHabitDialog dialog = EditHabitDialog.newInstance(new EditHabitDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(Habit habit) {
                        mHabitDatabase.updateHabit(habit.getDatabaseId(), habit);

                        int position = mHabitAdapter.getAdapterItemPosition(habit.getDatabaseId());
                        mHabitList.set(position, habit);
                        mHabitAdapter.notifyItemChanged(position);
                    }
                }, habit);

                dialog.show(getSupportFragmentManager(), "edit-habit");
            }

            @Override
            public void onHabitResetClick(final long habitId) {
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

            @Override
            public void onHabitDeleteClick(final long habitId) {
                String habitName = mHabitDatabase.getHabitName(habitId);
                new ConfirmationDialog(MainActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Do you really want to delete '" + habitName + "'?")
                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mSessionManager.getIsSessionActive(habitId)) {
                                    mSessionManager.cancelSession(habitId);
                                    mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
                                }

                                mHabitDatabase.deleteHabit(habitId);

                                int position = mHabitAdapter.getAdapterItemPosition(habitId);
                                mHabitList.remove(position);
                                mHabitAdapter.notifyItemRemoved(position);
                                checkIfHabitsAreAvailable();
                            }
                        })
                        .show();
            }

            @Override
            public void onHabitExportClick(long habitId) {
                Habit habit = mHabitDatabase.getHabit(habitId);
                mLocalExportManager.shareExportHabit(habit);
            }

            @Override
            public void onHabitArchiveClick(final long habitId) {
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

                                int position = mHabitAdapter.getAdapterItemPosition(habitId);
                                mHabitList.remove(position);
                                mHabitAdapter.notifyItemRemoved(position);
                            }
                        })
                        .show();
            }

            @Override
            public void onHabitStartSession(long habitId) {
                SessionActivity.startActivity(MainActivity.this, mHabitDatabase.getHabit(habitId));
            }
        };
    }
    //endregion -- end --

    private View.OnClickListener getOnSessionsCardClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActiveSessionsActivity.startActivity(MainActivity.this);
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
                            pauseButton.setImageResource(R.drawable.ic_play_24dp);
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

                mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
                applySpaceDecoration();
            }
        };
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
                        if (mSearchView != null){
                            mSearchView.setQuery("", false);
                            mSearchView.clearFocus();
                            mSearchView.onActionViewCollapsed();
                        }
                        findViewById(R.id.no_habits_available_layout).setVisibility(View.GONE);
                    }
                });

                dialog.show(getSupportFragmentManager(), "new-habit");
            }
        };
    }

    private RecyclerViewScrollObserver getOnScrollListener() {
        return new RecyclerViewScrollObserver() {
            FloatingActionButton fab = ui.mainInclude.fab;

            @Override
            public void onScrollUp() {
                if (mPreferenceChecker.hideFabOnScroll() && !fab.isShown())
                    fab.show(true);

                if (mPreferenceChecker.doHideCurrentSessionCard())
                    mCurrentSessionCard.showView();
            }

            @Override
            public void onScrollDown() {
                if (mPreferenceChecker.hideFabOnScroll() && fab.isShown())
                    fab.hide(true);

                if (mPreferenceChecker.doHideCurrentSessionCard())
                    mCurrentSessionCard.hideView();
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (ui.drawerLayout.isDrawerOpen(GravityCompat.START))
            ui.drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.menu_database_export:
                mLocalExportManager.exportDatabase(true);
                if (mGoogleDriveExportManager.isConnected())
                    mGoogleDriveExportManager.backupDatabase();
                break;

            case R.id.menu_database_restore:
                mLocalExportManager.importDatabase(true);
                showDatabase();
                break;

            case R.id.menu_export_database_as_csv:
                String filepath = mLocalExportManager.exportDatabaseAsCsv();
                Toast.makeText(this, "Database exported to: " + filepath, Toast.LENGTH_LONG).show();
                break;

            case R.id.menu_settings:
                SettingsActivity.startActivity(this);
                break;

            case R.id.menu_about:
                AboutActivity.startActivity(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.home_nav:
                setInitialFragment();
                break;

            case R.id.running_habits_nav:
                ActiveSessionsActivity.startActivity(this);
                break;

            case R.id.archived_habits:
                showArchiveDatabase();
                break;

            case (R.id.overall_stats_nav):
                if (mHabitDatabase.getNumberOfEntries() > 0)
                    DataOverviewActivity.startActivity(this);
                else
                    Toast.makeText(this, R.string.no_data_available_lower, Toast.LENGTH_SHORT).show();
                break;

            case R.id.settings_nav:
                SettingsActivity.startActivity(this);
                break;

            case R.id.about_nav:
                AboutActivity.startActivity(this);
                break;
        }

        ui.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.SETTINGS_ACTIVITY)
            recreate();
        else if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RequestCodes.GOOGLE_DRIVE_REQUEST_CODE:
                    mGoogleDriveExportManager.connect();
                    break;
            }
        }
    }
    //endregion [ ---------------- end ---------------- ]

}