package com.example.brandon.habitlogger.data.HabitSessions.DatabaseSchema;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Brandon on 12/4/2016.
 * Database helper/schema for active sessions.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class DatabaseSchema extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "habit_session_database";
    public static final int DATABASE_VERSION = 5;

    class SQL_TYPES {
        public static final String NOT_NULL = " NOT NULL ";
        public static final String PRI_INT_KEY = " INTEGER PRIMARY KEY ";
        public static final String INTEGER = " INTEGER ";
        public static final String TEXT = " TEXT ";
        public static final String UNIQUE = " UNIQUE ";
    }

    public DatabaseSchema(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SessionsTableSchema.getCreateTableStatement());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    public void resetDatabase(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.delete(SessionsTableSchema.TABLE_NAME, null, null);
    }
}