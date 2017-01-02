package com.example.brandon.habitlogger;

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

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.ActiveSessionViewAdapter;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.RecyclerTouchListener;
import com.example.brandon.habitlogger.SessionManager.SessionManager;

import java.util.List;

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
        sessionViewAdapter = new ActiveSessionViewAdapter(sessionEntries, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        sessionViewContainer.setLayoutManager(layoutManager);
        sessionViewContainer.setItemAnimator(new DefaultItemAnimator());
        sessionViewContainer.setAdapter(sessionViewAdapter);

        sessionViewContainer.addOnItemTouchListener(new RecyclerTouchListener(this, sessionViewContainer, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                long habitId = sessionEntries.get(position).getHabitId();
                startSession(habitId);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        updateCards = new Runnable() {
            @Override
            public void run() {
                int size = sessionEntries.size();

                for(int i = 0; i < size; i++){
                    SessionEntry entry = sessionEntries.get(i);

                    if(!sessionManager.isSessionActive(entry.getHabitId())){
                        sessionEntries.remove(i);
                        continue;
                    }

                    long duration = sessionManager.calculateDuration(entry.getHabitId());
                    entry.setDuration(duration);
                    sessionEntries.set(i, entry);
                }

                sessionViewAdapter.notifyDataSetChanged();
                handler.postDelayed(updateCards, 100);
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

    public void startSession(long habitId){
        Habit habit = habitDatabase.getHabit(habitId);

        Intent startSession = new Intent(this, SessionActivity.class);
        startSession.putExtra("habit", habit);
        startActivityForResult(startSession, SessionActivity.RESULT_SESSION_FINISH);
    }
}
