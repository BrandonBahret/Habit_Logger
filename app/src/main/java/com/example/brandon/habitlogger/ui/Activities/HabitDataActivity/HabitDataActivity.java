package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
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
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema.EntriesTableSchema;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.databinding.ActivityHabitDataBinding;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.EntriesFragment;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity.SessionActivity;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.EditEntryForm;
import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.EntryFormDialogBase;
import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.NewEntryForm;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.EditHabitDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.HabitDialogBase;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.NewHabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.FloatingDateRangeWidgetManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.brandon.habitlogger.R.string.menu_unarchive;

public class HabitDataActivity extends AppCompatActivity implements
        IHabitDataCallback.IUpdateEntries, IHabitDataCallback.IUpdateCategoryData,
        IHabitDataCallback, IScrollEvents, EntriesFragment.IEntriesEvents {

    //region (Member attributes)

    private static final String KEY_SEARCH_QUERY = "KEY_SEARCH_QUERY";
    private static final String KEY_SESSION_ENTRIES = "KEY_SESSION_ENTRIES";
    private String mSearchQuery = "";

    // Dependencies
    private HabitDatabase mHabitDatabase;
    private LocalDataExportManager mExportManager;
    private SessionManager mSessionManager;
    private PreferenceChecker mPreferenceChecker;

    // Data
    private Habit mHabit;
    private SessionEntryCollection mSessionEntries = new SessionEntryCollection();

    // View related members
    FloatingDateRangeWidgetManager dateRangeManager;
    private SearchView mSearchView;
    ActivityHabitDataBinding ui;
    private HabitDataActivityPagerAdapter mSectionsPagerAdapter;

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

        boolean usesSavedInstance = savedInstanceState != null;
        if (usesSavedInstance) {
            mSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY);
            List<SessionEntry> entries = savedInstanceState.getParcelableArrayList(KEY_SESSION_ENTRIES);
            mSessionEntries = new SessionEntryCollection(entries);
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
        mHabit = mHabitDatabase.getHabit(habitId);
        if (!usesSavedInstance) {
            mSessionEntries = mHabitDatabase.getEntries(habitId);
        }

        // Set up activity
        ui = DataBindingUtil.setContentView(this, R.layout.activity_habit_data);

        setSupportActionBar(ui.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new HabitDataActivityPagerAdapter(getSupportFragmentManager(), this);
        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), mSessionEntries);
        ui.container.setAdapter(mSectionsPagerAdapter);
        ui.tabs.setupWithViewPager(ui.container);
        ui.menuFab.setClosedOnTouchOutside(true);

        if (ui.tabs.getSelectedTabPosition() != 1) {
            dateRangeManager.hideView(false);
            ui.menuFab.hideMenu(false);
            ui.container.setCurrentItem(1);
        }

        if (!usesSavedInstance) {
            mSessionEntries = fetchEntriesWithinTimeRange();
            dateRangeManager.updateSessionEntries(mSessionEntries);
        }
        else {
            dateRangeManager.restoreState(savedInstanceState);
        }

        setUpActivityWithHabit(mHabit);

    }

    private void setUpActivityWithHabit(Habit habit) {
        ui.toolbar.setTitle(mHabit.getName());
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

    //region visible lifetime (onStart - onStop)
    @Override
    protected void onStart() {
        super.onStart();

        // Set/Add listeners
        ui.tabs.addOnTabSelectedListener(onTabSelectedListener);
        ui.enterSessionFab.setOnClickListener(onEnterSessionFabClickedListener);
        ui.createEntryFab.setOnClickListener(onCreateEntryFabClickedListener);
        dateRangeManager.setDateRangeChangeListener(onDateRangeChangeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // remove listeners
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
        if (mSearchQuery != null && !mSearchQuery.isEmpty()) {
            searchMenuItem.expandActionView();
            mSearchView.setQuery(mSearchQuery, false);
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
            if (mHabit.getIsArchived()) archive.setTitle(menu_unarchive);
            else archive.setTitle(R.string.menu_archive);
        }

        return super.onPrepareOptionsMenu(menu);
    }
    //endregion

    //endregion [ ---- end ---- ]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_QUERY, mSearchQuery);
        outState.putParcelableArrayList(KEY_SESSION_ENTRIES, mSessionEntries);
        dateRangeManager.onSaveInstanceState(outState);
    }

    //region Methods responsible for manipulating entries
    private SessionEntryCollection fetchEntriesWithinConditions(String query) {
        Set<Long> queryIds = mHabitDatabase.findEntryIdsByComment(
                mHabit.getDatabaseId(), query
        );

        Set<Long> dateRangeIds = mHabitDatabase.findEntriesWithinTimeRange(
                mHabit.getDatabaseId(),
                dateRangeManager.getDateFrom(), dateRangeManager.getDateTo()
        );

        queryIds.retainAll(dateRangeIds);

        return mHabitDatabase.lookUpEntries(queryIds);
    }

    private SessionEntryCollection fetchEntriesWithinTimeRange() {
        Set<Long> dateRangeIds = mHabitDatabase.findEntriesWithinTimeRange(
                mHabit.getDatabaseId(),
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
        int pos = mSessionEntries.addEntry(newEntry);
        updateDateRangeManagerEntries(mSessionEntries);
        if (checkIfEntryFitsWithinConditions(newEntry)) {
            mEntriesCallback.onNotifyEntryAdded(pos);
            mCalendarCallback.onUpdateEntries(mSessionEntries);
            mStatisticsCallback.onUpdateEntries(mSessionEntries);
        }
        else {
            mSessionEntries.removeEntry(newEntry);
        }
    }

    private void updateEntry(SessionEntry oldEntry, SessionEntry newEntry) {
        int oldIndex = mSessionEntries.indexOf(oldEntry);
        int newIndex = mSessionEntries.updateEntry(oldEntry, newEntry);
        dateRangeManager.updateSessionEntries(mSessionEntries);
        mEntriesCallback.onNotifyEntryUpdated(oldIndex, newIndex);
        mCalendarCallback.onUpdateEntries(mSessionEntries);
        mStatisticsCallback.onUpdateEntries(mSessionEntries);
    }

    private void removeEntry(SessionEntry oldEntry) {
        int pos = mSessionEntries.removeEntry(oldEntry);
        dateRangeManager.updateSessionEntries(mSessionEntries);
        mEntriesCallback.onNotifyEntryRemoved(pos);
        mCalendarCallback.onUpdateEntries(mSessionEntries);
        mStatisticsCallback.onUpdateEntries(mSessionEntries);
    }

    private void updateEntries(SessionEntryCollection sessionEntries) {
        updateDateRangeManagerEntries(sessionEntries);
        mEntriesCallback.onUpdateEntries(sessionEntries);
        mCalendarCallback.onUpdateEntries(sessionEntries);
        mStatisticsCallback.onUpdateEntries(mSessionEntries);
    }

    private void updateDateRangeManagerEntries(SessionEntryCollection sessionEntries) {
        SessionEntry minEntry = mHabitDatabase.getMinEntry(mHabit.getDatabaseId());
        SessionEntry maxEntry = mHabitDatabase.getMaxEntry(mHabit.getDatabaseId());
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
        mSessionEntries.clear();
        dateRangeManager.updateSessionEntries(mSessionEntries);
        mEntriesCallback.onUpdateEntries(mSessionEntries);
        mCalendarCallback.onUpdateEntries(mSessionEntries);
        mStatisticsCallback.onUpdateEntries(mSessionEntries);
    }
    //endregion -- end --

    //region Methods responsible for handling/dispatching events

    //region Scroll events
    @Override
    public void onScrollUp() {
        dateRangeManager.showView(true);
        if (mPreferenceChecker.hideFabOnScroll() && ui.tabs.getSelectedTabPosition() == 0)
            ui.menuFab.showMenu(true);
    }

    @Override
    public void onScrollDown() {
        dateRangeManager.hideView(true);
        if (mPreferenceChecker.hideFabOnScroll() && ui.tabs.getSelectedTabPosition() == 0)
            ui.menuFab.hideMenu(true);
    }
    //endregion -- end --

    //region Entries fragment events
    @Override
    public void onEntryViewClicked(final long entryId, SessionEntry entry) {
        EditEntryForm dialog = EditEntryForm.newInstance(
                entry, ContextCompat.getColor(this, R.color.textColorContrastBackground)
        );

        dialog.setOnFinishedListener(new EditEntryForm.OnFinishedListener() {
            @Override
            public void onPositiveClicked(SessionEntry newEntry) {
                if (newEntry != null) {
                    SessionEntry oldEntry = mHabitDatabase.getEntry(entryId);
                    mHabitDatabase.updateEntry(entryId, newEntry);

                    dateRangeManager.adjustDateRangeForEntry(newEntry);
                    if (checkIfEntryFitsWithinConditions(newEntry)) updateEntry(oldEntry, newEntry);
                    else removeEntry(oldEntry);
                }
            }

            @Override
            public void onNegativeClicked(SessionEntry entry) {
                mHabitDatabase.deleteEntry(entryId);
                removeEntry(entry);
            }
        });

        dialog.show(getSupportFragmentManager(), "edit-entry");
    }
    //endregion

    //region Methods to handle menu item events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case (android.R.id.home):
                finish();
                break;

            case (R.id.menu_habit_edit):
                onHabitEditClicked();
                break;

            case (R.id.menu_enter_session):
                SessionActivity.startActivity(HabitDataActivity.this, mHabit);
                break;

            case (R.id.menu_toggle_archive):
                onMenuToggleArchiveClicked();
                break;

            case (R.id.menu_export_habit):
                mExportManager.shareExportHabit(mHabitDatabase.getHabit(mHabit.getDatabaseId()));
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

    private void onMenuDeleteHabitClicked() {
        String habitName = mHabitDatabase.getHabitName(mHabit.getDatabaseId());

        new ConfirmationDialog(HabitDataActivity.this)
                .setTitle(R.string.confirm_delete)
                .setMessage("Do you really want to delete '" + habitName + "'?")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mSessionManager.getIsSessionActive(mHabit.getDatabaseId())) {
                            mSessionManager.cancelSession(mHabit.getDatabaseId());
                        }
                        mHabitDatabase.deleteHabit(mHabit.getDatabaseId());
                        finish();
                    }
                })
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
                .show();
    }

    private void onMenuResetHabitClicked() {
        String habitName = mHabitDatabase.getHabitName(mHabit.getDatabaseId());

        new ConfirmationDialog(HabitDataActivity.this)
                .setTitle(R.string.confirm_habit_data_reset_title)
                .setMessage("Do you really want to delete all entries for '" + habitName + "'?")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHabitDatabase.deleteEntriesForHabit(mHabit.getDatabaseId());
                        clearEntries();
                        Toast.makeText(HabitDataActivity.this, R.string.entries_deleted_message, Toast.LENGTH_SHORT).show();
                    }
                })
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
                .show();
    }

    private void onMenuToggleArchiveClicked() {
        String habitName = mHabitDatabase.getHabitName(mHabit.getDatabaseId());
        final boolean archivedState = mHabitDatabase.getIsHabitArchived(mHabit.getDatabaseId());
        String actionName = archivedState ? getString(R.string.menu_unarchive) : getString(R.string.menu_archive);
        String actionNameLower = archivedState ? "unarchive" : "archive";

        new ConfirmationDialog(HabitDataActivity.this)
                .setTitle("Confirm " + actionName)
                .setMessage("Do you really want to " + actionNameLower + " '" + habitName + "'? ")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHabitDatabase.updateHabitIsArchived(mHabit.getDatabaseId(), !archivedState);
                        mHabit.setIsArchived(!archivedState);
                        setUpActivityWithHabit(mHabit);
                    }
                })
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
                .show();
    }

    private void onHabitEditClicked() {
        HabitDialogBase.OnFinishedListener onFinished = new NewHabitDialog.OnFinishedListener() {
            @Override
            public void onFinishedWithResult(Habit habit) {
                mHabit = habit;
                mHabitDatabase.updateHabit(habit.getDatabaseId(), habit);
                setUpActivityWithHabit(mHabit);
            }
        };

        int accentColor = ContextCompat.getColor(this, R.color.textColorContrastBackground);
        EditHabitDialog dialog = EditHabitDialog.newInstance(onFinished, accentColor, mHabit);
        dialog.show(getSupportFragmentManager(), "edit-mHabit");
    }
    //endregion -- end --

    FloatingDateRangeWidgetManager.DateRangeChangeListener onDateRangeChangeListener =
            new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
                @Override
                public void onDateRangeChanged(long dateFrom, long dateTo) {
                    mSessionEntries = fetchEntriesWithinConditions(mSearchQuery);
                    updateEntries(mSessionEntries);
                }
            };

    SearchView.OnQueryTextListener onSearchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mSearchQuery = query;
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mSearchQuery = newText;
            mSessionEntries = newText.length() > 0 ?
                    fetchEntriesWithinConditions(newText) : fetchEntriesWithinTimeRange();

            updateEntries(mSessionEntries);

            return false;
        }
    };

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

            EntryFormDialogBase.OnFinishedListener onFinished = new NewEntryForm.OnFinishedListener() {
                @Override
                public void onPositiveClicked(SessionEntry entry) {
                    if (entry != null) {
                        mHabitDatabase.addEntry(mHabit.getDatabaseId(), entry);
                        dateRangeManager.adjustDateRangeForEntry(entry);
                        addNewEntry(entry);
                    }
                }

                @Override
                public void onNegativeClicked(SessionEntry entry) {}
            };

            NewEntryForm dialog = NewEntryForm.newInstance(ContextCompat.getColor(HabitDataActivity.this, R.color.textColorContrastBackground));
            dialog.setOnFinishedListener(onFinished);
            dialog.show(getSupportFragmentManager(), "new-entry");
        }
    };

    View.OnClickListener onEnterSessionFabClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ui.menuFab.close(true);
            SessionActivity.startActivity(HabitDataActivity.this, mHabit);
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
        return mHabit;
    }

    @Override
    public ThemeColorPalette getColorPalette() {
        return new ThemeColorPalette(mHabit.getColor());
    }

    @Override
    public SessionEntryCollection getSessionEntries() {
        return mSessionEntries;
    }

    @Override
    public CategoryDataCollection getCategoryDataSample() {
        return mHabitDatabase.getCategoryDataSample
                (mHabit.getCategory(), dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
    }
    //endregion -- end --

    public static void startActivity(Activity activity, long habitId) {
        Intent intent = new Intent(activity, HabitDataActivity.class);
        intent.putExtra(EntriesTableSchema.ENTRY_HABIT_ID, habitId);
        activity.startActivity(intent);
    }

}
