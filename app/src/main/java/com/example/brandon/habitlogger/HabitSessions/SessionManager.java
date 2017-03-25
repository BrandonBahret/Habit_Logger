package com.example.brandon.habitlogger.HabitSessions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.format.DateUtils;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.DatabaseSchema.DatabaseSchema;
import com.example.brandon.habitlogger.HabitSessions.DatabaseSchema.SessionsTableSchema;
import com.example.brandon.habitlogger.common.MyDatabaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 12/4/2016.
 * A class for managing habit sessions.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionManager implements MyDatabaseUtils.AccessAttributesMethods {
    DatabaseSchema databaseHelper;
    private SQLiteDatabase writableDatabase;
    private SQLiteDatabase readableDatabase;

    //region // Provide interface methods
    private static ArrayList<SessionChangeListeners> sessionChangeListeners = new ArrayList<>();

    public interface SessionChangeListeners {
        void onSessionPauseStateChanged(long habitId, boolean isPaused);

        void beforeSessionEnded(long habitId, boolean wasCanceled);

        void afterSessionEnded(long habitId, boolean wasCanceled);

        void onSessionStarted(long habitId);
    }

    public void addSessionChangedCallback(SessionChangeListeners callback) {
        sessionChangeListeners.add(callback);
    }

    public void removeSessionChangedCallback(SessionChangeListeners callback) {
        sessionChangeListeners.remove(callback);
    }
    //endregion

    public SessionManager(Context context) {
        databaseHelper = new DatabaseSchema(context);
        writableDatabase = databaseHelper.getWritableDatabase();
        readableDatabase = databaseHelper.getReadableDatabase();
    }

    public long insertSession(SessionEntry entry) {
        // Get insert statement
        final String insertStatement = SessionsTableSchema.getInsertStatement();
        SQLiteStatement insert = writableDatabase.compileStatement(insertStatement);

        // Bind object values to the statement
        SessionsTableSchema.bindObjectToStatement(insert, entry);
        long result = insert.executeInsert();
        insert.close();

        long habitId = entry.getHabit().getDatabaseId();
        for (SessionChangeListeners listener : sessionChangeListeners) {
            listener.onSessionStarted(habitId);
        }

        return result;
    }

    private long deleteSession(long habitId) {

        return writableDatabase.delete(
                SessionsTableSchema.TABLE_NAME,
                SessionsTableSchema.HABIT_ID + " =?",
                new String[]{String.valueOf(habitId)}
        );
    }

    /**
     * @param habit the habit to start a session for.
     * @return the row ID of the last row inserted, if this insert is successful. -1 otherwise.
     */
    public long startSession(Habit habit) {
        SessionEntry entry = new SessionEntry(System.currentTimeMillis(), 0, "");
        entry.setHabit(habit);

        return insertSession(entry);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise.
     */
    public long cancelSession(long habitId) {
        if (sessionChangeListeners != null) {
            for (SessionChangeListeners listener : sessionChangeListeners) {
                listener.beforeSessionEnded(habitId, true);
            }
        }

        long res = deleteSession(habitId);

        if (sessionChangeListeners != null) {
            for (SessionChangeListeners listener : sessionChangeListeners) {
                listener.afterSessionEnded(habitId, true);
            }
        }

        return res;
    }

    public long calculateElapsedTimeForEntry(long habitId) {
        SessionEntry entry = getSession(habitId);
        return calculateElapsedTimeForEntry(entry);
    }

    public static long calculateElapsedTimeForEntry(SessionEntry entry) {
        long currentTime = System.currentTimeMillis();
        long startingTime = entry.getStartTime();
        long duration = (currentTime - startingTime) - entry.getTotalPauseTime();

        if (entry.getIsPaused()) {
            // If we're paused then also subtract the time since we last paused.
            long lastPaused = entry.getLastTimePaused();
            long pausedTime = currentTime - lastPaused;
            duration -= pausedTime;
        }

        return duration;
    }

    /**
     * Pause a session
     *
     * @param habitId The id of the habit for the session.
     */
    private void pauseSession(long habitId) {
        setIsPaused(habitId, true);
        setLastPauseTime(habitId, System.currentTimeMillis());
        setDuration(habitId, calculateElapsedTimeForEntry(habitId));
    }

    /**
     * Un-pause a session
     *
     * @param habitId The id of the habit for the session.
     */
    private void playSession(long habitId) {
        long lastPauseTime = getLastPauseTime(habitId);
        long pauseTimeDelta = System.currentTimeMillis() - lastPauseTime;

        long totalPauseTime = getTotalPauseTime(habitId) + pauseTimeDelta;
        setTotalPauseTime(habitId, totalPauseTime);
        setIsPaused(habitId, false);
    }

    public void setPauseState(long habitId, boolean pause) {
        if (pause)
            pauseSession(habitId);
        else
            playSession(habitId);

        if (sessionChangeListeners != null) {
            for (SessionChangeListeners listener : sessionChangeListeners) {
                listener.onSessionPauseStateChanged(habitId, pause);
            }
        }
    }

    /**
     * End an active session.
     *
     * @param habitId The id of the habit for the session.
     * @return the entry created from the session.
     */
    public SessionEntry finishSession(long habitId) {
        if (getIsPaused(habitId)) {
            setPauseState(habitId, false);
        }

        if (sessionChangeListeners != null) {
            for (SessionChangeListeners listener : sessionChangeListeners) {
                listener.beforeSessionEnded(habitId, false);
            }
        }

        SessionEntry entry = getSession(habitId);
        entry.setDuration(calculateElapsedTimeForEntry(habitId));
        deleteSession(habitId);

        return entry;
    }

    public List<SessionEntry> queryActiveSessionList(String query) {
        List<SessionEntry> sessions = new ArrayList<>();

        if (query.isEmpty()) { // If the query is an empty string return null;
            return sessions;
        }

        Cursor c = readableDatabase.rawQuery(
                SessionsTableSchema.getSearchRecordsByHabitOrCategoryNameStatement(query), null
        );

        if (c.moveToFirst()) {
            sessions = new ArrayList<>(c.getCount());
            do sessions.add(getSessionEntryFromCursor(c)); while (c.moveToNext());
        }
        c.close();

        return sessions;
    }

    public long getSessionCount() {
        return DatabaseUtils.queryNumEntries(readableDatabase, SessionsTableSchema.TABLE_NAME);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return Session entry
     */
    public SessionEntry getSession(long habitId) {
        Cursor cursor = readableDatabase.query(
                SessionsTableSchema.TABLE_NAME,
                null,
                SessionsTableSchema.HABIT_ID + " =?",
                new String[]{String.valueOf(habitId)},
                null, null, null
        );

        cursor.moveToFirst();
        SessionEntry entry = getSessionEntryFromCursor(cursor);
        cursor.close();

        return entry;
    }

    public List<SessionEntry> getActiveSessionList() {
        Cursor cursor = readableDatabase.query(
                SessionsTableSchema.TABLE_NAME,
                null, null, null, null, null, null
        );

        List<SessionEntry> sessions = new ArrayList<>(cursor.getCount());

        if (cursor.moveToFirst()) {
            // Load all the entries in the database into the ArrayList.
            do sessions.add(getSessionEntryFromCursor(cursor)); while (cursor.moveToNext());

            // Sort the entries by Category Name
            Collections.sort(sessions, SessionEntry.CategoryComparator);
        }
        cursor.close();

        return sessions;
    }

    //region // Getters
    @Override
    public <Type> Type getAttribute(long habitId, String columnKey, Class<Type> clazz) {
        return MyDatabaseUtils.getAttribute(
                readableDatabase, SessionsTableSchema.TABLE_NAME, SessionsTableSchema.HABIT_ID,
                habitId, columnKey, clazz
        );
    }

    private SessionEntry getSessionEntryFromCursor(Cursor cursor) {
        ContentValues contentValues = new ContentValues(cursor.getCount());
        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);

        return SessionsTableSchema.objectFromContentValues(contentValues);
    }

    public long getDuration(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.DURATION, Long.class);
    }

    public String getDurationAsString(long habitId) {
        return SessionEntry.stringifyDuration(getDuration(habitId) / DateUtils.SECOND_IN_MILLIS);
    }

    public String getNote(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.NOTE, String.class);
    }

    public String getCategoryName(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.HABIT_CATEGORY, String.class);
    }

    public boolean getIsSessionActive(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.HABIT_ID, Long.class) != null;
    }

    public long getTotalPauseTime(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.TOTAL_PAUSE_TIME, Long.class);
    }

    public long getLastPauseTime(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.LAST_TIME_PAUSED, Long.class);
    }

    public long getStartingTime(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.STARTING_TIME, Long.class);
    }

    public boolean getIsPaused(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.IS_PAUSED, Long.class) == 1;
    }
    //endregion

    //region // Setters
    @Override
    public int setAttribute(long habitId, String columnKey, Object object) {
        return MyDatabaseUtils.setAttribute(
                writableDatabase, SessionsTableSchema.TABLE_NAME, SessionsTableSchema.HABIT_ID,
                habitId, columnKey, object
        );
    }

    public int setDuration(long habitId, Long duration) {
        return setAttribute(habitId, SessionsTableSchema.DURATION, duration);
    }

    public int setNote(long habitId, String note) {
        return setAttribute(habitId, SessionsTableSchema.NOTE, note);
    }

    public int setTotalPauseTime(long habitId, Long time) {
        return setAttribute(habitId, SessionsTableSchema.TOTAL_PAUSE_TIME, time);
    }

    private int setLastPauseTime(long habitId, long time) {
        return setAttribute(habitId, SessionsTableSchema.LAST_TIME_PAUSED, time);
    }

    private int setIsPaused(long habitId, boolean state) {
        return setAttribute(habitId, SessionsTableSchema.IS_PAUSED, state);
    }
    //endregion
}