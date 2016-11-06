package com.example.brandon.habitlogger.HabitDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import static com.example.brandon.habitlogger.HabitDatabase.DatabaseHelper.CATEGORY_ID;

/**
 * Created by Brandon on 10/26/2016.
 * This is the class for managing the habit database
 */

public class HabitDatabase {

    private DatabaseHelper databaseHelper;

    public HabitDatabase(Context context){
        databaseHelper = new DatabaseHelper(context);
    }

    /**
     * Delete everything from the database.
     */
    public void resetDatabase(){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        databaseHelper.resetDatabase(db);
    }

    /**
     * @param tableName The name of the table to search.
     * @param columns The columns to retrieve.
     * @param whereClause The where clause to filter the table.
     * @param selectionArgs The arguments to the where clause.
     * @return The number of rows found, -1 if query failed.
     */
    private int getNumberOfRows(String tableName, @Nullable String columns[],
                                @Nullable String whereClause, @Nullable String selectionArgs[]){

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.query(tableName, columns, whereClause, selectionArgs, null, null, null);

        int count = -1;
        if(c != null){
            count = c.getCount();
            c.close();
        }

        return count;
    }

    /**
     * @param index The index of the record to be looked up.
     * @param tableName The table name of the table to search.
     * @param rowIdColumnString The name of the row id.
     * @param whereClause The where clause used to filter the table.
     * @param selectionArgs The args to the where clause.
     * @return The found row id, -1 if found nothing.
     */
    private long getRowIdByIndex(int index, String tableName, String rowIdColumnString,
                                 @Nullable String whereClause, @Nullable String selectionArgs[]){

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.query(tableName, new String[]{rowIdColumnString}, whereClause, selectionArgs,
                null, null, null);

        long rowId = -1;
        if(c.moveToPosition(index)){
            int idInd = c.getColumnIndex(rowIdColumnString);
            rowId = c.getLong(idInd);
        }

        c.close();

        return rowId;
    }

    /**
     * @param SQL The SQL string to be executed
     * @param idColumnString The string id for the column that holds row ids.
     * @return An array of row ids found by the sqlite query.
     */
    @Nullable
    private long[] searchTableForIdsByName(String SQL, String idColumnString){

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor c = db.rawQuery(SQL, null);

        if(c != null) {
            long ids[] = new long[c.getCount()];

            while (c.moveToNext()) {
                int idInd = c.getColumnIndex(idColumnString);
                ids[c.getPosition()] = c.getLong(idInd);
            }

            c.close();
            return ids;
        }

        return null;
    }

    // ---- categories methods ----

    /**
     * @return The total number of categories stored in the database.
     */
    public int getNumberOfCategories(){
        return getNumberOfRows(DatabaseHelper.CATEGORIES_TABLE_NAME,
                new String[]{CATEGORY_ID}, null, null);
    }

    /**
     * @param category The category object to be added to the database.
     * @return The row id for the new row, -1 if error.
     */
    public long addCategory(HabitCategory category){
        long id = getCategoryIdByObject(category);

        if(id == -1) { // Make sure this category doesn't exist in the database.
            ContentValues values = new ContentValues(2);
            values.put(DatabaseHelper.CATEGORY_NAME, category.getName());
            values.put(DatabaseHelper.CATEGORY_COLOR, category.getColor());

            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            id = db.insert(DatabaseHelper.CATEGORIES_TABLE_NAME, null, values);
            category.setDatabaseId(id);
        }

        return id;
    }

    /**
     * @param index The category index to look up.
     * @return The unique category id of the row.
     */
    public long getCategoryIdFromIndex(int index){
        return getRowIdByIndex(index, DatabaseHelper.CATEGORIES_TABLE_NAME,
                CATEGORY_ID, null, null);
    }

