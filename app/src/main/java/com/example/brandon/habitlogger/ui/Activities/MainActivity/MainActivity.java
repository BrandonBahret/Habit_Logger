package com.example.brandon.habitlogger.ui.Activities.MainActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.common.ResultCodes;
import com.example.brandon.habitlogger.data.DataExportHelpers.GoogleDriveDataExportManager;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.data.HabitSessions.SessionNotificationManager;
import com.example.brandon.habitlogger.databinding.ActivityMainBinding;
import com.example.brandon.habitlogger.ui.Activities.AboutActivity;
import com.example.brandon.habitlogger.ui.Activities.ActiveSessionsActivity.ActiveSessionsActivity;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments.AllHabitsFragment;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments.ArchivedHabitsFragment;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments.MyFragmentBase;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.SettingsActivity;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.EditHabitDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.NewHabitDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog2.HabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.CurrentSessionCardManager;
import com.github.clans.fab.FloatingActionButton;

import java.util.Locale;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MyFragmentBase.IMainActivity,
        NewHabitDialog.OnFinishedListener, EditHabitDialog.OnFinishedListener {

    //region (Member attributes)
    private static final String KEY_QUERY = "KEY_QUERY";

    private Integer mLastThemeMode = null;
    private HabitDatabase mHabitDatabase;
    private SessionManager mSessionManager;
    private SessionNotificationManager mSessionNotificationManager;
    private PreferenceChecker mPreferenceChecker;
    private LocalDataExportManager mLocalExportManager;
    private GoogleDriveDataExportManager mGoogleDriveExportManager;
    private CurrentSessionCardManager mCurrentSessionCard;

    ActivityMainBinding ui;
    private SearchView mSearchView;
    private String mSearchViewQuery;
    private MyFragmentBase mFragment;
    //endregion

    //region [ ---- Methods responsible for handling the lifecycle of the activity ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivityTheme();

        super.onCreate(savedInstanceState);

        resetDialogListeners();

        if (savedInstanceState != null) {
            mSearchViewQuery = savedInstanceState.getString(KEY_QUERY, null);
        }

        ui = DataBindingUtil.setContentView(this, R.layout.activity_main);

        gatherDependencies();

        setListeners();

        setUpActivityUi(savedInstanceState != null);

    }

    private void setActivityTheme() {
        mPreferenceChecker = new PreferenceChecker(this);
        int themeMode = mPreferenceChecker.getThemeMode();
        AppCompatDelegate.setDefaultNightMode(themeMode);
        mLastThemeMode = themeMode;
    }

    private void setUpActivityUi(boolean hasSavedInstanceState) {
        setSupportActionBar(ui.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, ui.drawerLayout, ui.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        ui.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);

        if (mPreferenceChecker.doShowNotifications())
            mSessionNotificationManager.launchNotificationsForAllActiveSessions();

        prepareNavigationDrawer();

        if (!hasSavedInstanceState)
            setFragmentForNavId(R.id.home_nav);
        else {
            setTitle(getContentFragment().getFragmentTitle());
        }

    }

    private void prepareNavigationDrawer() {
        Menu menu = ui.navView.getMenu();
        MenuItem navNightModeSwitch = menu.findItem(R.id.night_mode);

        boolean isNightMode = mPreferenceChecker.isNightMode();
        Switch nightModeSwitch = (Switch) navNightModeSwitch.getActionView().findViewById(R.id.menu_switch);
        nightModeSwitch.setChecked(isNightMode);
    }

    private void setListeners() {
        ui.mainInclude.fab.setOnClickListener(getOnNewHabitButtonClicked());
        ui.navView.setNavigationItemSelectedListener(this);
        mCurrentSessionCard.setOnClickListener(getOnSessionsCardClickListener());
    }

    private void gatherDependencies() {
        mCurrentSessionCard = new CurrentSessionCardManager(ui.mainInclude.currentSessionsCard.itemRoot);
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

        int themeMode = mPreferenceChecker.getThemeMode();
        if ((mLastThemeMode != null && mLastThemeMode != themeMode) || AppCompatDelegate.getDefaultNightMode() != themeMode) {
            recreate();
        }

        prepareNavigationDrawer();
        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
        getContentFragment().reapplySpaceDecoration();
        getContentFragment().callNotifyDataSetChanged();
//        getContentFragment().restartFragment();
    }
    //endregion

    //region visible lifetime (onStart - onStop)
    @Override
    protected void onStart() {
        super.onStart();
        mSessionManager.addSessionChangedCallback(onSessionChangeCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSessionManager.removeSessionChangedCallback(onSessionChangeCallback);
    }
    //endregion -- end --

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        //focus the SearchView
        if (mSearchViewQuery != null && !mSearchViewQuery.isEmpty()) {
            searchMenuItem.expandActionView();
            mSearchView.setQuery(mSearchViewQuery, false);
            mSearchView.clearFocus();
        }

        mSearchView.setQueryHint(getString(R.string.filter_main_activity));
        mSearchView.setOnQueryTextListener(getSearchListener());

        return super.onCreateOptionsMenu(menu);
    }

    //endregion [ ---------------- end ---------------- ]

    //region Methods responsible for maintaining state changes
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY, mSearchViewQuery);
    }
    //endregion -- end --

    //region [ ---- Code responsible for updating the ui ---- ]

    //region Methods responsible for hiding/showing elements on the screen.
    @Override
    public void hideFab(boolean animate) {
        ui.mainInclude.fab.hide(animate);
    }

    @Override
    public void showFab(boolean animate) {
        ui.mainInclude.fab.show(animate);
    }

    @Override
    public void hideCurrentSessionsCard(boolean animate) {
        mCurrentSessionCard.hideView(animate);
    }

    @Override
    public void showCurrentSessionsCard(boolean animate) {
        mCurrentSessionCard.showView(animate);
    }
    //endregion -- end --

    private void setFragmentForNavId(@IdRes int menuId) {
        // Create a new fragment and specify the planet to onSessionToggleClick based on position
        switch (menuId) {
            case R.id.home_nav:
//                if (mPreferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_CARDS)
//                    mFragment = CategoryCardHabitsFragment.newInstance();
//                else
                mFragment = AllHabitsFragment.newInstance();

                break;

            case R.id.archived_habits:
                mFragment = ArchivedHabitsFragment.newInstance();
                break;

            default:
                mFragment = AllHabitsFragment.newInstance();
        }

        clearFocusFromSearchView();

        if (mPreferenceChecker.doHideCurrentSessionCard())
            mCurrentSessionCard.showView(false);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, mFragment)
                .commit();


        // Change the title accordingly
        setTitle(getContentFragment().getFragmentTitle());
        ui.drawerLayout.closeDrawer(GravityCompat.START);
    }

    public MyFragmentBase getContentFragment() {
        if (mFragment == null) {
            mFragment = (MyFragmentBase) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        }

        return mFragment;
    }
    //endregion -- end --

    //region [ ---- Methods responsible for handling events ---- ]

    //region Methods responsible for handling search events
    private SearchView.OnQueryTextListener getSearchListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchViewQuery = query.trim();
                return getContentFragment().handleOnQuery(mSearchViewQuery);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchViewQuery = newText.trim();
                return getContentFragment().handleOnQuery(mSearchViewQuery);
            }
        };
    }
    //endregion -- end --

    @Override
    public IScrollEvents getScrollEventsListener() {
        return new IScrollEvents() {
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

    private View.OnClickListener getOnSessionsCardClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActiveSessionsActivity.startActivity(MainActivity.this);
            }
        };
    }

    private SessionManager.SessionChangeCallback onSessionChangeCallback =
            new SessionManager.SessionChangeCallback() {
                @Override
                public void onSessionPauseStateChanged(long habitId, boolean isPaused) {}

                @Override
                public void beforeSessionEnded(long habitId, boolean wasCanceled) {
                    mSessionNotificationManager.cancel((int) habitId);
                    getContentFragment().notifySessionEnded(habitId);
                }

                @Override
                public void afterSessionEnded(long habitId, boolean wasCanceled) {
                    mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
                    getContentFragment().reapplySpaceDecoration();
                }

                @Override
                public void onSessionStarted(long habitId) {
                    if (mPreferenceChecker.doShowNotifications()) {
                        Habit habit = mHabitDatabase.getHabit(habitId);
                        mSessionNotificationManager.updateNotification(habit);
                    }

                    mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
                    getContentFragment().reapplySpaceDecoration();
                }
            };

    //region Code responsible for handling new Habit requests
    HabitDialog.DialogResult onYesCreateHabit = new HabitDialog.DialogResult() {
        @Override
        public void onResult(@Nullable Habit initHabit, Habit habit) {
            mHabitDatabase.addHabit(habit);
            getContentFragment().addHabitToLayout(habit);
            clearFocusFromSearchView();
            findViewById(R.id.no_habits_available_layout).setVisibility(View.GONE);
        }
    };

    private View.OnClickListener getOnNewHabitButtonClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HabitDialog dialog = new HabitDialog()
                        .setTitle("Create Habit")
                        .setPositiveButton("Create", onYesCreateHabit)
                        .setNegativeButton("Cancel", null);

                dialog.show(getSupportFragmentManager(), "create-new-habit");
            }
        };
    }
    //endregion -- end --

    @Override
    public void onNewHabitCreated(Habit newHabit) {
        mHabitDatabase.addHabit(newHabit);
        getContentFragment().addHabitToLayout(newHabit);
        clearFocusFromSearchView();
        findViewById(R.id.no_habits_available_layout).setVisibility(View.GONE);
    }

    @Override
    public void onUpdateHabit(Habit oldHabit, Habit newHabit) {
        getContentFragment().onUpdateHabit(oldHabit, newHabit);
    }

    private void clearFocusFromSearchView() {
        if (mSearchView != null) {
            mSearchView.setQuery("", false);
            mSearchView.clearFocus();
            mSearchView.onActionViewCollapsed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.home_nav:
                setFragmentForNavId(id);
                break;

            case R.id.running_habits_nav:
                ActiveSessionsActivity.startActivity(this);
                break;

            case R.id.archived_habits:
                setFragmentForNavId(id);
                break;

//            case (R.id.overall_stats_nav):
//                DataOverviewActivity.startActivity(this);
//                break;

            case R.id.settings_nav:
                SettingsActivity.startActivity(this);
                break;

            case R.id.about_nav:
                AboutActivity.startActivity(this);
                break;

            case R.id.night_mode:
                ((Switch) item.getActionView().findViewById(R.id.menu_switch)).toggle();
                mPreferenceChecker.toggleNightMode();
                recreate();
                break;
        }

        ui.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
                handleOnBackupDatabase();
                break;

            case R.id.menu_database_restore:
                handleOnDatabaseRestore();
                break;

            case R.id.menu_export_database_as_csv:
                handleOnExportDatabaseCsv();
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

    private void handleOnExportDatabaseCsv() {
        String filepath = mLocalExportManager.exportDatabaseAsCsv();
        String messageFormat = getString(R.string.database_export_format);
        String message = String.format(Locale.getDefault(), messageFormat, filepath);
        makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void resetDialogListeners() {
        DialogFragment dialog;

        FragmentManager fragmentManager = getSupportFragmentManager();

        if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-database-backup")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-database-restore")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("create-new-habit")) != null)
            ((HabitDialog)dialog).setPositiveButton(null, onYesCreateHabit);
    }

    private void setDialogListener(ConfirmationDialog dialog) {
        switch (dialog.getTag()) {
            case "confirm-database-backup":
                dialog.setOnYesClickListener(onYesBackupDatabaseClicked);
                break;
            case "confirm-database-restore":
                dialog.setOnYesClickListener(onYesRestoreDatabaseClicked);
                break;
        }
    }

    //region Code responsible for handling database backup requests.
    DialogInterface.OnClickListener onYesBackupDatabaseClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mLocalExportManager.exportDatabase(true);
            if (mGoogleDriveExportManager.isConnected())
                mGoogleDriveExportManager.backupDatabase();
            makeText(MainActivity.this, R.string.backup_created, Toast.LENGTH_SHORT).show();
        }
    };

    private void handleOnBackupDatabase() {
        new ConfirmationDialog()
                .setIcon(R.drawable.ic_save_2_24dp)
                .setTitle(getString(R.string.confirm_data_export))
                .setMessage(getString(R.string.confirm_data_export_message))
                .setOnYesClickListener(onYesBackupDatabaseClicked)
                .show(getSupportFragmentManager(), "confirm-database-backup");
    }
    //endregion -- end --

    //region Code responsible for handling database restore requests.
    DialogInterface.OnClickListener onYesRestoreDatabaseClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mLocalExportManager.importDatabase(true);
            getContentFragment().restartFragment();
            makeText(MainActivity.this, R.string.data_restored, Toast.LENGTH_SHORT).show();
        }
    };

    private void handleOnDatabaseRestore() {

        new ConfirmationDialog()
                .setIcon(R.drawable.ic_data_restore_24dp)
                .setTitle(getString(R.string.confirm_data_restore))
                .setMessage(getString(R.string.confirm_data_restore_message))
                .setOnYesClickListener(onYesRestoreDatabaseClicked)
                .show(getSupportFragmentManager(), "confirm-database-restore");
    }
    //endregion -- end --

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RequestCodes.SETTINGS_ACTIVITY:
                recreate();
                break;

            case RequestCodes.HABIT_DATA_ACTIVITY:
                if (resultCode == ResultCodes.HABIT_CHANGED)
                    getContentFragment().restartFragment();
                break;
        }

    }
    //endregion [ ---------------- end ---------------- ]

}