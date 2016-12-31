package com.example.brandon.habitlogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.SessionManager.SessionManager;

import java.util.Locale;

public class SessionActivity extends AppCompatActivity {

    public static final int RESULT_SESSION_FINISH = 300;

    Runnable updateTimeDisplayRunnable;
    Handler handler;

    private SessionManager sessionManager;
    private Habit habit;
    private long habitId;

    private ImageButton playButton;
    private TextView hoursView, minutesView, secondsView;
    private EditText noteArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        playButton = (ImageButton)findViewById(R.id.session_pause_play);

        hoursView   = (TextView) findViewById(R.id.session_hours_view);
        minutesView = (TextView) findViewById(R.id.session_minutes_view);
        secondsView = (TextView) findViewById(R.id.session_seconds_view);
        noteArea = (EditText) findViewById(R.id.session_note);

        habit = (Habit)getIntent().getSerializableExtra("habit");
        habitId = habit.getDatabaseId();

        sessionManager = new SessionManager(this);
        if(!sessionManager.isSessionActive(habitId)){
            sessionManager.startSession(habitId);
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(habit.getName());
        }

        Button cancelButton = (Button)findViewById(R.id.session_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionManager.cancelSession(habitId);
                finish();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sessionManager.getIsPaused(habitId)){
                    sessionManager.playSession(habitId);
                    playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                }
                else{
                    sessionManager.pauseSession(habitId);
                    playButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                }
            }
        });

        updateTimeDisplayRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeDisplay();
                handler.postDelayed(updateTimeDisplayRunnable, 1000);
            }
        };

        handler = new Handler();
    }



    public void updateTimeDisplay(){
        SessionEntry entry = sessionManager.getSession(habitId);

        long time = entry.getDuration();
        SessionManager.TimeDisplay display = new SessionManager.TimeDisplay(time);

        hoursView.setText(String.format(Locale.US, "%02d",   display.hours));
        minutesView.setText(String.format(Locale.US, "%02d", display.minutes));
        secondsView.setText(String.format(Locale.US, "%02d", display.seconds));
    }

    public void reloadNote(){
        String note = sessionManager.getNote(habitId);
        if(!note.equals("")){
            noteArea.setText(note);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session_confirmation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case (R.id.finish_session_button): {
                SessionEntry entry = sessionManager.finishSession(habitId);

                String note = noteArea.getText().toString();
                entry.setNote(note);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("entry", entry);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(sessionManager.getIsPaused(habitId)){
            playButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }
        updateTimeDisplay();
        reloadNote();

        handler.postDelayed(updateTimeDisplayRunnable, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(sessionManager.getIsPaused(habitId)){
            playButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }
        updateTimeDisplay();
        reloadNote();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionManager.setNote(habitId, noteArea.getText().toString());
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(updateTimeDisplayRunnable);
        super.onPause();
    }
}