    /**
     * @param category The category object to look for in the database.
     * @return The row id of the category found, -1 if no record found.
     */
    public long getCategoryIdByObject(HabitCategory category){
        long rowId = category.getDatabaseId();

        if(rowId == -1) {
            String whereClause = DatabaseHelper.CATEGORY_NAME + " =? AND " +
                    DatabaseHelper.CATEGORY_COLOR + " =?";
            String selectionArgs[] = {category.getName(), category.getColor()};

            rowId = getRowIdByIndex(0, DatabaseHelper.CATEGORIES_TABLE_NAME,
                    CATEGORY_ID, whereClause, selectionArgs);

            category.setDatabaseId(rowId);
        }

        return rowId;
    }

    /**
     * @param name The name of the category to look up.
     * @return The found category ids as an array.
     */
    public long[] searchCategoryIdsByName(String name){
        return searchTableForIdsByName(
                "SELECT "+ CATEGORY_ID+" FROM " +
                        DatabaseHelper.CATEGORIES_TABLE_NAME + " WHERE " +
                        DatabaseHelper.CATEGORY_NAME + " LIKE  '%" + name + "%'",

                CATEGORY_ID
        );
    }

    /**
     * @param categoryId The database id for the category to be received.
     * @return The category object from the database.
     */
    @Nullable
    public HabitCategory getCategory (long categoryId){
        String columns[]       = {DatabaseHelper.CATEGORY_NAME, DatabaseHelper.CATEGORY_COLOR};
        String selectionArgs[] = {String.valueOf(categoryId)};

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.CATEGORIES_TABLE_NAME, columns,
                CATEGORY_ID + "=?", selectionArgs, null, null, null);

        HabitCategory category = null;
        if(c != null) {
            if (c.moveToFirst()) {
                int colorInd = c.getColumnIndex(DatabaseHelper.CATEGORY_COLOR);
                int nameInd  = c.getColumnIndex(DatabaseHelper.CATEGORY_NAME);

                String color = c.getString(colorInd);
                String name  = c.getString(nameInd);
                category     = new HabitCategory(color, name);
                category.setDatabaseId(categoryId);
            }

            c.close();
        }

