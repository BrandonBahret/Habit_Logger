package com.example.brandon.habitlogger.HabitActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
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
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.ConfirmationDialog;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.ui.FloatingDateRangeWidgetManager;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class HabitActivity extends AppCompatActivity implements CallbackInterface, GetScrollEventsFromFragmentsInterface, HabitDatabase.OnEntryChangedListener {

    private ViewPager viewPager;

    HabitDatabase habitDatabase;
    PreferenceChecker preferenceChecker;
    List<SessionEntry> sessionEntries = new ArrayList<>();
    LocalDataExportManager exportManager;
    SessionManager sessionManager;
    public Habit habit;
    long habitId;

    TabLayout tabLayout;
    Toolbar toolbar;
    FloatingActionMenu fabMenu;
    FloatingActionButton enterSession, createEntry;
    FloatingDateRangeWidgetManager dateRangeManager;

    List<UpdateEntriesInterface> callbacks = new ArrayList<>();
    List<UpdateCategorySampleInterface> newCategoryDataSampleCallbacks = new ArrayList<>();

    private GestureDetectorCompat mDetector;

    @Override
    public void addCallback(UpdateEntriesInterface callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeCallback(UpdateEntriesInterface callback) {
        callbacks.remove(callback);
    }

    @Override
    public SessionEntriesSample getSessionEntries() {
        return new SessionEntriesSample(sessionEntries, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
    }

    @Override
    public void addOnNewCategoryDataSampleCallback(UpdateCategorySampleInterface callback) {
        newCategoryDataSampleCallbacks.add(callback);
    }

    @Override
    public CategoryDataSample getCategoryDataSample() {
        long dateFrom = dateRangeManager.getDateFrom();
        long dateTo = dateRangeManager.getDateTo();

        return habitDatabase.getCategoryDataSample(habit.getCategory(), dateFrom, dateTo);
    }

    @Override
    public int getDefaultColor() {
        return habit.getColor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        fabMenu = (FloatingActionMenu) findViewById(R.id.menu_fab);
        enterSession = (FloatingActionButton) findViewById(R.id.enter_session_fab);
        createEntry = (FloatingActionButton) findViewById(R.id.create_entry_fab);
        viewPager = (ViewPager) findViewById(R.id.container);

        preferenceChecker = new PreferenceChecker(this);
        habitDatabase = new HabitDatabase(this);

        HabitDatabase.addOnEntryChangedListener(this);

        sessionManager = new SessionManager(this);
        exportManager = new LocalDataExportManager(this);

        Intent data = getIntent();
        habitId = data.getLongExtra("habitId", -1);
        habit = habitDatabase.getHabit(habitId);
        sessionEntries = habitDatabase.getEntries(habitId);

        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), sessionEntries);
        dateRangeManager.setDateRangeChangeListener(new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
            @Override
            public void onDateRangeChanged(long dateFrom, long dateTo) {
                Set<Long> ids = habitDatabase.searchEntriesWithTimeRangeForAHabit(habitId, dateFrom, dateTo);
                HabitActivity.this.sessionEntries = habitDatabase.lookUpEntries(ids);
                dateRangeManager.updateSessionEntries(HabitActivity.this.sessionEntries);
                updateEntries(HabitActivity.this.sessionEntries);
            }
        });
        dateRangeManager.callOnDateRangeChangedListener();

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(sectionsPagerAdapter);


        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case (0): {
                        fabMenu.showMenu(true);
                        dateRangeManager.showView();

                    }
                    break;

                    case (1): {
                        fabMenu.hideMenu(true);
                        dateRangeManager.hideView();

                    }
                    break;

                    case (2): {
                        fabMenu.hideMenu(true);
                        dateRangeManager.showView();

                    }
                    break;
                }
            }
        });


        tabLayout.setupWithViewPager(viewPager);

        fabMenu.setClosedOnTouchOutside(true);

        enterSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                startSession();
            }
        });

        createEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NewEntryForm dialog = new NewEntryForm();
                dialog.setOnFinishedListener(new NewEntryForm.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(SessionEntry entry) {
                        if (entry != null) {
                            habitDatabase.addEntry(habitId, entry);
                            sessionEntries.add(entry);
                            updateEntries(sessionEntries);
                        }
                    }

                    @Override
                    public void onDeleteClicked(SessionEntry entry) {

                    }
                });

                fabMenu.close(true);
                dialog.show(getSupportFragmentManager(), "new-entry");
            }
        });

        updateActivity();
        fabMenu.hideMenu(false);
        dateRangeManager.hideView(false);
        viewPager.setCurrentItem(1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        callbacks.clear();
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
            if (habit.getIsArchived()) {
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
            searchView.setOnQueryTextListener(
                    new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            processQuery(query);
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String query) {
                            processQuery(query);
                            return false;
                        }
                    }
            );
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public void processQuery(String query) {
        List<SessionEntry> entries = habitDatabase.lookUpEntries(
                habitDatabase.searchEntryIdsByComment(habitId, query)
        );

        dateRangeManager.updateSessionEntries(entries);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case (android.R.id.home): {
                finish();
            }
            break;

            case (R.id.menu_habit_edit): {

                EditHabitDialog dialog = EditHabitDialog.newInstance(new NewHabitDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(Habit habit) {
                        HabitActivity.this.habit = habit;
                        habitDatabase.updateHabit(habit.getDatabaseId(), habit);
                        updateActivity();
                    }
                }, habit);

                dialog.show(getSupportFragmentManager(), "edit-habit");
            }
            break;

            case (R.id.menu_enter_session): {
                startSession();
            }
            break;

            case (R.id.menu_toggle_archive): {

                String habitName = habitDatabase.getHabitName(habitId);
                final boolean archivedState = habitDatabase.getIsHabitArchived(habitId);
                String actionName = archivedState ? "Unarchive" : "Archive";
                String actionNameLower = archivedState ? "unarchive" : "archive";

                new ConfirmationDialog(HabitActivity.this)
                        .setTitle("Confirm " + actionName)
                        .setMessage("Do you really want to " + actionNameLower + " '" + habitName + "'? ")
                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                habitDatabase.updateHabitIsArchived(habitId, !archivedState);
                                habit.setIsArchived(!archivedState);
                                updateColorTheme();
                            }
                        })
                        .show();

            }
            break;

            case (R.id.menu_export_habit): {
                Habit habit = habitDatabase.getHabit(habitId);
                exportManager.shareExportHabit(habit);
            }
            break;

            case (R.id.menu_reset_habit): {

                String habitName = habitDatabase.getHabitName(habitId);

                new ConfirmationDialog(HabitActivity.this)
                        .setTitle("Confirm Data Reset")
                        .setMessage("Do you really want to delete all entries for '" + habitName + "'?")
                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                habitDatabase.deleteEntriesForHabit(habitId);
                            }
                        })
                        .show();

            }
            break;

            case (R.id.menu_delete_habit): {
                String habitName = habitDatabase.getHabitName(habitId);

                new ConfirmationDialog(HabitActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Do you really want to delete '" + habitName + "'?")
                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (sessionManager.getIsSessionActive(habitId)) {
                                    sessionManager.cancelSession(habitId);
                                }

                                habitDatabase.deleteHabit(habitId);
                                finish();
                            }
                        })
                        .show();

            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startSession() {
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) habit);
        startActivity(startSession);
    }

    private void updateActivity() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(habit.getName());
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

        if (!habit.getIsArchived()) {
            color = habit.getCategory().getColorAsInt();
            darkerColor = HabitCategory.darkenColor(color, 0.7f);
        }

        getWindow().setStatusBarColor(darkerColor);
        tabLayout.setBackgroundColor(color);

        toolbar.setBackgroundColor(color);

        fabMenu.setMenuButtonColorNormal(color);
        fabMenu.setMenuButtonColorPressed(darkerColor);

        enterSession.setColorNormal(color);
        enterSession.setColorPressed(darkerColor);

        createEntry.setColorNormal(color);
        createEntry.setColorPressed(darkerColor);
    }

    public void updateEntries(List<SessionEntry> sessionEntries) {
        SessionEntriesSample dataSample = new SessionEntriesSample
                (sessionEntries, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());

        dateRangeManager.updateSessionEntries(sessionEntries);

        for (UpdateEntriesInterface callback : callbacks) {
            callback.updateEntries(dataSample);
        }
    }

    @Override
    public void onScrollUp() {
        dateRangeManager.showView();
        if (tabLayout.getSelectedTabPosition() == 0)
            fabMenu.showMenu(true);
    }

    @Override
    public void onScrollDown() {
        dateRangeManager.hideView();
        if (tabLayout.getSelectedTabPosition() == 0)
            fabMenu.hideMenu(true);
    }

    @Override
    public void onEntryDeleted(SessionEntry removedEntry) {
        Set<Long> ids = habitDatabase.searchEntriesWithTimeRangeForAHabit(habitId, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
        HabitActivity.this.sessionEntries = habitDatabase.lookUpEntries(ids);
        dateRangeManager.updateSessionEntries(HabitActivity.this.sessionEntries);
    }

    @Override
    public void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry) {
        dateRangeManager.entryChanged(oldEntry, newEntry);

        Set<Long> ids = habitDatabase.searchEntriesWithTimeRangeForAHabit(habitId, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
        HabitActivity.this.sessionEntries = habitDatabase.lookUpEntries(ids);
        dateRangeManager.updateSessionEntries(HabitActivity.this.sessionEntries);
    }

    @Override
    public void onEntriesReset(long habitId) {
        if (habitId == this.habitId) {
            sessionEntries = new ArrayList<>();
            updateEntries(sessionEntries);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public final String[] titles = {"Entries", "Calendar", "Statistics"};

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return EntriesFragment.newInstance(habitId);
                }

                case 1: {
                    CalendarFragment fragment = CalendarFragment.newInstance();
//                    fragment.setHasOptionsMenu(true);
                    fragment.setListener(new CalendarFragment.Listener() {
                        @Override
                        public void onDateClicked(int year, int month, int dayOfMonth) {
                            dateRangeManager.setDateRangeForDate(year, month, dayOfMonth, true);
                            viewPager.setCurrentItem(0, true);
                        }
                    });
                    return fragment;
                }

                case 2: {
                    return StatisticsFragment.newInstance();
                }
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
