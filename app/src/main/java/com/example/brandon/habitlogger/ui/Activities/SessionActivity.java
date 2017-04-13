package com.example.brandon.habitlogger.ui.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.databinding.ActivitySessionBinding;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.HabitViewHolder;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;

import java.io.Serializable;
import java.util.Locale;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionActivity extends AppCompatActivity implements
        SessionManager.SessionChangeListeners, View.OnClickListener {

    public static class BundleKeys {
        public static final String SERIALIZED_HABIT = "SERIALIZED_HABIT_KEY";
    }

    private static class DialogSettingKeys {
        static final String DIALOG_SETTINGS_BUNDLE = "DIALOG_SETTINGS_BUNDLE";
        static final String SHOW_DIALOG = "SHOW_DIALOG";
        static final String SHOW_CANCEL_DIALOG = "IS_CANCEL";
        static final String SHOULD_PAUSE = "SHOULD_PAUSE";
        static final String INITIAL_PAUSE_STATE = "INITIAL_PAUSE_STATE";
    }

    //region (Member attributes)
    private ConfirmationDialog mConfirmationDialog;
    private Bundle mDialogSettings = new Bundle();

    private SessionManager mSessionManager;
    private Habit mHabit;

    private Handler mUpdateHandler = new Handler();
    ActivitySessionBinding ui;
    //endregion

    //region [ ---- Methods responsible for handling the lifecycle of the activity ---- ]

    //region entire lifetime (onCreate - onDestroy)
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
    //endregion -- end --

    //region visible lifetime (onStart - onStop)
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

    @Override
    protected void onStop() {
        super.onStop();
        mSessionManager.removeSessionChangedCallback(this);
    }
    //endregion -- end --

    //region foreground lifetime (onResume - onPause)
    @Override
    protected void onResume() {
        super.onResume();
        updateSessionPlayButton(mSessionManager.getIsPaused(mHabit.getDatabaseId()));
        updateTimeDisplay(true);

        // Load note from database
        String note = mSessionManager.getNote(mHabit.getDatabaseId());
        if (!note.isEmpty()) ui.sessionNote.setText(note);

        applyHabitColorToTheme();
        startRepeatingTask();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save note in database
        mSessionManager.setNote(mHabit.getDatabaseId(), ui.sessionNote.getText().toString());
        stopRepeatingTask();
    }
    //endregion -- end --

    //region Methods responsible for maintaining state
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mDialogSettings != null) {
//            if (mConfirmationDialog != null && mConfirmationDialog.isShowing())
//                mConfirmationDialog.dismiss();

            outState.putBundle(DialogSettingKeys.DIALOG_SETTINGS_BUNDLE, mDialogSettings);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mDialogSettings = savedInstanceState.getBundle(DialogSettingKeys.DIALOG_SETTINGS_BUNDLE);

        if (mDialogSettings != null && mDialogSettings.getBoolean(DialogSettingKeys.SHOW_DIALOG)) {
            if (mDialogSettings.getBoolean(DialogSettingKeys.SHOW_CANCEL_DIALOG))
                onCancelSessionClicked();
            else
                onFinishSessionClicked();
        }
    }
    //endregion -- end --

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Methods responsible for updating the UI ---- ]

    //region Methods responsible for updating the timer
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

            int time[] = MyTimeUtils.getTimePortion(entry.getDuration());

            ui.sessionHoursView.setText(String.format(Locale.US, "%02d", time[0]));
            ui.sessionMinutesView.setText(String.format(Locale.US, "%02d", time[1]));
            ui.sessionSecondsView.setText(String.format(Locale.US, "%02d", time[2]));

        }
    }

    private Runnable updateTimeDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeDisplay(false);
            mUpdateHandler.postDelayed(updateTimeDisplayRunnable, 1000);
        }
    };

    void startRepeatingTask() {
        updateTimeDisplayRunnable.run();
    }

    void stopRepeatingTask() {
        mUpdateHandler.removeCallbacks(updateTimeDisplayRunnable);
    }
    //endregion -- end --

    public void applyHabitColorToTheme() {

        int color = mHabit.getColor();
        int darkerColor = MyColorUtils.darkenColorBy(color, 0.08f);

        boolean isNightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        if (isNightMode) {
            if (MyColorUtils.getLightness(color) > 0.40) { // Clip lightness to 40% max
                darkerColor = MyColorUtils.setLightness(darkerColor, 0.40f);
                color = MyColorUtils.setLightness(color, 0.45f);
            }

            if (MyColorUtils.getSaturation(color) > 0.45) { // Clip saturation to 45% max
                darkerColor = MyColorUtils.setSaturation(darkerColor, 0.45f);
                color = MyColorUtils.setSaturation(color, 0.45f);
            }
        }

        getWindow().setStatusBarColor(darkerColor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
    }

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Methods responsible for handling events ---- ]

    //region [ -- Handle onClick events -- ]

    //region Actionbar events

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.finish_session_button:
                onFinishSessionClicked();
                break;

            case R.id.cancel_session_button:
                onCancelSessionClicked();
                break;

            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onFinishSessionClicked() {
        boolean shouldAsk = new PreferenceChecker(this).doAskBeforeFinish();

        if (shouldAsk) {
            boolean nightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            int iconRes = R.drawable.ic_check_24dp;

            askForConfirmation("Finish session", "Finish this session?", true, iconRes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishSession();
                        }
                    });

            mDialogSettings.putBoolean(DialogSettingKeys.SHOW_CANCEL_DIALOG, false);
        }
        else finishSession();
    }

    private void onCancelSessionClicked() {
        boolean shouldAsk = new PreferenceChecker(this).doAskBeforeCancel();

        if (shouldAsk) {
            int iconRes = R.drawable.ic_close_24dp;

            askForConfirmation("Cancel session", "Cancel this session?", false, iconRes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSessionManager.cancelSession(mHabit.getDatabaseId());
                        }
                    });

            mDialogSettings.putBoolean(DialogSettingKeys.SHOW_CANCEL_DIALOG, true);
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

    private void askForConfirmation(String title, String message, final boolean shouldPause,
                                    int iconRes, DialogInterface.OnClickListener onYesMethod) {

        long habitId = mHabit.getDatabaseId();

        if (!mDialogSettings.getBoolean(DialogSettingKeys.SHOW_DIALOG)) {
            mDialogSettings.putBoolean(DialogSettingKeys.INITIAL_PAUSE_STATE, mSessionManager.getIsPaused(habitId));
        }

        if (shouldPause) {
            if (!mSessionManager.getIsPaused(mHabit.getDatabaseId()))
                mSessionManager.setPauseState(habitId, true);
        }

        mConfirmationDialog = new ConfirmationDialog(this)
                .setIcon(iconRes)
                .setTitle(title)
                .setMessage(message)
                .setOnYesClickListener(onYesMethod)
                .setOnNoClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialogSettings.putBoolean(DialogSettingKeys.SHOW_DIALOG, false);
                        boolean initialPauseState = mDialogSettings.getBoolean(DialogSettingKeys.INITIAL_PAUSE_STATE);
                        if (mSessionManager.getIsPaused(mHabit.getDatabaseId()) != initialPauseState)
                            mSessionManager.setPauseState(mHabit.getDatabaseId(), initialPauseState);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mDialogSettings.putBoolean(DialogSettingKeys.SHOW_DIALOG, false);
                        boolean initialPauseState = mDialogSettings.getBoolean(DialogSettingKeys.INITIAL_PAUSE_STATE);
                        if (mSessionManager.getIsPaused(mHabit.getDatabaseId()) != initialPauseState)
                            mSessionManager.setPauseState(mHabit.getDatabaseId(), initialPauseState);
                    }
                })
                .setAccentColor(ContextCompat.getColor(this, R.color.contrastBackgroundAccent))
                .show();

        mDialogSettings.putBoolean(DialogSettingKeys.SHOW_DIALOG, true);
    }

    //endregion [ -- end -- ]

    //region Handle SessionManager events
    @Override
    public void onSessionPauseStateChanged(long habitId, boolean isPaused) {
        if (habitId == this.mHabit.getDatabaseId())
            updateSessionPlayButton(isPaused);
    }

    @Override
    public void beforeSessionEnded(long habitId, boolean wasCanceled) {
        if (habitId == this.mHabit.getDatabaseId() && wasCanceled)
            finish();
    }

    @Override
    public void afterSessionEnded(long habitId, boolean wasCanceled) {}

    @Override
    public void onSessionStarted(long habitId) {}
    //endregion -- end --

    //endregion [ ---------------- end ---------------- ]

    public static void startActivity(Activity activity, Habit habit) {
        Intent intent = new Intent(activity, SessionActivity.class);
        intent.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) habit);
        activity.startActivityForResult(intent, RequestCodes.SESSION_ACTIVITY);
    }

}