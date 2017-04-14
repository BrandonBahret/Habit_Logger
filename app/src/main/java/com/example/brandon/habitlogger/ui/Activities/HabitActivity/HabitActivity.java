package com.example.brandon.habitlogger.ui.Activities.HabitActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.databinding.ActivityHabitBinding;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.Fragments.CalendarFragment;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.Fragments.EntriesFragment;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.Fragments.StatisticsFragment;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity.SessionActivity;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.NewEntryForm;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.EditHabitDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.NewHabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.FloatingDateRangeWidgetManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class HabitActivity extends AppCompatActivity implements IHabitCallback, IScrollEvents {

    //region (Member attributes)
    public static String HABIT_ID = "HABIT_ID";

    private HabitDatabase mHabitDatabase;
    private List<SessionEntry> mSessionEntries = new ArrayList<>();
    private LocalDataExportManager mExportManager;
    private SessionManager mSessionManager;
    private Habit mHabit;
    private long mHabitId;

    FloatingDateRangeWidgetManager dateRangeManager;
    private SearchView mSearchView;
    ActivityHabitBinding ui;

    private List<IUpdateEntries> mSessionEntriesCallbacks = new ArrayList<>();
    private List<IUpdateCategorySample> mCategoryDataSampleCallbacks = new ArrayList<>();
    private List<IOnTabReselected> mOnTabReselectedCallbacks = new ArrayList<>();
    private List<IUpdateColor> mUpdateColorCallbacks = new ArrayList<>();
    private HabitDatabase.OnEntryChangedListener mOnEntryChangeInDatabaseListener;
    //endregion

    //region [ ---- Code responsible for providing an interface to this activity ---- ]

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

    @Override
    public void removeUpdateCategoryDataSampleCallback(IUpdateCategorySample callback) {
        mCategoryDataSampleCallbacks.remove(callback);
    }

    @Override
    public void addOnTabReselectedCallback(IOnTabReselected callback) {
        mOnTabReselectedCallbacks.add(callback);
    }

    @Override
    public void removeOnTabReselectedCallback(IOnTabReselected callback) {
        mOnTabReselectedCallbacks.remove(callback);
    }

    @Override
    public void addUpdateColorCallback(IUpdateColor callback) {
        mUpdateColorCallbacks.add(callback);
    }

    @Override
    public void removeUpdateColorCallback(IUpdateColor callback) {
        mUpdateColorCallbacks.remove(callback);
    }
    //endregion -- end --

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

    @Override
    public Habit getHabit() {
        return mHabit;
    }

    //endregion [ ---------------- end ---------------- ]

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

        Intent data = getIntent();
        mHabitId = data.getLongExtra(HABIT_ID, -1);
        mHabit = mHabitDatabase.getHabit(mHabitId);
        mSessionEntries = mHabitDatabase.getEntries(mHabitId);
        //endregion

        HabitActivityPagerAdapter pagerAdapter = new HabitActivityPagerAdapter(getSupportFragmentManager());
        ui.container.setAdapter(pagerAdapter);
        ui.container.addOnPageChangeListener(getOnPageChangedListener());

        ui.tabs.setupWithViewPager(ui.container);
        ui.tabs.addOnTabSelectedListener(getTabSelectedListener());

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
    protected void onStart() {
        super.onStart();
        HabitDatabase.addOnEntryChangedListener(getOnEntryChangeInDatabaseListener());
    }

    @Override
    protected void onStop() {
        super.onStop();
        HabitDatabase.removeOnEntryChangedListener(getOnEntryChangeInDatabaseListener());
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
            mSearchView = (SearchView) search.getActionView();
            mSearchView.setQueryHint(getString(R.string.filter_entries));
            mSearchView.setOnQueryTextListener(getOnSearchQueryListener());
        }

        return super.onPrepareOptionsMenu(menu);
    }
    //endregion -- end --

    //region Methods responsible for updating the Ui
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
        int accentColor = 0xFFCCCCCC;
        int darkerColor = 0xFFBBBBBB;
        int accentDarkerColor = 0xFFBBBBBB;

        if (!mHabit.getIsArchived()) {
            color = mHabit.getCategory().getColorAsInt();
            accentColor = color;
            darkerColor = MyColorUtils.darkenColorBy(color, 0.08f);
            accentDarkerColor = darkerColor;
        }

        for (IUpdateColor callback : mUpdateColorCallbacks) {
            callback.updateColor(color);
        }

        boolean isNightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        if (isNightMode) {
            if (MyColorUtils.getLightness(color) > 0.40) {
                darkerColor = MyColorUtils.setLightness(darkerColor, 0.40f);
                accentDarkerColor = MyColorUtils.setLightness(accentDarkerColor, 0.45f);
                color = MyColorUtils.setLightness(color, 0.45f);
                accentColor = MyColorUtils.setLightness(accentColor, 0.50f);
            }

            if (MyColorUtils.getSaturation(color) > 0.45) {
                darkerColor = MyColorUtils.setSaturation(darkerColor, 0.45f);
                accentDarkerColor = MyColorUtils.setSaturation(accentDarkerColor, 0.45f);
                color = MyColorUtils.setSaturation(color, 0.45f);
                accentColor = MyColorUtils.setSaturation(accentColor, 0.45f);
            }
        }

        getWindow().setStatusBarColor(darkerColor);
        ui.tabs.setBackgroundColor(color);

        ui.toolbar.setBackgroundColor(color);

        ui.menuFab.setMenuButtonColorNormal(accentColor);
        ui.menuFab.setMenuButtonColorPressed(accentDarkerColor);

        ui.enterSessionFab.setColorNormal(accentColor);
        ui.enterSessionFab.setColorPressed(accentDarkerColor);

        ui.createEntryFab.setColorNormal(accentColor);
        ui.createEntryFab.setColorPressed(accentDarkerColor);

    }

    private void updateDateRangeManagerEntries(List<SessionEntry> entries) {
        SessionEntry minEntry = mHabitDatabase.getMinEntry(mHabitId);
        SessionEntry maxEntry = mHabitDatabase.getMaxEntry(mHabitId);
        if (minEntry != null && maxEntry != null) {
            long minTime = minEntry.getStartingTimeIgnoreTimeOfDay();
            long maxTime = maxEntry.getStartingTimeIgnoreTimeOfDay();
            dateRangeManager.updateSessionEntries(entries, minTime, maxTime);
        }
        else {
            dateRangeManager.updateSessionEntries(new ArrayList<SessionEntry>(), -1, -1);
        }
    }

    public void updateEntries(List<SessionEntry> entries) {

        updateDateRangeManagerEntries(entries);

        SessionEntriesSample entriesDataSample = new SessionEntriesSample(entries);

        for (IUpdateEntries callback : mSessionEntriesCallbacks)
            callback.updateEntries(entriesDataSample);

        CategoryDataSample categoryDataSample = getCategoryDataSample();
        for (IUpdateCategorySample callback : mCategoryDataSampleCallbacks)
            callback.updateCategoryDataSample(categoryDataSample);
    }
    //endregion

    //region [ ---- Methods responsible for handling events ---- ]

    //region Methods to handle new entry fab events
    private View.OnClickListener getOnCreateEntryFabClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewEntryForm dialog = NewEntryForm.newInstance(ContextCompat.getColor(HabitActivity.this, R.color.textColorContrastBackground));
                dialog.setOnFinishedListener(new NewEntryForm.OnFinishedListener() {
                    @Override
                    public void onPositiveClicked(SessionEntry entry) {
                        if (entry != null) {
                            mHabitDatabase.addEntry(mHabitId, entry);
                            mSessionEntries.add(entry);
                            updateEntries(mSessionEntries);
                            if (mSearchView != null) {
                                mSearchView.setQuery("", false);
                                mSearchView.clearFocus();
                                mSearchView.onActionViewCollapsed();
                            }
                        }
                    }

                    @Override
                    public void onNegativeClicked(SessionEntry entry) {

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
                SessionActivity.startActivity(HabitActivity.this, mHabit);
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
        };
    }

    private TabLayout.OnTabSelectedListener getTabSelectedListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {}

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                for (IOnTabReselected callback : mOnTabReselectedCallbacks)
                    callback.onTabReselected(position);
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
                SessionActivity.startActivity(HabitActivity.this, mHabit);
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
                        HabitDatabase.removeOnEntryChangedListener(getOnEntryChangeInDatabaseListener());
                        mHabitDatabase.deleteHabit(mHabitId);
                        finish();
                    }
                })
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
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
                        Toast.makeText(HabitActivity.this, R.string.entries_deleted_message, Toast.LENGTH_SHORT).show();
                    }
                })
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
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
                .setAccentColor(ContextCompat.getColor(this, R.color.textColorContrastBackground))
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
        }, ContextCompat.getColor(this, R.color.textColorContrastBackground), mHabit);
        dialog.show(getSupportFragmentManager(), "edit-mHabit");
    }
    //endregion

    //region Methods to handle search events
    public void processQuery(String query) {
        List<SessionEntry> entries = mHabitDatabase.lookUpEntries(
                mHabitDatabase.findEntryIdsByComment(mHabitId, query)
        );

        updateDateRangeManagerEntries(entries);
        updateEntries(entries);
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
        dateRangeManager.showView(true);
        if (ui.tabs.getSelectedTabPosition() == 0)
            ui.menuFab.showMenu(true);
    }

    @Override
    public void onScrollDown() {
        dateRangeManager.hideView(true);
        if (ui.tabs.getSelectedTabPosition() == 0)
            ui.menuFab.hideMenu(true);
    }
    //endregion

    //region Methods to handle DateRangeManager events
    private FloatingDateRangeWidgetManager.DateRangeChangeListener getDateRangeChangeListener() {
        return new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
            @Override
            public void onDateRangeChanged(long dateFrom, long dateTo) {
                Set<Long> ids = mHabitDatabase.findEntriesWithinTimeRange(mHabitId, dateFrom, dateTo);
                HabitActivity.this.mSessionEntries = mHabitDatabase.lookUpEntries(ids);
                updateDateRangeManagerEntries(HabitActivity.this.mSessionEntries);
                updateEntries(mSessionEntries);
            }
        };
    }
    //endregion

    //region Methods to handle session entry change events
    private HabitDatabase.OnEntryChangedListener getOnEntryChangeInDatabaseListener() {
        if (mOnEntryChangeInDatabaseListener == null) {
            mOnEntryChangeInDatabaseListener = new HabitDatabase.OnEntryChangedListener() {
                @Override
                public void onEntryDeleted(SessionEntry removedEntry) {
                    Set<Long> ids = mHabitDatabase.findEntriesWithinTimeRange(mHabitId, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
                    HabitActivity.this.mSessionEntries = mHabitDatabase.lookUpEntries(ids);
                    updateDateRangeManagerEntries(HabitActivity.this.mSessionEntries);
                }

                @Override
                public void onEntryAdded(SessionEntry newEntry) {}

                @Override
                public void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry) {
                    dateRangeManager.entryChanged(oldEntry, newEntry);

                    Set<Long> ids = mHabitDatabase.findEntriesWithinTimeRange(mHabitId, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
                    HabitActivity.this.mSessionEntries = mHabitDatabase.lookUpEntries(ids);
                    updateDateRangeManagerEntries(HabitActivity.this.mSessionEntries);
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

        return mOnEntryChangeInDatabaseListener;
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Set<Long> ids = mHabitDatabase.findEntriesWithinTimeRange(mHabitId, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
        HabitActivity.this.mSessionEntries = mHabitDatabase.lookUpEntries(ids);
        updateEntries(HabitActivity.this.mSessionEntries);
    }


    //endregion [ ---------------- end ---------------- ]

    public static void startActivity(Activity activity, long habitId) {
        Intent intent = new Intent(activity, HabitActivity.class);
        intent.putExtra(HabitActivity.HABIT_ID, habitId);
        activity.startActivity(intent);
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
