package com.example.brandon.habitlogger.HabitDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.example.brandon.habitlogger.HabitDatabase.DatabaseHelper.CATEGORY_ID;

/**
 * Created by Brandon on 10/26/2016.
 * This is the class for managing the habit database
 */

public class HabitDatabase {

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase writableDatabase;
    private SQLiteDatabase readableDatabase;
    public  DatabaseCache dataCache;
    boolean useCache = false;
    private Context context;

    private SQLiteStatement insertHabitStatement;
    private SQLiteStatement insertEntryStatement;
    private SQLiteStatement insertCategoryStatement;

    public HabitDatabase(Context context, @Nullable Serializable cache, boolean useCache){
        databaseHelper = new DatabaseHelper(context);
        this.useCache = useCache;
        this.context = context;

        writableDatabase = databaseHelper.getWritableDatabase();
        readableDatabase = databaseHelper.getReadableDatabase();

        insertHabitStatement    = getHabitInsertStatement();
        insertEntryStatement    = getEntryInsertStatement();
        insertCategoryStatement = getCategoryInsertStatement();

        if(cache == null && useCache) {
            dataCache = new DatabaseCache();
            loadAllContents();
        }else if(useCache){
            dataCache = (DatabaseCache)cache;
        }
    }

    private OnDatabaseChange changeListener = null;

    public void setOnDatabaseChangeListener(OnDatabaseChange listener){
        changeListener = listener;
    }

    public interface OnDatabaseChange {
        void onDatabaseChanged();

        void onDatabaseClear();
    }

    /**
     * Run the call back method set on the database.
     */
    public void notifyChange(){
        if(changeListener != null) {
            changeListener.onDatabaseChanged();
        }
    }

    /**
     * Run the call back method set on the database.
     */
    public void notifyDatabaseClear(){
        if(changeListener != null) {
            changeListener.onDatabaseClear();
        }
    }

    private class loadAllContents extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            if(dataCache != null) {
                dataCache.clearCache();
                for (int categoryIndex = 0; categoryIndex < getNumberOfCategories(); categoryIndex++) {
                    long categoryId = getCategoryIdFromIndex(categoryIndex);

                    Habit loadHabits[] = getHabits(categoryId);
                    dataCache.cacheHabits(loadHabits);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyChange();
        }

    }


    public void loadAllContents(){
        new loadAllContents().execute();
    }

    /**
     * Delete everything from the database.
     */
    public void resetDatabase(){
        databaseHelper.resetDatabase(writableDatabase);
        if(useCache) {
            dataCache.clearCache();
        }
        notifyDatabaseClear();
    }

    /**
     * @return The name of the database stored in memory.
     */
    public String getDatabaseName(){
        return DatabaseHelper.DATABASE_NAME;
    }

