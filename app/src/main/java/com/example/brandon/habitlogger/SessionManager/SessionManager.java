package com.example.brandon.habitlogger.SessionManager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

import java.util.ArrayList;
import java.util.List;

import static com.example.brandon.habitlogger.SessionManager.DatabaseHelper.HABIT_ID;
import static com.example.brandon.habitlogger.SessionManager.DatabaseHelper.SESSIONS_TABLE;

/**
 * Created by Brandon on 12/4/2016.
 * A class for managing habit sessions.
 */

public class SessionManager {
    DatabaseHelper dbHelper;

    private SQLiteStatement insertSessionStatement;
    private Context context;

    private HabitDatabase habitDatabase;

    public SessionManager(Context context){
        this.context = context;
        dbHelper = new DatabaseHelper(context);
        insertSessionStatement = getInsertSessionStatement();

        this.habitDatabase = new HabitDatabase(context, null, false);
    }

    /**
     * Get a sqlite statement for creating new sessions.
     */
    public SQLiteStatement getInsertSessionStatement(){
        String sql = "INSERT INTO "+ SESSIONS_TABLE +
                " ("+
                HABIT_ID +
                ", "+ DatabaseHelper.STARTING_TIME+
                ", "+ DatabaseHelper.LAST_TIME_PAUSED+
                ", "+ DatabaseHelper.TOTAL_PAUSE_TIME+
                ", "+ DatabaseHelper.IS_PAUSED+
                ", "+ DatabaseHelper.NOTE+
                ") VALUES(?, ?, ?, ?, ?, ?)";

        return dbHelper.writableDatabase.compileStatement(sql);
    }

    public List<SessionEntry> getActiveSessionList(){
        List<SessionEntry> sessions = new ArrayList<>();

        Cursor c = dbHelper.getReadableDatabase()
                .query(DatabaseHelper.SESSIONS_TABLE, new String[]{DatabaseHelper.HABIT_ID},
                        null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                long habitId = c.getLong(0);
                SessionEntry entry = getSession(habitId);
                sessions.add(entry);
            } while (c.moveToNext());
        }

        c.close();

        return sessions;
    }

    /**
     * @return The current time in ms.
     */
    public long getCurrentTime(){
        return System.currentTimeMillis();
    }

    /**
     * @param habitId The id of the habit for the session.
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

    /**
     * Pause a session
     * @param habitId The id of the habit for the session.
     */
    public void pauseSession(long habitId){
        setLastPauseTime(habitId, getCurrentTime());
        setIsPaused(habitId, true);
    }

    /**
     * Un-pause a session
     * @param habitId The id of the habit for the session.
     */
    public void playSession(long habitId){
        long lastPauseTime  = getLastPauseTime(habitId);
        long pauseTimeDelta = getCurrentTime() - lastPauseTime;

        long totalPauseTime = getTotalPauseTime(habitId) + pauseTimeDelta;
        setTotalPauseTime(habitId, totalPauseTime);
        setIsPaused(habitId, false);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise.
     */
    public long cancelSession(long habitId){
        return dbHelper.writableDatabase.delete(
                SESSIONS_TABLE,
                HABIT_ID + " =?",
                new String[]{String.valueOf(habitId)}
        );
    }

    /**
     * End an active session.
     * @param habitId The id of the habit for the session.
     * @return the entry created from the session.
     */
    public SessionEntry finishSession(long habitId){
        if(getIsPaused(habitId)){
            playSession(habitId);
        }

        SessionEntry entry = getSession(habitId);

        cancelSession(habitId);

        return entry;
    }

    /**
     * Check if a session is active.
     * @param habitId The id of the habit for the session.
     * @return True if active, otherwise false.
     */
    public boolean isSessionActive(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, HABIT_ID);
        return c.getCount() != 0;
    }


    // SETTERS AND GETTERS

    public int getSessionCount(){
        return dbHelper.length();
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return Session entry
     */
    public SessionEntry getSession(long habitId){
        long startingTime = getStartingTime(habitId);
        long duration = (getCurrentTime() - startingTime) - getTotalPauseTime(habitId);

        if(getIsPaused(habitId)){
            long lastPaused = getLastPauseTime(habitId);
            long pausedTime = getCurrentTime() - lastPaused;
            duration -= pausedTime;
        }

        String note = getNote(habitId);

        SessionEntry newEntry = new SessionEntry(startingTime, duration, note);
        newEntry.setHabitId(habitId);

        newEntry.setName(habitDatabase.getHabitName(habitId));
        return newEntry;
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param note The note to be set for the session.
     * @return The number of rows affected.
     */
    public int setNote(long habitId, String note){
        return dbHelper.setAttribute(habitId, DatabaseHelper.NOTE, note);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return The note set on the session.
     */
    public String getNote(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.NOTE);
        String note = c.getString(0);
        c.close();
        return note;
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param time The time in ms that the session has been paused.
     * @return The number of rows affected.
     */
    public int setTotalPauseTime(long habitId, Long time){
        return dbHelper.setAttribute(habitId, DatabaseHelper.TOTAL_PAUSE_TIME, time);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return Total time paused
     */
    public long getTotalPauseTime(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.TOTAL_PAUSE_TIME);
        long totalPauseTime = c.getLong(0);
        c.close();

        return totalPauseTime;
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param time The last time in ms that the session was paused
     * @return The number of rows affected.
     */
    private int setLastPauseTime(long habitId, long time){
        return dbHelper.setAttribute(habitId, DatabaseHelper.LAST_TIME_PAUSED, time);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return total time paused
     */
    public long getLastPauseTime(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.LAST_TIME_PAUSED);
        long lastPauseTime = c.getLong(0);
        c.close();
        return lastPauseTime;
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return The time in ms which the session was started.
     */
    public long getStartingTime(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.STARTING_TIME);
        long startingTime = c.getLong(0);
        c.close();
        return startingTime;
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param state True if paused, false if not.
     * @return The number of rows affected.
     */
    private int setIsPaused(long habitId, boolean state){
        Long stateConversion = state?(long)1:(long)0;
        return dbHelper.setAttribute(habitId, DatabaseHelper.IS_PAUSED, stateConversion);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return True if paused, false if not.
     */
    public boolean getIsPaused(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.IS_PAUSED);
        boolean isPaused = c.getLong(0) == 1;
        c.close();
        return isPaused;
    }

    public static class TimeDisplay{
        public long hours, minutes, seconds;

        public TimeDisplay(long time){
            updateTime(time);
        }

        public void updateTime(long time){
            this.hours = (time - (time % 3600) ) / 3600;
            time -= this.hours * 3600;

            this.minutes = (time - (time % 60) ) / 60;
            time -= this.minutes * 60;

            this.seconds = time;
        }
    }
}
