package com.example.brandon.habitlogger.data.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the habit object.
 */

@SuppressWarnings("WeakerAccess")
public class Habit implements Serializable, Parcelable {

    //region (Member attributes)
    @NonNull private String mName;
    @NonNull private HabitCategory mCategory;
    @NonNull private String mDescription;
    @Nullable private String mIconResId;
    @Nullable private SessionEntryCollection mSessionEntries;
    private long mDatabaseId = -1;
    private boolean mIsArchived = false;
    //endregion

    //region (Static Helper Interfaces)
    public static Comparator<Habit> ICompareCategoryName = new Comparator<Habit>() {
        @Override
        public int compare(Habit itemOne, Habit itemTwo) {
            return itemOne.getCategory().getName().compareTo(itemTwo.getCategory().getName());
        }
    };

    public static Comparator<Habit> ICompareDuration = new Comparator<Habit>() {
        @Override
        public int compare(Habit itemOne, Habit itemTwo) {
            return Long.compare(itemTwo.getEntriesDuration(), itemOne.getEntriesDuration());
        }
    };

    public static Predicate<Habit> ICheckIfIsArchived = new Predicate<Habit>() {
        @Override
        public boolean apply(Habit item) {
            return item.getIsArchived();
        }
    };

    public static Predicate<Habit> ICheckIfIsNotArchived = new Predicate<Habit>() {
        @Override
        public boolean apply(Habit item) {
            return !item.getIsArchived();
        }
    };

    public static MyCollectionUtils.IGetKey<Habit, Long> IGetEntriesDuration = new MyCollectionUtils.IGetKey<Habit, Long>() {
        @Override
        public Long get(Habit habit) {
            return habit.getEntriesDuration();
        }
    };
    //endregion

    //region Constructors {}
    public Habit(@NonNull String name, @NonNull String description, @NonNull HabitCategory category,
                 @Nullable String iconResId, @NonNull SessionEntryCollection entries,
                 boolean isArchived, long databaseId) {

        mName = name;
        mCategory = category;
        mDescription = description;
        mIconResId = iconResId;
        mIsArchived = isArchived;
        mDatabaseId = databaseId;
        mSessionEntries = entries;
    }

    public Habit(@NonNull String name, @NonNull String description, @NonNull HabitCategory category,
                 @Nullable String iconResId, @NonNull SessionEntryCollection entries) {
        this(name, description, category, iconResId, entries, false, -1);
    }

    public Habit(@NonNull String name, @NonNull HabitCategory category) {
        this(name, "No Description", category, null, new SessionEntryCollection());
    }

    public Habit() {
        this("No name", new HabitCategory());
    }
    //endregion -- end --

    //region Methods responsible for making this object parcelable
    public Habit(Parcel in) {
        Habit habit = (Habit) in.readSerializable();
        Habit.copy(this, habit);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
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
    //endregion -- end --

    //region Methods responsible for enabling deep copying
    private static void copy(Habit dest, Habit source) {
        dest.mName = source.mName;
        dest.mDescription = source.mDescription;
        dest.mCategory = source.mCategory;
        dest.mIconResId = source.mIconResId;
        dest.mDatabaseId = source.mDatabaseId;
        dest.mIsArchived = source.mIsArchived;
        dest.mSessionEntries = new SessionEntryCollection(source.getEntries());
    }

    public static Habit duplicate(Habit habit) {
        Habit result = new Habit("", new HabitCategory(0, ""));
        Habit.copy(result, habit);
        return result;
    }
    //endregion

    //region Methods responsible for converting to and from CSV

    //region Create from CSV

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
            habit = createHabitFromStrings(habitArray);
            int numberOfEntries = Integer.parseInt(habitArray[6]);

            reader.readNext(); // Skip line
            reader.readNext(); // Skip line

            SessionEntryCollection entries = new SessionEntryCollection(numberOfEntries);
            for (int entryIndex = 0; entryIndex < numberOfEntries; entryIndex++) {
                SessionEntry entry = createSessionEntryFromStrings(reader.readNext());
                entries.add(entryIndex, entry);
            }
            habit.setEntries(entries);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return habit;
    }

    private static Habit createHabitFromStrings(String[] habitArray) {
        boolean isArchived = Boolean.parseBoolean(habitArray[0]);
        String habitName = habitArray[1];
        String habitDescription = habitArray[2];
        String categoryName = habitArray[3];
        String categoryColor = habitArray[4];
        String habitIconResId = habitArray[5];
        HabitCategory category = new HabitCategory(categoryColor, categoryName);

        return new Habit(habitName, habitDescription, category, habitIconResId, new SessionEntryCollection(), isArchived, -1);
    }

