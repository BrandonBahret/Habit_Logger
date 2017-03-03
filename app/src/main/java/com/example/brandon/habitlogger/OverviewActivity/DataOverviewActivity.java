package com.example.brandon.habitlogger.OverviewActivity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.brandon.habitlogger.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.HabitActivity.AppBarStateChangeListener;
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
        FloatingDateRangeWidgetManager.DateRangeChangeListener {

    private FloatingDateRangeWidgetManager dateRangeManager;

    private HabitDatabase habitDatabase;
    private List<SessionEntry> sessionEntries = new ArrayList<>();
    private LocalDataExportManager exportManager;

    //region // Methods responsible for handling the activity lifecycle

    //region // Entire lifecycle (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDataOverviewBinding ui = DataBindingUtil.setContentView(this, R.layout.activity_data_overview);

        // Set stylization
        int statusColor = ContextCompat.getColor(DataOverviewActivity.this, R.color.colorPrimaryDark);
        getWindow().setStatusBarColor(statusColor);

        // Gather dependencies
        exportManager = new LocalDataExportManager(this);
        habitDatabase = new HabitDatabase(this);
        sessionEntries = habitDatabase.lookUpEntries(habitDatabase.searchAllEntriesWithTimeRange(0, Long.MAX_VALUE));
        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), sessionEntries);

        setSupportActionBar(ui.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ui.container.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        ui.container.addOnPageChangeListener(this);
        dateRangeManager.setDateRangeChangeListener(this);
        ui.tabs.setupWithViewPager(ui.container);
        ui.appbar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                dateRangeManager.setViewShown(state == State.EXPANDED);
            }
        });

        updateEntries();
    }
    //endregion

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem search = menu.findItem(R.id.search);

        if (search != null)
            ((SearchView) search.getActionView()).setOnQueryTextListener(this);

        return super.onPrepareOptionsMenu(menu);
    }
    //endregion

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

        updateEntries();
        dateRangeManager.updateSessionEntries(entries);
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
        DataOverviewActivity.this.sessionEntries =
                habitDatabase.lookUpEntries(
                        habitDatabase.searchAllEntriesWithTimeRange(dateFrom, dateTo)
                );

        dateRangeManager.updateSessionEntries(DataOverviewActivity.this.sessionEntries);
        updateEntries();
    }
    //endregion

    //region // Allow fragments to interface with this activity
    List<UpdateHabitDataSampleInterface> callbacks = new ArrayList<>();

    public void updateEntries() {
        for (UpdateHabitDataSampleInterface callback : callbacks) {
            callback.updateDataSample(new HabitDataSample());
        }
    }

    @Override
    public void addCallback(UpdateHabitDataSampleInterface callback) {
        callbacks.add(callback);
    }

    @Override
    public HabitDataSample getDataSample() {
        return new HabitDataSample();
    }
    //endregion

}
