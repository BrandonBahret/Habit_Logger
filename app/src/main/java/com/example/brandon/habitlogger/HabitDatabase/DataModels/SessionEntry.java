package com.example.brandon.habitlogger.HabitDatabase.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

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
    private String mName = "NAME_NOT_SET";
    private boolean mIsPaused = false;
    private long lastTimePaused;
    private long totalPauseTime;
    @NonNull private String note;
    private String categoryName;

    private long habitId = -1;
    private long databaseId = -1;

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
        this.habitId = entry.habitId;
        this.mName = entry.mName;
        this.categoryName = entry.categoryName;
        this.mIsPaused = entry.mIsPaused;
        this.lastTimePaused = entry.lastTimePaused;
        this.totalPauseTime = entry.totalPauseTime;
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
            if (sessionOne.categoryName == null || sessionTwo.categoryName == null) {
                throw new IllegalArgumentException("SessionEntry.CategoryComparator requires entries to have categories set.");
            }
            return sessionOne.categoryName.compareTo(sessionTwo.categoryName);
        }
    };

    public static Comparator<SessionEntry> Alphabetical = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            return sessionOne.getName().compareTo(sessionTwo.getName());
        }
    };


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SessionEntry) {
            SessionEntry compare = (SessionEntry) obj;

            return compare.getHabitId().equals(getHabitId()) &&
                    String.valueOf(compare.getNote()).equals(String.valueOf(getNote())) &&
                    compare.getDuration() == getDuration() &&
                    compare.getStartTime() == getStartTime();
        }
        else {
            return false;
        }
    }

    public String toString() {
        String format = "{\n\tStarting Time: %d,\n\tDuration: %d\n\tNote: %s\n}\n";
        return String.format(Locale.US, format, getStartTime(), getDuration(), getNote());
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
     * @param habitId The row id of the habitd associated with this entry.
     */
    public void setHabitId(Long habitId) {
        this.habitId = habitId;
    }

    /**
     * @return The row id of the habitd associated with this entry.
     */
    public Long getHabitId() {
        return habitId;
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

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getCategoryName(){
        return this.categoryName;
    }

    public String getName() {
        return this.mName;
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

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

}
