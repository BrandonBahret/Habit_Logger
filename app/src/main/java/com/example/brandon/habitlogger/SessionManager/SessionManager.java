package com.example.brandon.habitlogger.SessionManager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

/**
 * Created by Brandon on 12/4/2016.
 */

public class SessionManager {
    Context context;
    DatabaseHelper dbHelper;
    private SQLiteDatabase writableDatabase;
    private SQLiteDatabase readableDatabase;

    private SQLiteStatement insertSessionStatement;

    public SessionManager(Context context){
        this.context = context;
        dbHelper = new DatabaseHelper(context);

        writableDatabase = dbHelper.getWritableDatabase();
        readableDatabase = dbHelper.getReadableDatabase();

        insertSessionStatement = getInsertSessionStatement();
    }

    public SQLiteStatement getInsertSessionStatement(){
        String sql = "INSERT INTO "+ DatabaseHelper.SESSIONS_TABLE +
                " ("+
                DatabaseHelper.HABIT_ID +
                ", "+ DatabaseHelper.STARTING_TIME+
                ", "+ DatabaseHelper.LAST_TIME_PAUSED+
                ", "+ DatabaseHelper.TOTAL_PAUSE_TIME+
                ", "+ DatabaseHelper.IS_PAUSED+
                ", "+ DatabaseHelper.NOTE+
                ") VALUES(?, ?, ?, ?, ?, ?)";

        return writableDatabase.compileStatement(sql);
    }

    public long getCurrentTime(){
        return System.currentTimeMillis();
    }

    /**
     * @param habitId
     * @return the row ID of the last row inserted, if this insert is successful. -1 otherwise.
     */
    public long startSession(long habitId){
        insertSessionStatement.bindLong(1, habitId);          // HABIT_ID
        insertSessionStatement.bindLong(2, getCurrentTime()); // STARTING_TIME
        insertSessionStatement.bindLong(3, 0);                // LAST_TIME_PAUSED
        insertSessionStatement.bindLong(4, 0);                // TOTAL_PAUSE_TIME
        insertSessionStatement.bindLong(5, 0);                // IS_PAUSED
        insertSessionStatement.bindString(6, "");             // NOTE

        return insertSessionStatement.executeInsert();
    }

    private int setLastPauseTime(long habitId, long time){
        return dbHelper.setAttribute(habitId, DatabaseHelper.LAST_TIME_PAUSED, time);
    }

    private int setIsPaused(long habitId, boolean state){
        return dbHelper.setAttribute(habitId, DatabaseHelper.IS_PAUSED, state?1:0);
    }

    public void pauseSession(long habitId){
        setLastPauseTime(habitId, getCurrentTime());
        setIsPaused(habitId, true);
    }

    public void playSession(long habitId){
        long lastPauseTime  = getLastPauseTime(habitId);
        long pauseTimeDelta = getCurrentTime() - lastPauseTime;

        long totalPauseTime = getTotalPauseTime(habitId) + pauseTimeDelta;
        setTotalPauseTime(habitId, totalPauseTime);
        setIsPaused(habitId, false);
    }

    public String getNote(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.NOTE);
        String note = c.getString(0);
        c.close();
        return note;
    }

    /**
     * @param habitId
     * @param note
     * @return the number of rows affected
     */
    public int setNote(long habitId, String note){
        return dbHelper.setAttribute(habitId, DatabaseHelper.NOTE, note);
    }

    /**
     * @param habitId
     * @param time
     * @return the number of rows affected
     */
    public int setTotalPauseTime(long habitId, Long time){
        return dbHelper.setAttribute(habitId, DatabaseHelper.TOTAL_PAUSE_TIME, time);
    }

    /**
     * @param habitId
     * @return total time paused
     */
    public long getTotalPauseTime(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.TOTAL_PAUSE_TIME);
        long totalPauseTime = c.getLong(0);
        c.close();
        return totalPauseTime;
    }

    /**
     * @param habitId
     * @return total time paused
     */
    public long getLastPauseTime(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.LAST_TIME_PAUSED);
        long lastPauseTime = c.getLong(0);
        c.close();
        return lastPauseTime;
    }

    public long getStartingTime(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.STARTING_TIME);
        long startingTime = c.getLong(0);
        c.close();
        return startingTime;
    }

    public boolean getIsPaused(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.IS_PAUSED);
        boolean isPaused = c.getLong(0) == 1;
        c.close();
        return isPaused;
    }

    /**
     * @param habitId
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise.
     */
    public long cancelSession(long habitId){
        return writableDatabase.delete(
                DatabaseHelper.SESSIONS_TABLE,
                DatabaseHelper.HABIT_ID + " =?",
                new String[]{String.valueOf(habitId)}
        );
    }

    public SessionEntry finishSession(long habitId){
        if(getIsPaused(habitId)){
            playSession(habitId);
        }

        long startingTime = getStartingTime(habitId);
        long duration = (getCurrentTime() - startingTime) - getTotalPauseTime(habitId);

        cancelSession(habitId);

        return new SessionEntry(startingTime, duration, getNote(habitId));
    }
}
