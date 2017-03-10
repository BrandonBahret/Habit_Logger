package com.example.brandon.habitlogger.HabitDatabase.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.example.brandon.habitlogger.common.TimeDisplay;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This class defines the SessionEntry object.
 * This object is used to store the session data for the habits.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SessionEntry implements Serializable, Parcelable {
    private long mStartTime, mDuration;
    private boolean mIsPaused = false;
    private long lastTimePaused;
    private long totalPauseTime;
    private long databaseId = -1;
    @NonNull private String note;

    private Habit habit;
    private long habitId;

    /**
     * @param startTime a time in milliseconds for the time the session was started.
     * @param duration  a time in milliseconds for the getDatabaseLength of the session.
     * @param note      an optional note to be associated with the session.
     */
    public SessionEntry(long startTime, long duration, @NonNull String note) {
        this.mStartTime = startTime;
        this.mDuration = duration;
        this.note = note;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Creator<SessionEntry> CREATOR = new Creator<SessionEntry>() {
        @Override
        public SessionEntry createFromParcel(Parcel in) {
            return new SessionEntry(in);
        }

        @Override
        public SessionEntry[] newArray(int size) {
            return new SessionEntry[size];
        }
    };

    public SessionEntry(Parcel in) {
        SessionEntry entry = (SessionEntry) in.readSerializable();

        this.mStartTime = entry.mStartTime;
        this.mDuration = entry.mDuration;
        this.note = entry.note;
        this.databaseId = entry.databaseId;
        this.mIsPaused = entry.mIsPaused;
        this.lastTimePaused = entry.lastTimePaused;
        this.totalPauseTime = entry.totalPauseTime;
        this.habitId = entry.habitId;
        this.habit = entry.habit;
    }

    public static Comparator<SessionEntry> StartingTimeComparator = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            return Long.compare(sessionOne.getStartTime(), sessionTwo.getStartTime());
        }
    };

    public static Comparator<SessionEntry> CategoryComparator = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            if (sessionOne.habit == null || sessionTwo.habit == null) {
                throw new IllegalArgumentException("SessionEntry.CategoryComparator requires entries to have habit set.");
            }
            return sessionOne.getCategoryName().compareTo(sessionTwo.getCategoryName());
        }
    };

    public static Comparator<SessionEntry> Alphabetical = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            return sessionOne.habit.getName().compareTo(sessionTwo.habit.getName());
        }
    };


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SessionEntry) {
            SessionEntry compare = (SessionEntry) obj;

            return (compare.getHabitId() == this.getHabitId()) &&
                    String.valueOf(compare.getNote()).equals(String.valueOf(getNote())) &&
                    compare.getDuration() == getDuration() &&
                    compare.getStartTime() == getStartTime();
        }
        else {
            return false;
        }
    }

    public String toString() {
        String format = "{\n\tStarting Time: %s,\n\tDuration: %s\n\tNote: %s\n}\n";
        String startTimeString = getDate(getStartTime(), "MMMM/dd/yyyy");
        return String.format(Locale.US, format, startTimeString, getDurationAsString(), getNote());
    }

    /**
     * @param startTime a time in milliseconds for the time the session was started.
     */
    public void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    /**
     * @return a time in milliseconds for the time the session was started.
     */
    public long getStartTime() {
        return this.mStartTime;
    }

    public long getStartingTimeDate() {
        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(getStartTime());

        c.set(Calendar.AM_PM, 0);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }

    public int getStartingTimeHours() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getStartTime());

        return c.get(Calendar.HOUR_OF_DAY);
    }

    public int getStartingTimeMinutes() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getStartTime());

        return c.get(Calendar.MINUTE);
    }

    /**
     * @return The time portion of the timestamp.
     * Ex: (Jan/20/2017 5:60 PM) returns 5:60 PM in millis
     */
    public long getStartingTimePortion() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getStartTime());

        long hours = c.get(Calendar.HOUR_OF_DAY) * DateUtils.HOUR_IN_MILLIS;
        long minutes = c.get(Calendar.MINUTE) * DateUtils.MINUTE_IN_MILLIS;
        long seconds = c.get(Calendar.SECOND) * DateUtils.SECOND_IN_MILLIS;

        return hours + minutes + seconds;
    }

    public void setStartingHour(int hour) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getStartTime());

        c.set(Calendar.HOUR_OF_DAY, hour);

        long time = c.getTimeInMillis();
        setStartTime(time);
    }

    public void setStartingMinute(int minute) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getStartTime());

        c.set(Calendar.MINUTE, minute);

        long time = c.getTimeInMillis();
        setStartTime(time);
    }

    public long getLastTimePaused() {
        return lastTimePaused;
    }

    public void setLastTimePaused(long milliseconds) {
        lastTimePaused = milliseconds;
    }

    public long getTotalPauseTime() {
        return totalPauseTime;
    }

    public void setTotalPauseTime(long milliseconds) {
        totalPauseTime = milliseconds;
    }

    public long getHabitId() {
        return habitId;
    }

    /**
     * @param duration a time in milliseconds for the getDatabaseLength of the session.
     */
    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    /**
     * @return a time in milliseconds for the getDatabaseLength of the session.
     */
    public long getDuration() {
        return this.mDuration;
    }

    /**
     * @param note an optional note to be associated with the session.
     */
    public void setNote(@NonNull String note) {
        this.note = note;
    }

    /**
     * @return Get the note for this entry, this can potentially be a null value.
     */
    @NonNull
    public String getNote() {
        return this.note;
    }

    /**
     * @param databaseId The row id of the entry object in the database
     */
    public void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    /**
     * @return The row id of the entry object in the database
     */
    public long getDatabaseId() {
        return databaseId;
    }

    public String getCategoryName() {
        return this.habit.getCategory().getName();
    }

    public void setIsPaused(boolean isPaused) {
        this.mIsPaused = isPaused;
    }

    public boolean getIsPaused() {
        return this.mIsPaused;
    }

    public String getDurationAsString() {
        return TimeDisplay.getDisplay(getDuration());
    }

    public String getStartTimeAsString(String dateFormat) {
        return getDate(getStartTime(), dateFormat);
    }

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        return formatter.format(new Date(milliSeconds));
    }


    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
        setHabitId(habit.getDatabaseId());
    }

    public void setHabitId(long habitId) {
        this.habitId = habitId;
    }
}
