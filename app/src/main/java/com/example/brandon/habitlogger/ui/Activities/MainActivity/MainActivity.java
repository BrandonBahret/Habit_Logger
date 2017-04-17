package com.example.brandon.habitlogger.ui.Activities.MainActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.data.DataExportHelpers.GoogleDriveDataExportManager;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.data.HabitSessions.SessionNotificationManager;
import com.example.brandon.habitlogger.databinding.ActivityMainBinding;
import com.example.brandon.habitlogger.ui.Activities.AboutActivity;
import com.example.brandon.habitlogger.ui.Activities.ActiveSessionsActivity.ActiveSessionsActivity;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments.AllHabitsFragment;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments.ArchivedHabitsFragment;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments.CategoryCardHabitsFragment;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments.MyFragmentBase;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.DataOverviewActivity;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.SettingsActivity;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.NewHabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.CurrentSessionCardManager;
import com.github.clans.fab.FloatingActionButton;

import java.util.Locale;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MyFragmentBase.IMainActivity {

    //region (Member attributes)
//    private static final String RECYCLER_STATE = "RECYCLER_STATE";
//    final int NO_ARCHIVED_HABITS = 0, ONLY_ARCHIVED_HABITS = 1;
//    private int mHabitDisplayMode = NO_ARCHIVED_HABITS;
    private Integer mLastThemeMode = null;

    //    private List<Habit> mHabitList = new ArrayList<>();
    private HabitDatabase mHabitDatabase;
    private SessionManager mSessionManager;
    private SessionNotificationManager mSessionNotificationManager;
    private PreferenceChecker mPreferenceChecker;
    private LocalDataExportManager mLocalExportManager;
    private GoogleDriveDataExportManager mGoogleDriveExportManager;
    private CurrentSessionCardManager mCurrentSessionCard;

    ActivityMainBinding ui;
    //    private RecyclerView mHabitCardContainer;
    private SearchView mSearchView;
    private MyFragmentBase mFragment;

//    private Handler mUpdateHandler = new Handler();
//    private SpaceOffsetDecoration mSpaceDecoration;
//    private GroupDecoration mGroupDecoration;
//    private HabitViewAdapter mHabitAdapter;
//    private CategoryCardAdapter mCategoryAdapter;
    //endregion

    //region [ ---- Methods responsible for handling the lifecycle of the activity ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivityTheme();

        super.onCreate(savedInstanceState);

        ui = DataBindingUtil.setContentView(this, R.layout.activity_main);

        gatherDependencies();

        setListeners();

        setUpActivityUi();

    }

    private void setActivityTheme() {
        mPreferenceChecker = new PreferenceChecker(this);
        int themeMode = mPreferenceChecker.getThemeMode();
        AppCompatDelegate.setDefaultNightMode(themeMode);
        mLastThemeMode = themeMode;
    }

    private void setUpActivityUi() {
        setSupportActionBar(ui.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, ui.drawerLayout, ui.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        ui.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
//        mHabitCardContainer.setLayoutManager(layoutManager);
//        mHabitCardContainer.setItemAnimator(new DefaultItemAnimator());
//
//        if (mPreferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_CARDS) {
//            List<CategoryDataSample> categoryContainers = mHabitDatabase.getAllData();
//            MyCollectionUtils.filter(categoryContainers, CategoryDataSample.IFilterEmptySamples);
//            mCategoryAdapter = new CategoryCardAdapter(categoryContainers, getHabitMenuItemClickListener(), getHabitButtonClickCallback());
//            mHabitCardContainer.setAdapter(mCategoryAdapter);
//        }
//        else {
//            mHabitAdapter = new HabitViewAdapter(mHabitList, getHabitMenuItemClickListener(), getHabitButtonClickCallback());
//            mHabitCardContainer.setAdapter(mHabitAdapter);
//            showDatabase();
//        }
//
//        applyGroupDecoration();
        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);

        if (mPreferenceChecker.doShowNotificationsAutomatically())
            mSessionNotificationManager.launchNotificationsForAllActiveSessions();

        prepareNavigationDrawer();

        setFragmentForNavId(R.id.home_nav);

    }

    private void prepareNavigationDrawer() {
        Menu menu = ui.navView.getMenu();
        MenuItem navNightModeSwitch = menu.findItem(R.id.night_mode);

        boolean isNightMode = mPreferenceChecker.isNightMode();
        Switch nightModeSwitch = (Switch) navNightModeSwitch.getActionView().findViewById(R.id.menu_switch);
        nightModeSwitch.setChecked(isNightMode);
    }

    private void setListeners() {
        mSessionManager.addSessionChangedCallback(getOnSessionChangedListener());
        ui.mainInclude.fab.setOnClickListener(getOnNewHabitButtonClicked());
        ui.navView.setNavigationItemSelectedListener(this);
//        mHabitCardContainer.addOnScrollListener(getOnScrollListener());
        mCurrentSessionCard.setOnClickListener(getOnSessionsCardClickListener());
    }

    private void gatherDependencies() {
        mCurrentSessionCard = new CurrentSessionCardManager(ui.mainInclude.currentSessionsCard.itemRoot);
//        mHabitCardContainer = ui.mainInclude.habitRecyclerView;
        mSessionNotificationManager = new SessionNotificationManager(this);
        mHabitDatabase = new HabitDatabase(this);
        mSessionManager = new SessionManager(this);
        mLocalExportManager = new LocalDataExportManager(this);
        mGoogleDriveExportManager = new GoogleDriveDataExportManager(this);
    }
    //endregion

    //region foreground lifetime (onResume - onPause)
    @Override
    protected void onResume() {
        super.onResume();

        int themeMode = mPreferenceChecker.getThemeMode();
        if ((mLastThemeMode != null && mLastThemeMode != themeMode) || AppCompatDelegate.getDefaultNightMode() != themeMode) {
            recreate();
//            mHabitCardContainer.invalidate();
        }

//        if (getIntent().hasExtra(RECYCLER_STATE)) {
//            RecyclerView rv = ui.mainInclude.habitRecyclerView;
//            rv.getLayoutManager().onRestoreInstanceState(getIntent().getExtras().getParcelable(RECYCLER_STATE));
//        }

        prepareNavigationDrawer();
        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
        mFragment.refreshLayout();
//        startRepeatingTask();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        stopRepeatingTask();
//        RecyclerView rv = ui.mainInclude.habitRecyclerView;
//        getIntent().putExtra(RECYCLER_STATE, rv.getLayoutManager().onSaveInstanceState());
//    }

    //endregion

    //region Methods responsible for maintaining state changes
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        if (mCategoryAdapter != null)
//            mCategoryAdapter.onSaveInstanceState(outState);
//
//        RecyclerView rv = ui.mainInclude.habitRecyclerView;
//        outState.putParcelable(RECYCLER_STATE, rv.getLayoutManager().onSaveInstanceState());
//
//    }

//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        if (mCategoryAdapter != null)
//            mCategoryAdapter.onRestoreInstanceState(savedInstanceState);
//    }
    //endregion -- end --

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem search = menu.findItem(R.id.search);
        if (search != null) {
            mSearchView = (SearchView) search.getActionView();
            mSearchView.setQueryHint(getString(R.string.filter_main_activity));
            mSearchView.setOnQueryTextListener(getSearchListener());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Code responsible for updating the ui ---- ]

    private void setFragmentForNavId(@IdRes int menuId) {
        // Create a new fragment and specify the planet to onSessionToggleClick based on position
        switch (menuId) {
            case R.id.home_nav:
                if (mPreferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_CARDS)
                    mFragment = CategoryCardHabitsFragment.newInstance();
                else
                    mFragment = AllHabitsFragment.newInstance();

                break;

            case R.id.archived_habits:
                mFragment = ArchivedHabitsFragment.newInstance();
                break;

            default:
                mFragment = AllHabitsFragment.newInstance();
        }

        clearFocusFromSearchView();

        if (mPreferenceChecker.doHideCurrentSessionCard())
            mCurrentSessionCard.showView(false);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, mFragment)
                .commit();

        // Change the title accordingly
        setTitle(mFragment.getFragmentTitle());
        ui.drawerLayout.closeDrawer(GravityCompat.START);
    }

    public MyFragmentBase getContentFragment(){
        return mFragment;
    }

    //region Code responsible to update habit card views
