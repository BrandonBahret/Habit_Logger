package com.example.brandon.habitlogger.Preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.R;

public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    PreferenceChecker preferenceChecker;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferenceChecker = new PreferenceChecker(this);
        sessionManager = new SessionManager(this);

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null){
            toolbar.setDisplayHomeAsUpEnabled(true);
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferenceChecker.preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        preferenceChecker.preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        preferenceChecker.checkPreferences();
        if (key.equals("do_show_notifications") && !preferenceChecker.doShowNotifications()) {
            sessionManager.clearAllNotifications();
        }
        else if (key.equals("do_show_notifications") && preferenceChecker.doShowNotifications()) {
            if(preferenceChecker.doShowNotificationsAutomatically())
                sessionManager.createAllSessionNotifications();
        }
        else if (key.equals("do_automatically_show_notifications") && preferenceChecker.doShowNotificationsAutomatically()) {
            sessionManager.createAllSessionNotifications();
        }
    }
}
