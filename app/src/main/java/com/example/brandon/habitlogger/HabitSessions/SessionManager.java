package com.example.brandon.habitlogger.HabitSessions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitSessions.DatabaseSchema.DatabaseSchema;
import com.example.brandon.habitlogger.HabitSessions.DatabaseSchema.SessionsTableSchema;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.TimeFormatUtils.TimeDisplay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by Brandon on 12/4/2016.
 * A class for managing habit sessions.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionManager {
    DatabaseSchema databaseHelper;
    private SQLiteDatabase writableDatabase;
    private SQLiteDatabase readableDatabase;

    private Context context;
    private PreferenceChecker preferenceChecker;
    private HabitDatabase habitDatabase;
    private NotificationManager notificationManager;

    private static ArrayList<SessionChangeListener> sessionChangeListeners = new ArrayList<>();

    public interface SessionChangeListener {
        void sessionPauseStateChanged(long habitId, boolean isPaused);

        void sessionEnded(long habitId, boolean wasCanceled);
    }

    public void addSessionChangedListener(SessionChangeListener listener) {
        sessionChangeListeners.add(listener);
    }

    public SessionManager(Context context) {
        this.context = context;
        databaseHelper = new DatabaseSchema(context);
        writableDatabase = databaseHelper.getWritableDatabase();
        readableDatabase = databaseHelper.getReadableDatabase();

        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        this.preferenceChecker = new PreferenceChecker(context);
        this.habitDatabase = new HabitDatabase(context);
    }

    public int getDatabaseLength() {
        Cursor c = readableDatabase.query(
                SessionsTableSchema.TABLE_NAME,
                new String[]{SessionsTableSchema.HABIT_ID},
                null, null, null, null, null);

        int length = c.getCount();
        c.close();
        return length;
    }

    /**
     * @param habitId     The id of the habit for the session.
     * @param valueColumn The column in the database to be set.
     * @param object      The value to be set at "valueColumn"
     * @return the number of rows affected
     */
    public int setAttribute(long habitId, String valueColumn, Object object) {
        ContentValues value = new ContentValues(1);

        if (object instanceof String) {
            value.put(valueColumn, (String) object);
        }
        else if (object instanceof Long) {
            value.put(valueColumn, (Long) object);
        }
        else if (object instanceof Boolean) {
            value.put(valueColumn, (Boolean) object ? 1 : 0);
        }

        return writableDatabase.update(SessionsTableSchema.TABLE_NAME, value,
                SessionsTableSchema.HABIT_ID + " =?", new String[]{String.valueOf(habitId)});
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return The result of the query as a cursor.
     */
    public <Type> Type getAttribute(long habitId, String column, Class<Type> clazz) {
        Cursor c = readableDatabase.query(
                SessionsTableSchema.TABLE_NAME,
                new String[]{column},
                SessionsTableSchema.HABIT_ID + " =?",
                new String[]{String.valueOf(habitId)},
                null, null, null
        );

        Object result = null;
        if (c.moveToFirst()) {
            final int type = c.getType(0);
            switch (type) {
                case (FIELD_TYPE_STRING): {
                    result = c.getString(0);
                }
                break;
                case (FIELD_TYPE_INTEGER): {
                    result = c.getLong(0);
                    if (clazz == Boolean.class) {
                        result = ((long) result == 1);
                    }
                }
                break;
                case (FIELD_TYPE_FLOAT): {
                    result = c.getDouble(0);
                }
                break;
            }
        }

        c.close();
        return clazz.cast(result);
    }

    /**
     * Get a sqlite statement for creating new sessions.
     */
    public SQLiteStatement getInsertSessionStatement() {
        final String insertStatement = SessionsTableSchema.getInsertStatement();
        return writableDatabase.compileStatement(insertStatement);
    }

    private void bindSessionObject(SQLiteStatement statement, SessionEntry entry,
                                   String habitName, String habitCategoryName) {

        statement.bindLong(1, entry.getHabitId());            // HABIT_ID
        statement.bindString(2, habitName);                   // HABIT_NAME
        statement.bindString(3, habitCategoryName);           // HABIT_CATEGORY
        statement.bindLong(4, entry.getDuration());           // DURATION
        statement.bindLong(5, entry.getStartTime());          // STARTING_TIME
        statement.bindLong(6, entry.getLastTimePaused());     // LAST_TIME_PAUSED
        statement.bindLong(7, entry.getTotalPauseTime());     // TOTAL_PAUSE_TIME
        statement.bindLong(8, entry.getIsPaused() ? 1L : 0L); // IS_PAUSED
        statement.bindString(9, entry.getNote());             // NOTE

    }

    /**
     * @param habitId The id of the habit for the session.
     * @return the row ID of the last row inserted, if this insert is successful. -1 otherwise.
     */
    public long startSession(long habitId) {
        SessionEntry entry = new SessionEntry(getCurrentTime(), 0, "");
        entry.setHabitId(habitId);
        return insertSession(entry);
    }

    public long insertSession(SessionEntry entry) {
        Habit habit = habitDatabase.getHabit(entry.getHabitId());
        String habitName = habit.getName();
        String habitCategoryName = habit.getCategory().getName();

        SQLiteStatement insert = getInsertSessionStatement();
        bindSessionObject(insert, entry, habitName, habitCategoryName);
        long result = insert.executeInsert();

        if (preferenceChecker.doShowNotificationsAutomatically()) {
            createSessionNotification(habit);
        }

        return result;
    }

    public void createSessionNotification(long habitId) {
        Habit habit = habitDatabase.getHabit(habitId);
        createSessionNotification(habit);
    }

    public static int getResourceIdForPauseButton(boolean isPaused) {
        return isPaused ? R.drawable.ic_play_circle_filled_black_24dp :
                R.drawable.ic_play_circle_outline_black_24dp;
    }

    public void createSessionNotification(Habit habit) {
        // TODO CLEAN UP!
        if (preferenceChecker.doShowNotifications()) {
            final int habitId = (int) habit.getDatabaseId();

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.notification_layout);

            remoteViews.setTextViewText(R.id.active_session_habit_name, habit.getName());
            int categoryColor = habit.getColor();
            remoteViews.setInt(R.id.card_accent, "setBackgroundColor", categoryColor);
            remoteViews.setInt(R.id.session_pause_play, "setImageResource",
                    getResourceIdForPauseButton(getIsPaused(habitId)));

            Intent sessionToggleButton = new Intent("session_toggle_pressed");
            sessionToggleButton.putExtra("habitId", habitId);
            sessionToggleButton.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(context, habitId, sessionToggleButton, 0);
            remoteViews.setOnClickPendingIntent(R.id.session_pause_play, pendingToggleIntent);


            final NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_notification_icon)
                            .setContentText("")
                            .setContentTitle("")
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setAutoCancel(false)
                            .setOngoing(true)
                            .setSortKey(habit.getName())
                            .setCustomContentView(remoteViews);

            if (preferenceChecker.doShowTicker()) {
                String ticker = String.format
                        (Locale.getDefault(), context.getString(R.string.session_notification_ticker_format_string), habit.getName());
                builder.setTicker(ticker);
            }

            Intent resultIntent = new Intent(context, SessionActivity.class);
            resultIntent.putExtra("habit", (Serializable) habit);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, habitId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            notificationManager.notify(habitId, builder.build());

            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            if (isSessionActive(habitId) && preferenceChecker.doShowNotifications()) {
                                SessionEntry entry = getSession(habitId);
                                String timeDisplayText = TimeDisplay.getDisplay(entry.getDuration());

                                RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                                        R.layout.notification_layout);
                                remoteViews.setTextViewText(R.id.active_habit_time, timeDisplayText);
                                remoteViews.setTextViewText(R.id.time_started, entry.getStartTimeAsString("h:mm a"));
                                remoteViews.setInt(R.id.session_pause_play, "setImageResource",
                                        getResourceIdForPauseButton(getIsPaused(habitId)));
                                int categoryColor = habitDatabase.getIsHabitArchived(habitId) ? 0xFFCCCCCC :
                                        habitDatabase.getHabitColor(habitId);

                                remoteViews.setInt(R.id.card_accent, "setBackgroundColor", categoryColor);

                                builder.setCustomContentView(remoteViews);
                                Intent resultIntent = new Intent(context, SessionActivity.class);
                                resultIntent.putExtra("habit", (Serializable) habitDatabase.getHabit(habitId));
                                PendingIntent resultPendingIntent = PendingIntent.getActivity
                                        (
                                                context, habitId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
                                        );

                                builder.setContentIntent(resultPendingIntent);

                                notificationManager.notify(habitId, builder.build());

                                try {
                                    Thread.sleep(1000);
                                    this.run();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                notificationManager.cancel(habitId);
                            }
                        }
                    }
            ).start();
        }
    }

    public static class SessionToggle extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent data) {
            int habitId = data.getIntExtra("habitId", -1);

            SessionManager manager = new SessionManager(context);
            boolean isPaused = manager.getIsPaused(habitId);
            manager.setPauseState(habitId, !isPaused);
            manager.createSessionNotification(habitId);
        }
    }

    public void createAllSessionNotifications() {
        List<SessionEntry> entries = getActiveSessionList();
        for (SessionEntry entry : entries) {
            createSessionNotification(entry.getHabitId());
        }
    }

    public void clearAllNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * @param SQL            The SQL string to be executed
     * @param idColumnString The string id for the column that holds row ids.
     * @return An array of row ids found by the sqlite query.
     */
    @Nullable
    private long[] searchTableForIdsByName(String SQL, String idColumnString) {

        Cursor c = readableDatabase.rawQuery(SQL, null);

        if (c != null) {
            long ids[] = new long[c.getCount()];

            while (c.moveToNext()) {
                int idInd = c.getColumnIndex(idColumnString);
                ids[c.getPosition()] = c.getLong(idInd);
            }

            c.close();
            return ids;
        }

        return null;
    }

    public void setPauseState(long habitId, boolean pause) {
        if (pause)
            pauseSession(habitId);
        else
            playSession(habitId);

        if (sessionChangeListeners != null) {
            for (SessionChangeListener listener : sessionChangeListeners) {
                listener.sessionPauseStateChanged(habitId, pause);
            }
        }
    }

    /**
     * Pause a session
     *
     * @param habitId The id of the habit for the session.
     */
    public void pauseSession(long habitId) {
        setIsPaused(habitId, true);
        setLastPauseTime(habitId, getCurrentTime());
        setDuration(habitId, calculateDuration(habitId));
    }

    /**
     * Un-pause a session
     *
     * @param habitId The id of the habit for the session.
     */
    public void playSession(long habitId) {
        long lastPauseTime = getLastPauseTime(habitId);
        long pauseTimeDelta = getCurrentTime() - lastPauseTime;

        long totalPauseTime = getTotalPauseTime(habitId) + pauseTimeDelta;
        setTotalPauseTime(habitId, totalPauseTime);
        setIsPaused(habitId, false);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise.
     */
    public long cancelSession(long habitId) {
        notificationManager.cancel((int) habitId);

        if (sessionChangeListeners != null) {
            for (SessionChangeListener listener : sessionChangeListeners) {
                listener.sessionEnded(habitId, true);
            }
        }

        return writableDatabase.delete(
                SessionsTableSchema.TABLE_NAME,
                SessionsTableSchema.HABIT_ID + " =?",
                new String[]{String.valueOf(habitId)}
        );
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
            for (SessionChangeListener listener : sessionChangeListeners) {
                listener.sessionEnded(habitId, false);
            }
        }

        SessionEntry entry = getSession(habitId);
        entry.setDuration(calculateDuration(habitId));
        cancelSession(habitId);

        return entry;
    }

    /**
     * Check if a session is active.
     *
     * @param habitId The id of the habit for the session.
     * @return True if active, otherwise false.
     */
    public boolean isSessionActive(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.HABIT_ID, Long.class) != null;
    }


    // SETTERS AND GETTERS

    public List<SessionEntry> getActiveSessionList() {
        List<SessionEntry> sessions = new ArrayList<>();

        Cursor c = databaseHelper.getReadableDatabase()
                .query(SessionsTableSchema.TABLE_NAME, new String[]{SessionsTableSchema.HABIT_ID},
                        null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                long habitId = c.getLong(0);
                SessionEntry entry = getSession(habitId);

                sessions.add(entry);
            } while (c.moveToNext());
        }

        c.close();

        Collections.sort(sessions, SessionEntry.CategoryComparator);

        return sessions;
    }

    public List<SessionEntry> queryActiveSessionList(String query) {

        if (query.length() == 0) {
            return null;
        }

        List<SessionEntry> sessions = new ArrayList<>();

        Cursor c = readableDatabase.rawQuery("SELECT " + SessionsTableSchema.HABIT_ID + " FROM " +
                SessionsTableSchema.TABLE_NAME + " WHERE " +
                SessionsTableSchema.HABIT_NAME + " LIKE  '%" + query + "%' OR " +
                SessionsTableSchema.HABIT_CATEGORY + " LIKE  '%" + query + "%'", null);

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
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public int getSessionCount() {
        return getDatabaseLength();
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return Session entry
     */
    public SessionEntry getSession(long habitId) {
        long startingTime = getStartingTime(habitId);
        long lastPausedTime = getLastPauseTime(habitId);
        long totalPausedTime = getTotalPauseTime(habitId);
        boolean isPaused = getIsPaused(habitId);
        long duration = isPaused ? getDuration(habitId) : calculateDuration(habitId);
        String note = getNote(habitId);
        String categoryName = getCategoryName(habitId);

        SessionEntry newEntry = new SessionEntry(startingTime, duration, note);
        newEntry.setLastTimePaused(lastPausedTime);
        newEntry.setTotalPauseTime(totalPausedTime);
        newEntry.setIsPaused(isPaused);
        newEntry.setHabitId(habitId);
        newEntry.setCategoryName(categoryName);
        newEntry.setName(habitDatabase.getHabitName(habitId));

        return newEntry;
    }

    public long calculateDuration(long habitId) {
        long startingTime = getStartingTime(habitId);
        long duration = (getCurrentTime() - startingTime) - getTotalPauseTime(habitId);

        // Makes sure the displayed time doesn't change while paused.
        if (getIsPaused(habitId)) {
            long lastPaused = getLastPauseTime(habitId);
            long pausedTime = getCurrentTime() - lastPaused;
            duration -= pausedTime;
        }

        return duration;
    }

    /**
     * @param habitId  The id of the habit for the session.
     * @param duration The duration in ms to be set for the session.
     * @return The number of rows affected.
     */
    public int setDuration(long habitId, Long duration) {
        return setAttribute(habitId, SessionsTableSchema.DURATION, duration);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return The duration last set on the session.
     */
    public long getDuration(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.DURATION, Long.class);
    }

    public String getDurationAsString(long habitId) {
        long duration = getDuration(habitId) / 1000;


        TimeDisplay time = new TimeDisplay(duration);
        return String.format(Locale.US, "%02d:%02d:%02d", time.hours, time.minutes, time.seconds);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param note    The note to be set for the session.
     * @return The number of rows affected.
     */
    public int setNote(long habitId, String note) {
        return setAttribute(habitId, SessionsTableSchema.NOTE, note);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return The note set on the session.
     */
    public String getNote(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.NOTE, String.class);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return The note set on the session.
     */
    public String getCategoryName(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.HABIT_CATEGORY, String.class);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param time    The time in ms that the session has been paused.
     * @return The number of rows affected.
     */
    public int setTotalPauseTime(long habitId, Long time) {
        return setAttribute(habitId, SessionsTableSchema.TOTAL_PAUSE_TIME, time);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return Total time paused
     */
    public long getTotalPauseTime(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.TOTAL_PAUSE_TIME, Long.class);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param time    The last time in ms that the session was paused
     * @return The number of rows affected.
     */
    private int setLastPauseTime(long habitId, long time) {
        return setAttribute(habitId, SessionsTableSchema.LAST_TIME_PAUSED, time);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return total time paused
     */
    public long getLastPauseTime(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.LAST_TIME_PAUSED, Long.class);
    }

    /**
     * @return The time in ms which the session was started.
     */
    public long getStartingTime(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.STARTING_TIME, Long.class);
    }

    private int setIsPaused(long habitId, boolean state) {
        return setAttribute(habitId, SessionsTableSchema.IS_PAUSED, state);
    }

    public boolean getIsPaused(long habitId) {
        return getAttribute(habitId, SessionsTableSchema.IS_PAUSED, Boolean.class);
    }
}
