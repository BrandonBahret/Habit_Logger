package com.example.brandon.habitlogger.HabitDatabase;

import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This class defines the SessionEntry object.
 * This object is used to store the session data for the habits.
 */

public class SessionEntry {

    private long startTime, duration;
    private String note = null;

    private @Nullable Long databaseId = null;
    private @Nullable Long habitId = null;

    /**
     * @param startTime a time in milliseconds for the time the session was started.
     * @param duration a time in milliseconds for the length of the session.
     * @param note an optional note to be associated with the session.
     */
    public SessionEntry(long startTime, long duration, @Nullable String note){
        setStartTime(startTime);
        setDuration(duration);
        setNote(note);
    }

    public String toString(){
        String format = "{\n\tStarting Time: %d,\n\tDuration: %d\n\tNote: %s\n}";
        return String.format(Locale.US, format, getStartTime(), getDuration(), getNote());
    }

    /**
     * @param startTime a time in milliseconds for the time the session was started.
     */
    public void setStartTime(long startTime){
        this.startTime = startTime;
    }

    /**
     * @param duration a time in milliseconds for the length of the session.
     */
    public void setDuration(long duration){
        this.duration = duration;
    }

    /**
     * @param note an optional note to be associated with the session.
     */
    public void setNote(String note){
        if(note != null){
            this.note = note;
        }
    }

    /**
     * @return a time in milliseconds for the time the session was started.
     */
    public long getStartTime(){
        return this.startTime;
    }

    /**
     * @return a time in milliseconds for the length of the session.
     */
    public long getDuration(){
        return this.duration;
    }

    /**
     * @return Get the note for this entry, this can potentially be a null value.
     */
    @Nullable
    public String getNote(){
        return this.note;
    }

    @Nullable
    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(@Nullable Long databaseId) {
        this.databaseId = databaseId;
    }

    @Nullable
    public Long getHabitId() {
        return habitId;
    }

    public void setHabitId(@Nullable Long habitId) {
        this.habitId = habitId;
    }
}
