package com.example.brandon.habitlogger.HabitDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.CategoryHabitsContainer;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.CategoriesTableSchema;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.DatabaseSchema;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.EntriesTableSchema;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.HabitsTableSchema;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

import java.util.ArrayList;
import java.util.Arrays;
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
    public DatabaseSchema databaseHelper;
    private SQLiteDatabase writableDatabase;
    private SQLiteDatabase readableDatabase;
    private Context mContext;

    public SessionEntriesSample getEntriesSample(long habitId, long dateFrom, long dateTo) {
        List<SessionEntry> entries = lookUpEntries(
                searchEntriesWithTimeRangeForAHabit(habitId, dateFrom, dateTo)
        );

        return new SessionEntriesSample(entries, dateFrom, dateTo);
    }

    //region // Interface methods for database

    public interface OnEntryChangedListener {
        /**
         * This method is only called when individual entries are removed.
         */
        void onEntryDeleted(SessionEntry removedEntry);

        /**
         * This method is only called when individual entries are updated.
         */
        void onEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry);
    }

    private static List<OnEntryChangedListener> onEntryChangedListeners = new ArrayList<>();

    private void notifyEntryDeleted(SessionEntry removedEntry) {
        for (OnEntryChangedListener listener : onEntryChangedListeners) {
            listener.onEntryDeleted(removedEntry);
        }
    }

    private void notifyEntryUpdated(SessionEntry oldEntry, SessionEntry newEntry) {
        for (OnEntryChangedListener listener : onEntryChangedListeners) {
            listener.onEntryUpdated(oldEntry, newEntry);
        }
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

        mContext = context;
        writableDatabase = databaseHelper.getWritableDatabase();
        readableDatabase = databaseHelper.getReadableDatabase();
    }

    /**
     * @param tableName     The name of the table to search.
     * @param columns       The columns to retrieve.
     * @param whereClause   The where clause to filter the table.
     * @param selectionArgs The arguments to the where clause.
     * @return The number of rows found, -1 if query failed.
     */
    private int getNumberOfRows(String tableName, @Nullable String columns[],
                                @Nullable String whereClause, @Nullable String selectionArgs[]) {

        Cursor c = readableDatabase.query(tableName, columns, whereClause, selectionArgs,
                null, null, null);

        int count = -1;
        if (c != null) {
            count = c.getCount();
            c.close();
        }

        return count;
    }

    /**
     * @param index             The index of the record to be looked up.
     * @param tableName         The table name of the table to search.
     * @param rowIdColumnString The name of the row id.
     * @param whereClause       The where clause used to filter the table.
     * @param selectionArgs     The args to the where clause.
     * @return The found row id, -1 if found nothing.
     */
    private long getRowIdByIndex(int index, String tableName, String rowIdColumnString,
                                 @Nullable String whereClause, @Nullable String selectionArgs[],
                                 String limit) {

        Cursor c = readableDatabase.query(tableName, new String[]{rowIdColumnString}, whereClause,
                selectionArgs, null, null, null, limit);

        long rowId = -1;
        if (c.moveToPosition(index)) {
            rowId = c.getLong(0);
        }

        c.close();

        return rowId;
    }

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

        return getRowIdByIndex(index, tableName, rowIdColumnString, whereClause, selectionArgs, null);
    }

    /**
     * @param SQL            The SQL string to be executed
     * @param idColumnString The string id for the column that holds row ids.
     * @return An array of row ids found by the sqlite query.
     */
    @Nullable
    private Set<Long> searchTableForIdsByName(String SQL, String[] values, String idColumnString) {

        Cursor c = readableDatabase.rawQuery(SQL, values);

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

    // ---- categories methods ----

    private SQLiteStatement getCategoryInsertStatement() {
        return writableDatabase.compileStatement(CategoriesTableSchema.getInsertStatement());
    }

    /**
     * @return The total number of categories stored in the database.
     */
    public int getNumberOfCategories() {
        return getNumberOfRows(CategoriesTableSchema.TABLE_NAME,
                new String[]{CategoriesTableSchema.CATEGORY_ID}, null, null);
    }

    private void addCategories(HabitCategory categories[], boolean whenExists) {
        writableDatabase.beginTransaction();

        if (whenExists) {
            for (HabitCategory category : categories) {
                addCategory(category);
            }
        }
        else {
            for (HabitCategory category : categories) {
                addCategoryIfNotExists(category);
            }
        }

        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
    }

    public void addCategories(HabitCategory categories[]) {
        addCategories(categories, true);
    }

    public void addCategoriesWhenNotExists(HabitCategory categories[]) {
        addCategories(categories, false);
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

        Cursor c = readableDatabase.query(CategoriesTableSchema.TABLE_NAME, columns,
                CategoriesTableSchema.CATEGORY_ID + "=?", selectionArgs, null, null, null);

        HabitCategory category = null;

        if (c != null) {
            if (c.moveToFirst()) {
                String name = c.getString(0); // The name field is 1st in the db
                String color = c.getString(1); // The color field is 2nd in the db

                category = new HabitCategory(color, name);
                category.setDatabaseId(categoryId);
            }

            c.close();
        }

        return category;
    }

    public List<HabitCategory> getCategories() {
        Cursor cursor = readableDatabase.query(CategoriesTableSchema.TABLE_NAME,
                null, null, null, null, null, null);

        int size = cursor.getCount();
        List<HabitCategory> categories = new ArrayList<>(size);

        cursor.moveToFirst();
        do categories.add(getHabitCategoryFromCursor(cursor)); while (cursor.moveToNext());

        cursor.close();

        categories.add(0, HabitCategory.getUncategorizedCategory(mContext));

        return categories;
    }

    private HabitCategory getHabitCategoryFromCursor(Cursor cursor) {
        ContentValues contentValues = new ContentValues(cursor.getColumnCount());
        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);

        return CategoriesTableSchema.getObjectFromContentValues(contentValues);
    }

    /**
     * @param categoryId The database id for the category to update.
     * @param name       The new name for the category.
     * @return The number of rows changed, -1 if fail.
     */
    public long updateCategoryName(long categoryId, String name) {
        ContentValues values = new ContentValues(1);
        values.put(CategoriesTableSchema.CATEGORY_NAME, name);

        String whereClause = CategoriesTableSchema.CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        return writableDatabase.update(CategoriesTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param categoryId The database id for the category to update.
     * @param color      The new color for the category.
     * @return The number of rows changed, -1 if fail.
     */
    public long updateCategoryColor(long categoryId, String color) {
        ContentValues values = new ContentValues(1);
        values.put(CategoriesTableSchema.CATEGORY_COLOR, color);

        String whereClause = CategoriesTableSchema.CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        return writableDatabase.update(CategoriesTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

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

        return writableDatabase.delete(CategoriesTableSchema.TABLE_NAME,
                whereClause, whereArgs);
    }

    // ---- Entries methods ----

    private SQLiteStatement getEntryInsertStatement() {
        return writableDatabase.compileStatement(EntriesTableSchema.getInsertStatement());
    }

    private void bindValuesForEntryObject(SQLiteStatement insert, SessionEntry entry, long habitId) {
        insert.bindLong(1, habitId);
        insert.bindLong(2, entry.getStartTime());
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

    public void addEntries(long habitId, @NonNull SessionEntry entries[]) {

        SQLiteStatement insert = getEntryInsertStatement();
        writableDatabase.beginTransaction();


        for (SessionEntry entry : entries) {
            bindValuesForEntryObject(insert, entry, habitId);

            long id = insert.executeInsert();
            entry.setDatabaseId(id);
            entry.setHabitId(habitId);
        }

        insert.close();
        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
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
        String selectionArgs[] = new String[]{String.valueOf(entry.getStartTime()),
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
     * @param habitId   The row id for the habit to search entries for.
     * @param beginTime The starting time for the query.
     * @param endTime   The ending time for the query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    @Nullable
    public Set<Long> searchEntriesWithTimeRangeForAHabit(long habitId, long beginTime, long endTime) {
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE HABIT_ID=habitId
        // AND START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + EntriesTableSchema.ENTRY_ID +
                " FROM " + EntriesTableSchema.TABLE_NAME + " WHERE " +
                EntriesTableSchema.ENTRY_HABIT_ID + "=? AND " +
                EntriesTableSchema.ENTRY_START_TIME + ">=? AND " +
                EntriesTableSchema.ENTRY_START_TIME + "<=?";

        String[] values = new String[]{
                String.valueOf(habitId),
                String.valueOf(beginTime),
                String.valueOf(endTime)
        };

        return searchTableForIdsByName(SQL, values, EntriesTableSchema.ENTRY_ID);
    }

    /**
     * @param beginTime The starting time for the query.
     * @param endTime   The ending time for the query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    public Set<Long> searchAllEntriesWithTimeRange(long beginTime, long endTime) {
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + EntriesTableSchema.ENTRY_ID +
                " FROM " + EntriesTableSchema.TABLE_NAME + " WHERE " +
                EntriesTableSchema.ENTRY_START_TIME + ">=? AND " +
                EntriesTableSchema.ENTRY_START_TIME + "<=?";

        String[] values = new String[]{
                String.valueOf(beginTime),
                String.valueOf(endTime)
        };

        return searchTableForIdsByName(SQL, values, EntriesTableSchema.ENTRY_ID);
    }

    public List<SessionEntry> lookUpEntries(Set<Long> ids) {
        List<SessionEntry> entries = new ArrayList<>(ids.size());

        for (long id : ids) {
            entries.add(getEntry(id));
        }

        Collections.sort(entries, SessionEntry.StartingTimeComparator);

        return entries;
    }

    /**
     * @param c A cursor object above an entry record.
     * @return An entry object.
     */
    @Nullable
    private SessionEntry getEntryFromCursor(Cursor c) {
        long databaseId = c.getLong(0);
        long habitId = c.getLong(1);
        long startTime = c.getLong(2);
        long duration = c.getLong(3);
        String note = c.getString(4);

        SessionEntry entry = new SessionEntry(startTime, duration, note);
        entry.setHabitId(habitId);
        entry.setDatabaseId(databaseId);

        return entry;
    }

    /**
     * @param habitId    The id of the habit which to look for it's entries.
     * @param entryIndex The index of the entry to be retrieved from the habit.
     * @return The found session entry row id.
     */
    public long getEntryId(long habitId, int entryIndex) {
        String selection = EntriesTableSchema.ENTRY_HABIT_ID + "=?";
        String selectionArgs[] = {String.valueOf(habitId)};

        Cursor c = readableDatabase.query(EntriesTableSchema.TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        long entryId = -1;
        if (c != null) {
            if (c.move(entryIndex + 1)) {
                int idInd = c.getColumnIndex(EntriesTableSchema.ENTRY_ID);
                entryId = c.getLong(idInd);
            }
            c.close();
        }

        return entryId;
    }

    /**
     * @param entryId The id of the session entry to look up.
     * @return The found session entry.
     */
    @Nullable
    public SessionEntry getEntry(long entryId) {
        String selection = EntriesTableSchema.ENTRY_ID + "=?";
        String selectionArgs[] = {String.valueOf(entryId)};

        Cursor c = readableDatabase.query(EntriesTableSchema.TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        SessionEntry entry = null;
        if (c != null) {
            if (c.moveToFirst()) {
                entry = getEntryFromCursor(c);
            }

            c.close();
        }

        return entry;
    }

    public List<SessionEntry> getEntries(long habitId) {
        int size = getNumberOfEntries(habitId);
        List<SessionEntry> entries = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            entries.add(i, getEntry(getEntryId(habitId, i)));
        }

        Collections.sort(entries, SessionEntry.StartingTimeComparator);

        return entries;
    }

    /**
     * @param entryId   The id of the entry to edit.
     * @param startTime The new start time to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryStartTime(long entryId, long startTime) {
        ContentValues values = new ContentValues(1);
        values.put(EntriesTableSchema.ENTRY_START_TIME, startTime);

        String whereClause = EntriesTableSchema.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        return writableDatabase.update(EntriesTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param entryId  The id of the entry to edit.
     * @param duration The new duration to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryDuration(long entryId, long duration) {
        ContentValues values = new ContentValues(1);
        values.put(EntriesTableSchema.ENTRY_DURATION, duration);

        String whereClause = EntriesTableSchema.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        return writableDatabase.update(EntriesTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param entryId The id of the entry to edit.
     * @param note    The new note to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryNote(long entryId, String note) {
        ContentValues values = new ContentValues(1);
        values.put(EntriesTableSchema.ENTRY_NOTE, note);

        String whereClause = EntriesTableSchema.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        return writableDatabase.update(EntriesTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param entryId The id of the entry to edit.
     * @param newEntry   The new session entry to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntry(long entryId, SessionEntry newEntry) {
        SessionEntry oldEntry = getEntry(entryId);

        if (oldEntry != null) {
            updateEntryStartTime(entryId, newEntry.getStartTime());
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
        notifyEntryDeleted(removedEntry);

        String whereClause = EntriesTableSchema.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        return writableDatabase.delete(EntriesTableSchema.TABLE_NAME,
                whereClause, whereArgs);
    }

    /**
     * @param habitId The habit which to delete it's entries.
     * @return The number of rows removed, -1 if error.
     */
    public long deleteEntriesForHabit(long habitId) {
        String whereClause = EntriesTableSchema.ENTRY_HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return writableDatabase.delete(EntriesTableSchema.TABLE_NAME,
                whereClause, whereArgs);
    }

    // ---- habits methods ----

    private SQLiteStatement getHabitInsertStatement() {
        return writableDatabase.compileStatement(HabitsTableSchema.getInsertStatement());
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
        SessionEntry entries[] = habit.getEntries();
        if (entries != null) {
            addEntries(habitId, entries);
        }

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

    public int getNumberOfHabits(long categoryId) {
        return getNumberOfRows(HabitsTableSchema.TABLE_NAME,
                new String[]{HabitsTableSchema.HABIT_ID},
                HabitsTableSchema.HABIT_CATEGORY + "=?",
                new String[]{String.valueOf(categoryId)});
    }

    public int getNumberOfEntries(long habitId) {
        return getNumberOfRows(EntriesTableSchema.TABLE_NAME,
                new String[]{EntriesTableSchema.ENTRY_HABIT_ID},
                EntriesTableSchema.ENTRY_HABIT_ID + "=?",
                new String[]{String.valueOf(habitId)});
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
    public Set<Long> queryDatabaseByTheUser(String query) {
        Set<Long> habitIds = searchHabitIdsByName(query);
        Set<Long> categoryIds = searchCategoryIdsByName(query);

        Set<Long> habitIdsByCategory = new HashSet<>();
        for (long id : categoryIds) {
            habitIdsByCategory.addAll(getHabitIds(id));
        }

        Set<Long> ids = new HashSet<>(habitIds.size() + habitIdsByCategory.size());
        ids.addAll(habitIds);
        ids.addAll(habitIdsByCategory);

        return ids;
    }

    public String getHabitName(long id) {
        Cursor c = readableDatabase.query(HabitsTableSchema.TABLE_NAME, new String[]{HabitsTableSchema.HABIT_NAME},
                HabitsTableSchema.HABIT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

        String name = null;

        if (c != null) {
            if (c.moveToFirst()) {
                int nameInd = c.getColumnIndex(HabitsTableSchema.HABIT_NAME);
                name = c.getString(nameInd);
                c.close();
            }
        }

        return name;
    }

    private Habit getHabitFromCursor(Cursor c) {
        Habit habit = null;

        long habitId = c.getLong(0);       // c.getColumnIndex(HabitsTableSchema.HABIT_ID);
        boolean isArchived = (c.getInt(1) == 1); // c.getColumnIndex(HabitsTableSchema.HABIT_IS_ARCHIVED);
        String name = c.getString(2);     // c.getColumnIndex(HabitsTableSchema.HABIT_NAME);
        String description = c.getString(3);     // c.getColumnIndex(HabitsTableSchema.HABIT_DESCRIPTION);
        long categoryId = c.getLong(4);       // c.getColumnIndex(HabitsTableSchema.HABIT_CATEGORY);
        String iconResId = c.getString(5);     // c.getColumnIndex(HabitsTableSchema.HABIT_ICON_RES_ID);

        HabitCategory category = getCategory(categoryId);

        if (category != null) {
            habit = new Habit(name, description, category, iconResId, null);
            habit.setDatabaseId(habitId);
            habit.setIsArchived(isArchived);
        }

        return habit;
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

    public long getHabitCategoryId(long habitId) {
        Cursor c = readableDatabase.query(HabitsTableSchema.TABLE_NAME, new String[]{HabitsTableSchema.HABIT_CATEGORY},
                HabitsTableSchema.HABIT_ID + "=?", new String[]{String.valueOf(habitId)}, null, null, null);

        long categoryId = -1;

        if (c != null) {
            if (c.moveToFirst()) {
                int categoryInd = c.getColumnIndex(HabitsTableSchema.HABIT_CATEGORY);
                categoryId = c.getLong(categoryInd);
                c.close();
            }
        }

        return categoryId;
    }

    public int getHabitColor(long habitId) {
        if (getIsHabitArchived(habitId)) return 0xFFCCCCCC;
        else {
            HabitCategory category = getCategory(getHabitCategoryId(habitId));
            if (category != null) {
                return category.getColorAsInt();
            }
        }

        return -1;
    }

    public Set<Long> getHabitIds(long categoryId) {
        return searchTableForIdsByName("SELECT " + HabitsTableSchema.HABIT_ID + " FROM " +
                        HabitsTableSchema.TABLE_NAME + " WHERE " +
                        HabitsTableSchema.HABIT_CATEGORY + " =?",
                new String[]{String.valueOf(categoryId)}, HabitsTableSchema.HABIT_ID);
    }

    /**
     * @param categoryId The category to fetch habits from.
     * @return an array list containing the habits.
     */
    public Habit[] getHabits(long categoryId) {
        int numberOfHabits = getNumberOfHabits(categoryId);
        Habit[] habits = new Habit[numberOfHabits];

        Cursor c = readableDatabase.query(HabitsTableSchema.TABLE_NAME, null,
                HabitsTableSchema.HABIT_CATEGORY + " =?", new String[]{String.valueOf(categoryId)},
                null, null, null);

        int habitIndex = 0;
        while (c.moveToNext()) {
            habits[habitIndex++] = getHabitFromCursor(c);
        }

        c.close();

        return habits;
    }

    public ArrayList<Habit> getHabits() {
        ArrayList<Habit> habits = new ArrayList<>();

        int numberOfCategories = getNumberOfCategories();

        for (int i = 0; i < numberOfCategories; i++) {
            long categoryId = getCategoryIdFromIndex(i);
            habits.addAll(Arrays.asList(getHabits(categoryId)));
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

    public CategoryHabitsContainer getCategoryHabitsContainer(long categoryId) {
        Set<Long> ids = getHabitIds(categoryId);
        List<Habit> habits = lookUpHabits(ids);

        return new CategoryHabitsContainer(getCategory(categoryId), habits);
    }

    public List<CategoryHabitsContainer> getCategoryHabitsContainers() {
        int size = getNumberOfCategories();
        List<CategoryHabitsContainer> containers = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            long id = getCategoryIdFromIndex(i);
            containers.add(getCategoryHabitsContainer(id));
        }

        return containers;
    }

    public boolean getIsHabitArchived(long habitId) {
        Cursor c = readableDatabase.query(HabitsTableSchema.TABLE_NAME, new String[]{HabitsTableSchema.HABIT_IS_ARCHIVED},
                HabitsTableSchema.HABIT_ID + "=?", new String[]{String.valueOf(habitId)}, null, null, null);

        boolean isArchived = false;

        if (c != null) {
            if (c.moveToFirst()) {
                isArchived = c.getInt(0) != 0;
                c.close();
            }
        }

        return isArchived;
    }

    /**
     * @param habitId The row id to be updated.
     * @param state   Set true to archive the habit, false to unarchive.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitIsArchived(long habitId, boolean state) {
        ContentValues values = new ContentValues(1);
        values.put(HabitsTableSchema.HABIT_IS_ARCHIVED, state);

        String whereClause = HabitsTableSchema.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return writableDatabase.update(HabitsTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.update(HabitsTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param habitId     The row id to be updated.
     * @param description The new description to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitDescription(long habitId, String description) {
        ContentValues values = new ContentValues(1);
        values.put(HabitsTableSchema.HABIT_DESCRIPTION, description);

        String whereClause = HabitsTableSchema.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return writableDatabase.update(HabitsTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param habitId    The row id to be updated.
     * @param categoryId The new categoryId to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitCategory(long habitId, @Nullable Long categoryId) {
        ContentValues values = new ContentValues(1);
        values.put(HabitsTableSchema.HABIT_CATEGORY, categoryId);

        String whereClause = HabitsTableSchema.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return writableDatabase.update(HabitsTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param habitId   The row id to be updated.
     * @param iconResId The new iconResId to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitIconResId(long habitId, String iconResId) {
        ContentValues values = new ContentValues(1);
        values.put(HabitsTableSchema.HABIT_ICON_RES_ID, iconResId);

        String whereClause = HabitsTableSchema.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return writableDatabase.update(HabitsTableSchema.TABLE_NAME, values,
                whereClause, whereArgs);
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
            if (categoryId == -1) {
                categoryId = addCategory(category);
            }

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

        return writableDatabase.delete(HabitsTableSchema.TABLE_NAME,
                whereClause, whereArgs);
    }

    public CategoryDataSample getCategoryDataSample(HabitCategory category, long dateFrom, long dateTo) {
        Habit[] habits = getHabits(category.getDatabaseId());

        for (Habit habit : habits) {
            long habitId = habit.getDatabaseId();
            Set<Long> ids = searchEntriesWithTimeRangeForAHabit(habitId, dateFrom, dateTo);
            List<SessionEntry> entries = lookUpEntries(ids);
            habit.setEntries(entries);
        }

        return new CategoryDataSample(category, habits, dateFrom, dateTo);
    }

    public HabitDataSample getHabitDataSample(long dateFrom, long dateTo) {
        List<CategoryDataSample> data = new ArrayList<>();

        for (HabitCategory category : getCategories()) {
            data.add(getCategoryDataSample(category, dateFrom, dateTo));
        }

        return new HabitDataSample(data, dateFrom, dateTo);
    }
}