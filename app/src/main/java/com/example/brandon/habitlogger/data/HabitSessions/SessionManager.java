package com.example.brandon.habitlogger.data.HabitSessions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateUtils;

import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitSessions.DatabaseSchema.DatabaseSchema;
import com.example.brandon.habitlogger.data.HabitSessions.DatabaseSchema.SessionsTableSchema;
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

    //region (Member attributes)
    public DatabaseSchema databaseHelper;
    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;
    //endregion

    //region Code responsible for providing an interface to the database
    private static ArrayList<SessionChangeCallback> sessionChangeCallbacks = new ArrayList<>();

    public interface SessionChangeCallback {
        void onSessionPauseStateChanged(long habitId, boolean isPaused);

        void beforeSessionEnded(long habitId, boolean wasCanceled);

        void afterSessionEnded(long habitId, boolean wasCanceled);

        void onSessionStarted(long habitId);
    }

    public void addSessionChangedCallback(SessionChangeCallback callback) {
        sessionChangeCallbacks.add(callback);
    }

    public void removeSessionChangedCallback(SessionChangeCallback callback) {
        sessionChangeCallbacks.remove(callback);
    }
    //endregion

    public SessionManager(Context context) {
        databaseHelper = new DatabaseSchema(context);
        mWritableDatabase = databaseHelper.getWritableDatabase();
        mReadableDatabase = databaseHelper.getReadableDatabase();
    }

    //region [ ---- Methods responsible for managing active sessions ---- ]

    //region Methods responsible for calculating elapsed time
    public long calculateElapsedTimeForEntry(long habitId) {
        SessionEntry entry = getSession(habitId);
        return calculateElapsedTimeForEntry(entry);
    }

    public static long calculateElapsedTimeForEntry(SessionEntry entry) {
        long currentTime = System.currentTimeMillis();
        long startingTime = entry.getStartingTime();
        long duration = (currentTime - startingTime) - entry.getTotalPauseTime();

        if (entry.getIsPaused()) {
            // If we're paused then also subtract the amount of time since we last paused.
            long lastPaused = entry.getLastTimePaused();
            long pausedTime = currentTime - lastPaused;
            duration -= pausedTime;
        }

        return duration;
    }
    //endregion -- end --

    //region Methods responsible for managing pause state
    public void setPauseState(long habitId, boolean pause) {
        if (pause) pauseSession(habitId);
        else playSession(habitId);

        if (sessionChangeCallbacks != null) {
            for (SessionChangeCallback listener : sessionChangeCallbacks)
                listener.onSessionPauseStateChanged(habitId, pause);
        }
    }

    private void pauseSession(long habitId) {
        setLastPauseTime(habitId, System.currentTimeMillis());
        setDuration(habitId, calculateElapsedTimeForEntry(habitId));
        setIsPaused(habitId, true);
    }

    private void playSession(long habitId) {
        long lastPauseTime = getLastPauseTime(habitId);
        long pauseTimeDelta = System.currentTimeMillis() - lastPauseTime;
        long totalPauseTime = getTotalPauseTime(habitId) + pauseTimeDelta;

        setTotalPauseTime(habitId, totalPauseTime);
        setIsPaused(habitId, false);
    }
    //endregion -- end --

    //region Methods responsible for starting and ending sessions
    public long startSession(Habit habit) {
        SessionEntry entry = new SessionEntry(System.currentTimeMillis(), 0, "");
        entry.setHabit(habit);

        return insertSession(entry);
    }

    public SessionEntry finishSession(long habitId) {
        if (getIsPaused(habitId))
            setPauseState(habitId, false);

        if (sessionChangeCallbacks != null) {
            for (SessionChangeCallback listener : sessionChangeCallbacks)
                listener.beforeSessionEnded(habitId, false);
        }

        SessionEntry entry = getSession(habitId);
        entry.setDuration(calculateElapsedTimeForEntry(habitId));
        deleteSession(habitId);

        return entry;
    }

    public long cancelSession(long habitId) {
        if (sessionChangeCallbacks != null) {
            for (SessionChangeCallback listener : sessionChangeCallbacks)
                listener.beforeSessionEnded(habitId, true);
        }

        long res = deleteSession(habitId);

        if (sessionChangeCallbacks != null) {
            for (SessionChangeCallback listener : sessionChangeCallbacks)
                listener.afterSessionEnded(habitId, true);
        }

        return res;
    }
    //endregion -- end --

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Methods responsible for manipulating the database ---- ]

    public long getNumberOfActiveSessions() {
        return DatabaseUtils.queryNumEntries(mReadableDatabase, SessionsTableSchema.TABLE_NAME);
    }

    public boolean hasActiveSessions() {
        return getNumberOfActiveSessions() != 0;
    }

    public List<SessionEntry> queryActiveSessionList(String query) {
        List<SessionEntry> sessions = new ArrayList<>();

        if (!query.isEmpty()) {
            Cursor c = mReadableDatabase.rawQuery(
                    SessionsTableSchema.getSearchByNameStatement(query), null
            );

            sessions = fetchEntriesAtCursor(c);
        }

        return sessions;
    }

    //region [ -- CRUD records -- ]

    public long insertSession(SessionEntry entry) {
        long recordId = mWritableDatabase.insert(
                SessionsTableSchema.TABLE_NAME, null,
                SessionsTableSchema.getContentValuesFromObject(entry)
        );

        long habitId = entry.getHabit().getDatabaseId();
        for (SessionChangeCallback listener : sessionChangeCallbacks)
            listener.onSessionStarted(habitId);

        return recordId;
    }

    //region Read records
    private SessionEntry getSessionEntryFromCursor(Cursor cursor) {
        ContentValues contentValues = new ContentValues(cursor.getCount());
        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);

        return SessionsTableSchema.getObjectFromContentValues(contentValues);
    }

    public SessionEntry getSession(long habitId) {
        return MyDatabaseUtils.getRecord(
                mReadableDatabase, SessionsTableSchema.TABLE_NAME,
                SessionsTableSchema.HABIT_ID, habitId, SessionsTableSchema.IGetFromContentValues);
    }

    public List<SessionEntry> getActiveSessionList() {
        Cursor cursor = mReadableDatabase.query(
                SessionsTableSchema.TABLE_NAME,
                null, null, null, null, null, null
        );

        return fetchEntriesAtCursor(cursor);
    }

    private List<SessionEntry> fetchEntriesAtCursor(Cursor cursor) {
        if (cursor.moveToFirst()) {
            List<SessionEntry> resultList = new ArrayList<>(cursor.getCount());

            // Load all the entries in the database into the ArrayList.
            do resultList.add(getSessionEntryFromCursor(cursor)); while (cursor.moveToNext());

            // Sort the entries by Category Name
            Collections.sort(resultList, SessionEntry.ICompareCategoryNames);
            cursor.close();

            return resultList;
        }

        return new ArrayList<>();
    }
    //endregion -- end --

    private long deleteSession(long habitId) {
        return MyDatabaseUtils.deleteRecord(
                mWritableDatabase, SessionsTableSchema.TABLE_NAME,
                SessionsTableSchema.HABIT_ID, habitId
        );
    }

    //endregion [ -- end -- ]

    //region Get attributes
    @Override
    public <Type> Type getAttribute(long habitId, String columnKey, Class<Type> clazz) {
        return MyDatabaseUtils.getAttribute(
                mReadableDatabase, SessionsTableSchema.TABLE_NAME, SessionsTableSchema.HABIT_ID,
                habitId, columnKey, clazz
        );
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
        return getAttribute(habitId, SessionsTableSchema.HABIT_CATEGORY_NAME, String.class);
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

    //region Set attributes
    @Override
    public int setAttribute(long habitId, String columnKey, Object object) {
        return MyDatabaseUtils.setAttribute(
                mWritableDatabase, SessionsTableSchema.TABLE_NAME, SessionsTableSchema.HABIT_ID,
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

    //endregion [ ---------------- end ---------------- ]

}