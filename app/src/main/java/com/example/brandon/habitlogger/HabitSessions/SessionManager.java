package com.example.brandon.habitlogger.HabitSessions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.brandon.habitlogger.HabitSessions.DatabaseHelper.HABIT_ID;
import static com.example.brandon.habitlogger.HabitSessions.DatabaseHelper.SESSIONS_TABLE;

/**
 * Created by Brandon on 12/4/2016.
 * A class for managing habit sessions.
 */

public class SessionManager {
    DatabaseHelper dbHelper;

    private SQLiteStatement insertSessionStatement;
    private Context context;

    private PreferenceChecker preferenceChecker;
    private HabitDatabase habitDatabase;
    private NotificationManager notificationManager;

    private static SessionChangeListener sessionChangeListener = null;

    public interface SessionChangeListener{
        void sessionPauseStateChanged(long sessionId, boolean isPaused);
    }

    public void setSessionChangedListener(SessionChangeListener listener){
        this.sessionChangeListener = listener;
    }

    public SessionManager(Context context){
        this.context = context;
        dbHelper = new DatabaseHelper(context);
        insertSessionStatement = getInsertSessionStatement();

        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        this.preferenceChecker = new PreferenceChecker(context);
        this.habitDatabase = new HabitDatabase(context, null, false);
    }

