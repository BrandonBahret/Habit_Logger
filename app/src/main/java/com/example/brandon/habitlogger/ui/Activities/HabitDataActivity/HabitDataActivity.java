package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.common.ResultCodes;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.CategoryDataCollection;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema.EntriesTableSchema;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.databinding.ActivityHabitDataBinding;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.EntriesFragment.EntriesFragment;
import com.example.brandon.habitlogger.ui.Events.IStateContainer;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Events.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity.SessionActivity;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.EditEntryForm;
import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.NewEntryForm;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.HabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.FloatingDateRangeWidgetManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.brandon.habitlogger.R.string.menu_unarchive;

public class HabitDataActivity extends AppCompatActivity implements
        IHabitDataCallback.IUpdateEntries, IHabitDataCallback.IUpdateCategoryData,
        IHabitDataCallback, IScrollEvents, EntriesFragment.IEntriesEvents,
        EditEntryForm.OnFinishedListener, NewEntryForm.OnFinishedListener {

    //region (Member attributes)
    public class ActivityState implements IStateContainer {
        private Habit habit;
        SessionEntryCollection entries = new SessionEntryCollection();
        String query = "";
        int tabPosition;

        @Override
        public void saveState(Bundle outState) {
            outState.putString("query", query);
            outState.putParcelableArrayList("entries", entries);
            outState.putInt("tabPosition", tabPosition);
            outState.putSerializable("habit", habit);
        }

        @Override
        public void restoreState(Bundle savedInstanceState) {
            query = savedInstanceState.getString("query");
            habit = (Habit) savedInstanceState.getSerializable("habit");

            List<SessionEntry> restoredEntries = savedInstanceState.getParcelableArrayList("entries");
            entries = new SessionEntryCollection(restoredEntries);
            tabPosition = savedInstanceState.getInt("tabPosition");
        }
    }
    
    // Dependencies
    private HabitDatabase mHabitDatabase;
    private LocalDataExportManager mExportManager;
    private SessionManager mSessionManager;
    private PreferenceChecker mPreferenceChecker;

    // Data
    private ActivityState mActivityState = new ActivityState();

    // View related members
    FloatingDateRangeWidgetManager dateRangeManager;
    private SearchView mSearchView;
    ActivityHabitDataBinding ui;
    //endregion

    //region Code responsible for providing communication between child fragments and this activity

    // Callbacks
    private IEntriesFragment mEntriesCallback;
    private IStatisticsFragment mStatisticsCallback;
    private ICalendarFragment mCalendarCallback;

    @Override
    public void setEntriesFragmentCallback(IEntriesFragment callback) {
        mEntriesCallback = callback;
    }

    @Override
    public void setCalendarFragmentCallback(ICalendarFragment callback) {
        mCalendarCallback = callback;
    }

    @Override
    public void setStatisticsFragmentCallback(IStatisticsFragment callback) {
        mStatisticsCallback = callback;
    }

    private void callUpdateColorPaletteMethods(ThemeColorPalette palette) {
        if (mStatisticsCallback != null) mStatisticsCallback.onUpdateColorPalette(palette);
        if (mEntriesCallback != null) mEntriesCallback.onUpdateColorPalette(palette);
        if (mCalendarCallback != null) mCalendarCallback.onUpdateColorPalette(palette);
    }
    //endregion -- end --

    //region [ ---- Methods responsible for handling the activity lifecycle ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetDialogListeners();

        boolean usesSavedInstance = savedInstanceState != null;
        if (usesSavedInstance) {
            mActivityState.restoreState(savedInstanceState);
        }

        // Gather data from intent
        Intent data = getIntent();
        long habitId = data.getLongExtra(EntriesTableSchema.ENTRY_HABIT_ID, -1);

        // Create dependencies
        mHabitDatabase = new HabitDatabase(this);
        mSessionManager = new SessionManager(this);
        mPreferenceChecker = new PreferenceChecker(this);
        mExportManager = new LocalDataExportManager(this);

        // Fetch data from database
        mActivityState.habit = mHabitDatabase.getHabit(habitId);
        if (!usesSavedInstance) {
            mActivityState.entries = mHabitDatabase.getEntries(habitId);
        }

        // Set up activity
        ui = DataBindingUtil.setContentView(this, R.layout.activity_habit_data);

        setSupportActionBar(ui.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HabitDataActivityPagerAdapter pagerAdapter = new HabitDataActivityPagerAdapter(getSupportFragmentManager(), this);
        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), mActivityState.entries);
        ui.container.setAdapter(pagerAdapter);
        ui.tabs.setupWithViewPager(ui.container);
        ui.menuFab.setClosedOnTouchOutside(true);

        if (savedInstanceState == null || mActivityState.tabPosition == 1) {
            ui.container.setCurrentItem(1);
            dateRangeManager.hideView(false);
            ui.menuFab.hideMenu(false);
        }

        if (!usesSavedInstance) {
            mActivityState.entries = fetchEntriesWithinTimeRange();
            dateRangeManager.updateSessionEntries(mActivityState.entries);
        }
        else dateRangeManager.restoreState(savedInstanceState);

        setUpActivityWithHabit(mActivityState.habit);

    }

    private void setUpActivityWithHabit(Habit habit) {
        ui.toolbar.setTitle(mActivityState.habit.getName());
        setSupportActionBar(ui.toolbar);

        ThemeColorPalette palette = new ThemeColorPalette(habit.getColor());

        callUpdateColorPaletteMethods(palette);

        getWindow().setStatusBarColor(palette.getColorPrimaryDark());
        ui.tabs.setBackgroundColor(palette.getColorPrimary());

        ui.toolbar.setBackgroundColor(palette.getColorPrimary());

        ui.menuFab.setMenuButtonColorNormal(palette.getColorAccent());
        ui.menuFab.setMenuButtonColorPressed(palette.getColorAccentDark());

        ui.enterSessionFab.setColorNormal(palette.getColorAccent());
        ui.enterSessionFab.setColorPressed(palette.getColorAccentDark());

        ui.createEntryFab.setColorNormal(palette.getColorAccent());
        ui.createEntryFab.setColorPressed(palette.getColorAccentDark());
    }
    //endregion -- end --



    HabitDatabase.OnCategoryChangedListener onCategoryChangedListener = new HabitDatabase.OnCategoryChangedListener() {
        @Override
        public void onCategoryDeleted(HabitCategory removedCategory) {
            setResult(ResultCodes.HABIT_CHANGED);
            if(removedCategory.getDatabaseId() == mActivityState.habit.getCategory().getDatabaseId()){
                mActivityState.habit = mHabitDatabase.getHabit(mActivityState.habit.getDatabaseId());
                setUpActivityWithHabit(mActivityState.habit);
            }
        }

        @Override
        public void onCategoryAdded(HabitCategory newCategory) { }

        @Override
        public void onCategoryUpdated(HabitCategory oldCategory, HabitCategory newCategory) {
            setResult(ResultCodes.HABIT_CHANGED);
            if(oldCategory.getDatabaseId() == mActivityState.habit.getCategory().getDatabaseId()){
                mActivityState.habit = mHabitDatabase.getHabit(mActivityState.habit.getDatabaseId());
                setUpActivityWithHabit(mActivityState.habit);
            }
        }
    };

    //region visible lifetime (onStart - onStop)
    @Override
    protected void onStart() {
        super.onStart();

        // Set/Add listeners
        ui.tabs.addOnTabSelectedListener(onTabSelectedListener);
        HabitDatabase.addOnCategoryChangedListener(onCategoryChangedListener);
        ui.enterSessionFab.setOnClickListener(onEnterSessionFabClickedListener);
        ui.createEntryFab.setOnClickListener(onCreateEntryFabClickedListener);
        dateRangeManager.setDateRangeChangeListener(onDateRangeChangeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // remove listeners
        HabitDatabase.removeOnCategoryChangedListener(onCategoryChangedListener);
        ui.tabs.removeOnTabSelectedListener(onTabSelectedListener);
    }
    //endregion -- end --

    //region Methods to handle the menu lifetime
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_habit_data, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        //focus the SearchView
        if (mActivityState.query != null && !mActivityState.query.isEmpty()) {
            searchMenuItem.expandActionView();
            mSearchView.setQuery(mActivityState.query, false);
            mSearchView.clearFocus();
        }

        mSearchView.setQueryHint(getString(R.string.filter_entries));
        mSearchView.setOnQueryTextListener(onSearchQueryListener);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem archive = menu.findItem(R.id.menu_toggle_archive);
        if (archive != null) {
            if (mActivityState.habit.getIsArchived()) archive.setTitle(menu_unarchive);
            else archive.setTitle(R.string.menu_archive);
        }

        return super.onPrepareOptionsMenu(menu);
    }
    //endregion

    //endregion [ ---- end ---- ]

    //region Methods responsible for manipulating entries
    private SessionEntryCollection fetchEntriesWithinConditions(String query) {
        Set<Long> queryIds = mHabitDatabase.findEntryIdsByComment(
                mActivityState.habit.getDatabaseId(), query
        );

        Set<Long> dateRangeIds = mHabitDatabase.findEntriesWithinTimeRange(
                mActivityState.habit.getDatabaseId(),
                dateRangeManager.getDateFrom(), dateRangeManager.getDateTo()
        );

        queryIds.retainAll(dateRangeIds);

        return mHabitDatabase.lookUpEntries(queryIds);
    }

    private SessionEntryCollection fetchEntriesWithinTimeRange() {
        Set<Long> dateRangeIds = mHabitDatabase.findEntriesWithinTimeRange(
                mActivityState.habit.getDatabaseId(),
                dateRangeManager.getDateFrom(), dateRangeManager.getDateTo()
        );

        return mHabitDatabase.lookUpEntries(dateRangeIds);
    }

    private boolean checkIfEntryFitsWithinConditions(SessionEntry entry) {
        boolean fitsWithinRange = dateRangeManager.entryFitsRange(entry);
        boolean fitsQuery = true;

        CharSequence query = mSearchView.getQuery();
        if (query != null && query.length() != 0) {
            String stringQuery = String.valueOf(query);
            fitsQuery = entry.matchesQuery(stringQuery);
        }
        return fitsQuery && fitsWithinRange;
    }

    private void addNewEntry(SessionEntry newEntry) {
        int pos = mActivityState.entries.addEntry(newEntry);
        updateDateRangeManagerEntries(mActivityState.entries);
        if (checkIfEntryFitsWithinConditions(newEntry)) {
            mEntriesCallback.onNotifyEntryAdded(pos);
            mCalendarCallback.onUpdateEntries(mActivityState.entries);
            mStatisticsCallback.onUpdateEntries(mActivityState.entries);
        }
        else {
            mActivityState.entries.removeEntry(newEntry);
        }
    }

    private void updateEntry(SessionEntry oldEntry, SessionEntry newEntry) {
        int oldIndex = mActivityState.entries.indexOf(oldEntry);
        int newIndex = mActivityState.entries.updateEntry(oldEntry, newEntry);
        dateRangeManager.updateSessionEntries(mActivityState.entries);
        mEntriesCallback.onNotifyEntryUpdated(oldIndex, newIndex);
        mCalendarCallback.onUpdateEntries(mActivityState.entries);
        mStatisticsCallback.onUpdateEntries(mActivityState.entries);
    }

    private void removeEntry(SessionEntry oldEntry) {
        int pos = mActivityState.entries.removeEntry(oldEntry);
        dateRangeManager.updateSessionEntries(mActivityState.entries);
        mEntriesCallback.onNotifyEntryRemoved(pos);
        mCalendarCallback.onUpdateEntries(mActivityState.entries);
        mStatisticsCallback.onUpdateEntries(mActivityState.entries);
    }

    private void updateEntries(SessionEntryCollection sessionEntries) {
        updateDateRangeManagerEntries(sessionEntries);
        mEntriesCallback.onUpdateEntries(sessionEntries);
        mCalendarCallback.onUpdateEntries(sessionEntries);
        mStatisticsCallback.onUpdateEntries(mActivityState.entries);
    }

    private void updateDateRangeManagerEntries(SessionEntryCollection sessionEntries) {
        SessionEntry minEntry = mHabitDatabase.getMinEntry(mActivityState.habit.getDatabaseId());
        SessionEntry maxEntry = mHabitDatabase.getMaxEntry(mActivityState.habit.getDatabaseId());
        if (minEntry != null && maxEntry != null) {
            long minTime = minEntry.getStartingTimeIgnoreTimeOfDay();
            long maxTime = maxEntry.getStartingTimeIgnoreTimeOfDay();
            dateRangeManager.updateSessionEntries(sessionEntries.size(), sessionEntries.calculateDuration(), minTime, maxTime);
            sessionEntries.setDateFrom(dateRangeManager.getDateFrom());
            sessionEntries.setDateTo(dateRangeManager.getDateTo());
        }
        else {
            dateRangeManager.updateSessionEntries(new ArrayList<SessionEntry>(), -1, -1);
        }
    }

    private void clearEntries() {
        mActivityState.entries.clear();
        dateRangeManager.updateSessionEntries(mActivityState.entries);
        mEntriesCallback.onUpdateEntries(mActivityState.entries);
        mCalendarCallback.onUpdateEntries(mActivityState.entries);
        mStatisticsCallback.onUpdateEntries(mActivityState.entries);
    }
    //endregion -- end --

    //region Methods responsible for handling/dispatching events

    //region Filter events
    FloatingDateRangeWidgetManager.DateRangeChangeListener onDateRangeChangeListener =
            new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
                @Override
                public void onDateRangeChanged(long dateFrom, long dateTo) {
                    mActivityState.entries = fetchEntriesWithinConditions(mActivityState.query);
                    updateEntries(mActivityState.entries);
                }
            };

    SearchView.OnQueryTextListener onSearchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mActivityState.query = query.trim();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            mActivityState.query = query.trim();
            mActivityState.entries = query.length() > 0 ?
                    fetchEntriesWithinConditions(query) : fetchEntriesWithinTimeRange();

            updateEntries(mActivityState.entries);

            return false;
        }
    };
    //endregion -- end --

    //region Scroll events
    @Override
    public void onScrollUp() {
        boolean entriesFragmentShown = ui.tabs.getSelectedTabPosition() == 0;
        boolean statisticsFragmentShown = ui.tabs.getSelectedTabPosition() == 2;

        if (entriesFragmentShown || statisticsFragmentShown)
            dateRangeManager.showView(true);

        if (mPreferenceChecker.hideFabOnScroll() && entriesFragmentShown)
            ui.menuFab.showMenu(true);
    }

    @Override
    public void onScrollDown() {
        boolean entriesFragmentShown = ui.tabs.getSelectedTabPosition() == 0;
        boolean statisticsFragmentShown = ui.tabs.getSelectedTabPosition() == 2;

        if (entriesFragmentShown || statisticsFragmentShown)
            dateRangeManager.hideView(true);

        if (mPreferenceChecker.hideFabOnScroll() && entriesFragmentShown)
            ui.menuFab.hideMenu(true);
    }
    //endregion -- end --

    //region Entries fragment events
    @Override
    public void onEntryViewClicked(final long entryId, SessionEntry entry) {
        EditEntryForm.newInstance
                (entry, ContextCompat.getColor(this, R.color.textColorContrastBackground))
                .show(getSupportFragmentManager(), "edit-entry");
    }
    //endregion

    //region Methods to handle menu item events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case (android.R.id.home):
                if (ui.menuFab.isOpened())
                    ui.menuFab.close(true);
                else finish();
                break;

            case (R.id.menu_habit_edit):
                onHabitEditClicked();
                break;

            case (R.id.menu_enter_session):
                SessionActivity.startActivity(HabitDataActivity.this, mActivityState.habit);
                break;

            case (R.id.menu_toggle_archive):
                onMenuToggleArchiveClicked();
                break;

            case (R.id.menu_export_habit):
                mExportManager.shareExportHabit(mHabitDatabase.getHabit(mActivityState.habit.getDatabaseId()));
                break;

            case (R.id.menu_reset_habit):
                onMenuResetHabitClicked();
                break;

            case (R.id.menu_delete_habit):
                onMenuDeleteHabitClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void resetDialogListeners() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        DialogFragment dialog;

        if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-delete-habit")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-archive-habit")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-reset-habit")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("edit-habit")) != null)
            ((HabitDialog)dialog).setPositiveButton(null, onYesUpdateHabitClicked);
    }

    private void setDialogListener(ConfirmationDialog dialog) {
        switch (dialog.getTag()) {
            case "confirm-delete-habit":
                dialog.setOnYesClickListener(onYesDeleteHabitClicked);
                break;

            case "confirm-reset-habit":
                dialog.setOnYesClickListener(onYesResetHabitClick);
                break;

            case "confirm-archive-habit":
                dialog.setOnYesClickListener(onYesArchiveHabitClick);
                break;
        }
    }

    //region Code responsible for handling delete habit option clicks
    DialogInterface.OnClickListener onYesDeleteHabitClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mSessionManager.getIsSessionActive(mActivityState.habit.getDatabaseId())) {
                mSessionManager.cancelSession(mActivityState.habit.getDatabaseId());
            }
            mHabitDatabase.deleteHabit(mActivityState.habit.getDatabaseId());
            setResult(ResultCodes.HABIT_CHANGED);
            finish();
        }
    };

    private void onMenuDeleteHabitClicked() {
        String habitName = mHabitDatabase.getHabitName(mActivityState.habit.getDatabaseId());

        new ConfirmationDialog()
                .setIcon(R.drawable.ic_delete_forever_24dp)
                .setTitle(R.string.confirm_delete)
                .setMessage("Do you really want to delete '" + habitName + "'?")
                .setOnYesClickListener(onYesDeleteHabitClicked)
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
                .show(getSupportFragmentManager(), "confirm-delete-habit");
    }
    //endregion -- end --

    //region Code responsible for handling reset-habit option clicks
    DialogInterface.OnClickListener onYesResetHabitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mHabitDatabase.deleteEntriesForHabit(mActivityState.habit.getDatabaseId());
            clearEntries();
            Toast.makeText(HabitDataActivity.this, R.string.entries_deleted_message, Toast.LENGTH_SHORT).show();
        }
    };

    private void onMenuResetHabitClicked() {
        String habitName = mHabitDatabase.getHabitName(mActivityState.habit.getDatabaseId());

        new ConfirmationDialog()
                .setIcon(R.drawable.ic_delete_sweep_24dp)
                .setTitle(R.string.confirm_habit_data_reset_title)
                .setMessage("Do you really want to delete all entries for '" + habitName + "'?")
                .setOnYesClickListener(onYesResetHabitClick)
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
                .show(getSupportFragmentManager(), "confirm-reset-habit");
    }
    //endregion -- end --

    //region Code responsible for handling archive-habit option clicks
    DialogInterface.OnClickListener onYesArchiveHabitClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final boolean archivedState = mHabitDatabase.getIsHabitArchived(mActivityState.habit.getDatabaseId());
            mHabitDatabase.updateHabitIsArchived(mActivityState.habit.getDatabaseId(), !archivedState);
            mActivityState.habit.setIsArchived(!archivedState);
            setUpActivityWithHabit(mActivityState.habit);
            setResult(ResultCodes.HABIT_CHANGED);
        }
    };

    private void onMenuToggleArchiveClicked() {
        String habitName = mHabitDatabase.getHabitName(mActivityState.habit.getDatabaseId());

        final boolean archivedState = mHabitDatabase.getIsHabitArchived(mActivityState.habit.getDatabaseId());
        String actionName = archivedState ? getString(R.string.menu_unarchive) : getString(R.string.menu_archive);
        String actionNameLower = archivedState ? "unarchive" : "archive";
        int iconRes = archivedState ? R.drawable.ic_unarchive_24dp : R.drawable.ic_archive_2_24dp;

        new ConfirmationDialog()
                .setIcon(iconRes)
                .setTitle("Confirm " + actionName)
                .setMessage("Do you really want to " + actionNameLower + " '" + habitName + "'? ")
                .setOnYesClickListener(onYesArchiveHabitClick)
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
                .show(getSupportFragmentManager(), "confirm-archive-habit");
    }
    //endregion -- end --

    HabitDialog.DialogResult onYesUpdateHabitClicked = new HabitDialog.DialogResult() {
        @Override
        public void onResult(Habit initHabit, Habit habit) {
            mActivityState.habit = habit;
            mHabitDatabase.updateHabit(initHabit.getDatabaseId(), habit);
            setUpActivityWithHabit(mActivityState.habit);

            setResult(ResultCodes.HABIT_CHANGED);
        }
    };

    private void onHabitEditClicked() {
        int accentColor = ContextCompat.getColor(this, R.color.textColorContrastBackground);

        HabitDialog dialog = new HabitDialog()
                .setTitle("Edit Habit")
                .setInitHabit(mActivityState.habit)
                .setAccentColor(accentColor)
                .setPositiveButton("Update", onYesUpdateHabitClicked)
                .setNegativeButton("Cancel", null);

        dialog.show(getSupportFragmentManager(), "edit-habit");
    }

    //endregion -- end --

    //region EditEntryDialog.OnFinishedListener
    @Override
    public void onEditEntryUpdateEntry(SessionEntry newEntry) {
        if (newEntry != null) {
            SessionEntry oldEntry = mHabitDatabase.getEntry(newEntry.getDatabaseId());
            mHabitDatabase.updateEntry(newEntry.getDatabaseId(), newEntry);

            dateRangeManager.adjustDateRangeForEntry(newEntry);
            if (checkIfEntryFitsWithinConditions(newEntry)) updateEntry(oldEntry, newEntry);
            else removeEntry(oldEntry);
        }
    }

    @Override
    public void onEditEntryDeleteEntry(SessionEntry removeEntry) {
        mHabitDatabase.deleteEntry(removeEntry.getDatabaseId());
        removeEntry(removeEntry);
    }
    //endregion

    //region NewEntryDialog.OnFinishedListener
    @Override
    public void onNewEntryCreated(SessionEntry newEntry) {
        if (newEntry != null) {
            mHabitDatabase.addEntry(mActivityState.habit.getDatabaseId(), newEntry);
            dateRangeManager.adjustDateRangeForEntry(newEntry);
            addNewEntry(newEntry);
        }
    }
    //endregion

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mActivityState.saveState(outState);
        dateRangeManager.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (ui.menuFab.isOpened())
            ui.menuFab.close(true);
        else super.onBackPressed();
    }

    TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int position = tab.getPosition();
            switch (position) {
                case (0): {
                    ui.menuFab.showMenu(true);
                    dateRangeManager.showView(true);
                }
                break;

                case (1): {
                    ui.menuFab.hideMenu(true);
                    dateRangeManager.hideView(true);
                }
                break;

                case (2): {
                    ui.menuFab.hideMenu(true);
                    dateRangeManager.showView(true);
                }
                break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            if (tab.getPosition() == 0 && mEntriesCallback != null)
                mEntriesCallback.onTabReselected();
            else if (tab.getPosition() == 1 && mCalendarCallback != null)
                mCalendarCallback.onTabReselected();
            else if (tab.getPosition() == 2 && mStatisticsCallback != null)
                mStatisticsCallback.onTabReselected();
        }
    };

    View.OnClickListener onCreateEntryFabClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ui.menuFab.close(true);

            NewEntryForm.newInstance
                    (ContextCompat.getColor(HabitDataActivity.this, R.color.textColorContrastBackground))
                    .show(getSupportFragmentManager(), "new-entry");
        }
    };

    View.OnClickListener onEnterSessionFabClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ui.menuFab.close(true);
            SessionActivity.startActivity(HabitDataActivity.this, mActivityState.habit);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.SESSION_ACTIVITY && resultCode == ResultCodes.SESSION_FINISH) {
            SessionEntry newEntry = (SessionEntry) data.getSerializableExtra(SessionActivity.RESULT_NEW_ENTRY);
            addNewEntry(newEntry);
        }

    }

    //endregion -- end --

    //region Getters {}
    @Override
    public Habit getHabit() {
        return mActivityState.habit;
    }

    @Override
    public ThemeColorPalette getColorPalette() {
        return new ThemeColorPalette(mActivityState.habit.getColor());
    }

    @Override
    public SessionEntryCollection getSessionEntries() {
        return mActivityState.entries;
    }

    @Override
    public CategoryDataCollection getCategoryDataSample() {
        return mHabitDatabase.getCategoryDataSample
                (mActivityState.habit.getCategory(), dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
    }
    //endregion -- end --

    public static void startActivity(Activity activity, long habitId) {
        Intent intent = new Intent(activity, HabitDataActivity.class);
        intent.putExtra(EntriesTableSchema.ENTRY_HABIT_ID, habitId);
        activity.startActivityForResult(intent, RequestCodes.HABIT_DATA_ACTIVITY);
    }

}
