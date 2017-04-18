package com.example.brandon.habitlogger.ui.Activities.PreferencesActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import com.example.brandon.habitlogger.R;

import static java.lang.Integer.parseInt;

/**
 * Created by Brandon on 1/6/2017.
 * A class to check shared preferences.
 */

public class PreferenceChecker {

    //region (Member attributes)
    public static final int AS_CARDS = -1, AS_SECTIONS = 0, WITHOUT_CATEGORIES = 1;

    public SharedPreferences preferences;
    private Context mContext;
    //endregion

    public PreferenceChecker(Context context){
        mContext = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //region Methods related to main screen settings
    public int howToDisplayCategories(){
        String displayCategories = preferences.getString(
                mContext.getString(R.string.pref_show_categories), String.valueOf(AS_SECTIONS)
        );

        return parseInt(displayCategories);
    }

    public boolean makeCategoryHeadersSticky(){
        boolean wouldBeSticky = preferences.getBoolean(
                mContext.getString(R.string.pref_category_sticky), false
        );

        return howToDisplayCategories() == AS_SECTIONS && wouldBeSticky;
    }

    public boolean hideFabOnScroll() {
        return preferences.getBoolean(
                mContext.getString(R.string.pref_hide_fab_on_scroll), true
        );
    }
    //endregion -- end --

    //region Methods related to active sessions activity
    public boolean doShowCurrentSessions(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_display_current_sessions_card), true
        );
    }

    public boolean doAlwaysShowCurrentSessions(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_display_current_sessions_card_at_zero), true
        );
    }

    /**
     * @param sessionCount The number of active sessions
     * @return True if the current sessions card should be shown under these conditions.
     */
    public boolean shouldShowCurrentSessions(int sessionCount){
        return doShowCurrentSessions() && (sessionCount > 0 || doAlwaysShowCurrentSessions());
    }

    public boolean doHideCurrentSessionCard() {
        return preferences.getBoolean(
                mContext.getString(R.string.pref_hide_current_sessions_card_on_scroll), false
        );
    }

    public boolean allowActiveSessionsActivity(boolean hasActiveSessions) {
        return hasActiveSessions || preferences.getBoolean(
                mContext.getString(R.string.pref_allow_upon_no_active_sessions), false
        );
    }
    //endregion

    //region Methods related to entries settings
    public boolean makeDateHeadersSticky(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_date_header_sticky), true
        );
    }
    //endregion

    //region Methods related to session activity settings
    public boolean doAskBeforeCancel(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_ask_cancel), true
        );
    }

    public boolean doAskBeforeFinish(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_ask_finish), true
        );
    }
    //endregion -- end --

    //region Methods related to notification settings
    public boolean doShowNotifications(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_do_show_notifications), true
        );
    }

    public boolean doShowNotificationsAutomatically(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_do_show_notifications_auto), true
        );
    }
    //endregion -- end --

    //region Methods related to appearance settings
    public boolean isNightMode(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_is_night_mode), false
        );
    }

    public int getThemeMode(){
        return isNightMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
    }

    public void toggleNightMode() {
        boolean themeMode = !isNightMode();
        preferences.edit()
                .putBoolean(mContext.getString(R.string.pref_is_night_mode), themeMode)
                .apply();
    }

    public String stringGetDateFormat(){
        String[] dateOptions = mContext.getResources().getStringArray(R.array.date_format_options);
        int optionIndex = Integer.parseInt(
                preferences.getString(mContext.getString(R.string.pref_date_format), "0")
        );
        return dateOptions[optionIndex];
    }
    //endregion -- end --

}