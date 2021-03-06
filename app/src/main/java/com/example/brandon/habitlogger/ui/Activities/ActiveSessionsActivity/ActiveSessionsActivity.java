package com.example.brandon.habitlogger.ui.Activities.ActiveSessionsActivity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.databinding.ActivityActiveSessionsBinding;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity.SessionActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.widget.Toast.makeText;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ActiveSessionsActivity extends AppCompatActivity implements
        ActiveSessionViewAdapter.OnClickListeners, SearchView.OnQueryTextListener {

    //region (Member attributes)
    private static final String KEY_ACTIVE_SESSIONS = "KEY_ACTIVE_SESSIONS";
    private static final String KEY_SEARCH_QUERY = "KEY_SEARCH_QUERY";

    private List<Pair<SessionEntry, Integer>> mUndoStack = new ArrayList<>();
    private List<SessionEntry> mActiveSessions;
    private String mSearchQuery;

    private SessionManager mSessionManager;
    private PreferenceChecker mPreferenceChecker;

    private Handler mUpdateHandler = new Handler();
    private ActiveSessionViewAdapter mSessionViewAdapter;
    //endregion

    //region [ ---- Methods responsible for handling the lifecycle of the activity. ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityActiveSessionsBinding ui = DataBindingUtil.setContentView(this, R.layout.activity_active_sessions);

        setSupportActionBar(ui.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSessionManager = new SessionManager(this);
        mPreferenceChecker = new PreferenceChecker(this);

        if (savedInstanceState != null) {
            mActiveSessions = savedInstanceState.getParcelableArrayList(KEY_ACTIVE_SESSIONS);
            mSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY);
        }
        else {
            mActiveSessions = mSessionManager.getActiveSessionList();
            Collections.sort(mActiveSessions, SessionEntry.ICompareHabitNames);
            Collections.sort(mActiveSessions, SessionEntry.ICompareCategoryNames);
        }

        mSessionViewAdapter = new ActiveSessionViewAdapter(mActiveSessions, this, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        ui.sessionViewContainer.setLayoutManager(layoutManager);
        ui.sessionViewContainer.setItemAnimator(new DefaultItemAnimator());
        ui.sessionViewContainer.setAdapter(mSessionViewAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(createItemTouchCallback());
        touchHelper.attachToRecyclerView(ui.sessionViewContainer);

        showNoDataLayouts(mActiveSessions.isEmpty());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.active_sessions_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        //focus the SearchView
        if (mSearchQuery != null && !mSearchQuery.isEmpty()) {
            searchMenuItem.expandActionView();
            searchView.setQuery(mSearchQuery, false);
            searchView.clearFocus();
        }

        searchView.setQueryHint(getString(R.string.search));
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    //endregion -- end --

    //region visible lifetime (onStart - onStop)
    @Override
    protected void onStart() {
        super.onStart();
        mSessionManager.addSessionChangedCallback(onSessionChangeCallback);
        startRepeatingTask();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSessionManager.removeSessionChangedCallback(onSessionChangeCallback);
        stopRepeatingTask();
    }
    //endregion -- end --

    //region foreground lifetime (onResume - onPause)
    @Override
    protected void onResume() {
        super.onResume();

        if (!mPreferenceChecker.allowActiveSessionsActivity(mSessionManager.hasActiveSessions()))
            finish();

        showNoDataLayouts(mActiveSessions == null || mActiveSessions.isEmpty());
    }
    //endregion -- end --

    //endregion [ ---------------- end ---------------- ]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_ACTIVE_SESSIONS, (ArrayList<? extends Parcelable>) mActiveSessions);
        outState.putString(KEY_SEARCH_QUERY, mSearchQuery);
    }

    //region Methods responsible for updating the ui

    void startRepeatingTask() {
        mUpdateCards.run();
    }

    void stopRepeatingTask() {
        mUpdateHandler.removeCallbacks(mUpdateCards);
    }

    private Runnable mUpdateCards = new Runnable() {
        @Override
        public void run() {

            int size = mActiveSessions.size();
            for (int i = 0; i < size; i++) {

                long habitId = mActiveSessions.get(i).getHabitId();
                if (mSessionManager.getIsSessionActive(habitId)) {
                    SessionEntry entry = mSessionManager.getSession(habitId);
                    mActiveSessions.set(i, entry);
                    mSessionViewAdapter.notifyItemChanged(i, entry);
                }
                else {
                    mActiveSessions.remove(i);
                    mSessionViewAdapter.notifyItemRemoved(i);
                    size--;
                }
            }

            mUpdateHandler.postDelayed(mUpdateCards, 1000);
        }
    };

    public void showNoDataLayouts(boolean noEntriesDisplayed) {

        int noActiveSessionsLayoutVisibility = !mSessionManager.hasActiveSessions() ? View.VISIBLE : View.GONE;
        View noActiveSessionsLayout = findViewById(R.id.no_active_sessions_layout);
        if (noActiveSessionsLayoutVisibility != noActiveSessionsLayout.getVisibility()) {
            noActiveSessionsLayout.setVisibility(noActiveSessionsLayoutVisibility);
        }

        int noResultsLayoutVisibility = noEntriesDisplayed && mSessionManager.hasActiveSessions() ? View.VISIBLE : View.GONE;
        View noResultsLayout = findViewById(R.id.no_results_layout);
        if (noResultsLayoutVisibility != noResultsLayout.getVisibility())
            noResultsLayout.setVisibility(noResultsLayoutVisibility);

        findViewById(R.id.content).setVisibility(noEntriesDisplayed ? View.GONE : View.VISIBLE);
    }

    //endregion

    //region [ ---- Methods for handling events ---- ]

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    private ItemTouchHelper.Callback createItemTouchCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder
                    viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                final CardView rootView = ((CardView) (viewHolder.itemView));
                final float defaultElevation = getResources().getDimension(R.dimen.cardview_default_elevation);
                final float targetElevation = getResources().getDimension(R.dimen.slide_elevation);

                if (!isCurrentlyActive) {
                    ValueAnimator anim = ValueAnimator.ofFloat(targetElevation, defaultElevation);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float val = (Float) valueAnimator.getAnimatedValue();
                            rootView.setCardElevation(val);
                        }
                    });
                    anim.setDuration(150);
                    anim.start();
                }
                else {
                    rootView.setCardElevation(targetElevation);
                }

            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                long habitId = mActiveSessions.get(position).getHabitId();
                mUndoStack.add(0, new Pair<>(mSessionManager.getSession(habitId), position));

                mSessionManager.cancelSession(habitId);
                mActiveSessions.remove(position);
                showNoDataLayouts(mActiveSessions.isEmpty());
                mSessionViewAdapter.notifyItemRemoved(position);

                Snackbar.make(findViewById(R.id.activity_active_sessions), "Session canceled", Snackbar.LENGTH_LONG)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);

                                if (!mPreferenceChecker.allowActiveSessionsActivity(mSessionManager.hasActiveSessions()))
                                    finish();

                                if (event != DISMISS_EVENT_CONSECUTIVE)
                                    mUndoStack.clear();

                            }
                        })
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                for (Pair entryPair : mUndoStack) {
                                    mSessionManager.insertSession((SessionEntry) entryPair.first);

                                    mActiveSessions.add((Integer) entryPair.second, (SessionEntry) entryPair.first);
                                    mSessionViewAdapter.notifyItemInserted((Integer) entryPair.second);
                                }

                                showNoDataLayouts(mActiveSessions.isEmpty());
                                mUndoStack.clear();
                            }
                        })
                        .setActionTextColor(ContextCompat.getColor(ActiveSessionsActivity.this, R.color.colorAccent))
                        .show();
            }
        };
    }

    //region Methods responsible for handling search events
    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQueryTextChange(mSearchQuery);
    }

    @Override
    public boolean onQueryTextChange(String query) {

        mSearchQuery = query.trim();
        List<SessionEntry> entries = mSessionManager.queryActiveSessionList(mSearchQuery);

        if (mSearchQuery.isEmpty())
            entries = mSessionManager.getActiveSessionList();

        mActiveSessions.clear();
        mActiveSessions.addAll(entries);

        mSessionViewAdapter.notifyDataSetChanged();

        showNoDataLayouts(mActiveSessions.isEmpty());

        return true;
    }
    //endregion

    //region Methods responsible for handling RecyclerView mAdapter events
    @Override
    public void onSessionViewClick(long habitId) {
        Habit habit = new HabitDatabase(this).getHabit(habitId);
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) habit);
        startActivity(startSession);
    }

    @Override
    public void onSessionPauseButtonClick(ActiveSessionViewAdapter.ViewHolder holder, long habitId) {
        boolean isPaused = mSessionManager.getIsPaused(habitId);

        mSessionManager.setPauseState(habitId, !isPaused);
        mSessionViewAdapter.notifyItemChanged(holder.getAdapterPosition());
    }
    //endregion

    //region Methods responsible for handling SessionManager events
    private SessionManager.SessionChangeCallback onSessionChangeCallback = new SessionManager.SessionChangeCallback() {
        @Override
        public void onSessionPauseStateChanged(long habitId, boolean isPaused) {
            for (SessionEntry entry : mActiveSessions) {
                if (entry.getHabit().getDatabaseId() != habitId) continue;

                int position = mActiveSessions.indexOf(entry);
                mSessionViewAdapter.notifyItemChanged(position);
                break;
            }
        }
    };
    //endregion

    //endregion [ ---------------- end ---------------- ]

    public static void startActivity(Context context) {
        boolean hasActiveSessions = new SessionManager(context).hasActiveSessions();
        PreferenceChecker preferenceChecker = new PreferenceChecker(context);

        if (preferenceChecker.allowActiveSessionsActivity(hasActiveSessions)) {
            Intent intent = new Intent(context, ActiveSessionsActivity.class);
            context.startActivity(intent);
        }
        else {
            makeText(context, R.string.cannot_open_active_sessions_activity, Toast.LENGTH_SHORT).show();
        }
    }

}