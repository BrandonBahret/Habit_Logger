package com.example.brandon.habitlogger.HabitSessions.DatabaseSchema;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;

/**
 * Created by Brandon on 2/18/2017.
 *
 */

public class SessionsTableSchema {
    public static final String TABLE_NAME = "SESSIONS_TABLE";
    public static final String HABIT_NAME = "HABIT_NAME";
    public static final String HABIT_CATEGORY = "HABIT_CATEGORY";
    public static final String HABIT_ID = "HABIT_ID";
    public static final String DURATION = "Duration";
    public static final String STARTING_TIME = "STARTING_TIME";
    public static final String LAST_TIME_PAUSED = "LAST_TIME_PAUSED";
    public static final String TOTAL_PAUSE_TIME = "TOTAL_PAUSE_TIME";
    public static final String IS_PAUSED = "IS_PAUSED";
    public static final String NOTE = "NOTE";

    public static String getCreateTableStatement() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                HABIT_ID + " INTEGER UNIQUE, " +
                HABIT_NAME + " TEXT, " +
                HABIT_CATEGORY + " TEXT, " +
                DURATION + " INTEGER, " +
                STARTING_TIME + " INTEGER, " +
                LAST_TIME_PAUSED + " INTEGER," +
                TOTAL_PAUSE_TIME + " INTEGER," +
                IS_PAUSED + " INTEGER," +
                NOTE + " TEXT" +
                ");";
    }

    public static String getDropTableStatement() {
        // DROP TABLE IF EXISTS TABLE_NAME
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String getInsertStatement() {
        return "INSERT INTO " + TABLE_NAME + " ( " +
                HABIT_ID + ", " +
                HABIT_NAME + ", " +
                HABIT_CATEGORY + ", " +
                DURATION + ", " +
                STARTING_TIME + ", " +
                LAST_TIME_PAUSED + ", " +
                TOTAL_PAUSE_TIME + ", " +
                IS_PAUSED + ", " +
                NOTE +
                ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    public static String getSearchRecordsByHabitOrCategoryNameStatement(String query) {
        return "SELECT " + SessionsTableSchema.HABIT_ID + " FROM " +
                SessionsTableSchema.TABLE_NAME + " WHERE " +
                SessionsTableSchema.HABIT_NAME + " LIKE  '%" + query + "%' OR " +
                SessionsTableSchema.HABIT_CATEGORY + " LIKE  '%" + query + "%'";
    }


    public static SessionEntry objectFromContentValues(ContentValues contentValues) {
        long lastPausedTime = contentValues.getAsLong(LAST_TIME_PAUSED);
        long totalPausedTime = contentValues.getAsLong(TOTAL_PAUSE_TIME);
        boolean isPaused = contentValues.getAsInteger(IS_PAUSED) == 1;
        long startingTime = contentValues.getAsLong(STARTING_TIME);
        String note = contentValues.getAsString(NOTE);
        long habitId = contentValues.getAsLong(HABIT_ID);
        String habitName = contentValues.getAsString(HABIT_NAME);
        String categoryName = contentValues.getAsString(HABIT_CATEGORY);

        SessionEntry entry = new SessionEntry(startingTime, -1, note);
        entry.setLastTimePaused(lastPausedTime);
        entry.setTotalPauseTime(totalPausedTime);
        entry.setIsPaused(isPaused);
        entry.setHabitId(habitId);
        entry.setCategoryName(categoryName);
        entry.setName(habitName);

        // Set duration, this is done here since we may need to calculate the duration.
        long duration = isPaused ? contentValues.getAsLong(DURATION) : SessionManager.calculateElapsedTimeForEntry(entry);
        entry.setDuration(duration);

        return entry;
    }

    public static void bindObjectToStatement(SQLiteStatement statement, SessionEntry entry) {

        statement.bindLong(1, entry.getHabitId());            // HABIT_ID
        statement.bindString(2, entry.getName());             // HABIT_NAME
        statement.bindString(3, entry.getCategoryName());     // HABIT_CATEGORY
        statement.bindLong(4, entry.getDuration());           // DURATION
        statement.bindLong(5, entry.getStartTime());          // STARTING_TIME
        statement.bindLong(6, entry.getLastTimePaused());     // LAST_TIME_PAUSED
        statement.bindLong(7, entry.getTotalPauseTime());     // TOTAL_PAUSE_TIME
        statement.bindLong(8, entry.getIsPaused() ? 1L : 0L); // IS_PAUSED
        statement.bindString(9, entry.getNote());             // NOTE
    }
}
