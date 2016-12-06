package com.example.brandon.habitlogger.SessionManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Brandon on 12/4/2016.
 */

class DatabaseHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "habit_session_database";
    protected static final int DATABASE_VERSION = 1;

    public SQLiteDatabase writableDatabase;
    public SQLiteDatabase readableDatabase;

    protected static final String SESSIONS_TABLE = "SESSIONS_TABLE";
    protected static final String HABIT_ID          = "HABIT_ID";
    protected static final String STARTING_TIME     = "STARTING_TIME";
    protected static final String LAST_TIME_PAUSED  = "LAST_TIME_PAUSED";
    protected static final String TOTAL_PAUSE_TIME  = "TOTAL_PAUSE_TIME";
    protected static final String IS_PAUSED         = "IS_PAUSED";
    protected static final String NOTE              = "NOTE";

    private static final String CREATE_SESSIONS_TABLE =
            "CREATE TABLE " + SESSIONS_TABLE + " (" +
                    HABIT_ID         + " INTEGER UNIQUE, " +
                    STARTING_TIME    + " INTEGER, " +
                    LAST_TIME_PAUSED + " INTEGER,"    +
                    TOTAL_PAUSE_TIME + " INTEGER,"    +
                    IS_PAUSED        + " INTEGER," +
                    NOTE             + " TEXT" +
                    ");";

    // DROP TABLE IF EXISTS SESSIONS_TABLE
    private static final String DROP_HABITS_TABLE = "DROP TABLE IF EXISTS " + SESSIONS_TABLE;

    DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        writableDatabase = getWritableDatabase();
        readableDatabase = getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_SESSIONS_TABLE);
    }

    public void resetDatabase(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.delete(SESSIONS_TABLE, null, null);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param valueColumn The column in the database to be set.
     * @param value The value to be set at "valueColumn"
     * @return the number of rows affected
     */
    public <Value> int setAttribute(long habitId, String valueColumn, Value value){
        ContentValues values = new ContentValues(1);

        if(value instanceof String){
            values.put(valueColumn, (String)value);
        } else if(value instanceof Long){
            values.put(valueColumn, (Long)value);
        }

        return writableDatabase.update(DatabaseHelper.SESSIONS_TABLE, values,
                DatabaseHelper.HABIT_ID + " =?", new String[]{String.valueOf(habitId)});
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return The result of the query as a cursor.
     */
    public Cursor getAttribute(long habitId, String column){
        Cursor c =  readableDatabase.query(
                DatabaseHelper.SESSIONS_TABLE,
                new String[]{column},
                DatabaseHelper.HABIT_ID + " =?",
                new String[]{String.valueOf(habitId)},
                null, null, null
        );

        c.moveToFirst();

        return c;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        resetDatabase(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }
}