//    private Runnable mUpdateCards = new Runnable() {
//        @Override
//        public void run() {
//            List<SessionEntry> entries = mSessionManager.getActiveSessionList();
//            if (mHabitAdapter != null)
//                mHabitAdapter.updateHabitViews(entries);

//            mUpdateHandler.postDelayed(mUpdateCards, 1000);
//        }
//    };

//    void startRepeatingTask() {
//        mUpdateCards.run();
//    }

//    void stopRepeatingTask() {
//        mUpdateHandler.removeCallbacks(mUpdateCards);
//    }

//    private void removeHabitView(int position) {
//        mHabitList.remove(position);
//        mHabitAdapter.notifyItemRemoved(position);
//        applySpaceDecoration();
//        ui.mainInclude.habitRecyclerView.invalidateItemDecorations();
//        checkIfHabitsAreAvailable();
//    }

    //region Methods responsible for applying item decorations to the recycler view
//    private GroupDecoration getGroupDecoration() {
//        boolean shouldUseStickyHeaders = mPreferenceChecker.makeCategoryHeadersSticky();
//
//        return new GroupDecoration
//                (MainActivity.this, R.dimen.category_section_text_size, shouldUseStickyHeaders, new GroupDecoration.Callback() {
//                    @Override
//                    public long getGroupId(int position) {
//                        if (position >= 0 && position < mHabitList.size())
//                            return mHabitList.get(position).getCategory().getDatabaseId();
//                        else return -1;
//                    }
//
//                    @Override
//                    public String getGroupFirstLine(int position) {
//                        if (position >= 0 && position < mHabitList.size())
//                            return mHabitList.get(position).getCategory().getName();
//                        else return null;
//                    }
//                });
//    }

    //endregion -- end --

