package com.example.brandon.habitlogger.HabitDatabase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the habit object.
 */

public class Habit {

    private String name;
    @Nullable private String description;
    private HabitCategory category;
    @Nullable private SessionEntry[] entries;
    @Nullable private String iconResId;

    private long databaseId = -1;

    public Habit(String name, @Nullable String description, HabitCategory category,
          @Nullable SessionEntry[] entries, @Nullable String iconResId){

        this.name        = name;
        this.description = description;
        this.category    = category;
        this.entries     = entries;
        this.iconResId   = iconResId;
    }

    public String toString(){
        int entriesLength = 0;
        if(getEntries() != null) {
            entriesLength = getEntries().length;
        }

        HabitCategory category = getCategory();
        if(category != null) {
            String format = "%s{\n\tDescription: %s\n\tCategory: {\n\t\tName: %s,\n\t\tColor: %s\n\t}\n\tIconResId: %d\n\tNumber of entries: %d\n}\n";
            return String.format(Locale.US, format, getName(), getDescription(), category.getName(),
                    getCategory().getColor(), getIconResId(), entriesLength);
        }
        else {
            String format = "%s{\n\tDescription: %s\n\tIconResId: %d\n\tNumber of entries: %d\n}\n";
            return String.format(Locale.US, format, getName(), getDescription(), getIconResId(), entriesLength);
        }
    }

    /**
     * @return the resource id for a drawable as a string. Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    @Nullable
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
    public String getName() {
        return name;
    }

    /**
     * @param name the new habit name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the habit description
     */
    @Nullable
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
    public HabitCategory getCategory() {
        return category;
    }

    /**
     * @param category The category object associated with this habit.
     */
    public void setCategory(HabitCategory category) {
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
