//package com.example.brandon.habitlogger.ui.Activities.OverviewActivity;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.databinding.DataBindingUtil;
//import android.os.Bundle;
//import android.support.design.widget.TabLayout;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.SearchView;
//import android.view.Menu;
//import android.view.MenuItem;
//
//import com.example.brandon.habitlogger.R;
//import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
//import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
//import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
//import com.example.brandon.habitlogger.databinding.ActivityDataOverviewBinding;
//import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewEntriesFragment;
//import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
//import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.EditEntryForm;
//import com.example.brandon.habitlogger.ui.Widgets.FloatingDateRangeWidgetManager;
//
//import java.util.ArrayList;
//import java.util.Set;
//
//public class DataOverviewActivity extends AppCompatActivity implements
//        IDataOverviewCallback, IScrollEvents, OverviewEntriesFragment.IEntriesEvents {
//
//    //region (Member attributes)
//
//    // Dependencies
//    private HabitDatabase mHabitDatabase;
//
//    // Data
//    private SessionEntryCollection mSessionEntries = new SessionEntryCollection();
//
//    // View related members
//    FloatingDateRangeWidgetManager mDateRangeManager;
//    private SearchView mSearchView;
//    private DataOverviewActivityPagerAdapter mSectionsPagerAdapter;
//    ActivityDataOverviewBinding ui;
//
//    //endregion -- end --
//
//    //region Code responsible for providing communication between child fragments and this activity
//
//    // Callbacks
//    private IEntriesFragment mEntriesCallback;
//    private IStatisticsFragment mStatisticsCallback;
//    private ICalendarFragment mCalendarCallback;
//
//    @Override
//    public void setEntriesFragmentCallback(IEntriesFragment callback) {
//        mEntriesCallback = callback;
//    }
//
//    @Override
//    public void setCalendarFragmentCallback(ICalendarFragment callback) {
//        mCalendarCallback = callback;
//    }
//
//    @Override
//    public void setStatisticsFragmentCallback(IStatisticsFragment callback) {
//        mStatisticsCallback = callback;
//    }
//
//    //endregion -- end --
//
//    //region [ ---- Methods responsible for handling the activity lifecycle ---- ]
//
//    //region entire lifetime (onCreate - onDestroy)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Create dependencies
//        mHabitDatabase = new HabitDatabase(this);
//
//        // Fetch data from database
//        mSessionEntries = mHabitDatabase.getEntries();
//
//        // Set up activity
//        ui = DataBindingUtil.setContentView(this, R.layout.activity_data_overview);
//
//        setSupportActionBar(ui.toolbar);
//        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        mSectionsPagerAdapter = new DataOverviewActivityPagerAdapter(getSupportFragmentManager(), this);
//        mDateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), mSessionEntries);
//        ui.container.setAdapter(mSectionsPagerAdapter);
//        ui.tabs.setupWithViewPager(ui.container);
//
//        mSessionEntries = fetchEntriesWithinTimeRange();
//        mDateRangeManager.updateSessionEntries(mSessionEntries);
//
//        mDateRangeManager.hideView(false);
//        ui.container.setCurrentItem(1);
//
//    }
//    //endregion -- end --
//
//    //region visible lifetime (onStart - onStop)
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        // Set/Add listeners
//        ui.tabs.addOnTabSelectedListener(onTabSelectedListener);
//        mDateRangeManager.setDateRangeChangeListener(onDateRangeChangeListener);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        // remove listeners
//        ui.tabs.removeOnTabSelectedListener(onTabSelectedListener);
//    }
//    //endregion -- end --
//
//    //region Methods to handle the menu lifetime
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_overall_statistcs, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        MenuItem search = menu.findItem(R.id.search);
//        if (search != null) {
//            mSearchView = (SearchView) search.getActionView();
//            mSearchView.setQueryHint(getString(R.string.filter_entries));
//            mSearchView.setOnQueryTextListener(onSearchQueryListener);
//        }
//
//        return super.onPrepareOptionsMenu(menu);
//    }
//    //endregion
//
//    //endregion [ ---- end ---- ]
//
//    //region Methods responsible for manipulating entries
//    private SessionEntryCollection fetchEntriesWithinConditions(String query) {
//        Set<Long> queryIds = mHabitDatabase.findEntryIdsByComment(query);
//
//        Set<Long> dateRangeIds = mHabitDatabase.findEntriesWithinTimeRange(
//                mDateRangeManager.getDateFrom(), mDateRangeManager.getDateTo()
//        );
//
//        queryIds.retainAll(dateRangeIds);
//
//        return mHabitDatabase.lookUpEntries(queryIds);
//    }
//
//    private SessionEntryCollection fetchEntriesWithinTimeRange() {
//        Set<Long> dateRangeIds = mHabitDatabase.findEntriesWithinTimeRange(
//                mDateRangeManager.getDateFrom(), mDateRangeManager.getDateTo()
//        );
//
//        return mHabitDatabase.lookUpEntries(dateRangeIds);
//    }
//
//    private boolean checkIfEntryFitsWithinConditions(SessionEntry entry) {
//        boolean fitsWithinRange = mDateRangeManager.entryFitsRange(entry);
//        boolean fitsQuery = true;
//
//        CharSequence query = mSearchView.getQuery();
//        if (query != null && query.length() != 0) {
//            String stringQuery = String.valueOf(query);
//            fitsQuery = entry.matchesQuery(stringQuery);
//        }
//        return fitsQuery && fitsWithinRange;
//    }
//
//    private void updateEntry(SessionEntry oldEntry, SessionEntry newEntry) {
//        int oldIndex = mSessionEntries.indexOf(oldEntry);
//        int newIndex = mSessionEntries.updateEntry(oldEntry, newEntry);
//        mDateRangeManager.updateSessionEntries(mSessionEntries);
//        mEntriesCallback.onNotifyEntryUpdated(oldIndex, newIndex);
//        mCalendarCallback.onUpdateEntries(mSessionEntries);
////        mStatisticsCallback.onUpdateEntries(mSessionEntries);
//    }
//
//    private void removeEntry(SessionEntry oldEntry) {
//        int pos = mSessionEntries.removeEntry(oldEntry);
//        mDateRangeManager.updateSessionEntries(mSessionEntries);
//        mEntriesCallback.onNotifyEntryRemoved(pos);
//        mCalendarCallback.onUpdateEntries(mSessionEntries);
////        mStatisticsCallback.onUpdateEntries(mSessionEntries);
//    }
//
//    private void updateEntries(SessionEntryCollection sessionEntries) {
//        updateDateRangeManagerEntries(sessionEntries);
//        mEntriesCallback.onUpdateEntries(sessionEntries);
//        mCalendarCallback.onUpdateEntries(sessionEntries);
////        mStatisticsCallback.onUpdateEntries(mSessionEntries);
//    }
//
//    private void updateDateRangeManagerEntries(SessionEntryCollection sessionEntries) {
//        SessionEntry minEntry = mHabitDatabase.getMinEntry();
//        SessionEntry maxEntry = mHabitDatabase.getMaxEntry();
//        if (minEntry != null && maxEntry != null) {
//            long minTime = minEntry.getStartingTimeIgnoreTimeOfDay();
//            long maxTime = maxEntry.getStartingTimeIgnoreTimeOfDay();
//            mDateRangeManager.updateSessionEntries(sessionEntries.size(), sessionEntries.calculateDuration(), minTime, maxTime);
//            sessionEntries.setDateFrom(mDateRangeManager.getDateFrom());
//            sessionEntries.setDateTo(mDateRangeManager.getDateTo());
//        }
//        else {
//            mDateRangeManager.updateSessionEntries(new ArrayList<SessionEntry>(), -1, -1);
//        }
//    }
//    //endregion -- end --
//
//    //region Methods responsible for handling/dispatching events
//
//    //region Scroll events
//    @Override
//    public void onScrollUp() {
//        mDateRangeManager.showView(true);
//    }
//
//    @Override
//    public void onScrollDown() {
//        mDateRangeManager.hideView(true);
//    }
//    //endregion -- end --
//
//    //region Entries fragment events
//    @Override
//    public void onEntryViewClicked(final long entryId, SessionEntry entry) {
//        EditEntryForm dialog = EditEntryForm.newInstance(
//                entry, ContextCompat.getColor(this, R.color.textColorContrastBackground)
//        );
//
//        dialog.setOnFinishedListener(new EditEntryForm.OnFinishedListener() {
//            @Override
//            public void onEditEntryUpdateEntry(SessionEntry newEntry) {
//                if (newEntry != null) {
//                    SessionEntry oldEntry = mHabitDatabase.getEntry(entryId);
//                    mHabitDatabase.updateEntry(entryId, newEntry);
//
//                    mDateRangeManager.adjustDateRangeForEntry(newEntry);
//                    if (checkIfEntryFitsWithinConditions(newEntry)) updateEntry(oldEntry, newEntry);
//                    else removeEntry(oldEntry);
//                }
//            }
//
//            @Override
//            public void onEditEntryDeleteEntry(SessionEntry removeEntry) {
//                mHabitDatabase.deleteEntry(entryId);
//                removeEntry(removeEntry);
//            }
//        });
//
//        dialog.show(getSupportFragmentManager(), "edit-entry");
//    }
////    endregion
//
//    FloatingDateRangeWidgetManager.DateRangeChangeListener onDateRangeChangeListener =
//            new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
//                @Override
//                public void onDateRangeChanged(long dateFrom, long dateTo) {
//                    String query = String.valueOf(mSearchView.getQuery());
//                    mSessionEntries = fetchEntriesWithinConditions(query);
//                    updateEntries(mSessionEntries);
//                }
//            };
//
//    SearchView.OnQueryTextListener onSearchQueryListener = new SearchView.OnQueryTextListener() {
//        @Override
//        public boolean onQueryTextSubmit(String query) {
//            return false;
//        }
//
//        @Override
//        public boolean onQueryTextChange(String newText) {
//            mSessionEntries = newText.length() > 0 ?
//                    fetchEntriesWithinConditions(newText) : fetchEntriesWithinTimeRange();
//
//            updateEntries(mSessionEntries);
//
//            return false;
//        }
//    };
//
//    TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
//
//        @Override
//        public void onTabSelected(TabLayout.Tab tab) {
//            int position = tab.getPosition();
//            switch (position) {
//                case 0:
//                    mDateRangeManager.showView(true);
//                    break;
//
//                case 1:
//                    mDateRangeManager.hideView(true);
//                    break;
//
//                case 2:
//                    mDateRangeManager.showView(true);
//                    break;
//            }
//        }
//
//        @Override
//        public void onTabUnselected(TabLayout.Tab tab) {
//
//        }
//
//        @Override
//        public void onTabReselected(TabLayout.Tab tab) {
//            if (tab.getPosition() == 0 && mEntriesCallback != null)
//                mEntriesCallback.onTabReselected();
//            else if (tab.getPosition() == 1 && mCalendarCallback != null)
//                mCalendarCallback.onTabReselected();
//            else if (tab.getPosition() == 2 && mStatisticsCallback != null)
//                mStatisticsCallback.onTabReselected();
//        }
//
//    };
//
//    //endregion -- end --
//
//    //region Getters {}
//    @Override
//    public SessionEntryCollection getSessionEntries() {
//        return mSessionEntries;
//    }
//    //endregion -- end --
//
//    public static void startActivity(Activity activity) {
//        Intent intent = new Intent(activity, DataOverviewActivity.class);
//        activity.startActivity(intent);
//    }
//
//}
