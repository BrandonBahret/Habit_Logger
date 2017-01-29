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
    private Context context;

    public static final int AS_CARDS = 0, AS_SECTIONS = 1, WITHOUT_CATEGORIES = 2;
    public static final int LIGHT_THEME = 0, DARK_THEME = 1;


    public PreferenceChecker(Context context){
        this.context = context;
        checkPreferences();
    }

    public void checkPreferences(){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Main Screen Settings
    public int howToDisplayCategories(){
        String displayCategories = preferences.getString("category_preference", "1");
        return parseInt(displayCategories);
    }

    public boolean doShowCurrentSessions(){
        return preferences.getBoolean("display_current_sessions_card", true);
    }

    public boolean doAlwaysShowCurrentSessions(){
        return preferences.getBoolean("display_current_sessions_at_zero", true);
    }

    // Notifications
    public boolean doShowNotifications(){
        return preferences.getBoolean("do_show_notifications", true);
    }

    public boolean doShowNotificationsAutomatically(){
        return preferences.getBoolean("do_automatically_show_notifications", true);
    }

    public boolean doShowTicker(){
        return preferences.getBoolean("show_ticker", false);
    }

    // Appearances
    public int getTheme(){
        String themeIndex = preferences.getString("theme", "1");
        return parseInt(themeIndex);
    }

    public String stringGetDateFormat(){
        String[] dateOptions = context.getResources().getStringArray(R.array.date_format_options);
        int optionIndex = Integer.parseInt(preferences.getString("date_format", "0"));
        return dateOptions[optionIndex];
    }

}
