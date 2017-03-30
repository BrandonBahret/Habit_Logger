package com.example.brandon.habitlogger.HabitSessions;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
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
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerViewAdapters.ActiveSessionViewAdapter;
import com.example.brandon.habitlogger.databinding.ActivityActiveSessionsBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ActiveSessionsActivity extends AppCompatActivity implements
        SessionManager.SessionChangeListeners, ActiveSessionViewAdapter.OnClickListeners,
        SearchView.OnQueryTextListener {

    List<Pair<SessionEntry, Integer>> sessionEntriesUndoStack = new ArrayList<>();


    List<SessionEntry> sessionEntries;
    SessionManager sessionManager;

    ActiveSessionViewAdapter sessionViewAdapter;

    private ActivityActiveSessionsBinding ui;
    Handler handler = new Handler();

    //region // Methods responsible for handling the lifecycle of the activity.

    //region // entire lifetime (onCreate - onDestroy)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_active_sessions);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(this);

        sessionEntries = sessionManager.getActiveSessionList();

        Collections.sort(sessionEntries, SessionEntry.ICompareHabitNames);
        Collections.sort(sessionEntries, SessionEntry.ICompareCategoryNames);

        sessionViewAdapter = new ActiveSessionViewAdapter(sessionEntries, this, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        ui.sessionViewContainer.setLayoutManager(layoutManager);
        ui.sessionViewContainer.setItemAnimator(new DefaultItemAnimator());
        ui.sessionViewContainer.setAdapter(sessionViewAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(createItemTouchCallback());
        touchHelper.attachToRecyclerView(ui.sessionViewContainer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.active_sessions_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    //endregion // entire lifetime

    //region // visible lifetime (onStart - onStop)

    @Override
    protected void onStart() {
        super.onStart();
        sessionManager.addSessionChangedCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sessionManager.removeSessionChangedCallback(this);
    }

    //endregion // visible lifetime

    //region // foreground lifetime (onResume - onPause)

    @Override
    protected void onResume() {
        super.onResume();

        if (sessionManager.getSessionCount() == 0)
            finish();

        startRepeatingTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRepeatingTask();
    }

    //endregion // foreground lifetime

    //endregion // Methods responsible for handling the lifecycle of the activity.

    //region // Methods responsible for updating the ui.

    void startRepeatingTask() {
        updateCards.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(updateCards);
    }

    private Runnable updateCards = new Runnable() {
        @Override
        public void run() {

            int size = sessionEntries.size();
            for (int i = 0; i < size; i++) {

                long habitId = sessionEntries.get(i).getHabitId();
                if (sessionManager.getIsSessionActive(habitId)) {
                    SessionEntry entry = sessionManager.getSession(habitId);
                    sessionEntries.set(i, entry);

                    View item = ui.sessionViewContainer.getChildAt(i);

                    if (item != null) {
                        TextView timeTextView = (TextView) item.findViewById(R.id.active_habit_time);
                        timeTextView.setText(entry.stringifyDuration());
                    }
                }
                else {
                    sessionEntries.remove(i);
                    sessionViewAdapter.notifyItemRemoved(i);
                    size--;
                }
            }

            handler.postDelayed(updateCards, 1000);
        }
    };

    //endregion

    //region // Methods for handling events

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

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
                long habitId = sessionEntries.get(position).getHabitId();
                sessionEntriesUndoStack.add(0, new Pair<>(sessionManager.getSession(habitId), position));

                sessionManager.cancelSession(habitId);
                sessionEntries.remove(position);
                sessionViewAdapter.notifyItemRemoved(position);

                Snackbar.make(findViewById(R.id.activity_active_sessions), "Session canceled", Snackbar.LENGTH_LONG)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);

                                if (sessionManager.getSessionCount() == 0)
                                    finish();

                                if (event != DISMISS_EVENT_CONSECUTIVE)
                                    sessionEntriesUndoStack.clear();

                            }
                        })
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                for (Pair entryPair : sessionEntriesUndoStack) {
                                    sessionManager.insertSession((SessionEntry) entryPair.first);

                                    sessionEntries.add((Integer) entryPair.second, (SessionEntry) entryPair.first);
                                    sessionViewAdapter.notifyItemInserted((Integer) entryPair.second);
                                }

                                sessionEntriesUndoStack.clear();

//                                Collections.sort(sessionEntries, SessionEntry.ICompareHabitNames);
//                                Collections.sort(sessionEntries, SessionEntry.ICompareCategoryNames);
//                                sessionViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .setActionTextColor(ContextCompat.getColor(ActiveSessionsActivity.this, R.color.colorAccent))
                        .show();
            }
        };
    }

    //region // SearchView listeners
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        List<SessionEntry> entries = sessionManager.queryActiveSessionList(query);

        if (entries.isEmpty()) {
            entries = sessionManager.getActiveSessionList();
        }

        sessionEntries.clear();
        sessionEntries.addAll(entries);

        sessionViewAdapter.notifyDataSetChanged();
        return true;
    }
    //endregion

    //region // Adapter Listeners
    @Override
    public void onRootClick(long habitId) {
        Habit habit = new HabitDatabase(this).getHabit(habitId);
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) habit);
        startActivity(startSession);
    }

    @Override
    public void onPauseClick(ActiveSessionViewAdapter.ViewHolder holder, long habitId) {
        boolean isPaused = sessionManager.getIsPaused(habitId);

        sessionManager.setPauseState(habitId, !isPaused);
        sessionViewAdapter.notifyItemChanged(holder.getAdapterPosition());
    }
    //endregion

    //region // SessionManager listeners
    @Override
    public void onSessionPauseStateChanged(long habitId, boolean isPaused) {
        for (SessionEntry entry : sessionEntries) {
            if (entry.getHabit().getDatabaseId() == habitId) {
                int position = sessionEntries.indexOf(entry);
                sessionViewAdapter.notifyItemChanged(position);
                break;
            }
        }
    }

    @Override
    public void beforeSessionEnded(long habitId, boolean wasCanceled) {}

    @Override
    public void afterSessionEnded(long habitId, boolean wasCanceled) {}

    @Override
    public void onSessionStarted(long habitId) {}
    //endregion

    //endregion // Methods for handling events
}