//    private void applyGroupDecoration() {
//        switch (mPreferenceChecker.howToDisplayCategories()) {
//            case PreferenceChecker.AS_CARDS:
//                mHabitCardContainer.removeItemDecoration(mGroupDecoration);
//                break;
//
//            case PreferenceChecker.AS_SECTIONS:
//                mGroupDecoration = getGroupDecoration();
//
//                mHabitCardContainer.addItemDecoration(mGroupDecoration);
//                break;
//
//            case PreferenceChecker.WITHOUT_CATEGORIES:
//                mHabitCardContainer.removeItemDecoration(mGroupDecoration);
//                break;
//        }
//
//        applySpaceDecoration();
//    }

//    private void applySpaceDecoration() {
//        int sessionCount = (int) mSessionManager.getNumberOfActiveSessions();
//        boolean useLargeTopOffset = mPreferenceChecker.shouldShowCurrentSessions(sessionCount);
//        int topOffset = useLargeTopOffset ? (int) getResources().getDimension(R.dimen.large_top_offset_dp) : (int) getResources().getDimension(R.dimen.top_offset_dp);
//
//        if (mPreferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_SECTIONS)
//            topOffset += (int) getResources().getDimension(R.dimen.sections_top_offset_dp);
//
//        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);
//
//        if (mSpaceDecoration != null)
//            mHabitCardContainer.removeItemDecoration(mSpaceDecoration);
//
//        mSpaceDecoration = new SpaceOffsetDecoration(bottomOffset, topOffset);
//        mHabitCardContainer.addItemDecoration(mSpaceDecoration);
//    }

    //endregion -- end --

