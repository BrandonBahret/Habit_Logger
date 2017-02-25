package com.example.brandon.habitlogger.HabitDatabase.DataModels;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the habit object.
 */

@SuppressWarnings("WeakerAccess")
public class Habit implements Serializable, Parcelable {
    @NonNull private String name;
    @NonNull private HabitCategory category;
    @Nullable private String description;
    @Nullable private String iconResId;
    @Nullable private SessionEntry[] entries;

    private int isArchived = 0;
    private long databaseId = -1;

    public Habit(@NonNull String name, @NonNull HabitCategory category) {
        this.name = name;
        this.category = category;
    }

    public Habit(@NonNull String name, @Nullable String description, @NonNull HabitCategory category,
                 @Nullable String iconResId, @Nullable SessionEntry[] entries) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.iconResId = iconResId;
        this.entries = entries;
    }

    public Habit(@NonNull String name, @Nullable String description, @NonNull HabitCategory category,
                 @Nullable String iconResId, @Nullable SessionEntry[] entries,
                 int isArchived, long databaseId) {

        this.name = name;
        this.category = category;
        this.description = description;
        this.iconResId = iconResId;
        this.entries = entries;
        this.isArchived = isArchived;
        this.databaseId = databaseId;
    }

    public Habit(Parcel in) {
        Habit habit = (Habit) in.readSerializable();

        this.name = habit.name;
        this.description = habit.description;
        this.category = habit.category;
        this.entries = habit.entries;
        this.iconResId = habit.iconResId;
        this.databaseId = habit.databaseId;
        this.isArchived = habit.isArchived;
    }

    public Habit(Context context) {
        this.name = "";
        this.description = "";
        this.category = HabitCategory.getUncategorizedCategory(context);
        this.iconResId = "";
        this.databaseId = -1;
        this.isArchived = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(this);
    }

    public static final Creator<Habit> CREATOR = new Creator<Habit>() {
        @Override
        public Habit createFromParcel(Parcel in) {
            return new Habit(in);
        }

        @Override
        public Habit[] newArray(int size) {
            return new Habit[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Habit) {
            Habit compare = (Habit) obj;

            SessionEntry[] compareEntries = compare.getEntries();
            boolean entriesEqual =
                    !((this.entries != null) && (compare.getEntries() != null)) ||
                            Arrays.equals(compareEntries, getEntries());

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

    /**
     * @param CSV The CSV form of a habit
     * @return a habit object created from the csv
     */
    @Nullable
    public static Habit fromCSV(String CSV) {

        Habit habit = null;

        try {
            CSVReader reader = new CSVReader(new StringReader(CSV));

            reader.readNext(); // Skip line
            String[] habitArray = reader.readNext();

            int isArchived = Boolean.parseBoolean(habitArray[0]) ? 1 : 0;
            String habitName = habitArray[1];
            String habitDescription = habitArray[2];

            String categoryName = habitArray[3];
            String categoryColor = habitArray[4];

            String habitIconResId = habitArray[5];

            int numberOfEntries = Integer.parseInt(habitArray[6]);

            reader.readNext(); // Skip line
            reader.readNext(); // Skip line

            SessionEntry entries[] = new SessionEntry[numberOfEntries];

            for (int entryIndex = 0; entryIndex < numberOfEntries; entryIndex++) {
                String[] entryArray = reader.readNext();
                long entryStartTime = Long.parseLong(entryArray[0]);
                long entryDuration = Long.parseLong(entryArray[1]);
                String comment = entryArray[2];

                entries[entryIndex] = new SessionEntry(entryStartTime, entryDuration, comment);
            }

            HabitCategory category = new HabitCategory(categoryColor, categoryName);
            habit = new Habit(habitName, habitDescription, category,
                    habitIconResId, entries, isArchived, -1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return habit;
    }

    public static Comparator<Habit> CategoryNameComparator = new Comparator<Habit>() {
        @Override
        public int compare(Habit itemOne, Habit itemTwo) {
            return itemOne.getCategory().getName().compareTo(itemTwo.getCategory().getName());
        }
    };

    public String toString() {
        int entriesLength = 0;
        if (getEntries() != null) {
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
    public String toCSV() {
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
        if (entries != null) {
            for (SessionEntry eachEntry : entries) {
                String appendEntry = String.format(Locale.US, entryFormat,
                        eachEntry.getStartTime(), eachEntry.getDuration(), eachEntry.getNote());

                csv.append(appendEntry);
            }
        }

        return csv.toString();
    }

    /**
     * @return Get the number of entries
     */
    public long getEntriesLength() {
        if (getEntries() != null) {
            return getEntries().length;
        }
        else {
            return 0;
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
    @NonNull
    public HabitCategory getCategory() {
        return category;
    }

    public int getColor() {
        return getIsArchived() ? 0xFFCCCCCC : category.getColorAsInt();
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
    public void setIsArchived(boolean state) {
        this.isArchived = state ? 1 : 0;
    }

    /**
     * @return True if archived, else false.
     */
    public boolean getIsArchived() {
        return (this.isArchived == 1);
    }
}
