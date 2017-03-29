package com.example.brandon.habitlogger.HabitActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.brandon.habitlogger.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitSessions.SessionActivity;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ModifyHabitActivity.EditHabitDialog;
import com.example.brandon.habitlogger.ModifyHabitActivity.NewHabitDialog;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.ConfirmationDialog;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.databinding.ActivityHabitBinding;
import com.example.brandon.habitlogger.ui.FloatingDateRangeWidgetManager;
import com.example.brandon.habitlogger.ui.RecyclerViewScrollObserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class HabitActivity extends AppCompatActivity implements CallbackInterface, RecyclerViewScrollObserver.IScrollEvents {

    //region (Member attributes)
    public static String HABIT_ID = "HABIT_ID";

    private HabitDatabase mHabitDatabase;
    private List<SessionEntry> mSessionEntries = new ArrayList<>();
    private LocalDataExportManager mExportManager;
    private SessionManager mSessionManager;
    private Habit mHabit;
    private long mHabitId;

    FloatingDateRangeWidgetManager dateRangeManager;
    ActivityHabitBinding ui;

    private List<IUpdateEntries> mSessionEntriesCallbacks = new ArrayList<>();
    private List<IUpdateCategorySample> mCategoryDataSampleCallbacks = new ArrayList<>();
    //endregion

    //region Methods responsible for communication between this activity and fragments

    //region Methods to add and remove callback methods
    @Override
    public void addUpdateEntriesCallback(IUpdateEntries callback) {
        mSessionEntriesCallbacks.add(callback);
    }

    @Override
    public void removeUpdateEntriesCallback(IUpdateEntries callback) {
        mSessionEntriesCallbacks.remove(callback);
    }

    @Override
    public void addUpdateCategoryDataSampleCallback(IUpdateCategorySample callback) {
        mCategoryDataSampleCallbacks.add(callback);
    }

    ;

    @Override
    public void removeUpdateCategoryDataSampleCallback(IUpdateCategorySample callback) {
        mCategoryDataSampleCallbacks.remove(callback);
    }
    //endregion

    @Override
    public SessionEntriesSample getSessionEntries() {
        return new SessionEntriesSample
                (mSessionEntries, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
    }

    @Override
    public CategoryDataSample getCategoryDataSample() {
        return mHabitDatabase.getCategoryDataSample
                (mHabit.getCategory(), dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
    }

    @Override
    public int getDefaultColor() {
        return mHabit.getColor();
    }
    //endregion

    //region Methods responsible for handling the activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(HabitActivity.this, R.layout.activity_habit);
        setSupportActionBar(ui.toolbar);

        //region Gather dependencies
        mSessionManager = new SessionManager(this);
        mExportManager = new LocalDataExportManager(this);
        mHabitDatabase = new HabitDatabase(this);
        HabitDatabase.addOnEntryChangedListener(getOnEntryChangeInDatabaseListener());

        Intent data = getIntent();
        mHabitId = data.getLongExtra(HABIT_ID, -1);
        mHabit = mHabitDatabase.getHabit(mHabitId);
        mSessionEntries = mHabitDatabase.getEntries(mHabitId);
        //endregion

        HabitActivityPagerAdapter pagerAdapter = new HabitActivityPagerAdapter(getSupportFragmentManager());
        ui.container.setAdapter(pagerAdapter);
        ui.container.addOnPageChangeListener(getOnPageChangedListener());

        ui.tabs.setupWithViewPager(ui.container);

        ui.menuFab.hideMenu(false);
        ui.menuFab.setClosedOnTouchOutside(true);
        ui.enterSessionFab.setOnClickListener(getOnEnterSessionFabClickedListener());
        ui.createEntryFab.setOnClickListener(getOnCreateEntryFabClickedListener());

        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), mSessionEntries);
        dateRangeManager.setDateRangeChangeListener(getDateRangeChangeListener());
        dateRangeManager.hideView(false);

        updateActivity();
        ui.container.setCurrentItem(1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSessionEntriesCallbacks.clear();
        mCategoryDataSampleCallbacks.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_habit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem archive = menu.findItem(R.id.menu_toggle_archive);
        if (archive != null) {
            if (mHabit.getIsArchived()) {
                archive.setTitle("Unarchive");
            }
            else {
                archive.setTitle("Archive");
            }
        }

        MenuItem search = menu.findItem(R.id.search);
        if (search != null) {
            SearchView searchView = (SearchView) search.getActionView();
            searchView.setQueryHint(getString(R.string.filter_entries));
            searchView.setOnQueryTextListener(getOnSearchQueryListener());
        }

        return super.onPrepareOptionsMenu(menu);
    }
    //endregion

    //region Methods responsible for changing the appearance of the activity
    private void updateActivity() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mHabit.getName());
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        updateColorTheme();
    }

    private void updateColorTheme() {
        int color = 0xFFCCCCCC;
        int darkerColor = 0xFFBBBBBB;

        if (!mHabit.getIsArchived()) {
            color = mHabit.getCategory().getColorAsInt();
            darkerColor = HabitCategory.darkenColor(color, 0.7f);
        }

        getWindow().setStatusBarColor(darkerColor);
        ui.tabs.setBackgroundColor(color);

        ui.toolbar.setBackgroundColor(color);

        ui.menuFab.setMenuButtonColorNormal(color);
        ui.menuFab.setMenuButtonColorPressed(darkerColor);

        ui.enterSessionFab.setColorNormal(color);
        ui.enterSessionFab.setColorPressed(darkerColor);

        ui.createEntryFab.setColorNormal(color);
        ui.createEntryFab.setColorPressed(darkerColor);
    }
    //endregion

    //region Methods responsible for handling events

    //region Methods to handle new entry fab events
    private View.OnClickListener getOnCreateEntryFabClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewEntryForm dialog = new NewEntryForm();
                dialog.setOnFinishedListener(new NewEntryForm.OnFinishedListener() {
                    @Override
                    public void onUpdateClicked(SessionEntry entry) {
                        if (entry != null) {
                            mHabitDatabase.addEntry(mHabitId, entry);
                            mSessionEntries.add(entry);
                            updateEntries(mSessionEntries);
                        }
                    }

                    @Override
                    public void onDeleteClicked(SessionEntry entry) {

                    }
                });

                ui.menuFab.close(true);
                dialog.show(getSupportFragmentManager(), "new-entry");
            }
        };
    }

    private View.OnClickListener getOnEnterSessionFabClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui.menuFab.close(true);
                startSession();
            }
        };
    }
    //endregion

    //region Methods to handle HabitActivityPagerAdapter events
    private ViewPager.SimpleOnPageChangeListener getOnPageChangedListener() {
        return new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case (0): {
                        ui.menuFab.showMenu(true);
                        dateRangeManager.showView();
                    }
                    break;

                    case (1): {
                        ui.menuFab.hideMenu(true);
                        dateRangeManager.hideView();
                    }
                    break;

                    case (2): {
                        ui.menuFab.hideMenu(true);
                        dateRangeManager.showView();
                    }
                    break;
                }
            }
        };
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
                startSession();
                break;

            case (R.id.menu_toggle_archive):
                onMenuToggleArchiveClicked();
                break;

            case (R.id.menu_export_habit):
                mExportManager.shareExportHabit(mHabitDatabase.getHabit(mHabitId));
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
        String habitName = mHabitDatabase.getHabitName(mHabitId);

        new ConfirmationDialog(HabitActivity.this)
                .setTitle("Confirm Delete")
                .setMessage("Do you really want to delete '" + habitName + "'?")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mSessionManager.getIsSessionActive(mHabitId)) {
                            mSessionManager.cancelSession(mHabitId);
                        }

                        mHabitDatabase.deleteHabit(mHabitId);
                        finish();
                    }
                })
                .show();
    }

    private void onMenuResetHabitClicked() {
        String habitName = mHabitDatabase.getHabitName(mHabitId);

        new ConfirmationDialog(HabitActivity.this)
                .setTitle("Confirm Data Reset")
                .setMessage("Do you really want to delete all entries for '" + habitName + "'?")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHabitDatabase.deleteEntriesForHabit(mHabitId);
                    }
                })
                .show();
    }

    private void onMenuToggleArchiveClicked() {
        String habitName = mHabitDatabase.getHabitName(mHabitId);
        final boolean archivedState = mHabitDatabase.getIsHabitArchived(mHabitId);
        String actionName = archivedState ? "Unarchive" : "Archive";
        String actionNameLower = archivedState ? "unarchive" : "archive";

        new ConfirmationDialog(HabitActivity.this)
                .setTitle("Confirm " + actionName)
                .setMessage("Do you really want to " + actionNameLower + " '" + habitName + "'? ")
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHabitDatabase.updateHabitIsArchived(mHabitId, !archivedState);
                        mHabit.setIsArchived(!archivedState);
                        updateColorTheme();
                    }
                })
                .show();
    }

    private void onHabitEditClicked() {
        EditHabitDialog dialog = EditHabitDialog.newInstance(new NewHabitDialog.OnFinishedListener() {
            @Override
            public void onFinishedWithResult(Habit habit) {
                HabitActivity.this.mHabit = habit;
                mHabitDatabase.updateHabit(habit.getDatabaseId(), habit);
                updateActivity();
            }
        }, mHabit);

        dialog.show(getSupportFragmentManager(), "edit-mHabit");
    }
    //endregion

    //region Methods to handle search events
    public void processQuery(String query) {
        List<SessionEntry> entries = mHabitDatabase.lookUpEntries(
                mHabitDatabase.searchEntryIdsByComment(mHabitId, query)
        );

        dateRangeManager.updateSessionEntries(entries);
    }

    private SearchView.OnQueryTextListener getOnSearchQueryListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                processQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processQuery(newText);
                return true;
            }
        };
    }
    //endregion

    //region Methods to handle scroll events
    @Override
    public void onScrollUp() {
        dateRangeManager.showView();
        if (ui.tabs.getSelectedTabPosition() == 0)
            ui.menuFab.showMenu(true);
    }

    @Override
    public void onScrollDown() {
        dateRangeManager.hideView();
        if (ui.tabs.getSelectedTabPosition() == 0)
            ui.menuFab.hideMenu(true);
    }
    //endregion

    //region Methods to handle DateRangeManager events
    private FloatingDateRangeWidgetManager.DateRangeChangeListener getDateRangeChangeListener() {
        return new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
            @Override
            public void onDateRangeChanged(long dateFrom, long dateTo) {
                Set<Long> ids = mHabitDatabase.searchEntriesWithTimeRangeForAHabit(mHabitId, dateFrom, dateTo);
                HabitActivity.this.mSessionEntries = mHabitDatabase.lookUpEntries(ids);
                dateRangeManager.updateSessionEntries(HabitActivity.this.mSessionEntries);
                updateEntries(HabitActivity.this.mSessionEntries);
            }
        };
    }
    //endregion

    //region Methods to handle session entry change events
    private HabitDatabase.OnEntryChangedListener getOnEntryChangeInDatabaseListener() {
        return new HabitDatabase.OnEntryChangedListener() {
            @Override
            public void onEntryDeleted(SessionEntry removedEntry) {
                Set<Long> ids = mHabitDatabase.searchEntriesWithTimeRangeForAHabit(mHabitId, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
                HabitActivity.this.mSessionEntries = mHabitDatabase.lookUpEntries(ids);
                dateRangeManager.updateSessionEntries(HabitActivity.this.mSessionEntries);
            }

            @Override
            public void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry) {
                dateRangeManager.entryChanged(oldEntry, newEntry);

                Set<Long> ids = mHabitDatabase.searchEntriesWithTimeRangeForAHabit(mHabitId, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
                HabitActivity.this.mSessionEntries = mHabitDatabase.lookUpEntries(ids);
                dateRangeManager.updateSessionEntries(HabitActivity.this.mSessionEntries);
            }

            @Override
            public void onEntriesReset(long habitId) {
                if (habitId == HabitActivity.this.mHabitId) {
                    mSessionEntries = new ArrayList<>();
                    updateEntries(mSessionEntries);
                }
            }
        };
    }
    //endregion

    //endregion

    public void startSession() {
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) mHabit);
        startActivity(startSession);
    }

    public void updateEntries(List<SessionEntry> sessionEntries) {

        dateRangeManager.updateSessionEntries(sessionEntries);
        SessionEntriesSample entriesDataSample = getSessionEntries();
        CategoryDataSample categoryDataSample = getCategoryDataSample();

        for (IUpdateEntries callback : mSessionEntriesCallbacks)
            callback.updateEntries(entriesDataSample);

        for (IUpdateCategorySample callback : mCategoryDataSampleCallbacks)
            callback.updateCategoryDataSample(categoryDataSample);
    }

    public class HabitActivityPagerAdapter extends FragmentPagerAdapter {
        public HabitActivityPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public final String[] titles = getResources().getStringArray(R.array.tab_titles);

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EntriesFragment.newInstance();

                case 1:
                    return CalendarFragment.newInstance();

                case 2:
                    return StatisticsFragment.newInstance();
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 pages in total
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}