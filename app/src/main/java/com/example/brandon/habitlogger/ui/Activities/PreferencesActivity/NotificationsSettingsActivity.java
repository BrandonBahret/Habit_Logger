package com.example.brandon.habitlogger.ui.Activities.PreferencesActivity;

import android.os.Bundle;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.HabitSessions.SessionNotificationManager;

public class NotificationsSettingsActivity extends SettingsActivityBase {

    //region (Member attributes)
    private SessionNotificationManager mSessionNotificationManager;
    //endregion

    //region Methods responsible for handling the activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionNotificationManager = new SessionNotificationManager(this);
    }
    //endregion

    @Override
    protected int getPreferenceResource() {
        return R.xml.preferences_notifications;
    }

    @Override
    protected void handleOnSharedPreferenceChanged(String key) {

        if (key.equals(getString(R.string.pref_do_show_notifications))) {

            if (getPreferenceChecker().doShowNotifications())
                mSessionNotificationManager.launchNotificationsForAllActiveSessions();

            else
                mSessionNotificationManager.cancelAllNotifications();
        }

        else {
            boolean shouldLaunchAllNotifications =
                    key.equals(getString(R.string.pref_do_show_notifications_auto)) &&
                            getPreferenceChecker().doShowNotificationsAutomatically();

            if (shouldLaunchAllNotifications)
                mSessionNotificationManager.launchNotificationsForAllActiveSessions();
        }
    }

}
