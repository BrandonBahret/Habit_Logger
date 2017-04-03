package com.example.brandon.habitlogger.Preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import com.example.brandon.habitlogger.HabitSessions.SessionNotificationManager;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.RequestCodes;

public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //region (Member attributes)
    private PreferenceChecker mPreferenceChecker;
    private SessionNotificationManager mSessionNotificationManager;
    //endregion

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    //region [ ---- Methods to handle the activity lifecycle ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferenceChecker = new PreferenceChecker(this);

        AppCompatDelegate.setDefaultNightMode(
                mPreferenceChecker.isNightMode() ? AppCompatDelegate.MODE_NIGHT_YES :
                        AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }
    //endregion -- end --

    //region foreground lifetime (onPause - onResume)
    @Override
    public void onResume() {
        super.onResume();
        mPreferenceChecker.preferences.registerOnSharedPreferenceChangeListener(this);
    }
    //endregion -- end --

    //region visible lifetime (onStart - onStop)
    @Override
    protected void onStart() {
        super.onStart();
        mSessionNotificationManager = new SessionNotificationManager(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPreferenceChecker.preferences.unregisterOnSharedPreferenceChangeListener(this);
    }
    //endregion -- end --

    //endregion [ ---------------- end ---------------- ]

    //region Methods to handle events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_do_show_notifications))) {

            if (mPreferenceChecker.doShowNotifications())
                mSessionNotificationManager.launchNotificationsForAllActiveSessions();

            else
                mSessionNotificationManager.cancelAllNotifications();
        }

        else if (key.equals(getString(R.string.pref_do_show_notifications_auto)) && mPreferenceChecker.doShowNotificationsAutomatically())
            mSessionNotificationManager.launchNotificationsForAllActiveSessions();

        else if (key.equals(getString(R.string.pref_is_night_mode)))
            recreate();
    }
    //endregion -- end --

    public static void startActivity(AppCompatActivity activity) {
        Intent startSettings = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(startSettings, RequestCodes.SETTINGS_ACTIVITY);
    }

}
