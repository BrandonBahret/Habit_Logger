package com.example.brandon.habitlogger.ui.Activities.SessionActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.common.ResultCodes;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.databinding.ActivitySessionBinding;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity.Fragments.TimerFragment;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;

import java.io.Serializable;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionActivity extends AppCompatActivity implements
        SessionManager.SessionChangeCallback, TimerFragment.ITimerFragment {

    public static String RESULT_NEW_ENTRY = "RESULT_NEW_ENTRY";

    public static class BundleKeys {
        public static final String SERIALIZED_HABIT = "SERIALIZED_HABIT_KEY";
    }

    //region (Member attributes)
    private TimerFragment mTimerFragment;

    private SessionManager mSessionManager;
    private Habit mHabit;

    ActivitySessionBinding ui;
    //endregion

    //region [ ---- Methods responsible for handling the lifecycle of the activity ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetDialogListeners();

        ui = DataBindingUtil.setContentView(this, R.layout.activity_session);
        mHabit = (Habit) getIntent().getSerializableExtra(BundleKeys.SERIALIZED_HABIT);
        mTimerFragment = (TimerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_timer);
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

        mSessionManager.addSessionChangedCallback(this);

        mTimerFragment.callOnUpdateTimer();

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

        // Load note from database
        String note = mSessionManager.getNote(mHabit.getDatabaseId());
        if (!note.isEmpty()) ui.sessionNote.setText(note);

        applyHabitColorToTheme();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save note in database
        mSessionManager.setNote(mHabit.getDatabaseId(), ui.sessionNote.getText().toString());
    }
    //endregion -- end --

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Methods responsible for updating the UI ---- ]

    //region Methods responsible for updating the timer
    @Override
    public Long getSessionDuration(boolean required) {
        boolean shouldUpdate = required || (mSessionManager.getIsSessionActive(mHabit.getDatabaseId()) &&
                !mSessionManager.getIsPaused(mHabit.getDatabaseId()));

        if (shouldUpdate) {
            SessionEntry entry = mSessionManager.getSession(mHabit.getDatabaseId());
            return entry.getDuration();
        }

        else return null;
    }

    @Override
    public boolean getSessionState() {
        return mSessionManager.getIsPaused(mHabit.getDatabaseId());
    }
    //endregion -- end --

    public void applyHabitColorToTheme() {
        ThemeColorPalette palette = new ThemeColorPalette(mHabit.getColor());

        getWindow().setStatusBarColor(palette.getColorPrimaryDark());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(palette.getColorPrimary()));
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

    private void resetDialogListeners() {
        ConfirmationDialog dialog;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if ((dialog = (ConfirmationDialog) fragmentManager.findFragmentByTag("cancel-session")) != null)
            setDialogListener(dialog);

        else if ((dialog = (ConfirmationDialog) fragmentManager.findFragmentByTag("finish-session")) != null)
            setDialogListener(dialog);
    }

    private void setDialogListener(ConfirmationDialog dialog) {
        switch (dialog.getTag()) {
            case "cancel-session":
                dialog.setOnYesClickListener(onYesCancelSessionClicked);
                break;
            case "finish-session":
                dialog.setOnYesClickListener(onYesFinishSessionClickListener);
                break;
        }
    }

    //region Code responsible for handling finish session requests
    DialogInterface.OnClickListener onYesFinishSessionClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Get the new SessionEntry
            String note = ui.sessionNote.getText().toString();
            SessionEntry entry = mSessionManager.finishSession(mHabit.getDatabaseId());
            entry.setNote(note);

            // Add the new SessionEntry to the database
            HabitDatabase database = new HabitDatabase(SessionActivity.this);
            database.addEntry(mHabit.getDatabaseId(), entry);

            Intent data = new Intent().putExtra(SessionActivity.RESULT_NEW_ENTRY, (Serializable) entry);
            setResult(ResultCodes.SESSION_FINISH, data);

            finish();
        }
    };

    private void onFinishSessionClicked() {
        new ConfirmationDialog()
                .setTitle("Finish session")
                .setMessage("Finish this session?")
                .setIcon(R.drawable.ic_check_24dp)
                .setOnYesClickListener(onYesFinishSessionClickListener)
                .show(getSupportFragmentManager(), "finish-session");
    }
    //endregion -- end --

    //region Code responsible for handling cancel session requests
    DialogInterface.OnClickListener onYesCancelSessionClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mSessionManager.cancelSession(mHabit.getDatabaseId());
        }
    };

    private void onCancelSessionClicked() {

        new ConfirmationDialog()
                .setTitle("Cancel session")
                .setMessage("Cancel this session?")
                .setIcon(R.drawable.ic_close_24dp)
                .setOnYesClickListener(onYesCancelSessionClicked)
                .show(getSupportFragmentManager(), "cancel-session");

    }
    //endregion -- end --

    //endregion

    @Override
    public void onSessionToggleClick() {
        boolean isPaused = !mSessionManager.getIsPaused(mHabit.getDatabaseId());
        mSessionManager.setPauseState(mHabit.getDatabaseId(), isPaused);
    }

    //endregion [ -- end -- ]

    //region Handle SessionManager events
    @Override
    public void onSessionPauseStateChanged(long habitId, boolean isPaused) {
        if (habitId == this.mHabit.getDatabaseId())
            mTimerFragment.updateSessionPlayButton(isPaused);
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