package com.example.brandon.habitlogger.SessionManager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.MainActivity;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.SessionActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        String sql = "INSERT INTO "+ DatabaseHelper.SESSIONS_TABLE +
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

        String habitName = habit.getName();
        HabitCategory category = habit.getCategory();
        String habitCategoryName = category.getName();

        insertSessionStatement.bindLong(1, habitId);          // HABIT_ID
        insertSessionStatement.bindString(2, habitName);         // HABIT_NAME
        insertSessionStatement.bindString(3, habitCategoryName); // HABIT_CATEGORY
        insertSessionStatement.bindLong(4, 0);                // DURATION
        insertSessionStatement.bindLong(5, getCurrentTime()); // STARTING_TIME
        insertSessionStatement.bindLong(6, 0);                // LAST_TIME_PAUSED
        insertSessionStatement.bindLong(7, 0);                // TOTAL_PAUSE_TIME
        insertSessionStatement.bindLong(8, 0);                // IS_PAUSED
        insertSessionStatement.bindString(9, "");             // NOTE

        createSessionNotification(habit);

        return insertSessionStatement.executeInsert();
    }

    public void createSessionNotification(final Habit habit) {
        Toast.makeText(context, "Create notification", Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(context, SessionManager.class);

        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        NotificationCompat.Action action = new NotificationCompat.Action(
                R.drawable.ic_play_circle_outline_black_24dp,
                "pause",
                pIntent
        );

        NotificationCompat.Action action2 = new NotificationCompat.Action(
                R.drawable.ic_check_white_24px,
                "finish",
                pIntent
        );

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_timer_white_24px)
                        .setColor(habit.getCategory().getColorAsInt())
                        .setContentTitle(habit.getName())
                        .setContentText(habit.getCategory().getName())
                        .setTicker("Session for '" +habit.getName()+ "' started")
                        .addAction(action)
                        .addAction(action2)
                        ;

        Intent resultIntent = new Intent(context, SessionActivity.class);
        resultIntent.putExtra("habit", habit);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int)habit.getDatabaseId();

        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotificationId, mBuilder.build());

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(true) { // TODO fix this warning!
                            try {

                                SessionEntry entry = getSession(habit.getDatabaseId());
                                TimeDisplay display = new TimeDisplay(entry.getDuration());

                                // When the loop is finished, updates the notification
                                mBuilder.setContentText(display.toString());

                                mNotificationManager.notify((int) habit.getDatabaseId(), mBuilder.build());
                                Thread.sleep(1000);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
// Starts the thread by calling the run() method in its Runnable
        ).start();

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

    public List<SessionEntry> queryActiveSessionList(String query){

        if(query.length() == 0){
            return null;
        }

        List<SessionEntry> sessions = new ArrayList<>();

        Cursor c = dbHelper.readableDatabase.rawQuery("SELECT "+DatabaseHelper.HABIT_ID+" FROM " +
                DatabaseHelper.SESSIONS_TABLE + " WHERE " +
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
    }
}
