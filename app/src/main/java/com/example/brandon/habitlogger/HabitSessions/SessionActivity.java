package com.example.brandon.habitlogger.HabitSessions;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.TimeDisplay;
import com.example.brandon.habitlogger.databinding.ActivitySessionBinding;

import java.util.Locale;

import static com.example.brandon.habitlogger.R.drawable.ic_play_circle_filled_black_24dp;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionActivity extends AppCompatActivity {

    public static final int RESULT_SESSION_FINISH = 300;
    public static final int RESULT_NOTIFICATION_FINISH = 301;
    public static final int RESULT_NOTIFICATION_PAUSE_PLAY = 302;

    Runnable updateTimeDisplayRunnable;
    Handler handler = new Handler();

    private SessionManager sessionManager;
    Habit habit;
    private long habitId;
    private int position = RecyclerView.NO_POSITION;

    private ActionBar actionBar;

    ActivitySessionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_session);
        actionBar = getSupportActionBar();

        Intent data = getIntent();
        habit = (Habit) data.getSerializableExtra("habit");

        habitId = habit.getDatabaseId();

        if (data.hasExtra("position"))
            position = data.getIntExtra("position", RecyclerView.NO_POSITION);

        sessionManager = new SessionManager(this);
        sessionManager.addSessionChangedListener(new SessionManager.SessionChangeListener() {
            @Override
            public void sessionPauseStateChanged(long habitId, boolean isPaused) {
                updateSessionPlayButton(isPaused);
            }

            @Override
            public void sessionEnded(long habitId, boolean wasCanceled) {

            }

            @Override
            public void sessionStarted(long habitId) {

            }
        });

        if (!sessionManager.getIsSessionActive(habitId)) {
            sessionManager.startSession(habit);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(habit.getName());
        }

        binding.sessionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacks(updateTimeDisplayRunnable);
                sessionManager.cancelSession(habitId);


                Intent resultIntent = new Intent();
                resultIntent.putExtra("position", position);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        binding.sessionPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isPaused = !sessionManager.getIsPaused(habitId);
                sessionManager.setPauseState(habitId, isPaused);
                updateSessionPlayButton(isPaused);
            }
        });

        updateTimeDisplayRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeDisplay();
                handler.postDelayed(updateTimeDisplayRunnable, 100);
            }
        };
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
                handler.removeCallbacks(updateTimeDisplayRunnable);
                SessionEntry entry = sessionManager.finishSession(habitId);

                String note = binding.sessionNote.getText().toString();
                entry.setNote(note);

                HabitDatabase database = new HabitDatabase(this);
                database.addEntry(habitId, entry);
                finish();
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareTimerDisplay();
        setColorTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareTimerDisplay();
        setColorTheme();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionManager.setNote(habitId, binding.sessionNote.getText().toString());
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(updateTimeDisplayRunnable);
        super.onPause();
    }

    public void setColorTheme() {
        int color = 0xFFCCCCCC;
        int darkerColor = 0xFFBBBBBB;

        if (!habit.getIsArchived()) {
            color = habit.getCategory().getColorAsInt();
            darkerColor = HabitCategory.darkenColor(color, 0.7f);
        }

        getWindow().setStatusBarColor(darkerColor);
        binding.sessionCancel.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);

        Drawable drawable = binding.sessionNote.getBackground(); // get current EditText drawable
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP); // change the drawable color
        binding.sessionNote.setBackground(drawable);

        actionBar.setBackgroundDrawable(new ColorDrawable(color));
    }

    public void updateTimeDisplay() {
        SessionEntry entry = sessionManager.getSession(habitId);
        TimeDisplay display = new TimeDisplay(entry.getDuration());

        binding.sessionHoursView.setText(String.format(Locale.US, "%02d", display.hours));
        binding.sessionMinutesView.setText(String.format(Locale.US, "%02d", display.minutes));
        binding.sessionSecondsView.setText(String.format(Locale.US, "%02d", display.seconds));
    }

    public void reloadNote() {
        String note = sessionManager.getNote(habitId);
        if (!note.equals("")) {
            binding.sessionNote.setText(note);
        }
    }

    public void prepareTimerDisplay() {
        updateSessionPlayButton(sessionManager.getIsPaused(habitId));
        updateTimeDisplay();
        reloadNote();
        handler.post(updateTimeDisplayRunnable);
    }

    private void updateSessionPlayButton(boolean isPaused) {
        int resourceId = isPaused ? ic_play_circle_filled_black_24dp :
                R.drawable.ic_play_circle_outline_black_24dp;

        binding.sessionPausePlay.setImageResource(resourceId);
    }
}