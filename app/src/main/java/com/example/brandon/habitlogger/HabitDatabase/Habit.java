package com.example.brandon.habitlogger.HabitDatabase;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the habit object.
 */

public class Habit {

    private String name;
    @Nullable private String description;

    @Nullable private HabitCategory category;

    @Nullable private ArrayList<SessionEntry> entries;
    @Nullable private String iconResId;

    @Nullable private Long databaseId = null;

    public Habit(String name, @Nullable String description, @Nullable HabitCategory category,
          @Nullable ArrayList<SessionEntry> entries, @Nullable String iconResId){

        setName(name);
        setDescription(description);
        setCategory(category);
        setEntries(entries);
        setIconResId(iconResId);
    }

    public String toString(){
        int entriesLength = 0;
        if(getEntries() != null) {
            entriesLength = getEntries().size();
        }
        if(this.hasCategory()) {
            String format = "%s{\n\tDescription: %s\n\tCategory: {\n\t\tName: %s,\n\t\tColor: %s\n\t}\n\tIconResId: %d\n\tNumber of entries: %d\n}\n";
            return String.format(Locale.US, format, getName(), getDescription(), getCategory().getName(),
                    getCategory().getColor(), getIconResId(), entriesLength);
        }
        else{
            String format = "%s{\n\tDescription: %s\n\tIconResId: %d\n\tNumber of entries: %d\n}\n";
            return String.format(Locale.US, format, getName(), getDescription(), getIconResId(), entriesLength);
        }
    }

    public boolean hasCategory(){
        return getCategory() != null;
    }

    /**
     * @return the resource id for a drawable as a string. Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    @Nullable
    public String getIconResId() {
        return iconResId;
    }

    /**
     * @param iconResId a strinstringifiedgified version of an resource id for a drawable.
     *                  Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    public void setIconResId(String iconResId) {
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
     * @param description The habit description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The category object associated with this habit.
     */
    @Nullable
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
    public ArrayList<SessionEntry> getEntries() {
        return entries;
    }

    /**
     * @param entries The entry array to replace the old entries.
     */
    public void setEntries(ArrayList<SessionEntry> entries) {
        this.entries = entries;
    }

    @Nullable
    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(@Nullable Long databaseId) {
        this.databaseId = databaseId;
    }


    @Nullable
    public Long getCategoryDatabaseId(){
        HabitCategory category = getCategory();
        if(category != null){
            return getCategory().getDatabaseId();
        }

        return  null;
    }

    public void setCategoryDatabaseId(@Nullable Long databaseId){
        HabitCategory category = getCategory();
        if(category != null){
            getCategory().setDatabaseId(databaseId);
        }
    }
}
