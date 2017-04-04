package com.example.brandon.habitlogger.ui.Activities.OverviewActivity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.databinding.ActivityDataOverviewBinding;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewCalendarFragment;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewEntriesFragment;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewStatisticsFragment;
import com.example.brandon.habitlogger.ui.Widgets.ViewGroupManagers.FloatingDateRangeWidgetManager;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;

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
    List<IUpdateHabitSample> callbacks = new ArrayList<>();

    public void updateEntries() {
        HabitDataSample dataSample = getDataSample();
        List<SessionEntry> sessionEntries = dataSample.buildSessionEntriesList().getSessionEntries();
        mDateRangeManager.updateSessionEntries(sessionEntries);

        for (IUpdateHabitSample callback : callbacks)
            callback.updateHabitDataSample(dataSample);
    }

    @Override
    public void addCallback(IUpdateHabitSample callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeCallback(IUpdateHabitSample callback) {
        callbacks.remove(callback);
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
                (this, findViewById(R.id.date_range), mHabitDatabase.getEntries());
        mDateRangeManager.hideView(false);

    }

    private void setUpViews() {
        int statusColor = ContextCompat.getColor(DataOverviewActivity.this, R.color.colorPrimaryDark);
        getWindow().setStatusBarColor(statusColor);

        setSupportActionBar(ui.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ui.tabs.setupWithViewPager(ui.container);

        mDateRangeManager.setDateRangeChangeListener(getDateRangeListener());

        ui.container.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        ui.container.addOnPageChangeListener(getPageChangeListener());
        ui.container.setCurrentItem(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overall_statistcs, menu);

        MenuItem search = menu.findItem(R.id.search);
        if (search != null) {
            SearchView searchView = (SearchView) search.getActionView();
            searchView.setOnQueryTextListener(getSearchViewListener());
        }

        return true;
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

    private SearchView.OnQueryTextListener getSearchViewListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                List<SessionEntry> entries = mHabitDatabase.lookUpEntries(
                        mHabitDatabase.findEntryIdsByComment(query)
                );

                mDateRangeManager.updateSessionEntries(entries);
                updateEntries();
                return true;
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_backup_database: {
                Toast.makeText(this, "Backup Created", Toast.LENGTH_SHORT).show();
                mExportManager.exportDatabase(true);
            }
            break;

            case R.id.menu_restore_database: {
                Toast.makeText(this, "Data Restored", Toast.LENGTH_SHORT).show();
                mExportManager.importDatabase(true);
            }
            break;

            case R.id.menu_export_database_as_csv: {
                String filepath = mExportManager.exportDatabaseAsCsv();
                Toast.makeText(this, "Database exported to: " + filepath, Toast.LENGTH_LONG).show();
            }
            break;

        }

        return super.onOptionsItemSelected(item);
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

                List<SessionEntry> sessionEntries = dataSample.buildSessionEntriesList().getSessionEntries();
                mDateRangeManager.updateSessionEntries(sessionEntries);
            }

            @Override
            public void onEntriesReset(long habitId) {
                updateEntries();
            }

            @Override
            public void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry) {
                mDateRangeManager.entryChanged(oldEntry, newEntry);

                HabitDataSample dataSample = getDataSample();
                List<SessionEntry> sessionEntries = dataSample.buildSessionEntriesList().getSessionEntries();
                mDateRangeManager.updateSessionEntries(sessionEntries);
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
