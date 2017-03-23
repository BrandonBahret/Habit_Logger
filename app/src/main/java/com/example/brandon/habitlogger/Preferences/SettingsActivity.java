package com.example.brandon.habitlogger.Preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;

import com.example.brandon.habitlogger.HabitSessions.SessionNotificationManager;
import com.example.brandon.habitlogger.R;

public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    PreferenceChecker preferenceChecker;
    SessionNotificationManager sessionNotificationManager;

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    //region // Activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferenceChecker = new PreferenceChecker(this);

        AppCompatDelegate.setDefaultNightMode(
                preferenceChecker.isNightMode() ? AppCompatDelegate.MODE_NIGHT_YES :
                        AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        String foo = getIntent().getDataString();
        if (foo != null)
            Log.d("foobar", foo);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferenceChecker.preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sessionNotificationManager = new SessionNotificationManager(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        preferenceChecker.preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    //endregion // Activity lifecycle methods

    //region // Methods to handle events
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

            if (preferenceChecker.doShowNotifications())
                sessionNotificationManager.launchNotificationsForAllActiveSessions();

            else
                sessionNotificationManager.cancelAllNotifications();
        }

        else if (key.equals(getString(R.string.pref_do_show_notifications_auto)) && preferenceChecker.doShowNotificationsAutomatically()) {
            sessionNotificationManager.launchNotificationsForAllActiveSessions();
        }

        else if (key.equals(getString(R.string.pref_is_night_mode))) {
            recreate();
        }
    }
    //endregion // Methods to handle events

}