    /**
     * @return A byte array representation of the database.
     */
    public byte[] getBytes(){
        try {
            FileChannel src = databaseHelper.getInputChannel();

            ByteBuffer buffer = ByteBuffer.allocate((int)src.size());
            src.read(buffer);
            return buffer.array();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param bytes A byte array representation of the database.
     * @return True if success, false if failure.
     */
    public boolean setBytes(ByteBuffer bytes){
        try {
            FileChannel dst = databaseHelper.getOutputChannel();

            dst.write(bytes);
            dst.close();

            notifyChange();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return Create limited string representation of the database contents.
     */
    public String toString(){
        StringBuilder databaseString = new StringBuilder();

        for(int categoryIndex = 0; categoryIndex < getNumberOfCategories(); categoryIndex++) {
            long categoryId = getCategoryIdFromIndex(categoryIndex);

            Habit habits[] = getHabits(categoryId);
            for (Habit eachHabit : habits) {
                databaseString.append(eachHabit.toString());
            }
        }

        databaseString.append(getNumberOfCategories());

        return databaseString.toString();
    }

    /**
     * @return Create limited string representation of the database contents.
     */
    public String toString2(){
        StringBuilder databaseString = new StringBuilder();

        for(int categoryIndex = 0; categoryIndex < getNumberOfCategories(); categoryIndex++){
            long categoryId = getCategoryIdFromIndex(categoryIndex);

            Set<Long> ids = getHabitIds(categoryId);
            for(long id : ids){
                String name = getHabitName(id);
                databaseString.append(name);
                databaseString.append("\n");
            }
        }

        return databaseString.toString();
    }

    /**
     * @return Create limited string representation of the database contents.
     */
    public String toString3(){
        StringBuilder databaseString = new StringBuilder();

        for(Map.Entry<Long, Habit> entry : dataCache.habits.entrySet()){
            databaseString.append(entry.getValue().toString());
        }

        databaseString.append(getNumberOfCategories());

        return databaseString.toString();
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

        Cursor c = readableDatabase.query(tableName, columns, whereClause, selectionArgs,
                null, null, null);

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
                                 @Nullable String whereClause, @Nullable String selectionArgs[],
                                 String limit){

        Cursor c = readableDatabase.query(tableName, new String[]{rowIdColumnString}, whereClause,
                selectionArgs, null, null, null, limit);

        long rowId = -1;
        if(c.moveToPosition(index)){
            rowId = c.getLong(0);
        }

        c.close();

        return rowId;
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

        return getRowIdByIndex(index, tableName, rowIdColumnString, whereClause, selectionArgs, null);
    }

    /**
     * @param SQL The SQL string to be executed
     * @param idColumnString The string id for the column that holds row ids.
     * @return An array of row ids found by the sqlite query.
     */
    @Nullable
    private Set<Long> searchTableForIdsByName(String SQL, String[] values, String idColumnString){

        Cursor c = readableDatabase.rawQuery(SQL, values);

        if(c != null) {
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

    private SQLiteStatement getCategoryInsertStatement(){
        String sql = "INSERT INTO "+DatabaseHelper.CATEGORIES_TABLE_NAME+
                " ("+DatabaseHelper.CATEGORY_NAME+
                ", "+DatabaseHelper.CATEGORY_COLOR+
                ") VALUES(?, ?)";

        return writableDatabase.compileStatement(sql);
    }

    /**
     * @return The total number of categories stored in the database.
     */
    public int getNumberOfCategories(){
        return getNumberOfRows(DatabaseHelper.CATEGORIES_TABLE_NAME,
                new String[]{CATEGORY_ID}, null, null);
    }

   private void addCategories(HabitCategory categories[], boolean whenExists){
        writableDatabase.beginTransaction();

        if(whenExists) {
            for (HabitCategory category : categories) {
                addCategory(category);
            }
        } else {
            for (HabitCategory category : categories) {
                addCategoryIfNotExists(category);
            }

        }

        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
    }

    public void addCategories(HabitCategory categories[]){
        addCategories(categories, true);
    }

    public void addCategoriesWhenNotExists(HabitCategory categories[]){
        addCategories(categories, false);
    }

    /**
     * @param category The category object to be added to the database.
     * @return The row id for the new row, -1 if error.
     */
    public long addCategory(HabitCategory category){
        insertCategoryStatement.bindString(1, category.getName());
        insertCategoryStatement.bindString(2, category.getColor());

        long id = insertCategoryStatement.executeInsert();
        category.setDatabaseId(id);

        if(useCache){
            dataCache.cacheCategory(category);
        }

        return id;
    }

    /**
     * @param category The category object to be added to the database.
     * @return The row id for the new row, -1 if error.
     */
    public long addCategoryIfNotExists(HabitCategory category){
        long id = getCategoryIdByObject(category);

        if(id == -1) { // Make sure this category doesn't exist in the database.
            id = addCategory(category);
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

        if(rowId < 0) {
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
     * @param categoryId The database id for the category to be received.
     * @return The category object from the database.
     */
    @Nullable
    public HabitCategory getCategory(long categoryId){
        String columns[]       = {DatabaseHelper.CATEGORY_NAME, DatabaseHelper.CATEGORY_COLOR};
        String selectionArgs[] = {String.valueOf(categoryId)};

        Cursor c = readableDatabase.query(DatabaseHelper.CATEGORIES_TABLE_NAME, columns,
                CATEGORY_ID + "=?", selectionArgs, null, null, null);

        HabitCategory category = null;

        if(c != null) {
            if (c.moveToFirst()) {
                String name  = c.getString(0); // The name field is 1st in the db
                String color = c.getString(1); // The color field is 2nd in the db

                category = new HabitCategory(color, name);
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

        return writableDatabase.update(DatabaseHelper.CATEGORIES_TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.update(DatabaseHelper.CATEGORIES_TABLE_NAME, values,
                whereClause, whereArgs);
    }

    /**
     * @param categoryId The database id for the category to update
     * @param category The new category to update the old one.
     * @return Returns the number of rows changed, -1 if failed.
     */
    public long updateCategory(long categoryId, HabitCategory category){
        if(getCategory(categoryId) != null){
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
    public long deleteCategory(long categoryId){
        String whereClause = CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        return writableDatabase.delete(DatabaseHelper.CATEGORIES_TABLE_NAME,
                whereClause, whereArgs);
    }

    // ---- Entries methods ----

    private SQLiteStatement getEntryInsertStatement(){
        return writableDatabase.compileStatement("INSERT INTO ENTRIES_TABLE " +
                "(HABIT_ID, START_TIME, DURATION, NOTE) VALUES(?, ?, ?, ?);");
    }

    private void bindValuesForEntryObject(long habitId, SessionEntry entry){
        insertEntryStatement.bindLong(1, habitId);
        insertEntryStatement.bindLong(2, entry.getStartTime());
        insertEntryStatement.bindLong(3, entry.getDuration());
        insertEntryStatement.bindString(4, entry.getNote());
    }

    /**
     * @param habitId The id of the habit in the database to associate this entry with.
     * @param entry The entry to be inserted.
     * @return Row id of the new row, -1 if error.
     */
    public long addEntry(long habitId, SessionEntry entry){
        bindValuesForEntryObject(habitId, entry);

        long id = insertEntryStatement.executeInsert();

        entry.setDatabaseId(id);
        entry.setHabitId(habitId);
        return id;
    }

    public void addEntries(long habitId, @NonNull SessionEntry entries[]){

        writableDatabase.beginTransaction();

        for (SessionEntry entry : entries) {
            bindValuesForEntryObject(habitId, entry);
            insertEntryStatement.execute();

            entry.setHabitId(habitId);
        }

        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
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
    public Set<Long> searchEntryIdsByComment(long habitId, String query){
        return searchTableForIdsByName(
                "SELECT "+DatabaseHelper.ENTRY_ID+" FROM " +
                DatabaseHelper.ENTRIES_TABLE_NAME + " WHERE " +
                DatabaseHelper.ENTRY_NOTE + " LIKE ? AND " +
                DatabaseHelper.ENTRY_HABIT_ID + "=?",

                new String[]{"%" + query + "%", String.valueOf(habitId)}, DatabaseHelper.ENTRY_ID
        );
    }

    /**
     * @param habitId The row id for the habit to search entries for.
     * @param beginTime The starting time for the query.
     * @param endTime The ending time for the query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    @Nullable
    public Set<Long> searchEntriesWithTimeRangeForAHabit(long habitId, long beginTime, long endTime){
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE HABIT_ID=habitId
        // AND START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + DatabaseHelper.ENTRY_ID +
                " FROM " + DatabaseHelper.ENTRIES_TABLE_NAME + " WHERE " +
                DatabaseHelper.ENTRY_HABIT_ID + "=? AND " +
                DatabaseHelper.ENTRY_START_TIME + ">=? AND " +
                DatabaseHelper.ENTRY_START_TIME + "<=?";

        String[] values = new String[]{
                String.valueOf(habitId),
                String.valueOf(beginTime),
                String.valueOf(endTime)
        };

        return searchTableForIdsByName(SQL, values, DatabaseHelper.ENTRY_ID);
    }

    /**
     * @param beginTime The starting time for the query.
     * @param endTime The ending time for the query.
     * @return An array of entry ids found by the search, null if results were empty.
     */
    public Set<Long> searchAllEntriesWithTimeRange(long beginTime, long endTime){
        // SELECT ENTRY_ID FROM ENTRIES_TABLE WHERE START_TIME >= BEGIN AND START_TIME <= END
        String SQL = "SELECT " + DatabaseHelper.ENTRY_ID +
                " FROM " + DatabaseHelper.ENTRIES_TABLE_NAME + " WHERE " +
                DatabaseHelper.ENTRY_START_TIME + ">=? AND " +
                DatabaseHelper.ENTRY_START_TIME + "<=?";

        String[] values = new String[]{
                String.valueOf(beginTime),
                String.valueOf(endTime)
        };

        return searchTableForIdsByName(SQL, values, DatabaseHelper.ENTRY_ID);
    }

    /**
     * @param c A cursor object above an entry record.
     * @return An entry object.
     */
    @Nullable
    private SessionEntry getEntryFromCursor(Cursor c){
        SessionEntry entry;

        long databaseId   = c.getLong(0);   // c.getColumnIndex(DatabaseHelper.ENTRY_START_TIME);
        long entryHabitId = c.getLong(1);   // c.getColumnIndex(DatabaseHelper.ENTRY_DURATION);
        long startTime    = c.getLong(2);   // c.getColumnIndex(DatabaseHelper.ENTRY_HABIT_ID);
        long duration     = c.getLong(3);   // c.getColumnIndex(DatabaseHelper.ENTRY_NOTE);
        String note       = c.getString(4); // c.getColumnIndex(DatabaseHelper.ENTRY_ID);

        entry = new SessionEntry(startTime, duration, note);
        entry.setHabitId(entryHabitId);
        entry.setDatabaseId(databaseId);

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

        Cursor c = readableDatabase.query(DatabaseHelper.ENTRIES_TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        long entryId = -1;
        if(c != null) {
            if (c.move(entryIndex + 1)) {
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

        Cursor c = readableDatabase.query(DatabaseHelper.ENTRIES_TABLE_NAME, null, selection,
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

        return writableDatabase.update(DatabaseHelper.ENTRIES_TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.update(DatabaseHelper.ENTRIES_TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.update(DatabaseHelper.ENTRIES_TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.delete(DatabaseHelper.ENTRIES_TABLE_NAME,
                whereClause, whereArgs);
    }

    /**
     * @param habitId The habit which to delete it's entries.
     * @return The number of rows removed, -1 if error.
     */
    public long deleteEntriesForHabit(long habitId){
        String whereClause = DatabaseHelper.ENTRY_HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return writableDatabase.delete(DatabaseHelper.ENTRIES_TABLE_NAME,
                whereClause, whereArgs);
    }

    // ---- habits methods ----

    private SQLiteStatement getHabitInsertStatement(){
        String sql = "INSERT INTO "+DatabaseHelper.HABITS_TABLE_NAME+
                " ("+DatabaseHelper.HABIT_IS_ARCHIVED+
                ", "+DatabaseHelper.HABIT_NAME+
                ", "+DatabaseHelper.HABIT_DESCRIPTION+
                ", "+DatabaseHelper.HABIT_ICON_RES_ID+
                ", "+DatabaseHelper.HABIT_CATEGORY+") VALUES(?, ?, ?, ?, ?)";

        return writableDatabase.compileStatement(sql);
    }

    /**
     * @param habit The habit to be added to the database.
     * @return The row id of the new habit.
     */
    public long addHabitIfNotExists(Habit habit){
        long habitId = getHabitIdFromObject(habit);

        if(habitId == -1) {
            addHabit(habit);
        }

        return habitId;
    }

    /**
     * @param habit The habit to be added to the database.
     * @return The row id of the new habit.
     */
    public long addHabit(Habit habit){
        HabitCategory category = habit.getCategory();
        long categoryId = category.getDatabaseId();

        if(categoryId == -1){
            throw new IllegalArgumentException("Category database id is -1.");
        }

        insertHabitStatement.bindLong  (1, habit.getIsArchived()?1:0);
        insertHabitStatement.bindString(2, habit.getName());
        insertHabitStatement.bindString(3, habit.getDescription());
        insertHabitStatement.bindString(4, habit.getIconResId());
        insertHabitStatement.bindLong  (5, categoryId);

        long habitId = insertHabitStatement.executeInsert();
        habit.setDatabaseId(habitId);

        // Add entries to the new habit
        SessionEntry entries[] = habit.getEntries();
        if(entries != null) {
            addEntries(habitId, entries);
        }

        if(useCache){
            dataCache.cacheHabit(habit);
        }

        return habitId;
    }

    /**
     * @param habit The habit to be added to the database.
     * @return The row id of the new habit.
     */
    public long addHabitAndCategory(Habit habit){
        HabitCategory category = habit.getCategory();
        addCategoryIfNotExists(category);

        return addHabit(habit);
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

            habit.setDatabaseId(habitId);
        }

        return habitId;
    }

    /**
     * @param name The name of the habit to look up.
     * @return The found habit ids.
     */
    public Set<Long> searchHabitIdsByName(String name){
        return searchTableForIdsByName("SELECT "+DatabaseHelper.HABIT_ID+" FROM " +
                        DatabaseHelper.HABITS_TABLE_NAME + " WHERE " +
                        DatabaseHelper.HABIT_NAME + " LIKE  ?",

                new String[]{"%" + name + "%"}, DatabaseHelper.HABIT_ID);
    }

    /**
     * @param name The name of the category to look up.
     * @return The found category ids as an array.
     */
    public Set<Long> searchCategoryIdsByName(String name){
        return searchTableForIdsByName(
                "SELECT "+ CATEGORY_ID+" FROM " +
                        DatabaseHelper.CATEGORIES_TABLE_NAME + " WHERE " +
                        DatabaseHelper.CATEGORY_NAME + " LIKE  ?",

                new String[]{"%" + name + "%"}, CATEGORY_ID
        );
    }

    /**
     * @param query The name of the habit to look up.
     * @return The found habit ids.
     */
    public Set<Long> queryDatabaseByTheUser(String query){
        Set<Long> habitIds = searchHabitIdsByName(query);
        Set<Long> categoryIds = searchCategoryIdsByName(query);

        Set<Long> habitIdsByCategory = new HashSet<>();
        for(long id: categoryIds){
            habitIdsByCategory.addAll(getHabitIds(id));
        }

        Set<Long> ids = new HashSet<>(habitIds.size() + habitIdsByCategory.size());
        ids.addAll(habitIds);
        ids.addAll(habitIdsByCategory);

        return ids;
    }

    public String getHabitName(long id){
        Cursor c = readableDatabase.query(DatabaseHelper.HABITS_TABLE_NAME, new String[]{DatabaseHelper.HABIT_NAME},
                DatabaseHelper.HABIT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

        String name = null;

        if(c != null){
            if(c.moveToFirst()){
                int nameInd = c.getColumnIndex(DatabaseHelper.HABIT_NAME);
                name = c.getString(nameInd);
                c.close();
            }
        }

        return name;
    }

    private Habit getHabitFromCursor(Cursor c){
        Habit habit = null;

        long habitId       = c.getLong(0);       // c.getColumnIndex(DatabaseHelper.HABIT_ID);
        boolean isArchived = (c.getInt(1) == 1); // c.getColumnIndex(DatabaseHelper.HABIT_IS_ARCHIVED);
        String name        = c.getString(2);     // c.getColumnIndex(DatabaseHelper.HABIT_NAME);
        String description = c.getString(3);     // c.getColumnIndex(DatabaseHelper.HABIT_DESCRIPTION);
        long categoryId    = c.getLong(4);       // c.getColumnIndex(DatabaseHelper.HABIT_CATEGORY);
        String iconResId   = c.getString(5);     // c.getColumnIndex(DatabaseHelper.HABIT_ICON_RES_ID);

        HabitCategory category = getCategory(categoryId);


        SessionEntry entries[] = new SessionEntry[getNumberOfEntries(habitId)];
        for (int i = 0; i < getNumberOfEntries(habitId); i++) {
            entries[i] = getEntry(getEntryId(habitId, i));
        }

        if(category != null) {
            habit = new Habit(name, description, category, entries, iconResId);
            habit.setDatabaseId(habitId);
            habit.setIsArchived(isArchived);
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

        Habit habit = null;
        if(c != null) {
            if(c.moveToFirst()) {
                habit = getHabitFromCursor(c);
                c.close();
            }
        }

        return habit;
    }

    public long getHabitCategoryId(long habitId){
        Cursor c = readableDatabase.query(DatabaseHelper.HABITS_TABLE_NAME, new String[]{DatabaseHelper.HABIT_CATEGORY},
                DatabaseHelper.HABIT_ID + "=?", new String[]{String.valueOf(habitId)}, null, null, null);

        long categoryId = -1;

        if(c != null){
            if(c.moveToFirst()){
                int categoryInd = c.getColumnIndex(DatabaseHelper.HABIT_CATEGORY);
                categoryId = c.getLong(categoryInd);
                c.close();
            }
        }

        return categoryId;
    }

    public int getHabitColor(long habitId){
        HabitCategory category = getCategory(getHabitCategoryId(habitId));

        int color = -1;
        if(category != null){
            color = category.getColorAsInt();
        }

        return color;
    }

    public Set<Long> getHabitIds(long categoryId){
        return searchTableForIdsByName("SELECT "+DatabaseHelper.HABIT_ID+" FROM "+
                        DatabaseHelper.HABITS_TABLE_NAME+" WHERE " +
                        DatabaseHelper.HABIT_CATEGORY +" =?",
                new String[]{String.valueOf(categoryId)}, DatabaseHelper.HABIT_ID);
    }

    /**
     * @param categoryId The category to fetch habits from.
     * @return an array list containing the habits.
     */
    public Habit[] getHabits(long categoryId){
        int numberOfHabits = getNumberOfHabits(categoryId);
        Habit[] habits = new Habit[numberOfHabits];

        Cursor c = readableDatabase.query(DatabaseHelper.HABITS_TABLE_NAME, null,
                DatabaseHelper.HABIT_CATEGORY +" =?", new String[]{String.valueOf(categoryId)},
                null, null, null);

        int habitIndex = 0;
        while(c.moveToNext()){
            habits[habitIndex++] = getHabitFromCursor(c);
        }

        c.close();

        return habits;
    }

    public boolean getIsHabitArchived(long habitId) {
        Cursor c = readableDatabase.query(DatabaseHelper.HABITS_TABLE_NAME, new String[]{DatabaseHelper.HABIT_IS_ARCHIVED},
                DatabaseHelper.HABIT_ID + "=?", new String[]{String.valueOf(habitId)}, null, null, null);

        boolean isArchived = false;

        if(c != null){
            if(c.moveToFirst()){
                isArchived = c.getInt(0) != 0;
                c.close();
            }
        }

        return isArchived;
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

        return writableDatabase.update(DatabaseHelper.HABITS_TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.update(DatabaseHelper.HABITS_TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.update(DatabaseHelper.HABITS_TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.update(DatabaseHelper.HABITS_TABLE_NAME, values,
                whereClause, whereArgs);
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

        return writableDatabase.update(DatabaseHelper.HABITS_TABLE_NAME, values,
                whereClause, whereArgs);
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

            HabitCategory category = habit.getCategory();
            long categoryId = getCategoryIdByObject(category);
            if(categoryId == -1){
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
    public long deleteHabit(long habitId){
        // Delete all of the habit's entries
        deleteEntriesForHabit(habitId);

        // Delete the habit itself
        String whereClause = DatabaseHelper.HABIT_ID + "=?";
        String whereArgs[] = {String.valueOf(habitId)};

        return writableDatabase.delete(DatabaseHelper.HABITS_TABLE_NAME,
                whereClause, whereArgs);
    }
}