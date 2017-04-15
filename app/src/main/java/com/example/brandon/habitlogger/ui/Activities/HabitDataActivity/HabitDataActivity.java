package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;
import com.example.brandon.habitlogger.databinding.ActivityHabitDataBinding;
import com.example.brandon.habitlogger.ui.Widgets.FloatingDateRangeWidgetManager;

public class HabitDataActivity extends AppCompatActivity implements IHabitDataCallback {

    public static String HABIT_ID = "HABIT_ID";

    //region (Member attributes)

    // Dependencies
    private HabitDatabase mHabitDatabase;
    private LocalDataExportManager mExportManager;
    private SessionManager mSessionManager;

    // Data
    private Habit mHabit;
    private SessionEntriesCollection mSessionEntries = new SessionEntriesCollection();

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

    //endregion -- end --

    //region [ ---- Methods responsible for handling the activity lifecycle ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gather data from intent
        Intent data = getIntent();
        long habitId = data.getLongExtra(HABIT_ID, -1);

        // Create dependencies
        mHabitDatabase = new HabitDatabase(this);
        mSessionManager = new SessionManager(this);
        mExportManager = new LocalDataExportManager(this);

        // Fetch data from database
        mHabit = mHabitDatabase.getHabit(habitId);
        mSessionEntries = mHabitDatabase.getEntries(habitId);

        // Set up activity
        ui = DataBindingUtil.setContentView(this, R.layout.activity_habit_data);

        setSupportActionBar(ui.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new HabitDataActivityPagerAdapter(getSupportFragmentManager(), this);
        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), mSessionEntries);
        ui.container.setAdapter(mSectionsPagerAdapter);
        ui.tabs.setupWithViewPager(ui.container);
        ui.menuFab.setClosedOnTouchOutside(true);

//        dateRangeManager.hideView(false);
//        ui.container.setCurrentItem(1);
//        ui.menuFab.hideMenu(false);

        setUpActivityWithHabit(mHabit);

    }

    private void setUpActivityWithHabit(Habit habit) {
        ui.toolbar.setTitle(mHabit.getName());
        setSupportActionBar(ui.toolbar);

        ThemeColorPalette palette = new ThemeColorPalette(habit.getColor());

        if(mStatisticsCallback != null && mEntriesCallback != null && mCalendarCallback != null) {
            mStatisticsCallback.onUpdateColorPalette(palette);
            mEntriesCallback.onUpdateColorPalette(palette);
            mCalendarCallback.onUpdateColorPalette(palette);
        }

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

        // set / add listeners
//        HabitDatabase.addOnEntryChangedListener(getOnEntryChangeInDatabaseListener());
//        ui.container.addOnPageChangeListener(getOnPageChangedListener());
//        ui.tabs.addOnTabSelectedListener(getTabSelectedListener());
//        ui.enterSessionFab.setOnClickListener(getOnEnterSessionFabClickedListener());
//        ui.createEntryFab.setOnClickListener(getOnCreateEntryFabClickedListener());
//        dateRangeManager.setDateRangeChangeListener(getDateRangeChangeListener());
    }

    @Override
    protected void onStop() {
        super.onStop();

        // remove listeners
//        HabitDatabase.removeOnEntryChangedListener(getOnEntryChangeInDatabaseListener());
//        ui.container.removeOnPageChangeListener(getOnPageChangedListener());
//        ui.tabs.removeOnTabSelectedListener(getTabSelectedListener());
    }
    //endregion

    //region Methods to handle the menu lifetime
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_habit_data, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem archive = menu.findItem(R.id.menu_toggle_archive);
        if (archive != null) {
            if (mHabit.getIsArchived()) archive.setTitle(R.string.menu_unarchive);
            else archive.setTitle(R.string.menu_archive);
        }

        MenuItem search = menu.findItem(R.id.search);
        if (search != null) {
            mSearchView = (SearchView) search.getActionView();
            mSearchView.setQueryHint(getString(R.string.filter_entries));
//            mSearchView.setOnQueryTextListener(getOnSearchQueryListener());
        }

        return super.onPrepareOptionsMenu(menu);
    }
    //endregion

    //endregion [ ---- end ---- ]

    //region Methods responsible for handling/dispatching events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
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
    public SessionEntriesCollection getSessionEntries() {
        return mSessionEntries;
    }

    @Override
    public CategoryDataSample getCategoryDataSample() {
        return mHabitDatabase.getCategoryDataSample
                (mHabit.getCategory(), dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
    }
    //endregion -- end --

    public static void startActivity(Activity activity, long habitId) {
        Intent intent = new Intent(activity, HabitDataActivity.class);
        intent.putExtra(HabitDataActivity.HABIT_ID, habitId);
        activity.startActivity(intent);
    }

}
