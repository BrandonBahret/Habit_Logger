package com.example.brandon.habitlogger.data.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.MyTimeUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.example.brandon.habitlogger.common.MyTimeUtils.getTimePortion;

/**
 * Created by Brandon on 10/26/2016.
 * This class defines the SessionEntry object.
 * This object is used to store the session data for the habits.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SessionEntry implements Serializable, Parcelable {

    //region (Member attributes)

    // Fields used for representing inactive sessions
    private long mStartTime;
    private long mDuration;
    @NonNull private String mNote;
    private long mDatabaseId = -1;
    private Habit mHabit;
    private long mHabitId;

    // Fields used for managing active sessions
    private long mTotalPauseTime;
    private long mLastTimePaused;
    private boolean mIsPaused = false;
    //endregion

    //region (Static Helper Interfaces)
    public static Comparator<SessionEntry> ICompareStartingTimes = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            return Long.compare(sessionOne.getStartingTime(), sessionTwo.getStartingTime());
        }
    };

    public static MyCollectionUtils.ICompareKey IKeyCompareStartingTime = new MyCollectionUtils.ICompareKey() {
        @Override
        public int compare(Object element, Object key) {
            long elementStartTime = ((SessionEntry) element).getStartingTime();
            return Long.compare(elementStartTime, (long) key);
        }
    };

    public static Comparator<SessionEntry> ICompareCategoryNames = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            return sessionOne.getCategoryName().compareTo(sessionTwo.getCategoryName());
        }
    };

    public static Comparator<? super SessionEntry> ICompareCategoryIds = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            return Long.compare(
                    sessionOne.getHabit().getCategory().getDatabaseId(),
                    sessionTwo.getHabit().getCategory().getDatabaseId()
            );
        }
    };

    public static Comparator<? super SessionEntry> ICompareHabitIds = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            return Long.compare(
                    sessionOne.getHabit().getDatabaseId(),
                    sessionTwo.getHabit().getDatabaseId()
            );
        }
    };


    public static Comparator<SessionEntry> ICompareHabitNames = new Comparator<SessionEntry>() {
        @Override
        public int compare(SessionEntry sessionOne, SessionEntry sessionTwo) {
            return sessionOne.mHabit.getName().compareTo(sessionTwo.mHabit.getName());
        }
    };

    public static MyCollectionUtils.IGetKey<SessionEntry, Long> IGetSessionStartDate = new MyCollectionUtils.IGetKey<SessionEntry, Long>() {
        @Override
        public Long get(SessionEntry sessionEntry) {
            return sessionEntry.getStartingTimeIgnoreTimeOfDay();
        }
    };

    public static MyCollectionUtils.IGetKey<SessionEntry, Long> IGetSessionDuration = new MyCollectionUtils.IGetKey<SessionEntry, Long>() {
        @Override
        public Long get(SessionEntry sessionEntry) {
            return sessionEntry.getDuration();
        }
    };
    //endregion

    //region Constructors {}
    public SessionEntry(long startTime, long duration, @NonNull String note) {
        mStartTime = startTime;
        mDuration = duration;
        mNote = note;
    }

    public SessionEntry() {
        this(-1, -1, "");
    }

    public SessionEntry(SessionEntry sessionEntry) {
        mStartTime = sessionEntry.getStartingTime();
        mDuration = sessionEntry.getDuration();
        mNote = sessionEntry.getNote();
        mHabitId = sessionEntry.getHabitId();
        mDatabaseId = sessionEntry.getDatabaseId();
        mHabit = sessionEntry.getHabit();
        mTotalPauseTime = sessionEntry.getTotalPauseTime();
        mLastTimePaused = sessionEntry.getLastTimePaused();
        mIsPaused = sessionEntry.getIsPaused();
    }
    //endregion

    //region Methods responsible for making this object parcelable
    public SessionEntry(Parcel in) {
        this((SessionEntry)in.readSerializable());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeSerializable(this);
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
    //endregion -- end --

    //region Methods to enable basic object operations
    public static void copy(SessionEntry dest, SessionEntry source) {
        dest.mStartTime = source.mStartTime;
        dest.mDuration = source.mDuration;
        dest.mNote = source.mNote;
        dest.mDatabaseId = source.mDatabaseId;
        dest.mIsPaused = source.mIsPaused;
        dest.mLastTimePaused = source.mLastTimePaused;
        dest.mTotalPauseTime = source.mTotalPauseTime;
        dest.mHabitId = source.mHabitId;
        dest.mHabit = source.mHabit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SessionEntry) {
            SessionEntry compare = (SessionEntry) obj;

            return (compare.getHabitId() == getHabitId()) &&
                    String.valueOf(compare.getNote()).equals(String.valueOf(getNote())) &&
                    compare.getDuration() == getDuration() &&
                    compare.getStartingTime() == getStartingTime();
        }
        else return false;
    }

    @Override
    public String toString() {
        String format = "{\n\tStarting Time: %s,\n\tDuration: %s\n\tNote: %s\n}\n";
        String startTimeString = MyTimeUtils.stringifyTimestamp(getStartingTime(), "MMMM/dd/yyyy");
        return String.format(Locale.US, format, startTimeString, stringifyDuration(), getNote());
    }
    //endregion -- end --

    public boolean matchesQuery(String query) {
        return Pattern.compile(query, Pattern.CASE_INSENSITIVE | Pattern.LITERAL).matcher(getNote()).find();
    }

    //region Static helper methods for formatting durations into readable strings
    public static String stringifyDuration(long duration) {
        Integer[] timeComponents = getTimePortion(duration);
        int hours = timeComponents[0];
        int minutes = timeComponents[1];
        int seconds = timeComponents[2];

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String stringifyDuration() {
        return stringifyDuration(getDuration());
    }

    public String stringifyStartingTime(String dateFormat) {
        return MyTimeUtils.stringifyTimestamp(getStartingTime(), dateFormat);
    }
    //endregion

    //region Methods responsible for setting fields in the timestamp
    public void setStartingHour(int hour) {
        mStartTime = MyTimeUtils.setTimestampField(getStartingTime(), Calendar.HOUR_OF_DAY, hour);
    }

    public void setStartingMinute(int minute) {
        mStartTime = MyTimeUtils.setTimestampField(getStartingTime(), Calendar.MINUTE, minute);
    }
    //endregion -- end --

    //region Methods responsible for getting fields from the timestamp
    public int getStartingTimeMinutes() {
        return MyTimeUtils.getTimestampField(getStartingTime(), Calendar.MINUTE);
    }

    public int getStartingTimeHours() {
        return MyTimeUtils.getTimestampField(getStartingTime(), Calendar.HOUR_OF_DAY);
    }

    public int getStartingTimeDayOfMonth() {
        return MyTimeUtils.getTimestampField(getStartingTime(), Calendar.DAY_OF_MONTH);
    }

    public int getStartingTimeMonth() {
        return MyTimeUtils.getTimestampField(getStartingTime(), Calendar.MONTH);
    }

    public int getStartingTimeYear() {
        return MyTimeUtils.getTimestampField(getStartingTime(), Calendar.YEAR);
    }

    /**
     * @return The time portion of the timestamp.
     * Ex: (Jan/20/2017 5:60 PM) returns 5:60 PM in millis
     */
    public long getStartingTimePortion() {

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getStartingTime());

        long hours = c.get(Calendar.HOUR_OF_DAY) * DateUtils.HOUR_IN_MILLIS;
        long minutes = c.get(Calendar.MINUTE) * DateUtils.MINUTE_IN_MILLIS;
        long seconds = c.get(Calendar.SECOND) * DateUtils.SECOND_IN_MILLIS;

        return hours + minutes + seconds;
    }
    //endregion

    //region Setters {}
    public void setStartingTime(long startTime) {
        mStartTime = startTime;
    }

    public SessionEntry setLastTimePaused(long milliseconds) {
        mLastTimePaused = milliseconds;
        return this;
    }

    public SessionEntry setTotalPauseTime(long milliseconds) {
        mTotalPauseTime = milliseconds;
        return this;
    }

    public SessionEntry setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    public SessionEntry setNote(@NonNull String note) {
        mNote = note;
        return this;
    }

    public SessionEntry setDatabaseId(long databaseId) {
        mDatabaseId = databaseId;
        return this;
    }

    public SessionEntry setIsPaused(boolean isPaused) {
        mIsPaused = isPaused;
        return this;
    }

    public SessionEntry setHabit(Habit habit) {
        mHabit = habit;
        setHabitId(habit.getDatabaseId());
        return this;
    }

    public SessionEntry setHabitId(long habitId) {
        mHabitId = habitId;
        return this;
    }
    //endregion

    //region Getters {}
    public long getStartingTime() {
        return mStartTime;
    }

    public long getStartingTimeIgnoreTimeOfDay() {
        return MyTimeUtils.setTimePortion(getStartingTime(), true, 0, 0, 0, 0);
    }

    public long getLastTimePaused() {
        return mLastTimePaused;
    }

    public long getTotalPauseTime() {
        return mTotalPauseTime;
    }

    public long getHabitId() {
        return mHabitId;
    }

    public String getNote() {
        return mNote;
    }

    public long getDuration() {
        return mDuration;
    }

    public long getDatabaseId() {
        return mDatabaseId;
    }

    public String getCategoryName() {
        return mHabit.getCategory().getName();
    }

    public boolean getIsPaused() {
        return mIsPaused;
    }

    public Habit getHabit() {
        return mHabit;
    }
    //endregion

}
