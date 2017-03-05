package com.example.brandon.habitlogger.HabitSessions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerViewAdapters.HabitViewHolder;
import com.example.brandon.habitlogger.common.TimeDisplay;

import java.io.Serializable;

/**
 * Created by Brandon on 2/18/2017.
 * Manager for session notifications.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionNotificationManager {

    Context context;
    SessionManager sessionManager;
    NotificationManager notificationManager;

    public SessionNotificationManager(Context context) {
        this.context = context;
        sessionManager = new SessionManager(context);
        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static class SessionToggle extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent data) {
            Habit habit = data.getParcelableExtra("habit");

            SessionNotificationManager manager = new SessionNotificationManager(context);
            boolean isPaused = manager.sessionManager.getIsPaused(habit.getDatabaseId());
            manager.sessionManager.setPauseState(habit.getDatabaseId(), !isPaused);

            manager.updateNotification(habit);
        }
    }

    public RemoteViews createRemoteViews(Habit habit) {
        final int habitId = (int) habit.getDatabaseId();
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        remoteViews.setInt(R.id.card_accent, "setBackgroundColor", habit.getColor());
        remoteViews.setTextViewText(R.id.active_session_habit_name, habit.getName());

        if (sessionManager.getIsSessionActive(habitId)) {
            SessionEntry entry = sessionManager.getSession(habitId);
            remoteViews.setTextViewText(R.id.active_habit_time, TimeDisplay.getDisplay(entry.getDuration()));
            remoteViews.setTextViewText(R.id.time_started, entry.getStartTimeAsString("h:mm a"));
            remoteViews.setOnClickPendingIntent(R.id.session_pause_play, getPendingIntentForSessionToggle(habit));
            remoteViews.setInt(
                    R.id.session_pause_play, "setImageResource",
                    HabitViewHolder.getResourceIdForPauseButton(entry.getIsPaused())
            );
        }

        return remoteViews;
    }

    private PendingIntent getPendingIntentForSessionToggle(Habit habit) {
        Intent resultIntent = new Intent("session_toggle_pressed");
        resultIntent.putExtra("habit", (Parcelable) habit);
        return PendingIntent.getBroadcast(context, (int) habit.getDatabaseId(), resultIntent, 0);
    }

    private PendingIntent getPendingIntentForSessionActivity(Habit habit) {
        Intent resultIntent = new Intent(context, SessionActivity.class);
        resultIntent.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) habit);
        return PendingIntent.getActivity
                (context, (int) habit.getDatabaseId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification createNotification(Habit habit) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentText("")
                .setContentTitle("")
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSortKey(habit.getName())
                .setCustomContentView(createRemoteViews(habit))
                .setContentIntent(getPendingIntentForSessionActivity(habit))
                .build();
    }

    public void launchSessionNotification(final Habit habit) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        final int habitId = (int) habit.getDatabaseId();

                        if (sessionManager.getIsSessionActive(habitId)) {
                            notificationManager.notify(habitId, createNotification(habit));

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

    public void launchNotificationsForAllActiveSessions() {
        for (SessionEntry entry : sessionManager.getActiveSessionList()) {
            launchSessionNotification(entry.getHabit());
        }
    }

    public void updateNotification(Habit habit) {
        notificationManager.notify((int) habit.getDatabaseId(), createNotification(habit));
    }

    public void cancel(int habitId) {
        notificationManager.cancel(habitId);
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
