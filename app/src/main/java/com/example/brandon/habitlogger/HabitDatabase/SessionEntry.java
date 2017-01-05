package com.example.brandon.habitlogger.HabitDatabase;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This class defines the SessionEntry object.
 * This object is used to store the session data for the habits.
 */

public class SessionEntry implements Serializable{

    private long startTime, duration;
    private String name = "NAME_NOT_SET";
    private boolean isPaused = false;
    @NonNull private String note;
    private long habitId = -1;

    private long databaseId = -1;

    /**
     * @param startTime a time in milliseconds for the time the session was started.
     * @param duration a time in milliseconds for the length of the session.
     * @param note an optional note to be associated with the session.
     */
    public SessionEntry(long startTime, long duration, @NonNull String note){
        this.startTime = startTime;
        this.duration  = duration;
        this.note      = note;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SessionEntry) {
            SessionEntry compare = (SessionEntry) obj;

            return compare.getHabitId().equals(getHabitId()) &&
                    String.valueOf(compare.getNote()).equals(String.valueOf(getNote())) &&
                    compare.getDuration() == getDuration() &&
                    compare.getStartTime() == getStartTime();
        }
        else{
            return false;
        }
    }

    public String toString(){
        String format = "{\n\tStarting Time: %d,\n\tDuration: %d\n\tNote: %s\n}\n";
        return String.format(Locale.US, format, getStartTime(), getDuration(), getNote());
    }

    /**
     * @param startTime a time in milliseconds for the time the session was started.
     */
    public void setStartTime(long startTime){
        this.startTime = startTime;
    }

    /**
     * @return a time in milliseconds for the time the session was started.
     */
    public long getStartTime(){
        return this.startTime;
    }

    /**
     * @param duration a time in milliseconds for the length of the session.
     */
    public void setDuration(long duration){
        this.duration = duration;
    }

    /**
     * @return a time in milliseconds for the length of the session.
     */
    public long getDuration(){
        return this.duration;
    }

    /**
     * @param note an optional note to be associated with the session.
     */
    public void setNote(@NonNull String note){
        this.note = note;
    }

    /**
     * @return Get the note for this entry, this can potentially be a null value.
     */
    @NonNull
    public String getNote(){
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
    protected void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    /**
     * @return The row id of the entry object in the database
     */
    public long getDatabaseId() {
        return databaseId;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setIsPaused(boolean isPaused){
        this.isPaused = isPaused;
    }

    public boolean getIsPaused(){
        return this.isPaused;
    }

}
