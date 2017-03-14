package com.example.brandon.habitlogger.HabitSessions;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerViewAdapters.HabitViewHolder;
import com.example.brandon.habitlogger.common.TimeDisplay;
import com.example.brandon.habitlogger.databinding.ActivitySessionBinding;

import java.util.Locale;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionActivity extends AppCompatActivity implements
        SessionManager.SessionChangeListeners, View.OnClickListener {

    public static class BundleKeys {
        public static final String SERIALIZED_HABIT = "SERIALIZED_HABIT_KEY";
    }

    AlertDialog confirmationDialog;
    Bundle dialogSettings = new Bundle();

    private static class DialogSettingKeys {
        final static String DIALOG_SETTINGS_BUNDLE = "DIALOG_SETTINGS_BUNDLE";
        final static String SHOW_DIALOG = "SHOW_DIALOG";
        final static String SHOW_CANCEL_DIALOG = "IS_CANCEL";
        final static String SHOULD_PAUSE = "SHOULD_PAUSE";
        public static String INITIAL_PAUSE_STATE = "INITIAL_PAUSE_STATE";
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
        mHabit = (Habit) getIntent().getSerializableExtra(BundleKeys.SERIALIZED_HABIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSessionManager = new SessionManager(this);
        if (!mSessionManager.getIsSessionActive(mHabit.getDatabaseId())) {
            mSessionManager.startSession(mHabit);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(mHabit.getName());
        }

        updateTimeDisplay(true);

        mSessionManager.addSessionChangedCallback(this);
        ui.sessionPausePlay.setOnClickListener(this);
    }
    //endregion // Set-up activity

    //region // Restore activity
    @Override
    protected void onResume() {
        super.onResume();
        updateSessionPlayButton(mSessionManager.getIsPaused(mHabit.getDatabaseId()));
        updateTimeDisplay(true);

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
        mSessionManager.removeSessionChangedCallback(this);
    }
    //endregion // Tear-down activity

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (dialogSettings != null) {
            if (confirmationDialog != null && confirmationDialog.isShowing()) {
                confirmationDialog.dismiss();
            }

            outState.putBundle(DialogSettingKeys.DIALOG_SETTINGS_BUNDLE, dialogSettings);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        dialogSettings = savedInstanceState.getBundle(DialogSettingKeys.DIALOG_SETTINGS_BUNDLE);
        if (dialogSettings == null) throw new RuntimeException();

        else {
            if (dialogSettings.getBoolean(DialogSettingKeys.SHOW_DIALOG)) {
                if (dialogSettings.getBoolean(DialogSettingKeys.SHOW_CANCEL_DIALOG)) {
                    onCancelSessionClicked();
                }
                else {
                    onFinishSessionClicked();
                }
            }
        }
    }

    //endregion // Methods responsible for handling the lifecycle of the activity

    //region // Methods responsible for updating the UI

    //region // Methods for updating the timer.
    private void updateSessionPlayButton(boolean isPaused) {
        ui.sessionPausePlay.setImageResource(
                HabitViewHolder.getResourceIdForPauseButton(isPaused)
        );

        float alphaValue = isPaused ? 0.5f : 1.0f;

        ui.sessionTimeDisplayLayout.setAlpha(alphaValue);
    }

    public void updateTimeDisplay(boolean forceUpdate) {
        boolean shouldUpdate = mSessionManager.getIsSessionActive(mHabit.getDatabaseId()) &&
                !mSessionManager.getIsPaused(mHabit.getDatabaseId());

        if (forceUpdate || shouldUpdate) {
            SessionEntry entry = mSessionManager.getSession(mHabit.getDatabaseId());
            TimeDisplay display = new TimeDisplay(entry.getDuration());

            ui.sessionHoursView.setText(String.format(Locale.US, "%02d", display.hours));
            ui.sessionMinutesView.setText(String.format(Locale.US, "%02d", display.minutes));
            ui.sessionSecondsView.setText(String.format(Locale.US, "%02d", display.seconds));
        }
    }

    private Runnable updateTimeDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeDisplay(false);
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
//        ui.sessionCancel.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);

//        Drawable drawable = ui.sessionNote.getBackground(); // get current EditText drawable
//        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP); // change the drawable color
//        ui.sessionNote.setBackground(drawable);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }
    //endregion // Methods for changing the appearance of the UI

    //endregion // Methods responsible for updating the UI

    //region // Methods responsible for handling events

    //region // Handle onClick events

    //region // Get events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case (R.id.finish_session_button): {
                onFinishSessionClicked();
            }
            break;

            case (R.id.cancel_session_button): {
                onCancelSessionClicked();
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

            case (R.id.session_pause_play): {
                boolean isPaused = !mSessionManager.getIsPaused(mHabit.getDatabaseId());
                mSessionManager.setPauseState(mHabit.getDatabaseId(), isPaused);
            }
            break;
        }
    }
    //endregion

    //region // Handle events
    private void onFinishSessionClicked() {
        boolean shouldAsk = new PreferenceChecker(this).doAskBeforeFinish();

        if (shouldAsk) {
            boolean nightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            int iconRes = nightMode ? R.drawable.ic_check_white_24dp :
                    R.drawable.ic_check_black_24dp;

            askForConfirmation("Finish session", "Finish this session?", true, iconRes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishSession();
                        }
                    });

            dialogSettings.putBoolean(DialogSettingKeys.SHOW_CANCEL_DIALOG, false);
        }
        else {
            finishSession();
        }
    }

    private void onCancelSessionClicked() {
        boolean shouldAsk = new PreferenceChecker(this).doAskBeforeCancel();

        if (shouldAsk) {
            boolean nightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            int iconRes = nightMode ? R.drawable.ic_close_white_24dp :
                    R.drawable.ic_close_black_24dp;

            askForConfirmation("Cancel session", "Cancel this session?", false, iconRes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSessionManager.cancelSession(mHabit.getDatabaseId());
                        }
                    });

            dialogSettings.putBoolean(DialogSettingKeys.SHOW_CANCEL_DIALOG, true);
        }
        else {
            mSessionManager.cancelSession(mHabit.getDatabaseId());
        }
    }

    private void finishSession() {
        // Get the new SessionEntry
        String note = ui.sessionNote.getText().toString();
        SessionEntry entry = mSessionManager.finishSession(mHabit.getDatabaseId());
        entry.setNote(note);

        // Add the new SessionEntry to the database
        HabitDatabase database = new HabitDatabase(SessionActivity.this);
        database.addEntry(mHabit.getDatabaseId(), entry);

        finish();
    }
    //endregion

    private void askForConfirmation(String title, String message, final boolean shouldPause, int iconRes, DialogInterface.OnClickListener onYesMethod) {
        long habitId = mHabit.getDatabaseId();

        if (!dialogSettings.getBoolean(DialogSettingKeys.SHOW_DIALOG)) {
            dialogSettings.putBoolean(DialogSettingKeys.INITIAL_PAUSE_STATE, mSessionManager.getIsPaused(habitId));
        }

        if (shouldPause) {
            if (!mSessionManager.getIsPaused(mHabit.getDatabaseId()))
                mSessionManager.setPauseState(habitId, true);
        }

        confirmationDialog = new AlertDialog.Builder(this)
                .setIcon(iconRes)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", onYesMethod)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogSettings.putBoolean(DialogSettingKeys.SHOW_DIALOG, false);
                        boolean initialPauseState = dialogSettings.getBoolean(DialogSettingKeys.INITIAL_PAUSE_STATE);
                        if (mSessionManager.getIsPaused(mHabit.getDatabaseId()) != initialPauseState)
                            mSessionManager.setPauseState(mHabit.getDatabaseId(), initialPauseState);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialogSettings.putBoolean(DialogSettingKeys.SHOW_DIALOG, false);
                        boolean initialPauseState = dialogSettings.getBoolean(DialogSettingKeys.INITIAL_PAUSE_STATE);
                        if (mSessionManager.getIsPaused(mHabit.getDatabaseId()) != initialPauseState)
                            mSessionManager.setPauseState(mHabit.getDatabaseId(), initialPauseState);
                    }
                })
                .show();

        dialogSettings.putBoolean(DialogSettingKeys.SHOW_DIALOG, true);
    }

    //endregion // Handle onClick events

    //region // Handle SessionManager events
    @Override
    public void onSessionPauseStateChanged(long habitId, boolean isPaused) {
        if (habitId == this.mHabit.getDatabaseId())
            updateSessionPlayButton(isPaused);
    }

    @Override
    public void onSessionEnded(long habitId, boolean wasCanceled) {
        if (habitId == this.mHabit.getDatabaseId() && wasCanceled)
            finish();
    }

    @Override
    public void onSessionStarted(long habitId) {}
    //endregion // Handle SessionManager events

    //endregion // Methods responsible for handling events
}