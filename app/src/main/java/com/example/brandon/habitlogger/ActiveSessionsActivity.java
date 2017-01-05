package com.example.brandon.habitlogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.ActiveSessionViewAdapter;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.ActiveSessionViewAdapterWithSections;
import com.example.brandon.habitlogger.SessionManager.SessionManager;

import java.util.List;

public class ActiveSessionsActivity extends AppCompatActivity {

    List<SessionEntry> sessionEntries;
    SessionManager sessionManager;
    HabitDatabase habitDatabase;

    ActiveSessionViewAdapterWithSections sessionViewAdapter;
    RecyclerView sessionViewContainer;

    Runnable updateCards;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_sessions);

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null)
            toolbar.setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(this);
        habitDatabase  = new HabitDatabase(this, null, false);
        sessionEntries = sessionManager.getActiveSessionList();

        sessionViewContainer = (RecyclerView) findViewById(R.id.session_view_container);
        sessionViewAdapter = new ActiveSessionViewAdapterWithSections(sessionEntries, this, new ActiveSessionViewAdapterWithSections.OnClickListeners() {
            @Override
            public void onRootClick(long habitId) {
                startSession(habitId);
            }

            @Override
            public void onPauseClick(ActiveSessionViewAdapterWithSections.ViewHolder holder, long habitId) {
                boolean isPaused = sessionManager.getIsPaused(habitId);

                if(isPaused){
                    sessionManager.playSession(habitId);
                    holder.pauseButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                }
                else{
                    sessionManager.pauseSession(habitId);
                    holder.pauseButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                }
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        sessionViewContainer.setLayoutManager(layoutManager);
        sessionViewContainer.setItemAnimator(new DefaultItemAnimator());
        sessionViewContainer.setAdapter(sessionViewAdapter);

        updateCards = new Runnable() {
            @Override
            public void run() {
                int size = sessionEntries.size();

                for(int i = 0; i < size; i++){
                    long habitId = sessionEntries.get(i).getHabitId();
                    if(sessionManager.isSessionActive(habitId)) {
                        SessionEntry entry = sessionManager.getSession(habitId);
                        sessionEntries.set(i, entry);

                        View item = sessionViewContainer.getChildAt(i);

                        if(item != null) {
                            TextView timeTextView = (TextView) item.findViewById(R.id.active_habit_time);

                            String timeText =
                                    new SessionManager.TimeDisplay(entry.getDuration()).toString();

                            timeTextView.setText(timeText);
                        }
                    }
                    else{
                        sessionEntries.remove(i);
                        sessionViewAdapter.notifyItemRemoved(i);
                        size--;
                    }
                }

                if(sessionManager.getSessionCount() == 0)
                    finish();

                handler.postDelayed(updateCards, 1000);
            }
        };
        handler.post(updateCards);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.active_sessions_menu, menu);

        SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
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

        if(resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED){
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

        if(entries == null){
            entries = sessionManager.getActiveSessionList();
        }

        sessionEntries.clear();
        for (SessionEntry entry : entries) {
            sessionEntries.add(entry);
        }
        sessionViewAdapter.notifyDataSetChanged();
    }

    public TextView getCategoricalSectioningView(String categoryName){

        TextView categorySection = new TextView(ActiveSessionsActivity.this);
        categorySection.setBackgroundResource(R.drawable.underline_background);
        categorySection.setText(categoryName);
        ActiveSessionViewAdapter.setMargins(categorySection, 0, 15, 0, 10);
        categorySection.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        return categorySection;
    }

    public void startSession(long habitId){
        Habit habit = habitDatabase.getHabit(habitId);

        handler.removeCallbacks(updateCards);
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra("habit", habit);
        startActivityForResult(startSession, SessionActivity.RESULT_SESSION_FINISH);
    }
}
