package com.example.brandon.habitlogger.data.HabitSessions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.HabitViewHolder;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity.SessionActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 2/18/2017.
 * Manager for session notifications.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class SessionNotificationManager {

    //region (Member attributes)
    private Context mContext;
    private SessionManager mSessionManager;
    private NotificationManager mNotificationManager;
    private PreferenceChecker mPreferenceChecker;

    static List<Long> notificationIds = new ArrayList<>();
    //endregion

    public SessionNotificationManager(Context context) {
        mContext = context;
        mSessionManager = new SessionManager(context);
        mPreferenceChecker = new PreferenceChecker(context);
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    //region Methods responsible for building notifications
    private RemoteViews createRemoteViews(Habit habit) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);
        remoteViews.setInt(R.id.card_accent, "setBackgroundColor", habit.getColor());
        remoteViews.setTextViewText(R.id.active_session_habit_name, habit.getName());

        final long habitId = habit.getDatabaseId();
        if (mSessionManager.getIsSessionActive(habitId)) {
            SessionEntry entry = mSessionManager.getSession(habitId);
            remoteViews.setTextViewText(R.id.active_habit_time, entry.stringifyDuration());
            remoteViews.setTextViewText(R.id.time_started, entry.stringifyStartingTime("h:mm a"));
            remoteViews.setOnClickPendingIntent(R.id.session_pause_play, getPendingIntentForSessionToggle(habit));
            remoteViews.setInt(
                    R.id.session_pause_play, "setImageResource",
                    HabitViewHolder.getResourceIdForPauseButton(entry.getIsPaused())
            );
        }

        return remoteViews;
    }

    private PendingIntent getPendingIntentForSessionToggle(Habit habit) {
        Intent resultIntent = new Intent(SessionToggle.ACTION_STRING);
        resultIntent.putExtra(SessionToggle.PARCELABLE_HABIT, (Parcelable) habit);
        return PendingIntent.getBroadcast(mContext, (int) habit.getDatabaseId(), resultIntent, 0);
    }

    private Notification createNotification(Habit habit) {
        return new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notification_icon_24dp)
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

    private PendingIntent getPendingIntentForSessionActivity(Habit habit) {
        Intent resultIntent = new Intent(mContext, SessionActivity.class);
        resultIntent.putExtra(SessionActivity.BundleKeys.SERIALIZED_HABIT, (Serializable) habit);
        return PendingIntent.getActivity
                (mContext, (int) habit.getDatabaseId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    //endregion -- end --

    //region [ ---- Methods responsible for managing notifications ---- ]

    //region Methods responsible for launching notifications
    public void launchSessionNotification(final Habit habit) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        final int habitId = (int) habit.getDatabaseId();

                        if (mSessionManager.getIsSessionActive(habitId) && mPreferenceChecker.doShowNotifications()) {
                            mNotificationManager.notify(habitId, createNotification(habit));

                            try {
                                Thread.sleep(1000);
                                this.run();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else mNotificationManager.cancel(habitId);
                    }
                }
        ).start();
    }

    public void launchNotificationsForAllActiveSessions() {
        for (SessionEntry entry : mSessionManager.getActiveSessionList())
            launchSessionNotification(entry.getHabit());
    }
    //endregion -- end --

    //region Methods responsible for canceling notifications
    public void cancel(int habitId) {
        removeNotificationFromList((long) habitId);
        mNotificationManager.cancel(habitId);
    }

    public void cancelAllNotifications() {
        mNotificationManager.cancelAll();
    }
    //endregion -- end --

    private void storeNotificationInList(Habit habit) {
        if (!notificationIds.contains(habit.getDatabaseId())) {
            notificationIds.add(habit.getDatabaseId());
            launchSessionNotification(habit);
        }
    }

    private void removeNotificationFromList(Long habitId) {
        if (notificationIds.contains(habitId)) {
            notificationIds.remove(habitId);
        }
    }

    public void updateNotification(Habit habit) {
        storeNotificationInList(habit);
        mNotificationManager.notify((int) habit.getDatabaseId(), createNotification(habit));
    }

    //endregion [ ---------------- end ---------------- ]

    public static class SessionToggle extends BroadcastReceiver {
        public static final String ACTION_STRING = "session_toggle_pressed";
        public static final String PARCELABLE_HABIT = "habit";

        @Override
        public void onReceive(Context context, Intent data) {
            Habit habit = data.getParcelableExtra(PARCELABLE_HABIT);

            SessionNotificationManager manager = new SessionNotificationManager(context);
            boolean isPaused = manager.mSessionManager.getIsPaused(habit.getDatabaseId());
            manager.mSessionManager.setPauseState(habit.getDatabaseId(), !isPaused);

            manager.updateNotification(habit);
        }
    }
}