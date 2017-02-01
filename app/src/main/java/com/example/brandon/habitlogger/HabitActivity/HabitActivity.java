package com.example.brandon.habitlogger.HabitActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.habitlogger.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.FloatingDateRangeWidgetManager;
import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.SessionActivity;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ModifyHabitActivity.ModifyHabitActivity;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.example.brandon.habitlogger.HabitActivity.AppBarStateChangeListener.State.COLLAPSED;
import static com.example.brandon.habitlogger.HabitActivity.AppBarStateChangeListener.State.EXPANDED;


public class HabitActivity extends AppCompatActivity implements
        EntriesFragment.OnFragmentInteractionListener, CalendarFragment.OnFragmentInteractionListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private HabitDatabase habitDatabase;
    private PreferenceChecker preferenceChecker;
    private List<SessionEntry> sessionEntries = new ArrayList<>();
    private LocalDataExportManager exportManager;
    private SessionManager sessionManager;
    private Habit habit;
    private long habitId;

    TabLayout tabLayout;
    Toolbar toolbar;
    FloatingActionMenu fabMenu;
    FloatingActionButton enterSession, createEntry;

    FloatingDateRangeWidgetManager dateRangeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit);

        preferenceChecker = new PreferenceChecker(this);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        if(preferenceChecker.getTheme() == PreferenceChecker.DARK_THEME)
            toolbar.setPopupTheme(R.style.PopupMenu);

        AppBarLayout appBar = (AppBarLayout)findViewById(R.id.appbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        fabMenu = (FloatingActionMenu) findViewById(R.id.menu_fab);
        enterSession = (FloatingActionButton) findViewById(R.id.enter_session_fab);
        createEntry = (FloatingActionButton) findViewById(R.id.create_entry_fab);
        mViewPager = (ViewPager) findViewById(R.id.container);

        habitDatabase = new HabitDatabase(this, null, false);
        sessionManager = new SessionManager(this);

        List<SessionEntry> sessionEntries = new ArrayList<>();
        dateRangeManager = new FloatingDateRangeWidgetManager(this, findViewById(R.id.date_range), sessionEntries);
        dateRangeManager.setDateRangeChangeListener(new FloatingDateRangeWidgetManager.DateRangeChangeListener() {
            @Override
            public void dateRangeChanged(long dateFrom, long dateTo) {
                Set<Long> ids = habitDatabase.searchEntriesWithTimeRangeForAHabit(habitId, dateFrom, dateTo);
                HabitActivity.this.sessionEntries = habitDatabase.lookUpEntries(ids);
                dateRangeManager.updateSessionEntries(HabitActivity.this.sessionEntries);
                mSectionsPagerAdapter.updateEntries(HabitActivity.this.sessionEntries);
            }
        });

        appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if(state == COLLAPSED){
                    dateRangeManager.hideView();
                    if(mViewPager.getCurrentItem() == 0)
                        fabMenu.hideMenu(true);
                }
                else if(state == EXPANDED){
                    dateRangeManager.showView();
                    if(mViewPager.getCurrentItem() == 0)
                        fabMenu.showMenu(true);
                }
            }
        });

        Intent data = getIntent();
        habitId = data.getLongExtra("habitId", -1);
        habit = habitDatabase.getHabit(habitId);

        exportManager = new LocalDataExportManager(this);

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
                        if(entry != null){
                            habitDatabase.addEntry(habitId, entry);

                            mSectionsPagerAdapter.entriesFragment.addSessionEntry(entry);
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

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener (new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        fabMenu.showMenu(true);
                        dateRangeManager.showView();
                        break;

                    case 1:
                        fabMenu.hideMenu(true);
                        dateRangeManager.hideView();
                        break;

                    case 2:
                        fabMenu.hideMenu(true);
                        dateRangeManager.showView();
                        break;
                }
            }
        });
        tabLayout.setupWithViewPager(mViewPager);
        mSectionsPagerAdapter.updateEntries(this.sessionEntries);

        updateActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ModifyHabitActivity.EDIT_HABIT_RESULT_CODE: {
                    Habit editHabit = (Habit) data.getSerializableExtra("new_habit");
                    habit = editHabit;
                    habitDatabase.updateHabit(editHabit.getDatabaseId(), editHabit);

                    updateActivity();
                }break;
            }
        }
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
        if (habit.getIsArchived()) {
            archive.setTitle("Unarchive");
        } else {
            archive.setTitle("Archive");
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case(android.R.id.home):{
                finish();
            }break;

            case(R.id.menu_habit_edit):{
                startModifyHabitActivity();
            }break;

            case(R.id.menu_toggle_archive):{
                boolean archivedState = !habitDatabase.getIsHabitArchived(habitId);
                habitDatabase.updateHabitIsArchived(habitId, archivedState);
                habit.setIsArchived(archivedState);
                updateColorTheme();

            }break;

            case(R.id.menu_export_habit):{
                Habit habit = habitDatabase.getHabit(habitId);
                exportManager.shareExportHabit(habit);
            }break;

            case(R.id.menu_delete_habit):{
                if (sessionManager.isSessionActive(habitId)) {
                    sessionManager.cancelSession(habitId);
                }

                habitDatabase.deleteHabit(habitId);
                finish();
            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_habit, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public void startSession() {
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra("habit", habit);
        startActivity(startSession);
    }

    private void startModifyHabitActivity() {
        Intent startTargetActivity = new Intent(HabitActivity.this, ModifyHabitActivity.class);
        startTargetActivity.putExtra("edit", true);
        startTargetActivity.putExtra("habit", habit);
        startActivityForResult(startTargetActivity, ModifyHabitActivity.EDIT_HABIT_RESULT_CODE);
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
        // TODO create this method
        int color = 0xFFCCCCCC;
        int darkerColor = 0xFFBBBBBB;

        if(!habit.getIsArchived()){
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public EntriesFragment entriesFragment = EntriesFragment.newInstance(habitId);
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
                case 0:
                    return entriesFragment;

                case 1:
                    calendarFragment.setListener(new CalendarFragment.Listener() {
                        @Override
                        public void onDateClicked(int year, int month, int dayOfMonth) {
                            dateRangeManager.setDateRangeForDate(year, month, dayOfMonth);
                            mViewPager.setCurrentItem(0, true);
                            String text = String.format(Locale.getDefault(), "%d %d, %d", month, dayOfMonth, year);
                            Toast.makeText(HabitActivity.this, text, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return calendarFragment;

                case 2:
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
            statisticsFragment.updateEntries(sessionEntries);
        }
    }
}
