package com.example.brandon.habitlogger.HabitSessions;

import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.HabitViewHolder;
import com.example.brandon.habitlogger.common.TimeDisplay;
import com.example.brandon.habitlogger.databinding.ActivitySessionBinding;

import java.util.Locale;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionActivity extends AppCompatActivity implements
        SessionManager.SessionChangeListener, View.OnClickListener {

    public static class BundleKeys {
        public static final String SERIALIZED_HABIT = "SERIALIZED_HABIT_KEY";
    }

    private SessionManager mSessionManager;
    private Habit mHabit;

    ActivitySessionBinding ui;
    Handler handler = new Handler();

    //region // Methods responsible for handling the lifecycle of the activity

    //region // Set-up activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ui = DataBindingUtil.setContentView(this, R.layout.activity_session);
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session_confirmation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHabit = (Habit) getIntent().getSerializableExtra(BundleKeys.SERIALIZED_HABIT);

        mSessionManager = new SessionManager(this);
        if (!mSessionManager.getIsSessionActive(mHabit.getDatabaseId())) {
            mSessionManager.startSession(mHabit);
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(mHabit.getName());
        }

        mSessionManager.addSessionChangedListener(this);

        ui.sessionCancel.setOnClickListener(this);
        ui.sessionPausePlay.setOnClickListener(this);
    }
    //endregion // Set-up activity

    //region // Restore activity
    @Override
    protected void onResume() {
        super.onResume();
        updateSessionPlayButton(mSessionManager.getIsPaused(mHabit.getDatabaseId()));
        updateTimeDisplay();

        // Load note from database
        String note = mSessionManager.getNote(mHabit.getDatabaseId());
        if (!note.isEmpty()) {
            ui.sessionNote.setText(note);
        }

        applyHabitColorToTheme();
        startRepeatingTask();
    }
    //endregion // Restore activity

    //region // Tear-down activity
    @Override
    protected void onPause() {
        super.onPause();

        // Save note in database
        mSessionManager.setNote(mHabit.getDatabaseId(), ui.sessionNote.getText().toString());
        stopRepeatingTask();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSessionManager.removeSessionChangedListener(this);
    }
    //endregion // Tear-down activity

    //endregion // Methods responsible for handling the lifecycle of the activity

    //region // Methods responsible for updating the UI

    //region // Methods for updating the timer.
    private void updateSessionPlayButton(boolean isPaused) {
        ui.sessionPausePlay.setImageResource(
                HabitViewHolder.getResourceIdForPauseButton(isPaused)
        );
    }

    public void updateTimeDisplay() {
        SessionEntry entry = mSessionManager.getSession(mHabit.getDatabaseId());
        TimeDisplay display = new TimeDisplay(entry.getDuration());

        ui.sessionHoursView.setText(String.format(Locale.US, "%02d", display.hours));
        ui.sessionMinutesView.setText(String.format(Locale.US, "%02d", display.minutes));
        ui.sessionSecondsView.setText(String.format(Locale.US, "%02d", display.seconds));
    }

    private Runnable updateTimeDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeDisplay();
            handler.postDelayed(updateTimeDisplayRunnable, 1000);
        }
    };

    void startRepeatingTask() {
        updateTimeDisplayRunnable.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(updateTimeDisplayRunnable);
    }
    //endregion // Methods for updating the timer.

    //region // Methods for changing the appearance of the UI
    public void applyHabitColorToTheme() {
        int color = mHabit.getColor();
        int darkerColor = HabitCategory.darkenColor(color, 0.7f);

        getWindow().setStatusBarColor(darkerColor);
        ui.sessionCancel.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);

        Drawable drawable = ui.sessionNote.getBackground(); // get current EditText drawable
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP); // change the drawable color
        ui.sessionNote.setBackground(drawable);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }
    //endregion // Methods for changing the appearance of the UI

    //endregion // Methods responsible for updating the UI

    //region // Methods responsible for handling events

    //region // Handle onClick events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case (R.id.finish_session_button): {
                // Get the new SessionEntry
                String note = ui.sessionNote.getText().toString();
                SessionEntry entry = mSessionManager.finishSession(mHabit.getDatabaseId());
                entry.setNote(note);

                // Add the new SessionEntry to the database
                HabitDatabase database = new HabitDatabase(this);
                database.addEntry(mHabit.getDatabaseId(), entry);

                finish();
            }
            break;

            case (android.R.id.home): {
                finish();
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();

        switch (id) {
            case (R.id.session_cancel): {
                mSessionManager.cancelSession(mHabit.getDatabaseId());
            }
            break;

            case (R.id.session_pause_play): {
                boolean isPaused = !mSessionManager.getIsPaused(mHabit.getDatabaseId());
                mSessionManager.setPauseState(mHabit.getDatabaseId(), isPaused);
            }
            break;
        }
    }
    //endregion // Handle onClick events

    //region // Handle SessionManager events
    @Override
    public void sessionPauseStateChanged(long habitId, boolean isPaused) {
        if (habitId == this.mHabit.getDatabaseId())
            updateSessionPlayButton(isPaused);
    }

    @Override
    public void sessionEnded(long habitId, boolean wasCanceled) {
        if (habitId == this.mHabit.getDatabaseId() && wasCanceled)
            finish();
    }

    @Override
    public void sessionStarted(long habitId) {}
    //endregion // Handle SessionManager events

    //endregion // Methods responsible for handling events
}