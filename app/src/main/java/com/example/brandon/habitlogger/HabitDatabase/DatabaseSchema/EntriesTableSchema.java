package com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.DatabaseSchema.SQL_TYPES;
import com.example.brandon.habitlogger.common.MyDatabaseUtils;

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

    public static MyDatabaseUtils.GetRecordFromContentValues<? extends SessionEntry> IGetFromContentValues =
            new MyDatabaseUtils.GetRecordFromContentValues<SessionEntry>() {
                @Override
                public SessionEntry getRecordFromContentValues(ContentValues contentValues) {
                    return getObjectFromContentValues(contentValues);
                }
            };

    public static SessionEntry getObjectFromContentValues(@NonNull ContentValues contentValues) {
        Long startTime = contentValues.getAsLong(ENTRY_START_TIME);
        Long duration = contentValues.getAsLong(ENTRY_DURATION);
        String note = contentValues.getAsString(ENTRY_NOTE);
        long habitId = contentValues.getAsLong(ENTRY_HABIT_ID);
        long databaseId = contentValues.getAsLong(ENTRY_ID);

        return new SessionEntry(startTime, duration, note)
                .setDatabaseId(databaseId)
                .setHabitId(habitId);
    }

    public static ContentValues getContentValuesFromObject(SessionEntry entry, long habitId) {
        ContentValues values = new ContentValues(4);
        values.put(ENTRY_START_TIME, entry.getStartingTime());
        values.put(ENTRY_DURATION, entry.getDuration());
        values.put(ENTRY_NOTE, entry.getNote());
        values.put(ENTRY_HABIT_ID, habitId);

        return values;
    }
}
