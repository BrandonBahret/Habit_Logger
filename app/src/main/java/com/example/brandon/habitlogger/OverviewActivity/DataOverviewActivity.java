package com.example.brandon.habitlogger.OverviewActivity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.brandon.habitlogger.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.HabitActivity.GetScrollEventsFromFragmentsInterface;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.databinding.ActivityDataOverviewBinding;
import com.example.brandon.habitlogger.ui.FloatingDateRangeWidgetManager;

import java.util.ArrayList;
import java.util.List;

public class DataOverviewActivity extends AppCompatActivity implements
        CallbackInterface, SearchView.OnQueryTextListener, ViewPager.OnPageChangeListener,
        FloatingDateRangeWidgetManager.DateRangeChangeListener, GetScrollEventsFromFragmentsInterface, HabitDatabase.OnEntryChangedListener {

    private FloatingDateRangeWidgetManager dateRangeManager;
    private HabitDatabase habitDatabase;
    private LocalDataExportManager exportManager;

    //region // Methods responsible for handling the activity lifecycle

    //region // Entire lifecycle (onCreate - onDestroy)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDataOverviewBinding ui = DataBindingUtil.setContentView(this, R.layout.activity_data_overview);

        //region // Gather dependencies
        habitDatabase = new HabitDatabase(this);
        HabitDatabase.addOnEntryChangedListener(this);
        List<SessionEntry> sessionEntries = habitDatabase.lookUpEntries(habitDatabase.searchAllEntriesWithTimeRange(0, Long.MAX_VALUE));
        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), sessionEntries);
        dateRangeManager.hideView(false);
        exportManager = new LocalDataExportManager(this);
        //endregion

        // Set stylization
        int statusColor = ContextCompat.getColor(DataOverviewActivity.this, R.color.colorPrimaryDark);
        getWindow().setStatusBarColor(statusColor);

        //region // Set-up views
        ui.tabs.setupWithViewPager(ui.container);
        ui.container.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        setSupportActionBar(ui.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //endregion

        //region // Include listeners
        dateRangeManager.setDateRangeChangeListener(this);
        ui.container.addOnPageChangeListener(this);
        //endregion

        updateEntries();
        ui.container.setCurrentItem(1);
    }

    //endregion // Entire lifecycle (onCreate - onDestroy)

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overall_statistcs, menu);

        MenuItem search = menu.findItem(R.id.search);

        if (search != null)
            ((SearchView) search.getActionView()).setOnQueryTextListener(this);

        return true;
    }

    //endregion // Methods responsible for handling the activity lifecycle

    //region // Methods responsible for handling events
    //region // On query in SearchView events
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        List<SessionEntry> entries = habitDatabase.lookUpEntries(
                habitDatabase.searchEntryIdsByComment(query)
        );

        dateRangeManager.updateSessionEntries(entries);
        updateEntries();
        return true;
    }
    //endregion

    //region // Menu events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    //endregion

    //region // On viewpager events
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        boolean dateRangeShownStates[] = {true, false, true};
        dateRangeManager.setViewShown(dateRangeShownStates[position]);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    //endregion

    @Override
    public void onDateRangeChanged(long dateFrom, long dateTo) {
        updateEntries();
    }

    @Override
    public void onEntryDeleted(SessionEntry removedEntry) {
        HabitDataSample dataSample = getDataSample();

        List<SessionEntry> sessionEntries = dataSample.getSessionEntriesSample().getSessionEntries();
        dateRangeManager.updateSessionEntries(sessionEntries);
    }

    @Override
    public void onEntriesReset(long habitId) {
        updateEntries();
    }

    @Override
    public void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry) {
        HabitDataSample dataSample = getDataSample();

        List<SessionEntry> sessionEntries = dataSample.getSessionEntriesSample().getSessionEntries();
        dateRangeManager.updateSessionEntries(sessionEntries);
    }

    @Override
    public void onScrollUp() {
        dateRangeManager.showView();
    }

    @Override
    public void onScrollDown() {
        dateRangeManager.hideView();
    }

    //endregion

    //region // Allow fragments to interface with this activity

    List<UpdateHabitDataSampleInterface> callbacks = new ArrayList<>();

    public void updateEntries() {
        HabitDataSample dataSample = getDataSample();

        List<SessionEntry> sessionEntries = dataSample.getSessionEntriesSample().getSessionEntries();
        dateRangeManager.updateSessionEntries(sessionEntries);

        for (UpdateHabitDataSampleInterface callback : callbacks) {
            callback.updateDataSample(dataSample);
        }
    }

    @Override
    public void addCallback(UpdateHabitDataSampleInterface callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeCallback(UpdateHabitDataSampleInterface callback) {
        callbacks.remove(callback);
    }

    @Override
    public HabitDataSample getDataSample() {
        return habitDatabase.getHabitDataSample(dateRangeManager.getDateFrom(), dateRangeManager.getDateTo());
    }

    //endregion

}
