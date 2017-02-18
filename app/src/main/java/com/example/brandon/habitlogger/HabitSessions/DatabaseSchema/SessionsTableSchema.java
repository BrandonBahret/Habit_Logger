package com.example.brandon.habitlogger.HabitSessions.DatabaseSchema;

/**
 * Created by Brandon on 2/18/2017.
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
}