    /**
     * Get a sqlite statement for creating new sessions.
     */
    public SQLiteStatement getInsertSessionStatement(){
        String sql = "INSERT INTO "+ SESSIONS_TABLE +
                " ("+
                HABIT_ID +
                ", "+ DatabaseHelper.HABIT_NAME+
                ", "+ DatabaseHelper.HABIT_CATEGORY+
                ", "+ DatabaseHelper.DURATION+
                ", "+ DatabaseHelper.STARTING_TIME+
                ", "+ DatabaseHelper.LAST_TIME_PAUSED+
                ", "+ DatabaseHelper.TOTAL_PAUSE_TIME+
                ", "+ DatabaseHelper.IS_PAUSED+
                ", "+ DatabaseHelper.NOTE+
                ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return dbHelper.writableDatabase.compileStatement(sql);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return the row ID of the last row inserted, if this insert is successful. -1 otherwise.
     */
    public long startSession(long habitId){
        Habit habit = habitDatabase.getHabit(habitId);

        if(habit != null) {
            String habitName = habit.getName();
            HabitCategory category = habit.getCategory();
            String habitCategoryName = category.getName();

            insertSessionStatement.bindLong(1, habitId);             // HABIT_ID
            insertSessionStatement.bindString(2, habitName);         // HABIT_NAME
            insertSessionStatement.bindString(3, habitCategoryName); // HABIT_CATEGORY
            insertSessionStatement.bindLong(4, 0);                   // DURATION
            insertSessionStatement.bindLong(5, getCurrentTime());    // STARTING_TIME
            insertSessionStatement.bindLong(6, 0);                   // LAST_TIME_PAUSED
            insertSessionStatement.bindLong(7, 0);                   // TOTAL_PAUSE_TIME
            insertSessionStatement.bindLong(8, 0);                   // IS_PAUSED
            insertSessionStatement.bindString(9, "");                // NOTE

            long result = insertSessionStatement.executeInsert();

            if (preferenceChecker.doShowNotificationsAutomatically()) {
                createSessionNotification(habit);
            }

            return result;
        }

        return -1;
    }

    public void createSessionNotification(long habitId) {
        Habit habit = habitDatabase.getHabit(habitId);
        createSessionNotification(habit);
    }

    public int getResourceIdForPauseButton(boolean isPaused){
        return isPaused? R.drawable.ic_play_circle_filled_black_24dp :
                R.drawable.ic_play_circle_outline_black_24dp;
    }

    public void createSessionNotification(Habit habit) {
        // TODO CLEAN UP!
        if (preferenceChecker.doShowNotifications()) {
            final int habitId = (int) habit.getDatabaseId();

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.notification_layout);

            remoteViews.setTextViewText(R.id.active_session_habit_name, habit.getName());
            remoteViews.setInt(R.id.card_accent, "setBackgroundColor", habit.getCategory().getColorAsInt());
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

            if(preferenceChecker.doShowTicker()) {
                String ticker = String.format
                        (Locale.getDefault(), context.getString(R.string.session_notification_ticker_format_string), habit.getName());
                builder.setTicker(ticker);
            }

            Intent resultIntent = new Intent(context, SessionActivity.class);
            resultIntent.putExtra("habit", habit);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, habitId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            notificationManager.notify(habitId, builder.build());

            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            if(isSessionActive(habitId) && preferenceChecker.doShowNotifications()) {
                                SessionEntry entry = getSession(habitId);
                                String timeDisplayText = TimeDisplay.getDisplay(entry.getDuration());

                                RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                                        R.layout.notification_layout);
                                remoteViews.setTextViewText(R.id.active_habit_time, timeDisplayText);
                                remoteViews.setInt(R.id.session_pause_play, "setImageResource",
                                        getResourceIdForPauseButton(getIsPaused(habitId)));

                                builder.setCustomContentView(remoteViews);
                                Intent resultIntent = new Intent(context, SessionActivity.class);
                                resultIntent.putExtra("habit", habitDatabase.getHabit(habitId));
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(context, habitId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(resultPendingIntent);

                                notificationManager.notify(habitId, builder.build());

                                try {
                                    Thread.sleep(1000);
                                    this.run();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
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

    public void createAllSessionNotifications(){
        List<SessionEntry> entries = getActiveSessionList();
        for(SessionEntry entry: entries){
            createSessionNotification(entry.getHabitId());
        }
    }

    public void clearAllNotifications(){
        notificationManager.cancelAll();
    }

    /**
     * @param SQL The SQL string to be executed
     * @param idColumnString The string id for the column that holds row ids.
     * @return An array of row ids found by the sqlite query.
     */
    @Nullable
    private long[] searchTableForIdsByName(String SQL, String idColumnString){

        Cursor c = dbHelper.readableDatabase.rawQuery(SQL, null);

        if(c != null) {
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

    public void setPauseState(long habitId, boolean pause){
        if (sessionChangeListener != null) {
            sessionChangeListener.sessionPauseStateChanged(habitId, pause);
        }

        if(pause)
            pauseSession(habitId);
        else
            playSession(habitId);
    }

    /**
     * Pause a session
     * @param habitId The id of the habit for the session.
     */
    public void pauseSession(long habitId){
        setIsPaused(habitId, true);
        setLastPauseTime(habitId, getCurrentTime());
        setDuration(habitId, calculateDuration(habitId));
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
        notificationManager.cancel((int)habitId);

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
        entry.setDuration(calculateDuration(habitId));

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
        boolean result = c.getCount() != 0;
        c.close();
        return result;
    }


    // SETTERS AND GETTERS

    public List<SessionEntry> getActiveSessionList(){
        List<SessionEntry> sessions = new ArrayList<>();

        Cursor c = dbHelper.getReadableDatabase()
                .query(SESSIONS_TABLE, new String[]{HABIT_ID},
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

    public List<SessionEntry> queryActiveSessionList(String query){

        if(query.length() == 0){
            return null;
        }

        List<SessionEntry> sessions = new ArrayList<>();

        Cursor c = dbHelper.readableDatabase.rawQuery("SELECT "+ HABIT_ID+" FROM " +
                SESSIONS_TABLE + " WHERE " +
                DatabaseHelper.HABIT_NAME + " LIKE  '%" + query + "%' OR " +
                DatabaseHelper.HABIT_CATEGORY + " LIKE  '%" + query + "%'", null);

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

    public int getSessionCount(){
        return dbHelper.length();
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return Session entry
     */
    public SessionEntry getSession(long habitId){
        long startingTime = getStartingTime(habitId);

        long duration = getIsPaused(habitId) ? getDuration(habitId) : calculateDuration(habitId);

        String note = getNote(habitId);

        SessionEntry newEntry = new SessionEntry(startingTime, duration, note);
        newEntry.setHabitId(habitId);

        newEntry.setName(habitDatabase.getHabitName(habitId));
        return newEntry;
    }

    public long calculateDuration(long habitId){
        long startingTime = getStartingTime(habitId);
        long duration = (getCurrentTime() - startingTime) - getTotalPauseTime(habitId);

        // Makes sure the displayed time doesn't change while paused.
        if(getIsPaused(habitId)){
            long lastPaused = getLastPauseTime(habitId);
            long pausedTime = getCurrentTime() - lastPaused;
            duration -= pausedTime;
        }

        return duration;
    }

    /**
     * @param habitId The id of the habit for the session.
     * @param duration The duration in ms to be set for the session.
     * @return The number of rows affected.
     */
    public int setDuration(long habitId, Long duration){
        return dbHelper.setAttribute(habitId, DatabaseHelper.DURATION, duration);
    }

    /**
     * @param habitId The id of the habit for the session.
     * @return The duration last set on the session.
     */
    public long getDuration(long habitId){
        Cursor c = dbHelper.getAttribute(habitId, DatabaseHelper.DURATION);
        long duration = c.getLong(0);
        c.close();
        return duration;
    }

    public String getDurationAsString(long habitId) {
        long duration = getDuration(habitId) / 1000;

        SessionManager.TimeDisplay time = new SessionManager.TimeDisplay(duration);
        return String.format(Locale.US, "%02d:%02d:%02d", time.hours, time.minutes, time.seconds);
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
            time /= 1000;

            this.hours = (time - (time % 3600) ) / 3600;
            time -= this.hours * 3600;

            this.minutes = (time - (time % 60) ) / 60;
            time -= this.minutes * 60;

            this.seconds = time;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%02d:%02d:%02d", this.hours, this.minutes, this.seconds);
        }

        public static String getDisplay(long duration) {
            return new TimeDisplay(duration).toString();
        }
    }
}
