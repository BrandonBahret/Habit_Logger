package com.example.brandon.habitlogger.HabitDatabase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the habit object.
 */

public class Habit {

    @NonNull private String name;
    @NonNull private String description;
    @NonNull private HabitCategory category;
    @Nullable private SessionEntry[] entries;
    @NonNull private String iconResId;

    private long databaseId = -1;

    public Habit(@NonNull String name, @NonNull String description, @NonNull HabitCategory category,
          @Nullable SessionEntry[] entries, @NonNull String iconResId){

        this.name        = name;
        this.description = description;
        this.category    = category;
        this.entries     = entries;
        this.iconResId   = iconResId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Habit){
            Habit compare = (Habit)obj;

            return compare.getName().equals(getName()) &&
                    String.valueOf(compare.getDescription()).equals(String.valueOf(getDescription())) &&
                    compare.getCategory().equals(getCategory()) &&
                    String.valueOf(compare.getIconResId()).equals(String.valueOf(getIconResId()));
        }
        else {
            return false;
        }
    }

    public String toString(){
        int entriesLength = 0;
        if(getEntries() != null) {
            entriesLength = getEntries().length;
        }

        HabitCategory category = getCategory();

        String format = "%s{\n\tDescription: %s\n\tCategory: {\n\t\tName: %s,\n\t\tColor: %s\n\t}\n\tIconResId: %s\n\tNumber of entries: %d\n}\n";
        return String.format(Locale.US, format, getName(), getDescription(), category.getName(),
                getCategory().getColor(), getIconResId(), entriesLength);
    }

    /**
     * @return the resource id for a drawable as a string. Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    @NonNull
    public String getIconResId() {
        return iconResId;
    }

    /**
     * @param iconResId a stringified version of an resource id for a drawable.
     *                  Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    public void setIconResId(@NonNull String iconResId) {
        this.iconResId = iconResId;
    }

    /**
     * @return The habit name
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * @param name the new habit name
     */
    public void setName(@NonNull String name) {
        this.name = name;
    }

    /**
     * @return the habit description
     */
    @NonNull
    public String getDescription() {
        return description;
    }

    /**
     * @param newDescription The habit description
     */
    public void setDescription(@NonNull String newDescription) {
        this.description = newDescription;
    }

    /**
     * @return The category object associated with this habit.
     */
    @NonNull
    public HabitCategory getCategory() {
        return category;
    }

    /**
     * @param category The category object associated with this habit.
     */
    public void setCategory(@NonNull HabitCategory category) {
        this.category = category;
    }

    /**
     * @return all of the entries associated with the habit.
     */
    @Nullable
    public SessionEntry[] getEntries() {
        return entries;
    }

    /**
     * @param newEntries The new entry array to replace the old entries.
     */
    public void setEntries(@NonNull SessionEntry[] newEntries) {
        this.entries = newEntries;
    }

    /**
     * @return The row id of the Habit in the database, -1 when not available.
     */
    public long getDatabaseId() {
        return databaseId;
    }

    /**
     * @param databaseId The row id of the Habit in the database, -1 when not available.
     */
    protected void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

}
