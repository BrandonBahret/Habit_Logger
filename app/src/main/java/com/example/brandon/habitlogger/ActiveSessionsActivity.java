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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.ActiveSessionViewAdapter;
import com.example.brandon.habitlogger.SessionManager.SessionManager;

import java.util.List;
import java.util.Locale;

public class ActiveSessionsActivity extends AppCompatActivity {

    List<SessionEntry> sessionEntries;
    SessionManager sessionManager;
    HabitDatabase habitDatabase;

    ActiveSessionViewAdapter sessionViewAdapter;
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
        sessionViewAdapter = new ActiveSessionViewAdapter(sessionEntries, this,
                new ActiveSessionViewAdapter.OnClickListeners() {
            @Override
            public void onRootClick(long habitId) {
                startSession(habitId);
            }

            @Override
            public void onPauseClick(ActiveSessionViewAdapter.ViewHolder holder, long habitId) {
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
                    SessionEntry entry = sessionEntries.get(i);
                    long habitId = entry.getHabitId();

                    if(!sessionManager.isSessionActive(habitId)){
                        sessionEntries.remove(i);
                        sessionViewAdapter.notifyItemRemoved(i);
                        size--;
                    }

                    else {
                        long duration = sessionManager.calculateDuration(habitId) / 1000;
                        entry.setDuration(duration);
                        sessionEntries.set(i, entry);

                        View item = sessionViewContainer.getChildAt(i);
                        if(item != null) {
                            TextView timeTextView = (TextView) item.findViewById(R.id.active_habit_time);

                            SessionManager.TimeDisplay time = new SessionManager.TimeDisplay(duration);
                            String timeText = String.format(Locale.US, "%02d:%02d:%02d", time.hours, time.minutes, time.seconds);

                            timeTextView.setText(timeText);
                        }
                    }
                }

                handler.postDelayed(updateCards, 1000);
            }
        };
        handler.post(updateCards);
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

    public void startSession(long habitId){
        Habit habit = habitDatabase.getHabit(habitId);

        handler.removeCallbacks(updateCards);
        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra("habit", habit);
        startActivityForResult(startSession, SessionActivity.RESULT_SESSION_FINISH);
    }
}
