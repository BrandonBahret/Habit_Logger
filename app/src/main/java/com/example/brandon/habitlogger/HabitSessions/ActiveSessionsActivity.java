package com.example.brandon.habitlogger.HabitSessions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.ActiveSessionViewAdapter;
import com.example.brandon.habitlogger.common.TimeDisplay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ActiveSessionsActivity extends AppCompatActivity {

    List<SessionEntry> sessionEntriesUndoStack = new ArrayList<>();
    List<SessionEntry> sessionEntries;
    SessionManager sessionManager;
    HabitDatabase habitDatabase;

    ActiveSessionViewAdapter sessionViewAdapter;
    RecyclerView sessionViewContainer;

    Snackbar snackbar;

    Runnable updateCards;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_sessions);

        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null)
            toolbar.setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(this);
        sessionManager.addSessionChangedListener(new SessionManager.SessionChangeListener() {
            @Override
            public void sessionPauseStateChanged(long habitId, boolean isPaused) {
                for (SessionEntry entry : sessionEntries) {
                    if (entry.getHabitId() == habitId) {
                        int position = sessionEntries.indexOf(entry);
                        sessionViewAdapter.notifyItemChanged(position);
                        break;
                    }
                }
            }

            @Override
            public void sessionEnded(long habitId, boolean wasCanceled) {

            }

            @Override
            public void sessionStarted(long habitId) {

            }
        });

        habitDatabase = new HabitDatabase(this);
        sessionEntries = sessionManager.getActiveSessionList();

        sessionViewContainer = (RecyclerView) findViewById(R.id.session_view_container);
        sessionViewAdapter = new ActiveSessionViewAdapter(sessionEntries, this,
                new ActiveSessionViewAdapter.OnClickListeners() {
                    @Override
                    public void onRootClick(long habitId) {
                        startSession(habitId);
                    }

                    @Override
                    public void onPauseClick(ActiveSessionViewAdapter.ViewHolder holder, long habitId) {
                        boolean isPaused = sessionManager.getIsPaused(habitId);
                        sessionManager.setPauseState(habitId, !isPaused);
                        sessionViewAdapter.notifyItemChanged(holder.getAdapterPosition());
                    }
                });

        ItemTouchHelper touchHelper = new ItemTouchHelper(createItemTouchCallback());
        touchHelper.attachToRecyclerView(sessionViewContainer);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        sessionViewContainer.setLayoutManager(layoutManager);
        sessionViewContainer.setItemAnimator(new DefaultItemAnimator());
        sessionViewContainer.setAdapter(sessionViewAdapter);

        updateCards = new Runnable() {
            @Override
            public void run() {
                int size = sessionEntries.size();

                for (int i = 0; i < size; i++) {
                    long habitId = sessionEntries.get(i).getHabitId();
                    if (sessionManager.getIsSessionActive(habitId)) {
                        SessionEntry entry = sessionManager.getSession(habitId);
                        sessionEntries.set(i, entry);

                        View item = sessionViewContainer.getChildAt(i);

                        if (item != null) {
                            TextView timeTextView = (TextView) item.findViewById(R.id.active_habit_time);

                            String timeText = TimeDisplay.getDisplay(entry.getDuration());

                            timeTextView.setText(timeText);
                        }
                    }
                    else {
                        sessionEntries.remove(i);
                        sessionViewAdapter.notifyItemRemoved(i);
                        size--;
                    }
                }


                if (sessionManager.getSessionCount() == 0 && !isSnackBarShown())
                    finish();

                handler.postDelayed(updateCards, 1000);
            }
        };
        handler.post(updateCards);
    }

    private boolean isSnackBarShown() {
        boolean snackBarVisible = false;
        if (snackbar != null) {
            snackBarVisible = snackbar.isShownOrQueued();
        }

        return snackBarVisible;
    }

    private ItemTouchHelper.Callback createItemTouchCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                long habitId = sessionEntries.get(position).getHabitId();
                sessionEntriesUndoStack.add(sessionManager.getSession(habitId));

                sessionManager.cancelSession(habitId);
                sessionEntries.remove(position);
                sessionViewAdapter.notifyItemRemoved(position);

                if (!isSnackBarShown()) {
                    snackbar = Snackbar.make(findViewById(R.id.activity_active_sessions), "Session canceled", Snackbar.LENGTH_LONG)
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    super.onDismissed(snackbar, event);
                                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                                        sessionEntriesUndoStack.clear();

                                        if (sessionManager.getSessionCount() == 0) {
                                            finish();
                                        }
                                    }
                                }
                            })
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    for (SessionEntry entry : sessionEntriesUndoStack) {
                                        sessionManager.insertSession(entry);
                                        sessionEntries.add(entry);
                                    }
                                    sessionEntriesUndoStack.clear();
                                    sortEntries();

                                    sessionViewAdapter.notifyDataSetChanged();
                                }
                            })
                            .setActionTextColor(ContextCompat.getColor(ActiveSessionsActivity.this, R.color.colorAccent));

                    snackbar.show();
                }
                else {
                    snackbar.show();
                }
            }
        };
    }

    private void sortEntries() {
        Collections.sort(this.sessionEntries, SessionEntry.Alphabetical);
        Collections.sort(this.sessionEntries, SessionEntry.CategoryComparator);
    }

    private void sortEntries(List<SessionEntry> sessionEntries) {
        Collections.sort(sessionEntries, SessionEntry.Alphabetical);
        Collections.sort(sessionEntries, SessionEntry.CategoryComparator);
    }

    private void cancelSession(int adapterPosition) {
        SessionEntry entry = sessionEntries.get(adapterPosition);
        sessionManager.cancelSession(entry.getHabitId());
        handler.post(updateCards);
    }

    private void updateSessionToggleButtonImageResource(int adapterPosition, boolean isPaused) {
        SessionEntry entry = sessionEntries.get(adapterPosition);
        entry.setIsPaused(isPaused);
        sessionViewAdapter.notifyItemChanged(adapterPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.active_sessions_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        processUserQuery(query);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {
                        processUserQuery(query);

                        return false;
                    }
                }
        );

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == SessionActivity.RESULT_SESSION_FINISH) {
                handler = new Handler();
                handler.post(updateCards);

                // Just in case the user paused/resumed the session, notify "changes."
                sessionViewAdapter.notifyDataSetChanged();
            }
        }
    }

    public void processUserQuery(String query) {
        List<SessionEntry> entries = sessionManager.queryActiveSessionList(query);

        if (entries == null) {
            entries = sessionManager.getActiveSessionList();
        }

        sessionEntries.clear();
        for (SessionEntry entry : entries) {
            sessionEntries.add(entry);
        }
        sessionViewAdapter.notifyDataSetChanged();
    }

    public TextView getCategoricalSectioningView(String categoryName) {

        TextView categorySection = new TextView(ActiveSessionsActivity.this);
        categorySection.setBackgroundResource(R.drawable.underline_background);
        categorySection.setText(categoryName);
        ActiveSessionViewAdapter.setMargins(categorySection, 0, 15, 0, 10);
        categorySection.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        return categorySection;
    }

    public void startSession(long habitId) {
        Habit habit = habitDatabase.getHabit(habitId);

        handler.removeCallbacks(updateCards);
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra("habit", (Serializable) habit);
        startActivityForResult(startSession, SessionActivity.RESULT_SESSION_FINISH);
    }
}