//    public void showDatabase() {
//        if (mPreferenceChecker.howToDisplayCategories() != PreferenceChecker.AS_CARDS) {
//            mHabitList = mHabitDatabase.getHabits();
//            mHabitAdapter = new HabitViewAdapter(mHabitList, getHabitMenuItemClickListener(), getHabitButtonClickCallback());
//
//            if (mHabitDisplayMode == ONLY_ARCHIVED_HABITS)
//                MyCollectionUtils.filter(mHabitList, Habit.ICheckIfIsNotArchived);
//
//            else if (mHabitDisplayMode == NO_ARCHIVED_HABITS)
//                MyCollectionUtils.filter(mHabitList, Habit.ICheckIfIsArchived);
//
//            Collections.sort(mHabitList, Habit.ICompareCategoryName);
//            mHabitCardContainer.setAdapter(mHabitAdapter);
//        }
//
//        checkIfHabitsAreAvailable();
//    }

//    public void checkIfHabitsAreAvailable() {
//        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;
//
//        ui.contentFrame.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
//        findViewById(R.id.no_habits_available_layout).setVisibility(habitsAvailable ? View.GONE : View.VISIBLE);
//    }

//    public void updateNoResultsLayout(boolean isEmpty) {
//        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;
//        int visibility = isEmpty && habitsAvailable ? View.VISIBLE : View.GONE;
//        View emptyResultsLayout = findViewById(R.id.no_results_layout);
//
//        if (emptyResultsLayout.getVisibility() != visibility) {
//            mCurrentSessionCard.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
//            ui.contentFrame.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
//            emptyResultsLayout.setVisibility(visibility);
//        }
//    }

//    public void updateUiForSearchResult(boolean isEmpty) {
//        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;
//        int visibility = isEmpty && habitsAvailable ? View.VISIBLE : View.GONE;
//        View emptyResultsLayout = findViewById(R.id.no_results_layout);
//        if (emptyResultsLayout.getVisibility() != visibility) {

//            mCurrentSessionCard.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
//            ui.mainInclude.habitRecyclerView.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
//            emptyResultsLayout.setVisibility(visibility);
//        }
//    }

//    private void showArchiveDatabase() {
//        ActionBar toolbar = getSupportActionBar();
//        if (toolbar != null) toolbar.setTitle("Archive");
//
//        mHabitDisplayMode = ONLY_ARCHIVED_HABITS;
//        showDatabase();
//    }

//    public void setInitialFragment() {
//        mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
//        ActionBar toolbar = getSupportActionBar();
//        if (toolbar != null)
//            toolbar.setTitle(getString(R.string.app_name));
//
//        mHabitDisplayMode = NO_ARCHIVED_HABITS;
//        mHabitList.clear();
//        mHabitCardContainer.removeAllViews();
//        showDatabase();
//    }

    //endregion [ ------------ end ------------ ]

    //region [ ---- Methods responsible for handling events ---- ]

    //region Methods responsible for handling search events
    private SearchView.OnQueryTextListener getSearchListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return getContentFragment().handleOnQuery(query);
//                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                runDatabaseQuery(newText);
                return getContentFragment().handleOnQuery(newText);
//                return true;
            }
        };
    }

//    private void runDatabaseQuery(String query) {
//        if (mHabitAdapter != null && query.length() != 0) {
//            final Set<Long> databaseIds = mHabitDatabase.searchHabitDatabase(query);
//            List<Habit> allHabits = mHabitDatabase.getHabits();
//
//            MyCollectionUtils.filter(allHabits, new Predicate<Habit>() {
//                @Override
//                public boolean apply(Habit habit) {
//                    return !databaseIds.contains(habit.getDatabaseId()) ||
//                            mHabitDisplayMode == ONLY_ARCHIVED_HABITS && !habit.getIsArchived() ||
//                            mHabitDisplayMode == NO_ARCHIVED_HABITS && habit.getIsArchived();
//                }
//            });
//
//            mHabitList.clear();
//            mHabitList.addAll(allHabits);
//            mHabitAdapter.notifyDataSetChanged();
//
//            updateUiForSearchResult(mHabitList.isEmpty());
//        }
//        else {
//            showDatabase();
//            updateUiForSearchResult(false);
//        }
//    }
    //endregion -- end --

    //region Methods responsible for handling habit card events
