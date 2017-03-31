package com.example.brandon.habitlogger.HabitSessions.DatabaseSchema;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.DatabaseSchema.DatabaseSchema.SQL_TYPES;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.common.MyDatabaseUtils;

/**
 * Created by Brandon on 2/18/2017.
 * Schema defining the sessions table.
 */

public class SessionsTableSchema {
    public static final String TABLE_NAME = "SESSIONS_TABLE";
    public static final String HABIT_NAME = "HABIT_NAME";
    public static final String HABIT_CATEGORY_NAME = "HABIT_CATEGORY";
    public static final String HABIT_COLOR = "HABIT_COLOR";
    public static final String HABIT_ID = "HABIT_ID";
    public static final String DURATION = "DURATION";
    public static final String STARTING_TIME = "STARTING_TIME";
    public static final String LAST_TIME_PAUSED = "LAST_TIME_PAUSED";
    public static final String TOTAL_PAUSE_TIME = "TOTAL_PAUSE_TIME";
    public static final String IS_PAUSED = "IS_PAUSED";
    public static final String NOTE = "NOTE";

    public static String getCreateTableStatement() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                HABIT_ID + SQL_TYPES.INTEGER + SQL_TYPES.UNIQUE + ", " +
                HABIT_NAME + SQL_TYPES.TEXT + ", " +
                HABIT_CATEGORY_NAME + SQL_TYPES.TEXT + ", " +
                HABIT_COLOR + SQL_TYPES.TEXT + ", " +
                DURATION + SQL_TYPES.INTEGER + ", " +
                STARTING_TIME + SQL_TYPES.INTEGER + ", " +
                LAST_TIME_PAUSED + SQL_TYPES.INTEGER + ", " +
                TOTAL_PAUSE_TIME + SQL_TYPES.INTEGER + ", " +
                IS_PAUSED + SQL_TYPES.INTEGER + ", " +
                NOTE + SQL_TYPES.TEXT +
                ");";
    }

    public static String getDropTableStatement() {
        // DROP TABLE IF EXISTS TABLE_NAME
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String getSearchByNameStatement(String query) {
        return "SELECT * FROM " +
                SessionsTableSchema.TABLE_NAME + " WHERE " +
                SessionsTableSchema.HABIT_NAME + " LIKE  '%" + query + "%' OR " +
                SessionsTableSchema.HABIT_CATEGORY_NAME + " LIKE  '%" + query + "%'";
    }

    public static MyDatabaseUtils.GetRecordFromContentValues<? extends SessionEntry> IGetFromContentValues =
            new MyDatabaseUtils.GetRecordFromContentValues<SessionEntry>() {
                @Override
                public SessionEntry getRecordFromContentValues(ContentValues contentValues) {
                    return getObjectFromContentValues(contentValues);
                }
            };

    public static SessionEntry getObjectFromContentValues(@NonNull ContentValues contentValues) {
        long lastPausedTime = contentValues.getAsLong(LAST_TIME_PAUSED);
        long totalPausedTime = contentValues.getAsLong(TOTAL_PAUSE_TIME);
        boolean isPaused = contentValues.getAsInteger(IS_PAUSED) == 1;
        long startingTime = contentValues.getAsLong(STARTING_TIME);
        String note = contentValues.getAsString(NOTE);
        long habitId = contentValues.getAsLong(HABIT_ID);
        String habitName = contentValues.getAsString(HABIT_NAME);
        String categoryName = contentValues.getAsString(HABIT_CATEGORY_NAME);
        String habitColor = contentValues.getAsString(HABIT_COLOR);

        Habit habit = new Habit(habitName, new HabitCategory(habitColor, categoryName))
                .setDatabaseId(habitId);

        SessionEntry entry = new SessionEntry(startingTime, -1, note)
                .setLastTimePaused(lastPausedTime)
                .setTotalPauseTime(totalPausedTime)
                .setIsPaused(isPaused)
                .setHabit(habit);

        // Set duration, this is done here since we may need to calculate the duration.
        long duration = SessionManager.calculateElapsedTimeForEntry(entry);
        entry.setDuration(duration);

        return entry;
    }

    public static ContentValues getContentValuesFromObject(SessionEntry entry) {
        ContentValues values = new ContentValues(10);
        values.put(HABIT_ID, entry.getHabitId());
        values.put(HABIT_NAME, entry.getHabit().getName());
        values.put(HABIT_CATEGORY_NAME, entry.getCategoryName());
        values.put(HABIT_COLOR, entry.getHabit().getCategory().getColor());
        values.put(DURATION, entry.getDuration());
        values.put(STARTING_TIME, entry.getStartingTime());
        values.put(LAST_TIME_PAUSED, entry.getLastTimePaused());
        values.put(TOTAL_PAUSE_TIME, entry.getTotalPauseTime());
        values.put(IS_PAUSED, entry.getIsPaused() ? 1L : 0L);
        values.put(NOTE, entry.getNote());

        return values;
    }
}
