package com.example.brandon.habitlogger.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.brandon.habitlogger.R;

import static java.lang.Integer.parseInt;

/**
 * Created by Brandon on 1/6/2017.
 * A class to check shared preferences.
 */

public class PreferenceChecker {
    public SharedPreferences preferences;
    private Context mContext;

    public static final int AS_CARDS = 0, AS_SECTIONS = 1, WITHOUT_CATEGORIES = 2;

    public PreferenceChecker(Context context){
        mContext = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //region // Main Screen Settings
    public int howToDisplayCategories(){
        String displayCategories = preferences.getString(
                mContext.getString(R.string.pref_show_categories), String.valueOf(AS_SECTIONS)
        );

        return parseInt(displayCategories);
    }

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
    //endregion

    //region // Notifications
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
    //endregion

    //region // Appearances

    public boolean isNightMode(){
        return preferences.getBoolean(
                mContext.getString(R.string.pref_is_night_mode), false
        );
    }

    public String stringGetDateFormat(){
        String[] dateOptions = mContext.getResources().getStringArray(R.array.date_format_options);
        int optionIndex = Integer.parseInt(
                preferences.getString(mContext.getString(R.string.pref_date_format), "0")
        );
        return dateOptions[optionIndex];
    }
    //endregion
}