//    private HabitViewAdapter.ButtonClickCallback getHabitButtonClickCallback() {
//        return new HabitViewAdapter.ButtonClickCallback() {
//
//            @Override
//            public View.OnClickListener getPlayButtonClickedListener(final long habitId) {
//                return new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (!mSessionManager.getIsSessionActive(habitId))
//                            SessionActivity.startActivity(MainActivity.this, mHabitDatabase.getHabit(habitId));
//                        else {
//                            boolean isPaused = mSessionManager.getIsPaused(habitId);
//                            mSessionManager.setPauseState(habitId, !isPaused);
//                            mUpdateCards.run();
//                        }
//                    }
//                };
//            }
//
//            @Override
//            public View.OnLongClickListener getPlayButtonLongClickedListener(final long habitId) {
//                return new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        SessionActivity.startActivity(MainActivity.this, mHabitDatabase.getHabit(habitId));
//                        return true;
//                    }
//                };
//            }
//
//            @Override
//            public View.OnClickListener getHabitViewClickedListener(final long habitId) {
//                return new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        HabitActivity.startActivity(MainActivity.this, habitId);
//                    }
//                };
//            }
//
//        };
//    }

//    private HabitViewAdapter.MenuItemClickListener getHabitMenuItemClickListener() {
//        return new HabitViewAdapter.MenuItemClickListener() {
//            @Override
//            public void onHabitEditClick(long habitId) {
//                Habit habit = mHabitDatabase.getHabit(habitId);
//                EditHabitDialog dialog = EditHabitDialog.newInstance(new EditHabitDialog.OnFinishedListener() {
//                    @Override
//                    public void onFinishedWithResult(Habit habit) {
//                        mHabitDatabase.updateHabit(habit.getDatabaseId(), habit);
//
//                        int position = mHabitAdapter.getAdapterItemPosition(habit.getDatabaseId());
//                        mHabitList.set(position, habit);
//                        mHabitAdapter.notifyItemChanged(position);
//                    }
//                }, habit);
//
//                dialog.onSessionToggleClick(getSupportFragmentManager(), "edit-habit");
//            }
//
//            @Override
//            public void onHabitResetClick(final long habitId) {
//                String habitName = mHabitDatabase.getHabitName(habitId);
//                String messageFormat = getString(R.string.confirm_habit_data_reset_message_format);
//                String message = String.format(Locale.getDefault(), messageFormat, habitName);
//                new ConfirmationDialog(MainActivity.this)
//                        .setTitle(getString(R.string.confirm_habit_data_reset_title))
//                        .setMessage(message)
//                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                mHabitDatabase.deleteEntriesForHabit(habitId);
//                                makeText(MainActivity.this, R.string.entries_deleted_message, Toast.LENGTH_SHORT).onSessionToggleClick();
//                            }
//                        })
//                        .onSessionToggleClick();
//            }
//
//            @Override
//            public void onHabitDeleteClick(final long habitId) {
//                String habitName = mHabitDatabase.getHabitName(habitId);
//                new ConfirmationDialog(MainActivity.this)
//                        .setTitle("Confirm Delete")
//                        .setMessage("Do you really want to delete '" + habitName + "'?")
//                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                if (mSessionManager.getIsSessionActive(habitId)) {
//                                    mSessionManager.cancelSession(habitId);
//                                    mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
//                                }
//
//                                mHabitDatabase.deleteHabit(habitId);
//
//                                int position = mHabitAdapter.getAdapterItemPosition(habitId);
//                                removeHabitView(position);
//                            }
//                        })
//                        .onSessionToggleClick();
//            }
//
//            @Override
//            public void onHabitExportClick(long habitId) {
//                Habit habit = mHabitDatabase.getHabit(habitId);
//                mLocalExportManager.shareExportHabit(habit);
//            }
//
//            @Override
//            public void onHabitArchiveClick(final long habitId) {
//                String habitName = mHabitDatabase.getHabitName(habitId);
//                final boolean archivedState = mHabitDatabase.getIsHabitArchived(habitId);
//                String actionName = archivedState ? "Unarchive" : "Archive";
//                String actionNameLower = archivedState ? "unarchive" : "archive";
//
//                new ConfirmationDialog(MainActivity.this)
//                        .setTitle("Confirm " + actionName)
//                        .setMessage("Do you really want to " + actionNameLower + " '" + habitName + "'? ")
//                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                mHabitDatabase.updateHabitIsArchived(habitId, !archivedState);
//
//                                int position = mHabitAdapter.getAdapterItemPosition(habitId);
//                                removeHabitView(position);
//                            }
//                        })
//                        .onSessionToggleClick();
//            }
//
//            @Override
//            public void onHabitStartSession(long habitId) {
//                SessionActivity.startActivity(MainActivity.this, mHabitDatabase.getHabit(habitId));
//            }
//        };
//    }
    //endregion -- end --


    @Override
    public void hideFab(boolean animate) {
        ui.mainInclude.fab.hide(animate);
    }

    @Override
    public void hideCurrentSessionsCard(boolean animate) {
        mCurrentSessionCard.hideView(animate);
    }

    @Override
    public void showFab(boolean animate) {
        ui.mainInclude.fab.show(animate);
    }

    @Override
    public void showCurrentSessionsCard(boolean animate) {
        mCurrentSessionCard.showView(animate);
    }

    @Override
    public IScrollEvents getScrollEventsListener() {
        return new IScrollEvents() {
            FloatingActionButton fab = ui.mainInclude.fab;

            @Override
            public void onScrollUp() {
                if (mPreferenceChecker.hideFabOnScroll() && !fab.isShown())
                    fab.show(true);

                if (mPreferenceChecker.doHideCurrentSessionCard())
                    mCurrentSessionCard.showView();
            }

            @Override
            public void onScrollDown() {
                if (mPreferenceChecker.hideFabOnScroll() && fab.isShown())
                    fab.hide(true);

                if (mPreferenceChecker.doHideCurrentSessionCard())
                    mCurrentSessionCard.hideView();
            }
        };
    }

    private View.OnClickListener getOnSessionsCardClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActiveSessionsActivity.startActivity(MainActivity.this);
            }
        };
    }

    private SessionManager.SessionChangeListeners getOnSessionChangedListener() {
        return new SessionManager.SessionChangeListeners() {
            @Override
            public void onSessionPauseStateChanged(long habitId, boolean isPaused) {}

            @Override
            public void beforeSessionEnded(long habitId, boolean wasCanceled) {
                mSessionNotificationManager.cancel((int) habitId);

                mFragment.notifySessionEnded(habitId);

//                for (int i = 0; i < mHabitList.size(); i++) {
//                    if (mHabitList.get(i).getDatabaseId() == habitId) {
//                        View item = mHabitCardContainer.getChildAt(i);
//                        if (item != null) {
//                            TextView timeTextView = (TextView) item.findViewById(R.id.habit_card_time_display);
//                            timeTextView.setText(getString(R.string.time_display_placeholder));
//
//                            ImageButton pauseButton = (ImageButton) item.findViewById(R.id.session_control_button);
//                            pauseButton.setImageResource(R.drawable.ic_play_24dp);
//                        }
//                        break;
//                    }
//                }
            }

            @Override
            public void afterSessionEnded(long habitId, boolean wasCanceled) {
                mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
            }

            @Override
            public void onSessionStarted(long habitId) {
                if (mPreferenceChecker.doShowNotificationsAutomatically() && mPreferenceChecker.doShowNotifications()) {
                    Habit habit = mHabitDatabase.getHabit(habitId);
                    mSessionNotificationManager.updateNotification(habit);
                }

                mCurrentSessionCard.updateCard(mSessionManager, mPreferenceChecker);
//                applySpaceDecoration();
            }
        };
    }

    private View.OnClickListener getOnNewHabitButtonClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewHabitDialog dialog = NewHabitDialog.newInstance(new NewHabitDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(Habit habit) {
                        mHabitDatabase.addHabit(habit);
                        mFragment.addHabitToLayout(habit);
//                        showDatabase();
                        clearFocusFromSearchView();
                        findViewById(R.id.no_habits_available_layout).setVisibility(View.GONE);
                    }
                });

                dialog.show(getSupportFragmentManager(), "new-habit");
            }
        };
    }

    private void clearFocusFromSearchView() {
        if (mSearchView != null) {
            mSearchView.setQuery("", false);
            mSearchView.clearFocus();
            mSearchView.onActionViewCollapsed();
        }
    }

