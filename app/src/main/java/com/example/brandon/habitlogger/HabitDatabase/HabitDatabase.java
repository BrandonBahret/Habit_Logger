package com.example.brandon.habitlogger.HabitDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.CategoriesTableSchema;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.DatabaseSchema;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.EntriesTableSchema;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.HabitsTableSchema;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyDatabaseUtils;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private HabitCategory mDefaultCategory;
    //endregion

    //region Code responsible for providing an interface to the database

    public interface OnEntryChangedListener {
        void onEntryDeleted(SessionEntry removedEntry);

        void onEntriesReset(long habitId);

        void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry);
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

        mDefaultCategory = new HabitCategory("#ff000000", context.getString(R.string.uncategorized));
        mWritableDatabase = databaseHelper.getWritableDatabase();
        mReadableDatabase = databaseHelper.getReadableDatabase();
    }

    //region Helper methods responsible for getting and setting attributes
    //region Get attributes
    public <Type> Type getHabitAttribute(long habitId, String columnKey, Class<Type> clazz) {
        return MyDatabaseUtils.getAttribute(
                mReadableDatabase, HabitsTableSchema.TABLE_NAME,
                HabitsTableSchema.HABIT_ID, habitId, columnKey, clazz
        );
    }

    public <Type> Type getCategoryAttribute(long categoryId, String columnKey, Class<Type> clazz) {
        return MyDatabaseUtils.getAttribute(
                mReadableDatabase, CategoriesTableSchema.TABLE_NAME,
                CategoriesTableSchema.CATEGORY_ID, categoryId, columnKey, clazz
        );
    }

    public <Type> Type getEntryAttribute(long entryId, String columnKey, Class<Type> clazz) {
        return MyDatabaseUtils.getAttribute(
                mReadableDatabase, EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_ID, entryId, columnKey, clazz
        );
    }
    //endregion

    //region Set attributes
    public int setHabitAttribute(long habitId, String columnKey, Object object) {
        return MyDatabaseUtils.setAttribute(
                mWritableDatabase, HabitsTableSchema.TABLE_NAME,
                HabitsTableSchema.HABIT_ID, habitId, columnKey, object
        );
    }

    public int setCategoryAttribute(long categoryId, String columnKey, Object object) {
        return MyDatabaseUtils.setAttribute(
                mWritableDatabase, CategoriesTableSchema.TABLE_NAME,
                CategoriesTableSchema.CATEGORY_ID, categoryId, columnKey, object
        );
    }

    public int setEntryAttribute(long entryId, String columnKey, Object object) {
        return MyDatabaseUtils.setAttribute(
                mWritableDatabase, EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_ID, entryId, columnKey, object
        );
    }
    //endregion
    //endregion

    //region Methods responsible for counting the number of rows in tables

    /**
     * @param tableName     The name of the table to search.
     * @param columns       The columns to retrieve.
     * @param whereClause   The where clause to filter the table.
     * @param selectionArgs The arguments to the where clause.
     * @return The number of rows found, -1 if query failed.
     */
    private int getNumberOfRows(String tableName, @Nullable String columns[],
                                @Nullable String whereClause, @Nullable String selectionArgs[]) {

        Cursor c = mReadableDatabase.query(tableName, columns, whereClause, selectionArgs,
                null, null, null);

        int count = -1;
        if (c != null) {
            count = c.getCount();
            c.close();
        }

        return count;
    }

    public int getNumberOfHabits(long categoryId) {
        return getNumberOfRows(HabitsTableSchema.TABLE_NAME,
                new String[]{HabitsTableSchema.HABIT_ID},
                HabitsTableSchema.HABIT_CATEGORY + "=?",
                new String[]{String.valueOf(categoryId)});
    }

    /**
     * @return The total number of categories stored in the database.
     */
    public int getNumberOfCategories() {
        return getNumberOfRows(CategoriesTableSchema.TABLE_NAME,
                new String[]{CategoriesTableSchema.CATEGORY_ID}, null, null);
    }

    public int getNumberOfEntries(long habitId) {
        return getNumberOfRows(EntriesTableSchema.TABLE_NAME,
                new String[]{EntriesTableSchema.ENTRY_HABIT_ID},
                EntriesTableSchema.ENTRY_HABIT_ID + "=?",
                new String[]{String.valueOf(habitId)});
    }
    //endregion


    /**
     * @param index             The index of the record to be looked up.
     * @param tableName         The table name of the table to search.
     * @param rowIdColumnString The name of the row id.
     * @param whereClause       The where clause used to filter the table.
     * @param selectionArgs     The args to the where clause.
     * @return The found row id, -1 if found nothing.
     */
    private long getRowIdByIndex(int index, String tableName, String rowIdColumnString,
                                 @Nullable String whereClause, @Nullable String selectionArgs[]) {

        Cursor c = mReadableDatabase.query(tableName, new String[]{rowIdColumnString}, whereClause,
                selectionArgs, null, null, null, null);

        long rowId = -1;
        if (c.moveToPosition(index)) rowId = c.getLong(0);

        c.close();

        return rowId;
    }

    //region Methods responsible for fetching record ids

    /**
     * @param SQL            The SQL string to be executed
     * @param idColumnString The string id for the column that holds row ids.
     * @return An array of row ids found by the sqlite query.
     */
    @Nullable
    private Set<Long> searchTableForIdsByName(String SQL, String[] values, String idColumnString) {

        Cursor c = mReadableDatabase.rawQuery(SQL, values);

        if (c != null) {
            Set<Long> ids = new HashSet<>(c.getCount());

            while (c.moveToNext()) {
                int idInd = c.getColumnIndex(idColumnString);
                ids.add(c.getLong(idInd));
            }

            c.close();
            return ids;
        }

        return null;
    }

    public Set<Long> getHabitIds(long categoryId) {
        return searchTableForIdsByName("SELECT " + HabitsTableSchema.HABIT_ID + " FROM " +
                        HabitsTableSchema.TABLE_NAME + " WHERE " +
                        HabitsTableSchema.HABIT_CATEGORY + " =?",
                new String[]{String.valueOf(categoryId)}, HabitsTableSchema.HABIT_ID);
    }
    //endregion

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

        for (HabitCategory category : getCategories()) {
            data.add(getCategoryDataSample(category, dateFrom, dateTo));
        }

        return new HabitDataSample(data, dateFrom, dateTo);
    }

    public CategoryDataSample getCategoryDataSample(HabitCategory category, long dateFrom, long dateTo) {
        List<Habit> habits = getHabits(category.getDatabaseId());

        for (Habit habit : habits) {
            long habitId = habit.getDatabaseId();
            Set<Long> ids = searchEntriesWithTimeRangeForAHabit(habitId, dateFrom, dateTo);
            List<SessionEntry> entries = lookUpEntries(ids);
            habit.setEntries(entries);
        }

        return new CategoryDataSample(category, habits, dateFrom, dateTo);
    }

    public SessionEntriesSample getEntriesSample(long habitId, long dateFrom, long dateTo) {
        List<SessionEntry> entries = lookUpEntries(
                searchEntriesWithTimeRangeForAHabit(habitId, dateFrom, dateTo)
        );

        return new SessionEntriesSample(entries, dateFrom, dateTo);
    }
    //endregion

    //region Methods for manipulating the habits table
    private SQLiteStatement getHabitInsertStatement() {
        return mWritableDatabase.compileStatement(HabitsTableSchema.getInsertStatement());
    }

    /**
     * @param habit The habit to be added to the database.
     * @return The row id of the new habit.
     */
    public long addHabitIfNotExists(Habit habit) {
        long habitId = getHabitIdFromObject(habit);

        if (habitId == -1) {
            addHabit(habit);
        }

        return habitId;
    }

    private void bindValuesForHabitObject(SQLiteStatement insert, Habit habit) {
        long categoryId = habit.getCategory().getDatabaseId();
        if (categoryId == -1) {
            throw new IllegalArgumentException("Category database id is -1.");
        }

        insert.bindLong(1, habit.getIsArchived() ? 1 : 0);
        insert.bindString(2, habit.getName());
        insert.bindString(3, habit.getDescription());
        insert.bindString(4, habit.getIconResId());
        insert.bindLong(5, habit.getCategory().getDatabaseId());
    }

    /**
     * @param habit The habit to be added to the database.
     * @return The row id of the new habit.
     */
    public long addHabit(Habit habit) {
        SQLiteStatement insert = getHabitInsertStatement();
        bindValuesForHabitObject(insert, habit);

        long habitId = insert.executeInsert();
        habit.setDatabaseId(habitId);
        insert.close();

        // Add entries to the new habit
        List<SessionEntry> entries = habit.getEntries();
        if (entries != null)
            addEntries(habitId, entries);

        return habitId;
    }

    /**
     * @param habit The habit to be added to the database.
     * @return The row id of the new habit.
     */
    public long addHabitAndCategory(Habit habit) {
        HabitCategory category = habit.getCategory();
        addCategoryIfNotExists(category);

        return addHabit(habit);
    }

    /**
     * @param index The habit index to look up.
     * @return The unique habit id of the row.
     */
    public long getHabitIdFromIndex(long categoryId, int index) {
        return getRowIdByIndex(index, HabitsTableSchema.TABLE_NAME, HabitsTableSchema.HABIT_ID,
                HabitsTableSchema.HABIT_CATEGORY + "=?", new String[]{String.valueOf(categoryId)});
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
                    HabitsTableSchema.HABIT_CATEGORY + "=?";

            String selectionArgs[] = {habit.getName(), String.valueOf(categoryId)};

            habitId = getRowIdByIndex(0, HabitsTableSchema.TABLE_NAME, HabitsTableSchema.HABIT_ID,
                    whereClause, selectionArgs);

            habit.setDatabaseId(habitId);
        }

        return habitId;
    }

    /**
     * @param name The name of the habit to look up.
     * @return The found habit ids.
     */
    public Set<Long> searchHabitIdsByName(String name) {
        return searchTableForIdsByName("SELECT " + HabitsTableSchema.HABIT_ID + " FROM " +
                        HabitsTableSchema.TABLE_NAME + " WHERE " +
                        HabitsTableSchema.HABIT_NAME + " LIKE  ?",

                new String[]{"%" + name + "%"}, HabitsTableSchema.HABIT_ID);
    }

    /**
     * @param name The name of the category to look up.
     * @return The found category ids as an array.
     */
    public Set<Long> searchCategoryIdsByName(String name) {
        return searchTableForIdsByName(
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
        Set<Long> habitIds = searchHabitIdsByName(query);
        Set<Long> categoryIds = searchCategoryIdsByName(query);

        Set<Long> habitIdsByCategory = new HashSet<>();
        for (long id : categoryIds) habitIdsByCategory.addAll(getHabitIds(id));

        Set<Long> ids = new HashSet<>(habitIds.size() + habitIdsByCategory.size());
        ids.addAll(habitIds);
        ids.addAll(habitIdsByCategory);

        return ids;
    }

    private Habit getHabitFromCursor(Cursor c) {
        ContentValues contentValues = new ContentValues(c.getColumnCount());
        DatabaseUtils.cursorRowToContentValues(c, contentValues);

        return HabitsTableSchema.getObjectFromContentValues(contentValues, this);
    }

    /**
     * @param habitId The row id of the habit you wish to receive.
     * @return The habit object found.
     */
    public Habit getHabit(long habitId) throws RuntimeException {
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

    //region Get habit attributes
    public String getHabitName(long habitId) {
        return getHabitAttribute(habitId, HabitsTableSchema.HABIT_NAME, String.class);
    }

    public boolean getIsHabitArchived(long habitId) {
        return getHabitAttribute(habitId, HabitsTableSchema.HABIT_IS_ARCHIVED, Long.class) == 1;
    }

    public long getHabitCategoryId(long habitId) {
        return getHabitAttribute(habitId, HabitsTableSchema.HABIT_CATEGORY, Long.class);
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

    //region Set habit attributes

    /**
     * @param habitId The row id to be updated.
     * @param state   Set true to archive the habit, false to unarchive.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitIsArchived(long habitId, Boolean state) {
        return setHabitAttribute(habitId, HabitsTableSchema.HABIT_IS_ARCHIVED, state);
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
     * @param habitId     The row id to be updated.
     * @param description The new description to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitDescription(long habitId, String description) {
        return setHabitAttribute(habitId, HabitsTableSchema.HABIT_DESCRIPTION, description);
    }

    /**
     * @param habitId    The row id to be updated.
     * @param categoryId The new categoryId to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitCategory(long habitId, @Nullable Long categoryId) {
        return setHabitAttribute(habitId, HabitsTableSchema.HABIT_CATEGORY, categoryId);
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

    /**
     * @param categoryId The category to fetch habits from.
     * @return an array list containing the habits.
     */
    public List<Habit> getHabits(long categoryId) {
        int numberOfHabits = getNumberOfHabits(categoryId);
        List<Habit> habits = new ArrayList<>(numberOfHabits);

        Cursor c = mReadableDatabase.query(HabitsTableSchema.TABLE_NAME, null,
                HabitsTableSchema.HABIT_CATEGORY + " =?", new String[]{String.valueOf(categoryId)},
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

        return habits;
    }

    public List<Habit> lookUpHabits(Set<Long> ids) {
        List<Habit> habits = new ArrayList<>(ids.size());

        for (long id : ids) {
            habits.add(getHabit(id));
        }

        return habits;
    }

    /**
     * @param habitId The id of the habit to edit.
     * @param habit   A new habit to replace the old one.
     * @return The number of rows removed, -1 if error.
     */
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

    /**
     * @param habitId The row id of the habit to be removed.
     * @return The number of rows removed, -1 if error.
     */
    public long deleteHabit(long habitId) {
        // Delete all of the habit's entries
        deleteEntriesForHabit(habitId);

        // Delete the habit itself
        String whereClause = HabitsTableSchema.HABIT_ID + "=?";
        String whereArgs[] = {String.valueOf(habitId)};

        return mWritableDatabase.delete(HabitsTableSchema.TABLE_NAME,
                whereClause, whereArgs);
    }
    //endregion

    //region Methods for manipulating the categories table
    private SQLiteStatement getCategoryInsertStatement() {
        return mWritableDatabase.compileStatement(CategoriesTableSchema.getInsertStatement());
    }

    /**
     * @param category The category object to be added to the database.
     * @return The row id for the new row, -1 if error.
     */
    public long addCategory(HabitCategory category) {
        SQLiteStatement insert = getCategoryInsertStatement();

        insert.bindString(1, category.getName());
        insert.bindString(2, category.getColor());

        long id = insert.executeInsert();
        category.setDatabaseId(id);

        insert.close();
        return id;
    }

    /**
     * @param category The category object to be added to the database.
     * @return The row id for the new row, -1 if error.
     */
    public long addCategoryIfNotExists(HabitCategory category) {
        long id = getCategoryIdByObject(category);

        if (id == -1) { // Make sure this category doesn't exist in the database.
            id = addCategory(category);
        }

        return id;
    }

    /**
     * @param index The category index to look up.
     * @return The unique category id of the row.
     */
    public long getCategoryIdFromIndex(int index) {
        return getRowIdByIndex(index, CategoriesTableSchema.TABLE_NAME,
                CategoriesTableSchema.CATEGORY_ID, null, null);
    }

    /**
     * @param category The category object to look for in the database.
     * @return The row id of the category found, -1 if no record found.
     */
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

    /**
     * @param categoryId The database id for the category to be received.
     * @return The category object from the database.
     */
    @Nullable
    public HabitCategory getCategory(long categoryId) {
        String columns[] = {CategoriesTableSchema.CATEGORY_NAME, CategoriesTableSchema.CATEGORY_COLOR};
        String selectionArgs[] = {String.valueOf(categoryId)};

        Cursor c = mReadableDatabase.query(CategoriesTableSchema.TABLE_NAME, columns,
                CategoriesTableSchema.CATEGORY_ID + "=?", selectionArgs, null, null, null);

        HabitCategory category = null;

        if (c != null) {
            if (c.moveToFirst()) category = getHabitCategoryFromCursor(c).setDatabaseId(categoryId);
            c.close();
        }

        return category;
    }

    public List<HabitCategory> getCategories() {
        Cursor cursor = mReadableDatabase.query(CategoriesTableSchema.TABLE_NAME,
                null, null, null, null, null, null);

        int size = cursor.getCount();
        List<HabitCategory> categories = new ArrayList<>(size);

        if (cursor.moveToFirst())
            do categories.add(getHabitCategoryFromCursor(cursor)); while (cursor.moveToNext());

        cursor.close();

        categories.add(0, mDefaultCategory);

        return categories;
    }

    private HabitCategory getHabitCategoryFromCursor(Cursor cursor) {
        ContentValues contentValues = new ContentValues(cursor.getColumnCount());
        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);

        return CategoriesTableSchema.getObjectFromContentValues(contentValues);
    }

    //region Set attributes

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

    /**
     * @param categoryId The database id for the category to update
     * @param category   The new category to update the old one.
     * @return Returns the number of rows changed, -1 if failed.
     */
    public long updateCategory(long categoryId, HabitCategory category) {
        if (getCategory(categoryId) != null) {
            updateCategoryName(categoryId, category.getName());
            updateCategoryColor(categoryId, category.getColor());
            return 1;
        }

        return -1;
    }

    /**
     * @param categoryId The database id for the category to delete
     * @return Returns the number of rows removed, -1 if failed.
     */
    public long deleteCategory(long categoryId) {
        String whereClause = CategoriesTableSchema.CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        return mWritableDatabase.delete(CategoriesTableSchema.TABLE_NAME,
                whereClause, whereArgs);
    }
    //endregion

    //region Methods for manipulating the entries table
    private SQLiteStatement getEntryInsertStatement() {
        return mWritableDatabase.compileStatement(EntriesTableSchema.getInsertStatement());
    }

    private void bindValuesForEntryObject(SQLiteStatement insert, SessionEntry entry, long habitId) {
        insert.bindLong(1, habitId);
        insert.bindLong(2, entry.getStartingTime());
        insert.bindLong(3, entry.getDuration());
        insert.bindString(4, entry.getNote());
    }

    /**
     * @param habitId The id of the habit in the database to associate this entry with.
     * @param entry   The entry to be inserted.
     * @return Row id of the new row, -1 if error.
     */
    public long addEntry(long habitId, SessionEntry entry) {
        SQLiteStatement insert = getEntryInsertStatement();
        bindValuesForEntryObject(insert, entry, habitId);

        long id = insert.executeInsert();
        entry.setDatabaseId(id);
        entry.setHabitId(habitId);

        insert.close();
        return id;
    }

    public void addEntries(long habitId, @NonNull List<SessionEntry> entries) {

        SQLiteStatement insert = getEntryInsertStatement();
        mWritableDatabase.beginTransaction();


        for (SessionEntry entry : entries) {
            bindValuesForEntryObject(insert, entry, habitId);

            long id = insert.executeInsert();
            entry.setDatabaseId(id);
            entry.setHabitId(habitId);
        }

        insert.close();
        mWritableDatabase.setTransactionSuccessful();
        mWritableDatabase.endTransaction();
    }

    /**
     * @param habitId The habit id to search.
     * @param index   The index of the entry to look up.
     * @return The id of the entry.
     */
    public long getEntryIdFromIndex(long habitId, int index) {
        return getRowIdByIndex(index, EntriesTableSchema.TABLE_NAME, EntriesTableSchema.ENTRY_ID,
                EntriesTableSchema.ENTRY_HABIT_ID + "=?", new String[]{String.valueOf(habitId)});
    }

    /**
     * @param entry The entry object to look for in the database.
     * @return The row id of the entry found, -1 if it failed to locate a category.
     */
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
     * @param habitId The row id for the habit to query.
     * @param query   The search query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    @Nullable
    public Set<Long> searchEntryIdsByComment(long habitId, String query) {
        return searchTableForIdsByName(
                "SELECT " + EntriesTableSchema.ENTRY_ID + " FROM " +
                        EntriesTableSchema.TABLE_NAME + " WHERE " +
                        EntriesTableSchema.ENTRY_NOTE + " LIKE ? AND " +
                        EntriesTableSchema.ENTRY_HABIT_ID + "=?",

                new String[]{"%" + query + "%", String.valueOf(habitId)}, EntriesTableSchema.ENTRY_ID
        );
    }

    public Set<Long> searchEntryIdsByComment(String query) {
        return searchTableForIdsByName(
                "SELECT " + EntriesTableSchema.ENTRY_ID + " FROM " +
                        EntriesTableSchema.TABLE_NAME + " WHERE " +
                        EntriesTableSchema.ENTRY_NOTE + " LIKE ? ",

                new String[]{"%" + query + "%"}, EntriesTableSchema.ENTRY_ID
        );
    }

    /**
     * @param habitId  The row id for the habit to search entries for.
     * @param timeFrom The starting time for the query.
     * @param timeTo   The ending time for the query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    @Nullable
    public Set<Long> searchEntriesWithTimeRangeForAHabit(long habitId, long timeFrom, long timeTo) {
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

        return searchTableForIdsByName(SQL, values, EntriesTableSchema.ENTRY_ID);
    }

    /**
     * @param timeFrom The starting time for the query.
     * @param timeTo   The ending time for the query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    public Set<Long> searchAllEntriesWithTimeRange(long timeFrom, long timeTo) {
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + EntriesTableSchema.ENTRY_ID +
                " FROM " + EntriesTableSchema.TABLE_NAME + " WHERE " +
                EntriesTableSchema.ENTRY_START_TIME + ">=? AND " +
                EntriesTableSchema.ENTRY_START_TIME + "<=?";

        String[] values = new String[]{
                String.valueOf(timeFrom),
                String.valueOf(timeTo)
        };

        return searchTableForIdsByName(SQL, values, EntriesTableSchema.ENTRY_ID);
    }

    public List<SessionEntry> lookUpEntries(Set<Long> ids) {
        List<SessionEntry> entries = new ArrayList<>(ids.size());

        for (long id : ids) {
            entries.add(getEntry(id));
        }

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
                EntriesTableSchema.ENTRY_ID, entryId, EntriesTableSchema.IGetFromCursor
        );
    }

    public List<SessionEntry> getEntries(long habitId) {
        int size = (int) DatabaseUtils.queryNumEntries(mReadableDatabase, EntriesTableSchema.TABLE_NAME, EntriesTableSchema.ENTRY_HABIT_ID + "=?",
                new String[]{String.valueOf(habitId)});

//        int size = getNumberOfEntries(habitId);
        List<SessionEntry> entries = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            entries.add(i, getEntry(getEntryId(habitId, i)));
        }

        Collections.sort(entries, SessionEntry.ICompareStartingTimes);

        return entries;
    }

    //region Get attributes

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

    /**
     * @param entryId The id of the entry to delete.
     * @return The number of rows removed, -1 if error.
     */
    public long deleteEntry(long entryId) {
        SessionEntry removedEntry = getEntry(entryId);

        long res = MyDatabaseUtils.deleteRecord(
                mWritableDatabase,
                EntriesTableSchema.TABLE_NAME,
                EntriesTableSchema.ENTRY_ID, entryId
        );

        notifyEntryDeleted(removedEntry);

        return res;
    }

    /**
     * @param habitId The habit which to delete it's entries.
     * @return The number of rows removed, -1 if error.
     */
    public long deleteEntriesForHabit(long habitId) {
        String whereClause = EntriesTableSchema.ENTRY_HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        long res = mWritableDatabase.delete(EntriesTableSchema.TABLE_NAME,
                whereClause, whereArgs);

        notifyEntriesReset(habitId);

        return res;
    }
    //endregion

}