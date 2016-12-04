package com.example.brandon.habitlogger.HabitDatabase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the habit object.
 */

public class Habit implements Serializable{

    @NonNull private String name;
    @NonNull private String description;
    @NonNull private HabitCategory category;
    @Nullable private SessionEntry[] entries;
    @NonNull private String iconResId;
    private int isArchived = 0;

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

            SessionEntry[] compareEntries = compare.getEntries();
            boolean entriesEqual = !((this.entries != null) && (compare.getEntries() != null)) || Arrays.equals(compareEntries, getEntries());

            return (compare.getIsArchived() == getIsArchived()) &&
                    compare.getName().equals(getName()) &&
                    String.valueOf(compare.getDescription()).equals(String.valueOf(getDescription())) &&
                    compare.getCategory().equals(getCategory()) &&
                    String.valueOf(compare.getIconResId()).equals(String.valueOf(getIconResId())) &&
                    entriesEqual;
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

        String format = "%s{\n\tDescription: %s\n\tCategory: {\n\t\tName: %s,\n\t\tColor: %s\n\t}\n\tIconResId: %s\n\t" +
                "Number of entries: %d\n\tIsArchived: %b\n}\n";
        return String.format(Locale.US, format, getName(), getDescription(), category.getName(),
                getCategory().getColor(), getIconResId(), entriesLength, getIsArchived());
    }

    /**
     * @return A csv form of the habit
     */
    public String toCSV(){
        StringBuilder csv = new StringBuilder();

        String habitFormat = "ARCHIVED,NAME,DESCRIPTION,CATEGORY_NAME,CATEGORY_COLOR,ICON_ID,NUMBER_OF_ENTRIES\n" +
                "%b,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d\n" +
                "\n";

        String habit = String.format(Locale.US, habitFormat,
                getIsArchived(), getName(), getDescription(), getCategory().getName(),
                getCategory().getColor(), getIconResId(), getEntriesLength());

        csv.append(habit);
        csv.append("START_TIME,DURATION,COMMENT\n");

        String entryFormat = "%d,%d,\"%s\"\n";

        SessionEntry entries[] = getEntries();
        if(entries != null) {
            for (SessionEntry eachEntry : entries) {
                String appendEntry = String.format(Locale.US, entryFormat,
                        eachEntry.getStartTime(), eachEntry.getDuration(), eachEntry.getNote());

                csv.append(appendEntry);
            }
        }

        return csv.toString();
    }

    /**
     * @param CSV The CSV form of a habit
     * @return a habit object created from the csv
     */
    @Nullable
    public static Habit fromCSV(String CSV){

        Habit habit = null;

        try {
            CSVReader reader = new CSVReader(new StringReader(CSV));

            reader.readNext(); // Skip line
            String[] habitArray = reader.readNext();

            boolean isArchived = Boolean.parseBoolean(habitArray[0]);
            String habitName = habitArray[1];
            String habitDescription = habitArray[2];

            String categoryName = habitArray[3];
            String categoryColor = habitArray[4];

            String habitIconResId = habitArray[5];

            int numberOfEntries = Integer.parseInt(habitArray[6]);

            reader.readNext(); // Skip line
            reader.readNext(); // Skip line

            SessionEntry entries[] = new SessionEntry[numberOfEntries];

            for(int entryIndex = 0; entryIndex < numberOfEntries; entryIndex++){
                String[] entryArray = reader.readNext();
                long entryStartTime = Long.parseLong(entryArray[0]);
                long entryDuration = Long.parseLong(entryArray[1]);
                String comment = entryArray[2];

                entries[entryIndex] = new SessionEntry(entryStartTime, entryDuration, comment);
            }

            habit = new Habit(habitName, habitDescription, new HabitCategory(categoryColor, categoryName), entries, habitIconResId);
            habit.setIsArchived(isArchived);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return habit;
    }

    /**
     * @return Get the number of entries
     */
    public long getEntriesLength(){
        SessionEntry[] entries = getEntries();
        if(entries != null){
            return entries.length;
        }
        else{
            return 0;
        }
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
    public void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    /**
     * @param state 1 if archived, else 0.
     */
    public void setIsArchived(boolean state){
        this.isArchived = state ? 1 : 0;
    }

    /**
     * @return True if archived, else false.
     */
    public boolean getIsArchived(){
        return (this.isArchived == 1);
    }

}
