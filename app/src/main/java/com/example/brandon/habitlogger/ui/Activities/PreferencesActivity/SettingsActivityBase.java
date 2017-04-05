package com.example.brandon.habitlogger.ui.Activities.PreferencesActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

/**
 * Created by Brandon on 4/5/2017.
 * Base class for all settings activities
 */

public abstract class SettingsActivityBase extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //region (Member attributes)
    private PreferenceChecker mPreferenceChecker;
    //endregion

    public static class SettingsFragment extends PreferenceFragment {

        public final static String RESOURCE_ID = "RESOURCE_ID";

        public static SettingsFragment newInstance(int resourceId){
            SettingsFragment fragment = new SettingsFragment();

            Bundle args = new Bundle();
            args.putInt(RESOURCE_ID, resourceId);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            int resId = getArguments().getInt(RESOURCE_ID, -1);

            addPreferencesFromResource(resId);
        }

    }

    protected abstract int getPreferenceResource();

    protected abstract void handleOnSharedPreferenceChanged(String key);

    //region [ ---- Methods to handle the activity lifecycle ---- ]

    //region entire lifetime (onCreate - onDestroy)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mPreferenceChecker = new PreferenceChecker(this);
        AppCompatDelegate.setDefaultNightMode(mPreferenceChecker.getThemeMode());

        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        SettingsFragment settingsFragment = SettingsFragment.newInstance(getPreferenceResource());
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();

    }
    //endregion -- end --

    //region foreground lifetime (onPause - onResume)
    @Override
    protected void onPause() {
        super.onPause();
        mPreferenceChecker.preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        int themeMode = mPreferenceChecker.getThemeMode();
        if (AppCompatDelegate.getDefaultNightMode() != themeMode)
            recreate();

        else
            mPreferenceChecker.preferences.registerOnSharedPreferenceChangeListener(this);

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
        handleOnSharedPreferenceChanged(key);
    }

    //endregion -- end --

    //region Getters {}
    protected PreferenceChecker getPreferenceChecker() {return mPreferenceChecker;}
    //endregion

}