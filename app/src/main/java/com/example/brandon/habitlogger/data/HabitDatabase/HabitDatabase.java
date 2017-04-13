package com.example.brandon.habitlogger.data.HabitDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.MyDatabaseUtils;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema.CategoriesTableSchema;
import com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema.DatabaseSchema;
import com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema.EntriesTableSchema;
import com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema.HabitsTableSchema;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema.HabitsTableSchema.HABIT_IS_ARCHIVED;

/**
 * Created by Brandon on 10/26/2016.
 * This is the class for managing the habit database
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class HabitDatabase {

    //region (Member attributes)
    public DatabaseSchema databaseHelper;
    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;
    //endregion

    //region Code responsible for providing an interface to the database

    public interface OnEntryChangedListener {
        void onEntryDeleted(SessionEntry removedEntry);

        void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry);

        void onEntriesReset(long habitId);
    }

    private static List<OnEntryChangedListener> onEntryChangedListeners = new ArrayList<>();

    private void notifyEntryDeleted(SessionEntry removedEntry) {
        for (OnEntryChangedListener listener : onEntryChangedListeners)
            listener.onEntryDeleted(removedEntry);
    }

    private void notifyEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry) {
        for (OnEntryChangedListener listener : onEntryChangedListeners)
            listener.onEntryUpdated(oldEntry, newEntry);
    }

    private void notifyEntriesReset(long habitId) {
        for (OnEntryChangedListener listener : onEntryChangedListeners)
            listener.onEntriesReset(habitId);
    }

    public static void addOnEntryChangedListener(OnEntryChangedListener listener) {
        onEntryChangedListeners.add(listener);
    }

    public static void removeOnEntryChangedListener(OnEntryChangedListener listener) {
        onEntryChangedListeners.remove(listener);
    }
    //endregion

    public HabitDatabase(Context context) {
        databaseHelper = new DatabaseSchema(context);
        mWritableDatabase = databaseHelper.getWritableDatabase();
        mReadableDatabase = databaseHelper.getReadableDatabase();
    }

    //region Methods responsible for sampling data from the database
    public List<CategoryDataSample> getAllData() {
        int size = getNumberOfCategories();
        List<CategoryDataSample> containers = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            long categoryId = getCategoryIdFromIndex(i);
            CategoryDataSample data = getCategoryDataSample(getCategory(categoryId), 0, Long.MAX_VALUE);
            containers.add(data);
        }

        return containers;
    }

    public HabitDataSample getHabitDataSample(long dateFrom, long dateTo) {
        List<CategoryDataSample> data = new ArrayList<>();
        for (HabitCategory category : getCategories())
            data.add(getCategoryDataSample(category, dateFrom, dateTo));

        return new HabitDataSample(data, dateFrom, dateTo);
    }

    public HabitDataSample getHabitDataSample(SessionEntriesSample entriesSample) {

        List<Long> habitIdsList = MyCollectionUtils.collect(entriesSample.getSessionEntries(), new MyCollectionUtils.IGetKey<SessionEntry, Long>() {
            @Override
            public Long get(SessionEntry entry) {
                return entry.getHabitId();
            }
        });

        Set<Long> habitIdsSet = new HashSet<>(habitIdsList);

        Set<Long> categoryIdSet = new HashSet<>();

        for (long habitId : habitIdsSet) {
            categoryIdSet.add(getHabitCategoryId(habitId));
        }

        long dateFrom = entriesSample.getDateFromTime();
        long dateTo = entriesSample.getDateToTime();

        List<CategoryDataSample> categoryDataSamples = new ArrayList<>();

        for (long categoryId : categoryIdSet) {
            List<Habit> habits = getHabits(categoryId);

            for (Habit habit : habits) {
                final long habitId = habit.getDatabaseId();
                List<SessionEntry> entries = new ArrayList<>(entriesSample.getSessionEntries());
                MyCollectionUtils.filter(entries, new Predicate<SessionEntry>() {
                    @Override
                    public boolean apply(SessionEntry sessionEntry) {
                        return sessionEntry.getHabitId() != habitId;
                    }
                });
                habit.setEntries(entries);
            }

            categoryDataSamples.add(new CategoryDataSample(getCategory(categoryId), habits, dateFrom, dateTo));
        }

        return new HabitDataSample(categoryDataSamples, dateFrom, dateTo);
    }

    public CategoryDataSample getCategoryDataSample(HabitCategory category, long dateFrom, long dateTo) {
        List<Habit> habits = getHabits(category.getDatabaseId());
        for (Habit habit : habits) {
            long habitId = habit.getDatabaseId();
            Set<Long> ids = findEntriesWithinTimeRange(habitId, dateFrom, dateTo);
            List<SessionEntry> entries = lookUpEntries(ids);
            habit.setEntries(entries);
        }

        return new CategoryDataSample(category, habits, dateFrom, dateTo);
    }

    public SessionEntriesSample getEntriesSample(long habitId, long dateFrom, long dateTo) {
        List<SessionEntry> entries = lookUpEntries(
                findEntriesWithinTimeRange(habitId, dateFrom, dateTo)
        );

        return new SessionEntriesSample(entries, dateFrom, dateTo);
    }
    //endregion

    //region Methods responsible for counting the number of rows in tables
    public int getNumberOfHabits(long categoryId) {
        return (int) MyDatabaseUtils.getNumberOfRows(
                mReadableDatabase, HabitsTableSchema.TABLE_NAME,
                HabitsTableSchema.HABIT_CATEGORY_ID, categoryId
        );
    }

    public int getNumberOfHabits() {
        return (int) MyDatabaseUtils.getNumberOfRows(
                mReadableDatabase, HabitsTableSchema.TABLE_NAME,
                HabitsTableSchema.HABIT_IS_ARCHIVED, 0
        );
    }

    public int getNumberOfArchivedHabits() {
        return (int) MyDatabaseUtils.getNumberOfRows(
                mReadableDatabase, HabitsTableSchema.TABLE_NAME,
                HabitsTableSchema.HABIT_IS_ARCHIVED, 1
        );
    }

    public int getNumberOfCategories() {
        return (int) MyDatabaseUtils.getNumberOfRows(mReadableDatabase, CategoriesTableSchema.TABLE_NAME);
    }

    public int getNumberOfEntries() {
        return (int) MyDatabaseUtils.getNumberOfRows(mReadableDatabase, EntriesTableSchema.TABLE_NAME);
    }

    public int getNumberOfEntries(long habitId) {
        return (int) MyDatabaseUtils.getNumberOfRows(mReadableDatabase, EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_HABIT_ID, habitId);
    }
    //endregion

    //region Methods responsible for fetching record ids

    /**
     * @param index         The index of the record to be looked up.
     * @param tableName     The table name of the table to search.
     * @param recordKey     The name of the row id.
     * @param whereClause   The where clause used to filter the table.
     * @param selectionArgs The args to the where clause.
     * @return The found row id, -1 if found nothing.
     */
    private long getRowIdByIndex(int index, String tableName, String recordKey,
                                 @Nullable String whereClause, @Nullable String selectionArgs[]) {

        Cursor c = mReadableDatabase.query(tableName, new String[]{recordKey}, whereClause,
                selectionArgs, null, null, null, null);

        long rowId = -1;
        if (c != null && c.moveToPosition(index)) {
            rowId = c.getLong(0);
            c.close();
        }

        return rowId;
    }

    /**
     * @param SQL       The SQL string to be executed
     * @param recordKey The string id for the column that holds row ids.
     * @return An array of row ids found by the sqlite query.
     */
    @Nullable
    private Set<Long> searchTableForIds(String SQL, String[] values, String recordKey) {

        Cursor c = mReadableDatabase.rawQuery(SQL, values);

        if (c != null) {
            Set<Long> recordIds = new HashSet<>(c.getCount());
            while (c.moveToNext()) {
                int recordIdIndex = c.getColumnIndex(recordKey);
                recordIds.add(c.getLong(recordIdIndex));
            }

            c.close();
            return recordIds;
        }

        return null;
    }
    //endregion

    //region [ ---- Methods for manipulating the habits table ---- ]

    //region Methods responsible for finding record ids

    /**
     * @param index The habit index to look up.
     * @return The unique habit id of the row.
     */
    public long getHabitIdFromIndex(long categoryId, int index) {
        return getRowIdByIndex(index, HabitsTableSchema.TABLE_NAME, HabitsTableSchema.HABIT_ID,
                HabitsTableSchema.HABIT_CATEGORY_ID + "=?", new String[]{String.valueOf(categoryId)});
    }

    /**
     * @param habit The object to look up in the database.
     * @return The row id of the object in the database if found, else -1.
     */
    public long getHabitIdFromObject(Habit habit) {
        long habitId = habit.getDatabaseId();

        if (habitId == -1) {
            long categoryId = getCategoryIdByObject(habit.getCategory());

            String whereClause = HabitsTableSchema.HABIT_NAME + "=? AND " +
                    HabitsTableSchema.HABIT_CATEGORY_ID + "=?";
            String selectionArgs[] = {habit.getName(), String.valueOf(categoryId)};

            habitId = getRowIdByIndex(0, HabitsTableSchema.TABLE_NAME, HabitsTableSchema.HABIT_ID,
                    whereClause, selectionArgs);
            habit.setDatabaseId(habitId);
        }

        return habitId;
    }

    public Set<Long> getHabitIds(long categoryId) {
        return searchTableForIds("SELECT " + HabitsTableSchema.HABIT_ID + " FROM " +
                        HabitsTableSchema.TABLE_NAME + " WHERE " +
                        HabitsTableSchema.HABIT_CATEGORY_ID + " =?",
                new String[]{String.valueOf(categoryId)}, HabitsTableSchema.HABIT_ID);
    }

    /**
     * @param name The name of the habit to look up.
     * @return The found habit ids.
     */
    public Set<Long> findHabitIdsByName(String name) {
        return searchTableForIds("SELECT " + HabitsTableSchema.HABIT_ID + " FROM " +
                        HabitsTableSchema.TABLE_NAME + " WHERE " +
                        HabitsTableSchema.HABIT_NAME + " LIKE  ?",

                new String[]{"%" + name + "%"}, HabitsTableSchema.HABIT_ID);
    }

    /**
     * @param name The name of the category to look up.
     * @return The found category ids as an array.
     */
    public Set<Long> findCategoryIdsByName(String name) {
        return searchTableForIds(
                "SELECT " + CategoriesTableSchema.CATEGORY_ID + " FROM " +
                        CategoriesTableSchema.TABLE_NAME + " WHERE " +
                        CategoriesTableSchema.CATEGORY_NAME + " LIKE  ?",

                new String[]{"%" + name + "%"}, CategoriesTableSchema.CATEGORY_ID
        );
    }

    /**
     * @param query The name of the habit to look up.
     * @return The found habit ids.
     */
    public Set<Long> searchHabitDatabase(String query) {
        Set<Long> habitIds = findHabitIdsByName(query);
        Set<Long> categoryIds = findCategoryIdsByName(query);

        Set<Long> habitIdsByCategory = new HashSet<>();
        for (long id : categoryIds) habitIdsByCategory.addAll(getHabitIds(id));

        Set<Long> ids = new HashSet<>(habitIds.size() + habitIdsByCategory.size());
        ids.addAll(habitIds);
        ids.addAll(habitIdsByCategory);

        return ids;
    }
    //endregion

    //region [ -- CRUD records -- ]

    //region Create records
    public long addHabitIfNotInDatabase(Habit habit) {
        long habitId = getHabitIdFromObject(habit);
        if (habitId == -1) {
            habitId = addHabit(habit);
        }

        return habitId;
    }

    public long addHabit(Habit habit) {
        addCategoryIfNotExists(habit.getCategory());

        long habitId = mWritableDatabase.insert(
                HabitsTableSchema.TABLE_NAME, null,
                HabitsTableSchema.getContentValuesFromObject(habit)
        );
        habit.setDatabaseId(habitId);

        // Add entries to the new habit
        List<SessionEntry> entries = habit.getEntries();
        if (entries != null) addEntries(habitId, entries);

        return habitId;
    }
    //endregion

    //region Read records
    public List<Habit> lookUpHabits(Set<Long> ids) {
        List<Habit> habits = new ArrayList<>(ids.size());

        for (long id : ids) {
            habits.add(getHabit(id));
        }

        return habits;
    }

    private Habit getHabitFromCursor(Cursor c) {
        ContentValues contentValues = new ContentValues(c.getColumnCount());
        DatabaseUtils.cursorRowToContentValues(c, contentValues);

        return HabitsTableSchema.getObjectFromContentValues(contentValues, this);
    }

    public Habit getHabit(long habitId) {
        String selectionArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.query(HabitsTableSchema.TABLE_NAME, null, HabitsTableSchema.HABIT_ID + "=?",
                selectionArgs, null, null, null);

        Habit habit = null;
        if (c != null) {
            if (c.moveToFirst()) {
                habit = getHabitFromCursor(c);
                c.close();
            }
        }

        return habit;
    }

    public List<Habit> getHabits(long categoryId) {
        int numberOfHabits = getNumberOfHabits(categoryId);
        List<Habit> habits = new ArrayList<>(numberOfHabits);

        Cursor c = mReadableDatabase.query(HabitsTableSchema.TABLE_NAME, null,
                HabitsTableSchema.HABIT_CATEGORY_ID + " =?", new String[]{String.valueOf(categoryId)},
                null, null, null);

        int habitIndex = 0;
        while (c.moveToNext())
            habits.add(habitIndex++, getHabitFromCursor(c));

        c.close();

        return habits;
    }

    public ArrayList<Habit> getHabits() {
        ArrayList<Habit> habits = new ArrayList<>();

        int numberOfCategories = getNumberOfCategories();

        for (int i = 0; i < numberOfCategories; i++) {
            long categoryId = getCategoryIdFromIndex(i);
            habits.addAll(getHabits(categoryId));
        }

        Collections.sort(habits, Habit.ICompareCategoryName);

        return habits;
    }
    //endregion

    public long updateHabit(long habitId, Habit habit) {
        if (getHabit(habitId) != null) {
            updateHabitName(habitId, habit.getName());
            updateHabitDescription(habitId, habit.getDescription());

            HabitCategory category = habit.getCategory();
            long categoryId = getCategoryIdByObject(category);
            if (categoryId == -1) categoryId = addCategory(category);

            updateHabitCategory(habitId, categoryId);
            updateHabitIconResId(habitId, habit.getIconResId());
            return 1;
        }

        return -1;
    }

    public long deleteHabit(long habitId) {
        // Delete all of the habit's entries
        deleteEntriesForHabit(habitId);

        // Delete the habit itself
        String whereClause = HabitsTableSchema.HABIT_ID + "=?";
        String whereArgs[] = {String.valueOf(habitId)};

        return mWritableDatabase.delete(HabitsTableSchema.TABLE_NAME,
                whereClause, whereArgs);
    }

    //endregion [ -- end -- ]

    //region Get attributes
    public <Type> Type getHabitAttribute(long habitId, String columnKey, Class<Type> clazz) {
        return MyDatabaseUtils.getAttribute(
                mReadableDatabase, HabitsTableSchema.TABLE_NAME,
                HabitsTableSchema.HABIT_ID, habitId, columnKey, clazz
        );
    }

    public String getHabitName(long habitId) {
        return getHabitAttribute(habitId, HabitsTableSchema.HABIT_NAME, String.class);
    }

    public boolean getIsHabitArchived(long habitId) {
        return getHabitAttribute(habitId, HABIT_IS_ARCHIVED, Long.class) == 1;
    }

    public long getHabitCategoryId(long habitId) {
        return getHabitAttribute(habitId, HabitsTableSchema.HABIT_CATEGORY_ID, Long.class);
    }

    public int getHabitColor(long habitId) {
        if (getIsHabitArchived(habitId)) return 0xFFCCCCCC;
        else {
            HabitCategory category = getCategory(getHabitCategoryId(habitId));
            if (category != null) return category.getColorAsInt();
        }

        return -1;
    }
    //endregion

    //region Set attributes

    public int setHabitAttribute(long habitId, String columnKey, Object object) {
        return MyDatabaseUtils.setAttribute(
                mWritableDatabase, HabitsTableSchema.TABLE_NAME,
                HabitsTableSchema.HABIT_ID, habitId, columnKey, object
        );
    }

    /**
     * @param habitId The row id to be updated.
     * @param state   Set true to archive the habit, false to unarchive.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitIsArchived(long habitId, Boolean state) {
        return setHabitAttribute(habitId, HABIT_IS_ARCHIVED, state);
    }

    /**
     * @param habitId The row id to be updated.
     * @param name    The new name to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitName(long habitId, String name) {
        ContentValues values = new ContentValues(1);
        values.put(HabitsTableSchema.HABIT_NAME, name);

        String whereClause = HabitsTableSchema.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return mWritableDatabase.update(HabitsTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param habitId    The row id to be updated.
     * @param categoryId The new categoryId to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitCategory(long habitId, @Nullable Long categoryId) {
        return setHabitAttribute(habitId, HabitsTableSchema.HABIT_CATEGORY_ID, categoryId);
    }

    /**
     * @param habitId     The row id to be updated.
     * @param description The new description to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitDescription(long habitId, String description) {
        return setHabitAttribute(habitId, HabitsTableSchema.HABIT_DESCRIPTION, description);
    }

    /**
     * @param habitId   The row id to be updated.
     * @param iconResId The new iconResId to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitIconResId(long habitId, String iconResId) {
        return setHabitAttribute(habitId, HabitsTableSchema.HABIT_ICON_RES_ID, iconResId);
    }
    //endregion

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Methods for manipulating the categories table ---- ]

    //region Methods responsible for finding record ids
    public long getCategoryIdFromIndex(int index) {
        return getRowIdByIndex(index, CategoriesTableSchema.TABLE_NAME,
                CategoriesTableSchema.CATEGORY_ID, null, null);
    }

    public long getCategoryIdByObject(HabitCategory category) {
        long rowId = category.getDatabaseId();

        if (rowId < 0) {
            String whereClause = CategoriesTableSchema.CATEGORY_NAME + " =? AND " +
                    CategoriesTableSchema.CATEGORY_COLOR + " =?";
            String selectionArgs[] = {category.getName(), category.getColor()};

            rowId = getRowIdByIndex(0, CategoriesTableSchema.TABLE_NAME,
                    CategoriesTableSchema.CATEGORY_ID, whereClause, selectionArgs);

            category.setDatabaseId(rowId);
        }

        return rowId;
    }
    //endregion

    //region [ -- CRUD records -- ]

    //region Create records
    public long addCategory(HabitCategory category) {
        long categoryId = mWritableDatabase.insert(
                CategoriesTableSchema.TABLE_NAME, null,
                CategoriesTableSchema.getContentValuesFromObject(category)
        );
        category.setDatabaseId(categoryId);

        return categoryId;
    }

    public long addCategoryIfNotExists(HabitCategory category) {
        long id = getCategoryIdByObject(category);
        if (id == -1) { // Make sure this category doesn't exist in the database.
            id = addCategory(category);
        }

        return id;
    }
    //endregion Create records

    //region Read records
    @Nullable
    public HabitCategory getCategory(long categoryId) {
        String columns[] = {CategoriesTableSchema.CATEGORY_NAME, CategoriesTableSchema.CATEGORY_COLOR};
        String selectionArgs[] = {String.valueOf(categoryId)};

        Cursor c = mReadableDatabase.query(CategoriesTableSchema.TABLE_NAME, columns,
                CategoriesTableSchema.CATEGORY_ID + "=?", selectionArgs, null, null, null);

        HabitCategory category = null;

        if (c != null) {
            if (c.moveToFirst()) category = getCategoryFromCursor(c).setDatabaseId(categoryId);
            c.close();
        }

        return category;
    }

    private HabitCategory getCategoryFromCursor(Cursor cursor) {
        ContentValues contentValues = new ContentValues(cursor.getColumnCount());
        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);

        return CategoriesTableSchema.getObjectFromContentValues(contentValues);
    }

    public List<HabitCategory> getCategories() {
        Cursor cursor = mReadableDatabase.query(CategoriesTableSchema.TABLE_NAME,
                null, null, null, null, null, null);

        int size = cursor.getCount();
        List<HabitCategory> categories = new ArrayList<>(size);

        if (cursor.moveToFirst())
            do categories.add(getCategoryFromCursor(cursor)); while (cursor.moveToNext());

        cursor.close();

        return categories;
    }
    //endregion

    public long updateCategory(long categoryId, HabitCategory category) {
        if (getCategory(categoryId) != null) {
            updateCategoryName(categoryId, category.getName());
            updateCategoryColor(categoryId, category.getColor());
            return 1;
        }

        return -1;
    }

    public long deleteCategory(long categoryId) {
        String whereClause = CategoriesTableSchema.CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        return mWritableDatabase.delete(CategoriesTableSchema.TABLE_NAME,
                whereClause, whereArgs);
    }

    //endregion [ -- end -- ]

    //region Set attributes
    public int setCategoryAttribute(long categoryId, String columnKey, Object object) {
        return MyDatabaseUtils.setAttribute(
                mWritableDatabase, CategoriesTableSchema.TABLE_NAME,
                CategoriesTableSchema.CATEGORY_ID, categoryId, columnKey, object
        );
    }

    /**
     * @param categoryId The database id for the category to update.
     * @param name       The new name for the category.
     * @return The number of rows changed, -1 if fail.
     */
    public long updateCategoryName(long categoryId, String name) {
        return setCategoryAttribute(categoryId, CategoriesTableSchema.CATEGORY_NAME, name);
    }

    /**
     * @param categoryId The database id for the category to update.
     * @param color      The new color for the category.
     * @return The number of rows changed, -1 if fail.
     */
    public long updateCategoryColor(long categoryId, String color) {
        return setCategoryAttribute(categoryId, CategoriesTableSchema.CATEGORY_COLOR, color);
    }
    //endregion

    //endregion [ ---------------- end ---------------- ]

    //region [ ---- Methods for manipulating the entries table ---- ]

    //region Methods responsible for finding record ids
    public long getEntryIdFromIndex(long habitId, int index) {
        return getRowIdByIndex(index, EntriesTableSchema.TABLE_NAME, EntriesTableSchema.ENTRY_ID,
                EntriesTableSchema.ENTRY_HABIT_ID + "=?", new String[]{String.valueOf(habitId)});
    }

    public long getEntryIdByObject(SessionEntry entry) {
        String whereClause = EntriesTableSchema.ENTRY_START_TIME + " =? AND " +
                EntriesTableSchema.ENTRY_DURATION + " =? AND " +
                EntriesTableSchema.ENTRY_NOTE + "=?";

        String selectionArgs[] = new String[]{String.valueOf(entry.getStartingTime()),
                String.valueOf(entry.getDuration()), entry.getNote()};

        long rowId = getRowIdByIndex(0, EntriesTableSchema.TABLE_NAME, EntriesTableSchema.ENTRY_ID,
                whereClause, selectionArgs);

        entry.setDatabaseId(rowId);
        return rowId;
    }

    /**
     * @return An array of entry ids found by the search, null if results were empty.
     */
    @Nullable
    public Set<Long> findEntryIdsByComment(long habitId, String query) {
        return searchTableForIds(
                "SELECT " + EntriesTableSchema.ENTRY_ID + " FROM " +
                        EntriesTableSchema.TABLE_NAME + " WHERE " +
                        EntriesTableSchema.ENTRY_NOTE + " LIKE ? AND " +
                        EntriesTableSchema.ENTRY_HABIT_ID + "=?",

                new String[]{"%" + query + "%", String.valueOf(habitId)}, EntriesTableSchema.ENTRY_ID
        );
    }

    /**
     * @return An array of entry ids found by the search, null if results were empty.
     */
    public Set<Long> findEntryIdsByComment(String query) {
        return searchTableForIds(
                "SELECT " + EntriesTableSchema.ENTRY_ID + " FROM " +
                        EntriesTableSchema.TABLE_NAME + " WHERE " +
                        EntriesTableSchema.ENTRY_NOTE + " LIKE ? ",

                new String[]{"%" + query + "%"}, EntriesTableSchema.ENTRY_ID
        );
    }

    /**
     * @return An array of entry ids found by the search, null if results were empty.
     */
    @Nullable
    public Set<Long> findEntriesWithinTimeRange(long habitId, long timeFrom, long timeTo) {
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE HABIT_ID=habitId
        // AND START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + EntriesTableSchema.ENTRY_ID +
                " FROM " + EntriesTableSchema.TABLE_NAME + " WHERE " +
                EntriesTableSchema.ENTRY_HABIT_ID + "=? AND " +
                EntriesTableSchema.ENTRY_START_TIME + ">=? AND " +
                EntriesTableSchema.ENTRY_START_TIME + "<=?";

        String[] values = new String[]{
                String.valueOf(habitId),
                String.valueOf(timeFrom),
                String.valueOf(timeTo)
        };

        return searchTableForIds(SQL, values, EntriesTableSchema.ENTRY_ID);
    }

    /**
     * @return An array of entry ids found by the search, null if results were empty.
     */
    public Set<Long> findEntriesWithinTimeRange(long timeFrom, long timeTo) {
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + EntriesTableSchema.ENTRY_ID +
                " FROM " + EntriesTableSchema.TABLE_NAME + " WHERE " +
                EntriesTableSchema.ENTRY_START_TIME + ">=? AND " +
                EntriesTableSchema.ENTRY_START_TIME + "<=?";

        String[] values = new String[]{
                String.valueOf(timeFrom),
                String.valueOf(timeTo)
        };

        return searchTableForIds(SQL, values, EntriesTableSchema.ENTRY_ID);
    }
    //endregion

    //region [ -- CRUD records -- ]

    //region Create records
    public long addEntry(long habitId, SessionEntry entry) {
        long entryId = mWritableDatabase.insert(
                EntriesTableSchema.TABLE_NAME, null,
                EntriesTableSchema.getContentValuesFromObject(entry, habitId)
        );
        entry.setHabitId(habitId);
        entry.setDatabaseId(entryId);

        return entryId;
    }

    public void addEntries(long habitId, @NonNull List<SessionEntry> entries) {
        for (SessionEntry entry : entries) {
            long entryId = addEntry(habitId, entry);
            entry.setDatabaseId(entryId);
            entry.setHabitId(habitId);
        }
    }
    //endregion

    //region Read records
    // TODO Optimize get min/max entry methods
    @Nullable
    public SessionEntry getMinEntry(long habitId) {
        List<SessionEntry> entries = getEntries(habitId);
        if (!entries.isEmpty())
            return Collections.min(entries, SessionEntry.ICompareStartingTimes);
        else return null;
//        Cursor c = mReadableDatabase.query(EntriesTableSchema.TABLE_NAME,
//                new String[] { "min(" + EntriesTableSchema.ENTRY_START_TIME + ")" },
//                EntriesTableSchema.ENTRY_HABIT_ID + "=?", new String[]{String.valueOf(habitId)},
//                null, null, null
//        );
//
//        if(c.moveToFirst()) return getEntryFromCursor(c);
//        else return null;
    }

    @Nullable
    public SessionEntry getMaxEntry(long habitId) {
        List<SessionEntry> entries = getEntries(habitId);
        if (!entries.isEmpty())
            return Collections.max(entries, SessionEntry.ICompareStartingTimes);
        else return null;

//        Cursor c = mReadableDatabase.query(EntriesTableSchema.TABLE_NAME,
//                new String[] { "max(" + EntriesTableSchema.ENTRY_START_TIME + ")" },
//                EntriesTableSchema.ENTRY_HABIT_ID + "=?", new String[]{String.valueOf(habitId)},
//                null, null, null
//        );
//
//        if(c.moveToFirst()) return getEntryFromCursor(c);
//        else return null;
    }

    @Nullable
    public SessionEntry getMinEntry() {
        List<SessionEntry> entries = getEntries();
        if (!entries.isEmpty())
            return Collections.min(entries, SessionEntry.ICompareStartingTimes);
        else return null;
//        Cursor c = mReadableDatabase.query(EntriesTableSchema.TABLE_NAME,
//                new String[] { "min(" + EntriesTableSchema.ENTRY_START_TIME + ")" },
//                null, null, null, null, null
//        );
//
//        if(c.moveToFirst()) return getEntryFromCursor(c);
//        else return null;
    }

    @Nullable
    public SessionEntry getMaxEntry() {
        List<SessionEntry> entries = getEntries();
        if (!entries.isEmpty())
            return Collections.max(entries, SessionEntry.ICompareStartingTimes);
        else return null;
//        Cursor c = mReadableDatabase.query(EntriesTableSchema.TABLE_NAME,
//                new String[] { "max(" + EntriesTableSchema.ENTRY_START_TIME + ")" },
//                null, null, null, null, null
//        );
//
//        if(c.moveToFirst()) return getEntryFromCursor(c);
//        else return null;
    }

    public List<SessionEntry> lookUpEntries(Set<Long> ids) {
        List<SessionEntry> entries = new ArrayList<>(ids.size());

        for (long id : ids) entries.add(getEntry(id));

        Collections.sort(entries, SessionEntry.ICompareStartingTimes);

        return entries;
    }

    /**
     * @param c A cursor object above an entry record.
     * @return An entry object.
     */
    @Nullable
    private SessionEntry getEntryFromCursor(Cursor c) {
        ContentValues contentValues = new ContentValues(c.getColumnCount());
        DatabaseUtils.cursorRowToContentValues(c, contentValues);

        return EntriesTableSchema.getObjectFromContentValues(contentValues);
    }

    /**
     * @param entryId The id of the session entry to look up.
     * @return The found session entry.
     */
    @Nullable
    public SessionEntry getEntry(long entryId) {
        return MyDatabaseUtils.getRecord(
                mReadableDatabase, EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_ID, entryId, EntriesTableSchema.IGetFromContentValues
        );
    }

    public List<SessionEntry> getEntries(long habitId) {
        Cursor c = MyDatabaseUtils.queryTable(
                mReadableDatabase, EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_HABIT_ID, habitId
        );

        return fetchEntriesAtCursor(c);
    }

    private List<SessionEntry> fetchEntriesAtCursor(Cursor cursor) {
        if (cursor.moveToFirst()) {
            List<SessionEntry> resultList = new ArrayList<>(cursor.getCount());

            // Load all the entries in the database into the ArrayList.
            do resultList.add(getEntryFromCursor(cursor)); while (cursor.moveToNext());

            // Sort the entries by Category Name
            Collections.sort(resultList, SessionEntry.ICompareStartingTimes);
            cursor.close();

            return resultList;
        }

        return new ArrayList<>();
    }

    public List<SessionEntry> getEntries() {
        Cursor c = mReadableDatabase.query(
                EntriesTableSchema.TABLE_NAME,
                null, null, null, null, null, null
        );

        return fetchEntriesAtCursor(c);
    }
    //endregion

    //region Update records

    /**
     * @param entryId  The id of the entry to edit.
     * @param newEntry The new session entry to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntry(long entryId, SessionEntry newEntry) {
        SessionEntry oldEntry = getEntry(entryId);

        if (oldEntry != null) {
            updateEntryStartTime(entryId, newEntry.getStartingTime());
            updateEntryDuration(entryId, newEntry.getDuration());
            updateEntryNote(entryId, newEntry.getNote());

            notifyEntryUpdated(oldEntry, newEntry);
            return 1;
        }

        return -1;
    }
    //endregion

    //region Delete records
    public long deleteEntry(long entryId) {
        SessionEntry removedEntry = getEntry(entryId);

        long numberOfRecordsAffected = MyDatabaseUtils.deleteRecord(
                mWritableDatabase,
                EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_ID, entryId
        );

        notifyEntryDeleted(removedEntry);
        return numberOfRecordsAffected;
    }

    public long deleteEntriesForHabit(long habitId) {
        String whereClause = EntriesTableSchema.ENTRY_HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        long numberOfRecordsAffected = mWritableDatabase.delete(EntriesTableSchema.TABLE_NAME,
                whereClause, whereArgs);

        notifyEntriesReset(habitId);
        return numberOfRecordsAffected;
    }
    //endregion

    //endregion [ -- end -- ]

    //region Get attributes
    public <Type> Type getEntryAttribute(long entryId, String columnKey, Class<Type> clazz) {
        return MyDatabaseUtils.getAttribute(
                mReadableDatabase, EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_ID, entryId, columnKey, clazz
        );
    }

    /**
     * @param habitId    The id of the habit which to look for it's entries.
     * @param entryIndex The index of the entry to be retrieved from the habit.
     * @return The found session entry row id.
     */
    public long getEntryId(long habitId, int entryIndex) {
        return MyDatabaseUtils.getAttribute(
                mReadableDatabase, EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_HABIT_ID, habitId, EntriesTableSchema.ENTRY_ID, Long.class);
    }
    //endregion

    //region Set attributes

    public int setEntryAttribute(long entryId, String columnKey, Object object) {
        return MyDatabaseUtils.setAttribute(
                mWritableDatabase, EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_ID, entryId, columnKey, object
        );
    }

    /**
     * @param entryId   The id of the entry to edit.
     * @param startTime The new start time to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryStartTime(long entryId, long startTime) {
        return setEntryAttribute(entryId, EntriesTableSchema.ENTRY_START_TIME, startTime);
    }

    /**
     * @param entryId  The id of the entry to edit.
     * @param duration The new duration to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryDuration(long entryId, long duration) {
        return setEntryAttribute(entryId, EntriesTableSchema.ENTRY_DURATION, duration);
    }

    /**
     * @param entryId The id of the entry to edit.
     * @param note    The new note to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryNote(long entryId, String note) {
        return setEntryAttribute(entryId, EntriesTableSchema.ENTRY_NOTE, note);
    }
    //endregion

    //endregion [ ---------------- end ---------------- ]

}