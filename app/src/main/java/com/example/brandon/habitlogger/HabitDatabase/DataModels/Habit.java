package com.example.brandon.habitlogger.HabitDatabase.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the habit object.
 */

@SuppressWarnings("WeakerAccess")
public class Habit implements Serializable, Parcelable {

    //region (Member attributes)
    @NonNull private String mName;
    @NonNull private HabitCategory mCategory;
    @Nullable private String mDescription;
    @Nullable private String mIconResId;
    @Nullable private List<SessionEntry> mEntries;
    private long mEntriesDuration;

    private int mIsArchived = 0;
    private long mDatabaseId = -1;
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
    //endregion

    //region Constructors {}
    public Habit(@NonNull String name, @NonNull HabitCategory category) {
        this.mName = name;
        this.mCategory = category;
        mIconResId = "none";
    }

    public Habit(@NonNull String name, @Nullable String description, @NonNull HabitCategory category,
                 @Nullable String iconResId, List<SessionEntry> entries) {
        this.mName = name;
        this.mDescription = description;
        this.mCategory = category;
        this.mIconResId = iconResId;
        setEntries(entries);
    }

    public Habit(@NonNull String name, @Nullable String description, @NonNull HabitCategory category,
                 @Nullable String iconResId, List<SessionEntry> entries,
                 int isArchived, long databaseId) {

        this.mName = name;
        this.mCategory = category;
        this.mDescription = description;
        this.mIconResId = iconResId;
        this.mIsArchived = isArchived;
        this.mDatabaseId = databaseId;
        setEntries(entries);
    }

    public Habit(Parcel in) {
        Habit habit = in.readParcelable(Habit.class.getClassLoader());
        Habit.copy(this, habit);
    }
    //endregion

    //region Methods responsible for making this object parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this, flags);
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
    //endregion

    //region Methods responsible for enabling deep copying
    private static void copy(Habit dest, Habit source) {
        dest.mName = source.mName;
        dest.mDescription = source.mDescription;
        dest.mCategory = source.mCategory;
        dest.mIconResId = source.mIconResId;
        dest.mDatabaseId = source.mDatabaseId;
        dest.mIsArchived = source.mIsArchived;

        if (source.getEntries() != null) {
            dest.mEntries = new ArrayList<>(source.getEntries());
            dest.mEntriesDuration = source.calculateEntriesDurationSum();
        }
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

            List<SessionEntry> entries = new ArrayList<>(numberOfEntries);
            for (int entryIndex = 0; entryIndex < numberOfEntries; entryIndex++) {
                SessionEntry entry = createSessionEntryFromStrings(reader.readNext());
                entries.add(entryIndex,  entry);
            }
            habit.setEntries(entries);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return habit;
    }

    private static Habit createHabitFromStrings(String[] habitArray){
        int isArchived = Boolean.parseBoolean(habitArray[0]) ? 1 : 0;
        String habitName = habitArray[1];
        String habitDescription = habitArray[2];
        String categoryName = habitArray[3];
        String categoryColor = habitArray[4];
        String habitIconResId = habitArray[5];
        HabitCategory category = new HabitCategory(categoryColor, categoryName);

        return new Habit(habitName, habitDescription, category, habitIconResId, null, isArchived, -1);
    }

    private static SessionEntry createSessionEntryFromStrings(String[] entryArray){
        long entryStartTime = Long.parseLong(entryArray[0]);
        long entryDuration = Long.parseLong(entryArray[1]);
        String entryNote = entryArray[2];

        return new SessionEntry(entryStartTime, entryDuration, entryNote);
    }
    //endregion

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

        List<SessionEntry> entries = getEntries();
        if (entries != null) {
            for (SessionEntry eachEntry : entries) {
                String appendEntry = String.format(Locale.US, entryFormat,
                        eachEntry.getStartingTime(), eachEntry.getDuration(), eachEntry.getNote());

                csv.append(appendEntry);
            }
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

    private long calculateEntriesDurationSum() {
        return (long) MyCollectionUtils.sum(mEntries, SessionEntry.IGetSessionDuration);
    }

    @Nullable
    public List<SessionEntry> filterEntriesForDate(final long timestamp) {
        if(mEntries != null) {
            List<SessionEntry> entries = new ArrayList<>(mEntries);

            MyCollectionUtils.filter(entries, new Predicate<SessionEntry>() {
                @Override
                public boolean apply(SessionEntry sessionEntry) {
                    return !(sessionEntry.getStartingTimeIgnoreTimeOfDay() == timestamp);
                }
            });

            return entries;
        }

        return null;
    }

    //region Getters {}
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
    @Nullable
    public String getDescription() {
        return mDescription;
    }

    /**
     * @return The mCategory object associated with this habit.
     */
    @NonNull
    public HabitCategory getCategory() {
        return mCategory;
    }

    //region Get fields related to SessionEntries
    @Nullable
    public List<SessionEntry> getEntries() {
        return mEntries != null ? mEntries : null;
    }

    /**
     * @return Get the number of mEntries
     */
    public long getEntriesLength() {
        return getEntries() != null ? getEntries().size() : 0;
    }

    public long getEntriesDuration() {
        return this.mEntriesDuration;
    }

    public static MyCollectionUtils.IGetKey<Habit, Long> IGetEntriesDuration = new MyCollectionUtils.IGetKey<Habit, Long>() {
        @Override
        public Long get(Habit habit) {
            return habit.getEntriesDuration();
        }
    };
    //endregion

    /**
     * @return the resource id for a drawable as a string. Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    @Nullable
    public String getIconResId() {
        return mIconResId;
    }

    public int getColor() {
        return getIsArchived() ? 0xFFCCCCCC : mCategory.getColorAsInt();
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
        return (this.mIsArchived == 1);
    }
    //endregion

    //region Setters {}
    /**
     * @param name the new habit mName
     */
    public Habit setName(@NonNull String name) {
        this.mName = name;
        return this;
    }

    /**
     * @param newDescription The habit mDescription
     */
    public Habit setDescription(@NonNull String newDescription) {
        this.mDescription = newDescription;
        return this;
    }

    /**
     * @param category The mCategory object associated with this habit.
     */
    public Habit setCategory(@NonNull HabitCategory category) {
        this.mCategory = category;
        return this;
    }

    /**
     * @param newEntries The new entry array to replace the old mEntries.
     */
    public Habit setEntries(List<SessionEntry> newEntries) {
        if (newEntries != null) {
            this.mEntries = newEntries;
            this.mEntriesDuration = calculateEntriesDurationSum();
        }

        return this;
    }

    /**
     * @param iconResId a stringified version of an resource id for a drawable.
     *                  Ex: R.id.ic_launcher becomes "ic_launcher"
     */
    public Habit setIconResId(@NonNull String iconResId) {
        this.mIconResId = iconResId;
        return this;
    }

    /**
     * @param databaseId The row id of the Habit in the database, -1 when not available.
     */
    public Habit setDatabaseId(long databaseId) {
        this.mDatabaseId = databaseId;
        return this;
    }

    /**
     * @param state 1 if archived, else 0.
     */
    public Habit setIsArchived(boolean state) {
        this.mIsArchived = state ? 1 : 0;
        return this;
    }
    //endregion

}