//    private RecyclerViewScrollObserver getOnScrollListener() {
//        return new RecyclerViewScrollObserver() {
//            FloatingActionButton fab = ui.mainInclude.fab;
//
//            @Override
//            public void onScrollUp() {
//                if (mPreferenceChecker.hideFabOnScroll() && !fab.isShown())
//                    fab.onSessionToggleClick(true);
//
//                if (mPreferenceChecker.doHideCurrentSessionCard())
//                    mCurrentSessionCard.showView();
//            }
//
//            @Override
//            public void onScrollDown() {
//                if (mPreferenceChecker.hideFabOnScroll() && fab.isShown())
//                    fab.hide(true);
//
//                if (mPreferenceChecker.doHideCurrentSessionCard())
//                    mCurrentSessionCard.hideView();
//            }
//        };
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.menu_database_export:
                handleOnExportDatabase();
                break;

            case R.id.menu_database_restore:
                handleOnDatabaseRestore();
                break;

            case R.id.menu_export_database_as_csv:
                handleOnExportDatabaseCsv();
                break;

            case R.id.menu_settings:
                SettingsActivity.startActivity(this);
                break;

            case R.id.menu_about:
                AboutActivity.startActivity(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleOnExportDatabaseCsv() {
        String filepath = mLocalExportManager.exportDatabaseAsCsv();
        String messageFormat = getString(R.string.database_export_format);
        String message = String.format(Locale.getDefault(), messageFormat, filepath);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void handleOnExportDatabase() {
        new ConfirmationDialog(this)
                .setTitle(getString(R.string.confirm_data_export))
                .setMessage(getString(R.string.confirm_data_export_message))
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLocalExportManager.exportDatabase(true);
                        if (mGoogleDriveExportManager.isConnected())
                            mGoogleDriveExportManager.backupDatabase();
                        Toast.makeText(MainActivity.this, R.string.backup_created, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void handleOnDatabaseRestore() {
        new ConfirmationDialog(this)
                .setTitle(getString(R.string.confirm_data_restore))
                .setMessage(getString(R.string.confirm_data_restore_message))
                .setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLocalExportManager.importDatabase(true);
                        mFragment.refreshLayout();
                        makeText(MainActivity.this, R.string.data_restored, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.home_nav:
                setFragmentForNavId(id);
                break;

            case R.id.running_habits_nav:
                ActiveSessionsActivity.startActivity(this);
                break;

            case R.id.archived_habits:
                setFragmentForNavId(id);
                break;

            case (R.id.overall_stats_nav):
                DataOverviewActivity.startActivity(this);
                break;

            case R.id.settings_nav:
                SettingsActivity.startActivity(this);
                break;

            case R.id.about_nav:
                AboutActivity.startActivity(this);
                break;

            case R.id.night_mode:
                ((Switch) item.getActionView().findViewById(R.id.menu_switch)).toggle();
                mPreferenceChecker.toggleNightMode();
                recreate();
                break;
        }

        ui.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (ui.drawerLayout.isDrawerOpen(GravityCompat.START))
            ui.drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.SETTINGS_ACTIVITY) {
            recreate();
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RequestCodes.GOOGLE_DRIVE_REQUEST_CODE:
                    mGoogleDriveExportManager.connect();
                    break;
            }
        }
    }
    //endregion [ ---------------- end ---------------- ]

}