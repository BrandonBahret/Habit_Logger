package com.example.brandon.habitlogger.ui.Activities.OverviewActivity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;
import com.example.brandon.habitlogger.databinding.ActivityDataOverviewBinding;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewCalendarFragment;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewEntriesFragment;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewStatisticsFragment;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Widgets.FloatingDateRangeWidgetManager;

import java.util.ArrayList;
import java.util.List;

public class DataOverviewActivity extends AppCompatActivity implements
        IDataOverviewCallback, IScrollEvents {

    //region (Member attributes)
    private FloatingDateRangeWidgetManager mDateRangeManager;
    private HabitDatabase mHabitDatabase;
    private LocalDataExportManager mExportManager;
    private ActivityDataOverviewBinding ui;
    //endregion

    //region Code responsible for providing an interface to this activity

    List<IUpdateHabitSample> mUpdateDataCallbacks = new ArrayList<>();
    List<IOnTabReselected> mOnTabReselectedCallbacks = new ArrayList<>();

    //region Methods to add and remove callbacks
    @Override
    public void addCallback(IUpdateHabitSample callback) {
        mUpdateDataCallbacks.add(callback);
    }

    @Override
    public void removeCallback(IUpdateHabitSample callback) {
        mUpdateDataCallbacks.remove(callback);
    }

    @Override
    public void addOnTabReselectedCallback(IOnTabReselected callback) {
        mOnTabReselectedCallbacks.add(callback);
    }

    @Override
    public void removeOnTabReselectedCallback(IOnTabReselected callback) {
        mOnTabReselectedCallbacks.remove(callback);
    }
    //endregion

    public void updateEntries() {
        HabitDataSample dataSample = getDataSample();
        List<SessionEntry> sessionEntries = dataSample.buildSessionEntriesList().asList();

        updateDateRangeManagerEntries(sessionEntries);

        for (IUpdateHabitSample callback : mUpdateDataCallbacks)
            callback.updateHabitDataSample(dataSample);
    }

    private void updateDateRangeManagerEntries(List<SessionEntry> sessionEntries) {
        long minTime = mHabitDatabase.getMinEntry().getStartingTimeIgnoreTimeOfDay();
        long maxTime = mHabitDatabase.getMaxEntry().getStartingTimeIgnoreTimeOfDay();
        mDateRangeManager.updateSessionEntries(sessionEntries, minTime, maxTime);
    }

    private void updateEntries(List<SessionEntry> entries) {
        updateDateRangeManagerEntries(entries);

        SessionEntriesCollection entriesSample = new SessionEntriesCollection(entries);
        HabitDataSample dataSample = mHabitDatabase.getHabitDataSample(entriesSample);

        for (IUpdateHabitSample callback : mUpdateDataCallbacks)
            callback.updateHabitDataSample(dataSample);
    }

    @Override
    public HabitDataSample getDataSample() {
        return mHabitDatabase.getHabitDataSample(mDateRangeManager.getDateFrom(), mDateRangeManager.getDateTo());
    }
    //endregion --- end --

    //region Methods responsible for handling the activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ui = DataBindingUtil.setContentView(this, R.layout.activity_data_overview);

        gatherDependencies();

        setUpViews();

        updateEntries();
    }

    private void gatherDependencies() {
        mHabitDatabase = new HabitDatabase(this);
        HabitDatabase.addOnEntryChangedListener(getDatabaseListener());

        mExportManager = new LocalDataExportManager(this);

        mDateRangeManager = new FloatingDateRangeWidgetManager
                (this, findViewById(R.id.date_range), mHabitDatabase.getEntriesAsList());
        mDateRangeManager.hideView(false);

    }

    private void setUpViews() {
        int statusColor = ContextCompat.getColor(DataOverviewActivity.this, R.color.colorPrimaryDark);
        getWindow().setStatusBarColor(statusColor);

        setSupportActionBar(ui.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ui.tabs.setupWithViewPager(ui.container);
        ui.tabs.addOnTabSelectedListener(getTabSelectedListener());

        mDateRangeManager.setDateRangeChangeListener(getDateRangeListener());

        ui.container.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        ui.container.addOnPageChangeListener(getPageChangeListener());
        ui.container.setCurrentItem(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overall_statistcs, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem search = menu.findItem(R.id.search);
        if (search != null) {
            SearchView searchView = (SearchView) search.getActionView();
            searchView.setQueryHint(getString(R.string.filter_entries));
            searchView.setOnQueryTextListener(getSearchViewListener());
        }

        return super.onPrepareOptionsMenu(menu);
    }
    //endregion --- end ---

    //region [ ---- Methods responsible for handling events ---- ]

    //region [ -- methods responsible for handling ui events -- ]

    //region Methods responsible for handling scroll events
    @Override
    public void onScrollUp() {
        mDateRangeManager.showView(true);
    }

    @Override
    public void onScrollDown() {
        mDateRangeManager.hideView(true);
    }
    //endregion -- end --

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

    private SearchView.OnQueryTextListener getSearchViewListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                List<SessionEntry> entries = mHabitDatabase.lookUpEntriesAsList(
                        mHabitDatabase.findEntryIdsByComment(query)
                );

                updateDateRangeManagerEntries(entries);
                updateEntries(entries);
                return true;
            }
        };
    }

    private FloatingDateRangeWidgetManager.DateRangeChangeListener getDateRangeListener() {
        return new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
            @Override
            public void onDateRangeChanged(long dateFrom, long dateTo) {
                updateEntries();
            }
        };
    }

    private ViewPager.OnPageChangeListener getPageChangeListener() {
        return new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                boolean dateRangeShownStates[] = {true, false, true};
                mDateRangeManager.setViewShown(dateRangeShownStates[position]);
            }
        };
    }

    //endregion [ -- end -- ]

    private HabitDatabase.OnEntryChangedListener getDatabaseListener() {
        return new HabitDatabase.OnEntryChangedListener() {
            @Override
            public void onEntryDeleted(SessionEntry removedEntry) {
                HabitDataSample dataSample = getDataSample();

                List<SessionEntry> sessionEntries = dataSample.buildSessionEntriesList().asList();
                updateDateRangeManagerEntries(sessionEntries);
            }

            @Override
            public void onEntryAdded(SessionEntry newEntry) {

            }

            @Override
            public void onEntriesReset(long habitId) {
                updateEntries();
            }

            @Override
            public void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry) {
                mDateRangeManager.adjustDateRangeForEntry(newEntry);

                HabitDataSample dataSample = getDataSample();
                List<SessionEntry> sessionEntries = dataSample.buildSessionEntriesList().asList();
                updateDateRangeManagerEntries(sessionEntries);
            }
        };
    }

    //endregion [ ---------------- end ---------------- ]

    public static void startActivity(Context context) {
        Intent startTargetActivity = new Intent(context, DataOverviewActivity.class);
        context.startActivity(startTargetActivity);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public final String[] titles = getResources().getStringArray(R.array.tab_titles);

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return OverviewEntriesFragment.newInstance();

                case 1:
                    return OverviewCalendarFragment.newInstance();

                case 2:
                    return OverviewStatisticsFragment.newInstance();
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
