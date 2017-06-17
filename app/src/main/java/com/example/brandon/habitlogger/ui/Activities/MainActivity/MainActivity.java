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
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
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
import com.example.brandon.habitlogger.ui.Events.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.HabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.CurrentSessionCardManager;
import com.github.clans.fab.FloatingActionButton;

import java.util.Locale;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, MyFragmentBase.IMainActivity {

    //region (Member attributes)
    public class ActivityState {
        String query = "";

        public void saveState(Bundle outState) {
            outState.putString("query", query);
        }

        public void restoreState(Bundle savedInstanceState) {
            query = savedInstanceState.getString("query");
        }
    }

    private HabitDatabase mHabitDatabase;
    private SessionManager mSessionManager;
    private SessionNotificationManager mSessionNotificationManager;
    private PreferenceChecker mPreferenceChecker;
    private LocalDataExportManager mLocalExportManager;
    private GoogleDriveDataExportManager mGoogleDriveExportManager;
    private CurrentSessionCardManager mCurrentSessionCard;

    ActivityMainBinding ui;
    private SearchView mSearchView;
    private MyFragmentBase mFragment;
    private ActivityState mActivityState = new ActivityState();
    //endregion

    //region [ ---- Methods responsible for handling the lifecycle of the activity ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivityTheme();

        super.onCreate(savedInstanceState);

        resetDialogListeners();

        if (savedInstanceState != null) {
            mActivityState.restoreState(savedInstanceState);
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
        HabitDatabase.addOnCategoryChangedListener(onCategoryChangedListener);

        ui.mainInclude.fab.setOnClickListener(onNewHabitButtonClicked);
        mCurrentSessionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveSessionsActivity.startActivity(MainActivity.this);
            }
        });
        ui.navView.setNavigationItemSelectedListener(this);
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

        prepareNavigationDrawer();
        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
        getContentFragment().reapplySpaceDecoration();
        getContentFragment().callNotifyDataSetChanged();
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
        HabitDatabase.removeOnCategoryChangedListener(onCategoryChangedListener);
        mSessionManager.removeSessionChangedCallback(onSessionChangeCallback);
    }
    //endregion -- end --

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        //focus the SearchView
        if (mActivityState.query != null && !mActivityState.query.isEmpty()) {
            searchMenuItem.expandActionView();
            mSearchView.setQuery(mActivityState.query, false);
            mSearchView.clearFocus();
        }

        mSearchView.setQueryHint(getString(R.string.filter_main_activity));
        mSearchView.setOnQueryTextListener(getSearchListener());

        return super.onCreateOptionsMenu(menu);
    }

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Code responsible for updating the ui ---- ]

    //region [ -- Methods responsible for hiding/showing elements on the screen. -- ]

    //region Floating Action Button
    @Override
    public void hideFab(boolean animate) {
        ui.mainInclude.fab.hide(animate);
    }

    @Override
    public void showFab(boolean animate) {
        ui.mainInclude.fab.show(animate);
    }
    //endregion

    //region Current Sessions Card
    @Override
    public void hideCurrentSessionsCard(boolean animate) {
        mCurrentSessionCard.hideView(animate);
    }

    @Override
    public void showCurrentSessionsCard(boolean animate) {
        mCurrentSessionCard.showView(animate);
    }
    //endregion -- end --

    //endregion [ ---- end ---- ]

    private void setFragmentForNavId(@IdRes int menuId) {
        // Create a new fragment and specify the planet to onSessionToggleClick based on position
        switch (menuId) {
            case R.id.home_nav:
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

    private void clearFocusFromSearchView() {
        if (mSearchView != null) {
            mSearchView.setQuery("", false);
            mSearchView.clearFocus();
            mSearchView.onActionViewCollapsed();
        }
    }

    //endregion -- end --

    //region [ ---- Methods responsible for handling events ---- ]

    //region [ -- menu option events -- ]
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

    //region database backup requests.
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

    //region database restore requests.
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

    //endregion [ ---- end ---- ]

    //region search events
    private SearchView.OnQueryTextListener getSearchListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mActivityState.query = query.trim();
                return getContentFragment().handleOnQuery(mActivityState.query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mActivityState.query = newText.trim();
                return getContentFragment().handleOnQuery(mActivityState.query);
            }
        };
    }
    //endregion -- end --

    HabitDatabase.OnCategoryChangedListener onCategoryChangedListener = new HabitDatabase.OnCategoryChangedListener() {
        @Override
        public void onCategoryDeleted(HabitCategory removedCategory) {
            mFragment.onCategoryRemoved(removedCategory);
        }

        @Override
        public void onCategoryAdded(HabitCategory newCategory) {

        }

        @Override
        public void onCategoryUpdated(HabitCategory oldCategory, HabitCategory newCategory) {
            mFragment.onUpdateCategory(oldCategory, newCategory);
        }
    };

    //region Create Habit requests
    HabitDialog.DialogResult onYesCreateHabit = new HabitDialog.DialogResult() {
        @Override
        public void onResult(@Nullable Habit initHabit, Habit habit) {
            mHabitDatabase.addHabit(habit);
            getContentFragment().addHabitToLayout(habit);
            clearFocusFromSearchView();
            findViewById(R.id.no_habits_available_layout).setVisibility(View.GONE);
        }
    };

    View.OnClickListener onNewHabitButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            HabitDialog dialog = new HabitDialog()
                    .setTitle("Create Habit")
                    .setPositiveButton("Create", onYesCreateHabit)
                    .setNegativeButton("Cancel", null);

            dialog.show(getSupportFragmentManager(), "create-new-habit");
        }
    };
    //endregion -- end --

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.home_nav:
            case R.id.archived_habits:
                setFragmentForNavId(id);
                break;

            case R.id.running_habits_nav:
                ActiveSessionsActivity.startActivity(this);
                break;

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

    private SessionManager.SessionChangeCallback onSessionChangeCallback = new SessionManager.SessionChangeCallback() {
        @Override
        public void beforeSessionEnded(long habitId, boolean wasCanceled) {
            super.beforeSessionEnded(habitId, wasCanceled);
            mSessionNotificationManager.cancel((int) habitId);
            getContentFragment().notifySessionEnded(habitId);
        }

        @Override
        public void afterSessionEnded(long habitId, boolean wasCanceled) {
            super.afterSessionEnded(habitId, wasCanceled);
            mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
            getContentFragment().reapplySpaceDecoration();
        }

        @Override
        public void onSessionStarted(long habitId) {
            super.onSessionStarted(habitId);
            if (mPreferenceChecker.doShowNotifications()) {
                Habit habit = mHabitDatabase.getHabit(habitId);
                mSessionNotificationManager.updateNotification(habit);
            }

            mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
            getContentFragment().reapplySpaceDecoration();
        }
    };

    @Override
    public void onBackPressed() {
        if (ui.drawerLayout.isDrawerOpen(GravityCompat.START))
            ui.drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mActivityState.saveState(outState);
    }

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

    //region Getters{}
    public MyFragmentBase getContentFragment() {
        if (mFragment == null) {
            mFragment = (MyFragmentBase) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        }

        return mFragment;
    }
    //endregion -- end --

    //region Setters{}
    private void resetDialogListeners() {
        DialogFragment dialog;

        FragmentManager fragmentManager = getSupportFragmentManager();

        if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-database-backup")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-database-restore")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("create-new-habit")) != null)
            ((HabitDialog) dialog).setPositiveButton(null, onYesCreateHabit);
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
    //endregion -- end --

}