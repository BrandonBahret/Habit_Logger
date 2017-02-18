package com.example.brandon.habitlogger.OverallStatistics;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.brandon.habitlogger.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.FloatingDateRangeWidgetManager;
import com.example.brandon.habitlogger.HabitActivity.AppBarStateChangeListener;
import com.example.brandon.habitlogger.HabitActivity.CalendarFragment;
import com.example.brandon.habitlogger.HabitActivity.CallbackInterface;
import com.example.brandon.habitlogger.HabitActivity.StatisticsFragment;
import com.example.brandon.habitlogger.HabitActivity.UpdateEntriesInterface;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.brandon.habitlogger.HabitActivity.AppBarStateChangeListener.State.COLLAPSED;
import static com.example.brandon.habitlogger.HabitActivity.AppBarStateChangeListener.State.EXPANDED;

public class OverallStatisticsActivity extends AppCompatActivity implements CallbackInterface {

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    private FloatingDateRangeWidgetManager dateRangeManager;
    private List<SessionEntry> sessionEntries = new ArrayList<>();
    private HabitDatabase habitDatabase;

    private LocalDataExportManager exportManager;

    List<UpdateEntriesInterface> callbacks = new ArrayList<>();

    @Override
    public void addCallback(UpdateEntriesInterface callback) {
        callbacks.add(callback);
    }

    @Override
    public SessionEntriesSample getSessionEntries() {
        return new SessionEntriesSample(sessionEntries, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overall_statistcs);

        PreferenceChecker preferenceChecker = new PreferenceChecker(this);

        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (preferenceChecker.isNightMode())
            toolbar.setPopupTheme(R.style.PopupMenu);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.updateEntries(this.sessionEntries);
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        dateRangeManager.showView();
                        break;

                    case 1:
                        dateRangeManager.hideView();
                        break;

                    case 2:
                        dateRangeManager.showView();
                        break;
                }
            }
        });
        viewPager.setAdapter(sectionsPagerAdapter);

        habitDatabase = new HabitDatabase(this);
        sessionEntries = habitDatabase.lookUpEntries(habitDatabase.searchAllEntriesWithTimeRange(0, Long.MAX_VALUE));
        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), sessionEntries);
        dateRangeManager.setDateRangeChangeListener(
                new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
                    @Override
                    public void dateRangeChanged(long dateFrom, long dateTo) {
                        OverallStatisticsActivity.this.sessionEntries =
                                habitDatabase.lookUpEntries(
                                        habitDatabase.searchAllEntriesWithTimeRange(dateFrom, dateTo)
                                );

                        dateRangeManager.updateSessionEntries(OverallStatisticsActivity.this.sessionEntries);
                        sectionsPagerAdapter.updateEntries(OverallStatisticsActivity.this.sessionEntries);
                    }
                });

        appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == COLLAPSED) {
                    dateRangeManager.hideView();
                }
                else if (state == EXPANDED) {
                    dateRangeManager.showView();
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        int statusColor = ContextCompat
                .getColor(OverallStatisticsActivity.this, R.color.colorPrimaryDark);

        getWindow().setStatusBarColor(statusColor);

        exportManager = new LocalDataExportManager(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem search = menu.findItem(R.id.search);

        if (search != null) {
            SearchView searchView = (SearchView) search.getActionView();
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
                habitDatabase.searchEntryIdsByComment(query)
        );

        sectionsPagerAdapter.updateEntries(entries);
        dateRangeManager.updateSessionEntries(entries);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case (R.id.menu_backup_database): {
                Toast.makeText(this, "Backup Created", Toast.LENGTH_SHORT).show();
                exportManager.exportDatabase(true);
            }
            break;

            case (R.id.menu_restore_database): {
                Toast.makeText(this, "Data Restored", Toast.LENGTH_SHORT).show();
                exportManager.importDatabase(true);
            }
            break;

            case (R.id.menu_export_database_as_csv): {
                String filepath = exportManager.exportDatabaseAsCsv();
                Toast.makeText(this, "Database exported to: " + filepath, Toast.LENGTH_LONG).show();
            }
            break;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public OverallEntriesFragment entriesFragment = OverallEntriesFragment.newInstance();
        public CalendarFragment calendarFragment = CalendarFragment.newInstance();
        public StatisticsFragment statisticsFragment = StatisticsFragment.newInstance();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case (0):
                    entriesFragment.setHasOptionsMenu(true);
                    return entriesFragment;

                case (1):
                    calendarFragment.setHasOptionsMenu(true);
                    calendarFragment.setMenuRes(R.menu.menu_overall_statistcs);
                    calendarFragment.setListener(new CalendarFragment.Listener() {
                        @Override
                        public void onDateClicked(int year, int month, int dayOfMonth) {
                            viewPager.setCurrentItem(0, true);
                            String text = String.format(Locale.getDefault(), "%d %d, %d", month, dayOfMonth, year);
                            Toast.makeText(OverallStatisticsActivity.this, text, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return calendarFragment;

                case (2):
                    statisticsFragment.setHasOptionsMenu(true);
                    statisticsFragment.setMenuRes(R.menu.menu_overall_statistcs);
                    return statisticsFragment;
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
            switch (position) {
                case 0:
                    return "Entries";
                case 1:
                    return "Calendar";
                case 2:
                    return "Statistics";
            }
            return null;
        }

        public void updateEntries(List<SessionEntry> sessionEntries) {
            entriesFragment.updateEntries(sessionEntries);

            for (UpdateEntriesInterface callback : callbacks) {
                callback.updateEntries(sessionEntries, dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
            }
        }
    }
}
