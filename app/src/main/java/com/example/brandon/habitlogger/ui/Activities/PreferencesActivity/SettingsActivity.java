package com.example.brandon.habitlogger.ui.Activities.PreferencesActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.common.RequestCodes;
import com.example.brandon.habitlogger.data.HabitSessions.SessionNotificationManager;
import com.example.brandon.habitlogger.ui.Activities.AboutActivity;

public class SettingsActivity extends SettingsActivityBase {

    //region (Member attributes)
    private SessionNotificationManager mSessionNotificationManager;
    private boolean hasSetupPreferences = false;
    //endregion

    //region Methods responsible for handling the activity lifecycle

    @Override
    protected int getPreferenceResource() {
        return R.xml.preferences_experimental;
    }

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionNotificationManager = new SessionNotificationManager(this);
    }
    //endregion -- end --

    //region visible lifetime (onStart - onStop)
    @Override
    protected void onStart() {
        super.onStart();

        if(!hasSetupPreferences) {
            hasSetupPreferences = true;
            setUpDateFormatPreference();
        }
    }
    //endregion -- end --

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //endregion -- end --

    //region Methods responsible for updating the Ui
    private void setUpDateFormatPreference() {
        ListPreference dateFormat = (ListPreference) findPreference(getString(R.string.pref_date_format));
        CharSequence[] entries = dateFormat.getEntries();
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < entries.length; i++) {
            String format = String.valueOf(entries[i]);
            entries[i] = MyTimeUtils.stringifyTimestamp(currentTime, format);
        }
        dateFormat.setEntries(entries);
        dateFormat.setSummary(dateFormat.getEntry());
    }
    //endregion -- end --

    //region Methods responsible for handling events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.about:
                AboutActivity.startActivity(SettingsActivity.this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void handleOnSharedPreferenceChanged(String key) {
        if (key.equals(getString(R.string.pref_date_format))) {
            ListPreference dateFormat = (ListPreference) findPreference(getString(R.string.pref_date_format));
            dateFormat.setSummary(dateFormat.getEntry());
        }

        if (key.equals(getString(R.string.pref_do_show_notifications))) {

            if (getPreferenceChecker().doShowNotifications())
                mSessionNotificationManager.launchNotificationsForAllActiveSessions();

            else mSessionNotificationManager.cancelAllNotifications();
        }
    }
    //endregion -- end --

    public static void startActivity(AppCompatActivity activity) {
        Intent startSettings = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(startSettings, RequestCodes.SETTINGS_ACTIVITY);
    }

}
