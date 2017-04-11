package com.example.brandon.habitlogger.ui.Activities.PreferencesActivity;

import android.preference.ListPreference;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyTimeUtils;

public class UserInterfaceSettingsActivity extends SettingsActivityBase {

    //region Methods responsible for initializing preferences
    @Override
    protected void onStart() {
        super.onStart();
        setUpDateFormatPreference();
    }

    private void setUpDateFormatPreference() {ListPreference dateFormat = (ListPreference) findPreference(getString(R.string.pref_date_format));
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

    @Override
    protected int getPreferenceResource() {
        return R.xml.preferences_user_interface;
    }

    @Override
    protected void handleOnSharedPreferenceChanged(String key) {

        if(key.equals(getString(R.string.pref_date_format))){
            ListPreference dateFormat = (ListPreference) findPreference(getString(R.string.pref_date_format));
            dateFormat.setSummary(dateFormat.getEntry());
        }

    }

}
