package com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema;

import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.DatabaseSchema.SQL_TYPES;

/**
 * Created by Brandon on 2/17/2017.
 * Schema defining entries table
 */

public class EntriesTableSchema {
    public static final String TABLE_NAME = "ENTRIES_TABLE";
    public static final String ENTRY_ID = "ID";
    public static final String ENTRY_HABIT_ID = "HABIT_ID";
    public static final String ENTRY_START_TIME = "START_TIME";
    public static final String ENTRY_DURATION = "DURATION";
    public static final String ENTRY_NOTE = "NOTE";

    public static String getCreateTableStatement(){
        // CREATE TABLE ENTRIES_TABLE (ID INTEGER PRIMARY KEY, HABIT_ID INTEGER NOT NULL,
        // START_TIME INTEGER NOT NULL, DURATION INTEGER NOT NULL, NOTE TEXT NOT NULL)
        return "CREATE TABLE " + TABLE_NAME + " (" +
                ENTRY_ID + SQL_TYPES.PRI_INT_KEY + ", " +
                ENTRY_HABIT_ID + SQL_TYPES.INTEGER + SQL_TYPES.NOT_NULL + ", " +
                ENTRY_START_TIME + SQL_TYPES.INTEGER + SQL_TYPES.NOT_NULL + ", " +
                ENTRY_DURATION + SQL_TYPES.INTEGER + SQL_TYPES.NOT_NULL + ", " +
                ENTRY_NOTE + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL +
                ");";
    }

    public static String getDropTableStatement(){
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String getInsertStatement() {
        return "INSERT INTO ENTRIES_TABLE (" +
                ENTRY_HABIT_ID + ", " +
                ENTRY_START_TIME + ", " +
                ENTRY_DURATION + ", " +
                ENTRY_NOTE +
                ") VALUES(?, ?, ?, ?);";
    }
}