    private static SessionEntry createSessionEntryFromStrings(String[] entryArray) {
        long entryStartTime = Long.parseLong(entryArray[0]);
        long entryDuration = Long.parseLong(entryArray[1]);
        String entryNote = entryArray[2];

        return new SessionEntry(entryStartTime, entryDuration, entryNote);
    }
    //endregion -- end --

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

        SessionEntryCollection entries = getEntries();
        if (entries == null)
            return csv.toString();

        for (SessionEntry eachEntry : entries) {
            String appendEntry = String.format(Locale.US, entryFormat,
                    eachEntry.getStartingTime(), eachEntry.getDuration(), eachEntry.getNote());

            csv.append(appendEntry);
        }

        return csv.toString();
    }

    //endregion

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Habit) {
            Habit compare = (Habit) obj;

            return (compare.getIsArchived() == getIsArchived()) &&
                    compare.getName().equals(getName()) &&
                    String.valueOf(compare.getDescription()).equals(String.valueOf(getDescription())) &&
                    compare.getCategory().equals(getCategory()) &&
                    String.valueOf(compare.getIconResId()).equals(String.valueOf(getIconResId()));
        }

        else return false;
    }

    public SessionEntryCollection findEntriesWithDate(final long timestamp) {
        return mSessionEntries != null ? mSessionEntries.findEntriesWithDate(timestamp) : new SessionEntryCollection();
    }

    public boolean matchesQuery(String query) {
        Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
        return pattern.matcher(getName()).find() || pattern.matcher(getCategory().getName()).find();
    }

    //region Getters {}

    //region Get fields related to HabitCategory

    /**
     * @return The mCategory object associated with this habit.
     */
    @NonNull
    public HabitCategory getCategory() {
        return mCategory;
    }

    public int getColor() {
        return getIsArchived() ? 0xFFCCCCCC : mCategory.getColorAsInt();
    }
    //endregion -- end --

    //region Get fields related to SessionEntries
    @Nullable
    public SessionEntryCollection getEntries() {
        return mSessionEntries;
    }

    /**
     * @return Get the number of mSessionEntries
     */
    public long getEntriesLength() {
        return getEntries() != null ? getEntries().size() : 0;
    }

    public long getEntriesDuration() {
        return getEntries() != null ? getEntries().calculateDuration() : 0;
    }
    //endregion -- end --

    /**
     * @return The habit mName
     */
    @NonNull
    public String getName() {
        return mName;
    }

    /**
     * @return the habit mDescription
     */
    @NonNull
    public String getDescription() {
        return mDescription;
    }

    /**
     * @return the resource id for a drawable as a string. Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    @Nullable
    public String getIconResId() {
        return mIconResId;
    }

    /**
     * @return The row id of the Habit in the database, -1 when not available.
     */
    public long getDatabaseId() {
        return mDatabaseId;
    }

    /**
     * @return True if archived, else false.
     */
    public boolean getIsArchived() {
        return mIsArchived;
    }
    //endregion -- end --

    //region Setters {}

    /**
     * @param name the new habit mName
     */
    public Habit setName(@NonNull String name) {
        mName = name;
        return this;
    }

    /**
     * @param newDescription The habit mDescription
     */
    public Habit setDescription(@NonNull String newDescription) {
        mDescription = newDescription;
        return this;
    }

    /**
     * @param category The mCategory object associated with this habit.
     */
    public Habit setCategory(@NonNull HabitCategory category) {
        mCategory = category;
        return this;
    }

    /**
     * @param newEntries The new entry array to replace the old mSessionEntries.
     */
    public Habit setEntries(@NonNull SessionEntryCollection newEntries) {
        mSessionEntries = newEntries;
        return this;
    }

    /**
     * @param iconResId a stringified version of an resource id for a drawable.
     *                  Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    public Habit setIconResId(@NonNull String iconResId) {
        mIconResId = iconResId;
        return this;
    }

    /**
     * @param databaseId The row id of the Habit in the database, -1 when not available.
     */
    public Habit setDatabaseId(long databaseId) {
        mDatabaseId = databaseId;
        return this;
    }

    /**
     * @param state 1 if archived, else 0.
     */
    public Habit setIsArchived(boolean state) {
        mIsArchived = state;
        return this;
    }
    //endregion -- end --

}
