package com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema;

import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.DatabaseSchema.SQL_TYPES;

/**
 * Created by Brandon on 2/17/2017.
 * Schema defining habits table.
 */

public class HabitsTableSchema {
    public static final String TABLE_NAME = "HABITS_TABLE";
    public static final String HABIT_ID = "ID";
    public static final String HABIT_IS_ARCHIVED = "ARCHIVED";
    public static final String HABIT_NAME = "NAME";
    public static final String HABIT_DESCRIPTION = "DESCRIPTION";
    public static final String HABIT_CATEGORY = "CATEGORY";
    public static final String HABIT_ICON_RES_ID = "ICON_RES_ID";

    public static String getCreateTableStatement(){
        // CREATE TABLE HABITS_TABLE (ID INTEGER PRIMARY KEY, ARCHIVED INTEGER, NAME TEXT NOT NULL, DESCRIPTION TEXT NOT NULL,
        // CATEGORY INTEGER NOT NULL, ICON_RES_ID TEXT NOT NULL)
        return "CREATE TABLE " + TABLE_NAME + " (" +
                HABIT_ID + SQL_TYPES.PRI_INT_KEY + ", " +
                HABIT_IS_ARCHIVED + SQL_TYPES.INTEGER + ", " +
                HABIT_NAME + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL + ", " +
                HABIT_DESCRIPTION + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL + ", " +
                HABIT_CATEGORY + SQL_TYPES.INTEGER + SQL_TYPES.NOT_NULL + ", " +
                HABIT_ICON_RES_ID + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL +
                ");";
    }

    public static String getDropTableStatement(){
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String getInsertStatement() {
        return "INSERT INTO " + HabitsTableSchema.TABLE_NAME +
                " (" + HabitsTableSchema.HABIT_IS_ARCHIVED +
                ", " + HabitsTableSchema.HABIT_NAME +
                ", " + HabitsTableSchema.HABIT_DESCRIPTION +
                ", " + HabitsTableSchema.HABIT_ICON_RES_ID +
                ", " + HabitsTableSchema.HABIT_CATEGORY + ") VALUES(?, ?, ?, ?, ?)";
    }
}