        return category;
    }

    /**
     * @param categoryId The database id for the category to update.
     * @param name The new name for the category.
     * @return The number of rows changed, -1 if fail.
     */
    public long updateCategoryName(long categoryId, String name){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.CATEGORY_NAME, name);

        String whereClause = CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.CATEGORIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param categoryId The database id for the category to update.
     * @param color The new color for the category.
     * @return The number of rows changed, -1 if fail.
     */
    public long updateCategoryColor(long categoryId, String color){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.CATEGORY_COLOR, color);

        String whereClause = CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.CATEGORIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param categoryId The database id for the category to update
     * @param category The new category to update the old one.
     * @return Returns the number of rows changed, -1 if failed.
     */
    public long updateCategory(long categoryId, HabitCategory category){
        if(getCategory(categoryId) != null){
            updateCategoryName(categoryId,  category.getName());
            updateCategoryColor(categoryId, category.getColor());
            return 1;
        }

        return -1;
    }

    /**
     * @param categoryId The database id for the category to delete
     * @return Returns the number of rows removed, -1 if failed.
     */
    public long deleteCategory(long categoryId){
        String whereClause = CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.CATEGORIES_TABLE_NAME, whereClause, whereArgs);
    }

    // ---- Entries methods ----

    /**
     * @param habitId The id of the habit in the database to associate this entry with.
     * @param entry The entry to be inserted.
     * @return Row id of the new row, -1 if error.
     */
    public long addEntry(long habitId, SessionEntry entry){
        ContentValues values = new ContentValues(4);
        values.put(DatabaseHelper.ENTRY_START_TIME, entry.getStartTime());
        values.put(DatabaseHelper.ENTRY_DURATION,   entry.getDuration());
        values.put(DatabaseHelper.ENTRY_NOTE,       entry.getNote());
        values.put(DatabaseHelper.ENTRY_HABIT_ID,   habitId);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long id = db.insert(DatabaseHelper.ENTRIES_TABLE_NAME, null, values);

        entry.setDatabaseId(id);
        entry.setHabitId(habitId);
        return id;
    }

    /**
     * @param habitId The habit id to search.
     * @param index The index of the entry to look up.
     * @return The id of the entry.
     */
    public long getEntryIdFromIndex(long habitId, int index){
        return getRowIdByIndex(index, DatabaseHelper.ENTRIES_TABLE_NAME, DatabaseHelper.ENTRY_ID,
                DatabaseHelper.ENTRY_HABIT_ID + "=?", new String[]{String.valueOf(habitId)});
    }

    /**
     * @param entry The entry object to look for in the database.
     * @return The row id of the entry found, -1 if it failed to locate a category.
     */
    public long getEntryIdByObject(SessionEntry entry){
        String whereClause = DatabaseHelper.ENTRY_START_TIME + " =? AND " +
                DatabaseHelper.ENTRY_DURATION + " =? AND " +
                DatabaseHelper.ENTRY_NOTE + "=?";
        String selectionArgs[] = new String[]{String.valueOf(entry.getStartTime()),
                String.valueOf(entry.getDuration()), entry.getNote()};

        long rowId = getRowIdByIndex(0, DatabaseHelper.ENTRIES_TABLE_NAME, DatabaseHelper.ENTRY_ID,
                whereClause, selectionArgs);

        entry.setDatabaseId(rowId);
        return rowId;
    }

    /**
     * @param habitId The row id for the habit to query.
     * @param query The search query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    @Nullable
    public long[] searchEntryIdsByComment(long habitId, String query){
        return searchTableForIdsByName(
                "SELECT "+DatabaseHelper.ENTRY_ID+" FROM " +
                DatabaseHelper.ENTRIES_TABLE_NAME + " WHERE " +
                DatabaseHelper.ENTRY_NOTE + " LIKE  '%" + query + "%' AND " +
                DatabaseHelper.ENTRY_HABIT_ID + "=" + String.valueOf(habitId),

                DatabaseHelper.ENTRY_ID
        );
    }

    /**
     * @param habitId The row id for the habit to search entries for.
     * @param beginTime The starting time for the query.
     * @param endTime The ending time for the query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    @Nullable
    public long[] searchEntriesWithTimeRangeForAHabit(long habitId, long beginTime, long endTime){
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE HABIT_ID=habitId
        // AND START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + DatabaseHelper.ENTRY_ID +
                " FROM " + DatabaseHelper.ENTRIES_TABLE_NAME + " WHERE " +
                DatabaseHelper.ENTRY_HABIT_ID + "=" + String.valueOf(habitId) +
                " AND " + DatabaseHelper.ENTRY_START_TIME + ">=" + String.valueOf(beginTime) +
                " AND " + DatabaseHelper.ENTRY_START_TIME + "<=" + String.valueOf(endTime);

        return searchTableForIdsByName(SQL, DatabaseHelper.ENTRY_ID);
    }

    /**
     * @param beginTime The starting time for the query.
     * @param endTime The ending time for the query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    public long[] searchAllEntriesWithTimeRange(long beginTime, long endTime){
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + DatabaseHelper.ENTRY_ID +
                " FROM " + DatabaseHelper.ENTRIES_TABLE_NAME + " WHERE " +
                DatabaseHelper.ENTRY_START_TIME + ">=" + String.valueOf(beginTime) +
                " AND " + DatabaseHelper.ENTRY_START_TIME + "<=" + String.valueOf(endTime);

        return searchTableForIdsByName(SQL, DatabaseHelper.ENTRY_ID);
    }

    /**
     * @param c A cursor object above an entry record.
     * @return An entry object.
     */
    @Nullable
    private SessionEntry getEntryFromCursor(Cursor c){
        SessionEntry entry = null;

        if(c != null){
            if(c.moveToFirst()){
                int startTimeInd = c.getColumnIndex(DatabaseHelper.ENTRY_START_TIME);
                int durationInd  = c.getColumnIndex(DatabaseHelper.ENTRY_DURATION);
                int habitIdInd   = c.getColumnIndex(DatabaseHelper.ENTRY_HABIT_ID);
                int noteInd      = c.getColumnIndex(DatabaseHelper.ENTRY_NOTE);
                int idInd        = c.getColumnIndex(DatabaseHelper.ENTRY_ID);

                long entryHabitId = c.getLong(habitIdInd);
                long databaseId   = c.getLong(idInd);
                long startTime    = c.getLong(startTimeInd);
                long duration     = c.getLong(durationInd);
                String note       = c.getString(noteInd);

                entry = new SessionEntry(startTime, duration, note);
                entry.setHabitId(entryHabitId);
                entry.setDatabaseId(databaseId);
            }
        }

        return entry;
    }

    /**
     * @param habitId The id of the habit which to look for it's entries.
     * @param entryIndex The index of the entry to be retrieved from the habit.
     * @return The found session entry row id.
     */
    public long getEntryId(long habitId, int entryIndex){
        String selection = DatabaseHelper.ENTRY_HABIT_ID + "=?";
        String selectionArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.ENTRIES_TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        long entryId = -1;
        if(c != null) {
            if (c.moveToFirst()) {
                int idInd = c.getColumnIndex(DatabaseHelper.ENTRY_ID);
                entryId   = c.getLong(idInd);
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
    public SessionEntry getEntry(long entryId){
        String selection = DatabaseHelper.ENTRY_ID + "=?";
        String selectionArgs[] = {String.valueOf(entryId)};

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.ENTRIES_TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        SessionEntry entry = null;
        if(c != null) {
            if (c.moveToFirst()) {
                entry = getEntryFromCursor(c);
            }

            c.close();
        }

        return entry;
    }

    /**
     * @param entryId The id of the entry to edit.
     * @param startTime The new start time to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryStartTime(long entryId, long startTime){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.ENTRY_START_TIME, startTime);

        String whereClause = DatabaseHelper.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.ENTRIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param entryId The id of the entry to edit.
     * @param duration The new duration to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryDuration(long entryId, long duration){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.ENTRY_DURATION, duration);

        String whereClause = DatabaseHelper.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.ENTRIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param entryId The id of the entry to edit.
     * @param note The new note to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntryNote(long entryId, String note){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.ENTRY_NOTE, note);

        String whereClause = DatabaseHelper.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.ENTRIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param entryId The id of the entry to edit.
     * @param entry The new session entry to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateEntry(long entryId, SessionEntry entry){
        if(getEntry(entryId) != null){
            updateEntryStartTime(entryId, entry.getStartTime());
            updateEntryDuration(entryId, entry.getDuration());
            updateEntryNote(entryId, entry.getNote());
            return 1;
        }

        return -1;
    }

    /**
     * @param entryId The id of the entry to delete.
     * @return The number of rows removed, -1 if error.
     */
    public long deleteEntry(long entryId){
        String whereClause = DatabaseHelper.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.ENTRIES_TABLE_NAME, whereClause, whereArgs);
    }

    /**
     * @param habitId The habit which to delete it's entries.
     * @return The number of rows removed, -1 if error.
     */
    public long deleteEntriesForHabit(long habitId){
        String whereClause = DatabaseHelper.ENTRY_HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.ENTRIES_TABLE_NAME, whereClause, whereArgs);
    }

    // ---- habits methods ----

    /**
     * @param habit The habit to be added to the database.
     * @return The row id of the new habit.
     */
    public long addHabit(Habit habit){
        long habitId = getHabitIdFromObject(habit);

        if(habitId == -1) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues(5);
            long categoryId = getCategoryIdByObject(habit.getCategory());
            if(categoryId == -1){
                categoryId = addCategory(habit.getCategory());
            }

            values.put(DatabaseHelper.HABIT_IS_ARCHIVED, habit.getIsArchived());
            values.put(DatabaseHelper.HABIT_NAME,        habit.getName());
            values.put(DatabaseHelper.HABIT_DESCRIPTION, habit.getDescription());
            values.put(DatabaseHelper.HABIT_ICON_RES_ID, habit.getIconResId());
            values.put(DatabaseHelper.HABIT_CATEGORY,    categoryId);

            habitId = db.insert(DatabaseHelper.HABITS_TABLE_NAME, null, values);

            // Add entries to the new habit
            SessionEntry entries[] = habit.getEntries();
            if (entries != null) {
                for (SessionEntry entry : entries) {
                    addEntry(habitId, entry);
                }
            }

            habit.setDatabaseId(habitId);
        }

        return habitId;
    }

    public int getNumberOfHabits(long categoryId){
        return getNumberOfRows(DatabaseHelper.HABITS_TABLE_NAME,
                new String[]{DatabaseHelper.HABIT_ID},
                DatabaseHelper.HABIT_CATEGORY + "=?",
                new String[]{String.valueOf(categoryId)});
    }

    public int getNumberOfEntries(long habitId){
        return getNumberOfRows(DatabaseHelper.ENTRIES_TABLE_NAME,
                new String[]{DatabaseHelper.ENTRY_HABIT_ID},
                DatabaseHelper.ENTRY_HABIT_ID + "=?",
                new String[]{String.valueOf(habitId)});
    }

    /**
     * @param index The habit index to look up.
     * @return The unique habit id of the row.
     */
    public long getHabitIdFromIndex(long categoryId, int index){
        return getRowIdByIndex(index, DatabaseHelper.HABITS_TABLE_NAME, DatabaseHelper.HABIT_ID,
                DatabaseHelper.HABIT_CATEGORY + "=?", new String[]{String.valueOf(categoryId)});
    }

    /**
     * @param habit The object to look up in the database.
     * @return The row id of the object in the database if found, else -1.
     */
    public long getHabitIdFromObject(Habit habit){
        long habitId = habit.getDatabaseId();

        if(habitId == -1) {
            long categoryId = getCategoryIdByObject(habit.getCategory());

            String whereClause = DatabaseHelper.HABIT_NAME + "=? AND " +
                    DatabaseHelper.HABIT_CATEGORY + "=?";

            String selectionArgs[] = {habit.getName(), String.valueOf(categoryId)};

            habitId = getRowIdByIndex(0, DatabaseHelper.HABITS_TABLE_NAME, DatabaseHelper.HABIT_ID,
                    whereClause, selectionArgs);
        }

        return habitId;
    }

    /**
     * @param name The name of the habit to look up.
     * @param categoryId The category id of the habits to search.
     * @return The found habit ids.
     */
    public long[] searchHabitIdsByName(String name, long categoryId){
        return searchTableForIdsByName("SELECT "+DatabaseHelper.HABIT_ID+" FROM " +
                DatabaseHelper.HABITS_TABLE_NAME + " WHERE " +
                DatabaseHelper.HABIT_NAME + " LIKE  '%" + name + "%' AND " +
                DatabaseHelper.HABIT_CATEGORY + "=" +String.valueOf(categoryId),

                DatabaseHelper.HABIT_ID);
    }

    private Habit getHabitFromCursor(Cursor c){
        Habit habit = null;

        if(c != null) {
            if(c.moveToFirst()) {
                int descriptionInd = c.getColumnIndex(DatabaseHelper.HABIT_DESCRIPTION);
                int isArchivedInd  = c.getColumnIndex(DatabaseHelper.HABIT_IS_ARCHIVED);
                int iconResIdInd   = c.getColumnIndex(DatabaseHelper.HABIT_ICON_RES_ID);
                int categoryInd    = c.getColumnIndex(DatabaseHelper.HABIT_CATEGORY);
                int habitIdInd     = c.getColumnIndex(DatabaseHelper.HABIT_ID);
                int nameInd        = c.getColumnIndex(DatabaseHelper.HABIT_NAME);

                long categoryId        = c.getLong(categoryInd);
                HabitCategory category = getCategory(categoryId);
                boolean isArchived     = (c.getInt(isArchivedInd) == 1);
                String description     = c.getString(descriptionInd);
                String iconResId       = c.getString(iconResIdInd);
                long habitId           = c.getLong(habitIdInd);
                String name            = c.getString(nameInd);

                SessionEntry entries[] = new SessionEntry[getNumberOfEntries(habitId)];
                for (int i = 0; i < getNumberOfEntries(habitId); i++) {
                    entries[i] = getEntry(getEntryId(habitId, i));
                }

                if(category != null) {
                    habit = new Habit(name, description, category, entries, iconResId);
                    habit.setDatabaseId(habitId);
                    habit.setIsArchived(isArchived);
                }
            }
        }

        return habit;
    }

    /**
     * @param habitId The row id of the habit you wish to receive.
     * @return The habit object found.
     */
    @Nullable
    public Habit getHabit(long habitId){
        String selectionArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.HABITS_TABLE_NAME, null, DatabaseHelper.HABIT_ID + "=?",
                selectionArgs, null, null, null);

        Habit habit = getHabitFromCursor(c);

        c.close();

        return habit;
    }

    /**
     * @param categoryId The category to fetch habits from.
     * @return an array list containing the habits.
     */
    public Habit[] getHabits(long categoryId){
        Habit[] habits = new Habit[getNumberOfHabits(categoryId)];

        long ids[] = searchTableForIdsByName("SELECT "+DatabaseHelper.HABIT_ID+" FROM "+
                DatabaseHelper.HABITS_TABLE_NAME+" WHERE " +
                DatabaseHelper.HABIT_CATEGORY +" = "+String.valueOf(categoryId),

                DatabaseHelper.HABIT_ID);

        if(ids != null) {
            for (int i = 0; i < ids.length; i++) {
                habits[i] = getHabit(ids[i]);
            }
        }

        return habits;
    }

    /**
     * @param habitId The row id to be updated.
     * @param state Set true to archive the habit, false to unarchive.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitIsArchived(long habitId, boolean state){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_IS_ARCHIVED, state);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The row id to be updated.
     * @param name The new name to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitName(long habitId, String name){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_NAME, name);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The row id to be updated.
     * @param description The new description to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitDescription(long habitId, String description){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_DESCRIPTION, description);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The row id to be updated.
     * @param categoryId The new categoryId to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitCategory(long habitId, @Nullable Long categoryId){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_CATEGORY, categoryId);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The row id to be updated.
     * @param iconResId The new iconResId to replace the old one.
     * @return The number of rows changed, -1 if error.
     */
    public long updateHabitIconResId(long habitId, String iconResId){
        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_ICON_RES_ID, iconResId);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The id of the habit to edit.
     * @param habit A new habit to replace the old one.
     * @return The number of rows removed, -1 if error.
     */
    public long updateHabit(long habitId, Habit habit){
        if(getHabit(habitId) != null){
            updateHabitName(habitId, habit.getName());
            updateHabitDescription(habitId, habit.getDescription());
            updateHabitCategory(habitId, getCategoryIdByObject(habit.getCategory()));
            updateHabitIconResId(habitId, habit.getIconResId());
            return 1;
        }

        return -1;
    }

    /**
     * @param habitId The row id of the habit to be removed.
     * @return The number of rows removed, -1 if error.
     */
    public long deleteHabit(long habitId){
        // Delete all of the habit's entries
        deleteEntriesForHabit(habitId);

        // Delete the habit itself
        String whereClause = DatabaseHelper.HABIT_ID + "=?";
        String whereArgs[] = {String.valueOf(habitId)};

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.HABITS_TABLE_NAME, whereClause, whereArgs);
    }
}