package com.example.brandon.habitlogger.ui.Activities.PreferencesActivity;

import com.example.brandon.habitlogger.R;

public class UserInterfaceSettingsActivity extends SettingsActivityBase {

    @Override
    protected int getPreferenceResource() {
        return R.xml.preferences_user_interface;
    }

    @Override
    protected void handleOnSharedPreferenceChanged(String key) {
        if (key.equals(getString(R.string.pref_is_night_mode)))
            recreate();
    }

}
