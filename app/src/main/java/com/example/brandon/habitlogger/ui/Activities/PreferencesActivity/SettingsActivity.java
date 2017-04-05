package com.example.brandon.habitlogger.ui.Activities.PreferencesActivity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.RequestCodes;

public class SettingsActivity extends SettingsActivityBase {

    @Override
    protected int getPreferenceResource() {
        return R.xml.preferences;
    }

    @Override
    protected void handleOnSharedPreferenceChanged(String key) {}

    public static void startActivity(AppCompatActivity activity) {
        Intent startSettings = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(startSettings, RequestCodes.SETTINGS_ACTIVITY);
    }

}
