package com.example.brandon.habitlogger.HabitSessions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.HabitViewHolder;
import com.example.brandon.habitlogger.common.TimeDisplay;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * Created by Brandon on 2/18/2017.
 * Manager for session notifications.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionNotificationManager {

    Context context;
    PreferenceChecker preferenceChecker;
    HabitDatabase habitDatabase;
    SessionManager sessionManager;
    NotificationManager notificationManager;

    public SessionNotificationManager(Context context) {
        this.context = context;
        preferenceChecker = new PreferenceChecker(context);
        habitDatabase = new HabitDatabase(context);
        sessionManager = new SessionManager(context);
        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createSessionNotification(long habitId) {
        Habit habit = habitDatabase.getHabit(habitId);
        createSessionNotification(habit);
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
                    HabitViewHolder.getResourceIdForPauseButton(sessionManager.getIsPaused(habitId)));

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
                String ticker = String.format(
                        Locale.getDefault(),
                        context.getString(R.string.session_notification_ticker_format_string),
                        habit.getName()
                );

                builder.setTicker(ticker);
            }

            Intent resultIntent = new Intent(context, SessionActivity.class);
            resultIntent.putExtra("habit", (Serializable) habit);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    context, habitId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.setContentIntent(resultPendingIntent);

            notificationManager.notify(habitId, builder.build());

            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            if (sessionManager.getIsSessionActive(habitId) && preferenceChecker.doShowNotifications()) {
                                SessionEntry entry = sessionManager.getSession(habitId);
                                String timeDisplayText = TimeDisplay.getDisplay(entry.getDuration());

                                RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                                        R.layout.notification_layout);
                                remoteViews.setTextViewText(R.id.active_habit_time, timeDisplayText);
                                remoteViews.setTextViewText(R.id.time_started, entry.getStartTimeAsString("h:mm a"));
                                remoteViews.setInt(R.id.session_pause_play, "setImageResource",
                                        HabitViewHolder.getResourceIdForPauseButton(sessionManager.getIsPaused(habitId)));

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

    public void cancel(long habitId) {
        notificationManager.cancel((int) habitId);
    }

    public static class SessionToggle extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent data) {
            int habitId = data.getIntExtra("habitId", -1);

            SessionNotificationManager manager = new SessionNotificationManager(context);
            boolean isPaused = manager.sessionManager.getIsPaused(habitId);
            manager.sessionManager.setPauseState(habitId, !isPaused);
            manager.createSessionNotification(habitId);
        }
    }

    public void createAllSessionNotifications() {
        List<SessionEntry> entries = sessionManager.getActiveSessionList();

        if (entries != null) {
            for (SessionEntry entry : entries) {
                createSessionNotification(entry.getHabitId());
            }
        }
    }

    public void clearAllNotifications() {
        notificationManager.cancelAll();
    }
}
