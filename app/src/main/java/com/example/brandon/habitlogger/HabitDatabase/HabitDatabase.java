package com.example.brandon.habitlogger.HabitDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by Brandon on 10/26/2016.
 * This is the class for managing the habit database
 */

public class HabitDatabase {

    private DatabaseHelper databaseHelper;

    public HabitDatabase(Context context){
        databaseHelper = new DatabaseHelper(context);
    }

    public long getNumberOfCategories(){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor c = db.query(DatabaseHelper.CATEGORIES_TABLE_NAME, null, null, null, null, null, null);
        long count = c.getCount();
        c.close();

        return count;
    }

    public long getNumberOfHabits(long categoryId){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String columns[] = {DatabaseHelper.HABIT_CATEGORY};
        Cursor c = db.query(DatabaseHelper.HABITS_TABLE_NAME, columns, DatabaseHelper.HABIT_CATEGORY + "=?",
                new String[]{String.valueOf(categoryId)}, null, null, null);
        long count = c.getCount();
        c.close();

        return count;
    }

    public long getNumberOfEntries(long habitId){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String columns[] = {DatabaseHelper.ENTRY_HABIT_ID};
        Cursor c = db.query(DatabaseHelper.ENTRIES_TABLE_NAME, columns, DatabaseHelper.ENTRY_HABIT_ID + "=?",
                new String[]{String.valueOf(habitId)}, null, null, null);
        long count = c.getCount();
        c.close();

        return count;
    }

    // CRUD (Create, Read, Update, Destroy) categories

    /**
     * @param category The category object to be added to the database
     * @return The row id for the new row, -1 if error
     */
    public long addCategory(HabitCategory category){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(2);
        values.put(DatabaseHelper.CATEGORY_NAME, category.getName());
        values.put(DatabaseHelper.CATEGORY_COLOR, category.getColor());

        long id = db.insert(DatabaseHelper.CATEGORIES_TABLE_NAME, null, values);
        category.setDatabaseId(id);

        return id;
    }

    /**
     * @param index The category index to look up
     * @return The unique category id of the row
     */
    public long getCategoryIdFromIndex(int index){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String columns[] = {DatabaseHelper.CATEGORY_ID};
        Cursor c = db.query(DatabaseHelper.CATEGORIES_TABLE_NAME, columns, null, null, null, null, null);

        long rowId = -1;
        if(c.moveToPosition(index)){
            int idInd = c.getColumnIndex(DatabaseHelper.CATEGORY_ID);
            rowId = c.getLong(idInd);
        }

        c.close();

        return rowId;
    }

    /**
     * @param categoryId The database id for the category to be received
     * @return The category object from the database
     */
    @Nullable
    public HabitCategory getCategory (long categoryId){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String columns[] = {DatabaseHelper.CATEGORY_NAME, DatabaseHelper.CATEGORY_COLOR};
        String selectionArgs[] = {String.valueOf(categoryId)};
        Cursor c = db.query(DatabaseHelper.CATEGORIES_TABLE_NAME, columns, DatabaseHelper.CATEGORY_ID + "=?",
                selectionArgs, null, null, null);

        HabitCategory category = null;

        if(c.moveToFirst()){
            int colorInd = c.getColumnIndex(DatabaseHelper.CATEGORY_COLOR);
            int nameInd  = c.getColumnIndex(DatabaseHelper.CATEGORY_NAME);

            String color = c.getString(colorInd);
            String name  = c.getString(nameInd);
            category = new HabitCategory(color, name);
            category.setDatabaseId(categoryId);
        }

        c.close();

        return category;
    }

    /**
     * @param categoryId The database id for the category to update
     * @param name The new name for the category
     * @return The number of rows changed, -1 if fail.
     */
    public long updateCategoryName(long categoryId, String name){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.CATEGORY_NAME, name);
        String whereClause = DatabaseHelper.CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        return db.update(DatabaseHelper.CATEGORIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param categoryId The database id for the category to update
     * @param color The new color for the category
     * @return The number of rows changed, -1 if fail.
     */
    public long updateCategoryColor(long categoryId, String color){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.CATEGORY_COLOR, color);
        String whereClause = DatabaseHelper.CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        return db.update(DatabaseHelper.CATEGORIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param categoryId The database id for the category to update
     * @param category The new category to update the old one.
     * @return Returns the number of rows changed, -1 if failed.
     */
    public long updateCategory(long categoryId, HabitCategory category){
        // TODO TEST
        long count = updateCategoryName(categoryId, category.getName());
        if(count != -1){
            updateCategoryColor(categoryId, category.getColor());
        }

        return count;
    }

    /**
     * @param categoryId The database id for the category to delete
     * @return Returns the number of rows removed, -1 if failed.
     */
    public long deleteCategory(long categoryId){
        // TODO TEST

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.CATEGORY_ID + "=?";
        String whereArgs[] = {String.valueOf(categoryId)};

        return db.delete(DatabaseHelper.CATEGORIES_TABLE_NAME, whereClause, whereArgs);
    }

    // CRUD (Create, Read, Update, Destroy) entries

    /**
     * @param habitId The id of the habit in the database to associate this entry with
     * @param entry The entry to be inserted
     * @return Row id of the new row, -1 if error
     */
    public long addEntry(long habitId, SessionEntry entry){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put(DatabaseHelper.ENTRY_START_TIME, entry.getStartTime());
        values.put(DatabaseHelper.ENTRY_DURATION, entry.getDuration());
        values.put(DatabaseHelper.ENTRY_NOTE, entry.getNote());
        values.put(DatabaseHelper.ENTRY_HABIT_ID, habitId);

        long id = db.insert(DatabaseHelper.ENTRIES_TABLE_NAME, null, values);
        entry.setDatabaseId(id);
        entry.setHabitId(habitId);

        return id;
    }

    /**
     * @param habitId The habit id to search
     * @param index The index of the entry to look up
     * @return The id of the entry
     */
    public long getEntryIdFromIndex(long habitId, int index){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.ENTRIES_TABLE_NAME, null, DatabaseHelper.ENTRY_HABIT_ID + "=?",
                new String[]{String.valueOf(habitId)}, null, null, null);

        long entryId = -1;
        if(c.moveToPosition(index)){
            int idInd = c.getColumnIndex(DatabaseHelper.ENTRY_ID);
            entryId = c.getLong(idInd);
        }

        c.close();
        return entryId;
    }

    /**
     * @param habitId The id of the habit which to look for it's entries
     * @param entryIndex The index of the entry to be retrieved from the habit
     * @return The found session entry
     */
    @Nullable
    public SessionEntry getEntry(long habitId, long entryIndex){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection = DatabaseHelper.ENTRY_HABIT_ID + "=?";
        String selectionArgs[] = {String.valueOf(habitId)};
        Cursor c = db.query(DatabaseHelper.ENTRIES_TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        SessionEntry entry = null;

        if(c.moveToPosition((int)entryIndex)){
            entry = getEntryFromCursor(c);
        }

        c.close();

        return entry;
    }

    /**
     * @param entryId The id of the session entry to look up
     * @return The found session entry
     */
    @Nullable
    public SessionEntry getEntry(long entryId){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection = DatabaseHelper.ENTRY_ID + "=?";
        String selectionArgs[] = {String.valueOf(entryId)};
        Cursor c = db.query(DatabaseHelper.ENTRIES_TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        SessionEntry entry = null;

        if(c.moveToFirst()){
            entry = getEntryFromCursor(c);
        }

        c.close();

        return entry;
    }

    private SessionEntry getEntryFromCursor(Cursor c){
        int startTimeInd = c.getColumnIndex(DatabaseHelper.ENTRY_START_TIME);
        int durationInd  = c.getColumnIndex(DatabaseHelper.ENTRY_DURATION);
        int noteInd  = c.getColumnIndex(DatabaseHelper.ENTRY_NOTE);
        int habitIdInd  = c.getColumnIndex(DatabaseHelper.ENTRY_HABIT_ID);
        int idInd  = c.getColumnIndex(DatabaseHelper.ENTRY_ID);

        long startTime = c.getLong(startTimeInd);
        long duration = c.getLong(durationInd);
        String note  = c.getString(noteInd);
        long entryHabitId  = c.getLong(habitIdInd);
        long databaseId  = c.getLong(idInd);

        SessionEntry entry = new SessionEntry(startTime, duration, note);
        entry.setHabitId(entryHabitId);
        entry.setDatabaseId(databaseId);

        return entry;
    }

    /**
     * @param habitId The id of the habit which to edit an entry
     * @param entryIndex The index of the entry to edit
     * @param startTime The new start time to replace the old one.
     * @return The number of rows changed, -1 if error
     */
    public long updateEntryStartTime(long habitId, long entryIndex, long startTime){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.ENTRY_START_TIME, startTime);

        String whereClause = DatabaseHelper.ENTRY_HABIT_ID + " =? AND " + DatabaseHelper.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId), String.valueOf(entryIndex)};

        return db.update(DatabaseHelper.ENTRIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The id of the habit which to edit an entry
     * @param entryIndex The index of the entry to edit
     * @param duration The new duration to replace the old one.
     * @return The number of rows changed, -1 if error
     */
    public long updateEntryDuration(long habitId, long entryIndex, long duration){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.ENTRY_DURATION, duration);

        String whereClause = DatabaseHelper.ENTRY_HABIT_ID + " =? AND " + DatabaseHelper.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId), String.valueOf(entryIndex)};

        return db.update(DatabaseHelper.ENTRIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The id of the habit which to edit an entry
     * @param entryIndex The index of the entry to edit
     * @param note The new note to replace the old one.
     * @return The number of rows changed, -1 if error
     */
    public long updateEntryNote(long habitId, long entryIndex, String note){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.ENTRY_NOTE, note);

        String whereClause = DatabaseHelper.ENTRY_HABIT_ID + " =? AND " + DatabaseHelper.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId), String.valueOf(entryIndex)};

        return db.update(DatabaseHelper.ENTRIES_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The id of the habit which to edit an entry
     * @param entryIndex The index of the entry to edit
     * @param entry The new session entry to replace the old one
     * @return The number of rows changed, -1 if error
     */
    public long updateEntry(long habitId, int entryIndex, SessionEntry entry){
        // TODO TEST

        long count = -1;
        if(getEntry(habitId, entryIndex) != null){
            count = updateEntryStartTime(habitId, entryIndex, entry.getStartTime());
            updateEntryDuration(habitId, entryIndex, entry.getDuration());
            updateEntryNote(habitId, entryIndex, entry.getNote());
        }

        return count;
    }

    /**
     * @param entryId The id of the entry to delete
     * @return The number of rows removed, -1 if error
     */
    public long deleteEntry(long entryId){
        // TODO TEST

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.ENTRY_ID + " =?";
        String whereArgs[] = {String.valueOf(entryId)};

        return db.delete(DatabaseHelper.ENTRIES_TABLE_NAME, whereClause, whereArgs);
    }

    // CRUD (Create, Read, Update, Destroy)  habits

    /**
     * @param habit The habit to be added to the database.
     * @return The row id of the new habit
     */
    public long addHabit(Habit habit){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.HABIT_NAME, habit.getName());
        values.put(DatabaseHelper.HABIT_DESCRIPTION, habit.getDescription());
        values.put(DatabaseHelper.HABIT_ICON_RES_ID, habit.getIconResId());

        HabitCategory category = habit.getCategory();
        Long categoryId = null;

        if(category != null) {
            categoryId = category.getDatabaseId();
            if (categoryId == null) {
                categoryId = addCategory(category);
                habit.setCategoryDatabaseId(categoryId);
            }
        }
        values.put(DatabaseHelper.HABIT_CATEGORY, categoryId);

        long habitId = db.insert(DatabaseHelper.HABITS_TABLE_NAME, null, values);

        // Add entries to the new habit
        ArrayList<SessionEntry> entries = habit.getEntries();
        if(entries != null) {
            for (SessionEntry entry : entries) {
                addEntry(habitId, entry);
            }
        }

        habit.setDatabaseId(habitId);

        return habitId;
    }

    /**
     * @param index The habit index to look up
     * @return The unique habit id of the row
     */
    public long getHabitIdFromIndex(long categoryId, int index){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String columns[] = {DatabaseHelper.HABIT_ID};
        Cursor c = db.query(DatabaseHelper.HABITS_TABLE_NAME, columns,
                DatabaseHelper.HABIT_CATEGORY + "=?", new String[]{String.valueOf(categoryId)},
                null, null, null);

        long rowId = -1;
        if(c.moveToPosition(index)){
            int idInd = c.getColumnIndex(DatabaseHelper.HABIT_ID);
            rowId = c.getLong(idInd);
        }

        c.close();

        return rowId;
    }

    /**
     * @param habitId The row id of the habit you wish to receive
     * @return The habit object found
     */
    @Nullable
    public Habit getHabit(long habitId){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String columns[] = {DatabaseHelper.HABIT_NAME, DatabaseHelper.HABIT_DESCRIPTION,
                DatabaseHelper.HABIT_CATEGORY, DatabaseHelper.HABIT_ICON_RES_ID};

        String selectionArgs[] = {String.valueOf(habitId)};
        Cursor c = db.query(DatabaseHelper.HABITS_TABLE_NAME, columns, DatabaseHelper.HABIT_ID + "=?",
                selectionArgs, null, null, null);

        Habit habit = null;

        if(c.moveToFirst()){
            int nameInd = c.getColumnIndex(DatabaseHelper.HABIT_NAME);
            int descriptionInd  = c.getColumnIndex(DatabaseHelper.HABIT_DESCRIPTION);
            int categoryInd  = c.getColumnIndex(DatabaseHelper.HABIT_CATEGORY);
            int iconResIdInd  = c.getColumnIndex(DatabaseHelper.HABIT_ICON_RES_ID);

            String name  = c.getString(nameInd);
            String description  = c.getString(descriptionInd);
            long categoryId  = c.getInt(categoryInd);
            HabitCategory category = getCategory(categoryId);
            String iconResId  = c.getString(iconResIdInd);

            ArrayList<SessionEntry> entries = new ArrayList<>();
            for(int i = 0; i < getNumberOfEntries(habitId); i++){
                entries.add(getEntry(habitId, i));
            }

            habit = new Habit(name, description, category, entries, iconResId);
            habit.setDatabaseId(habitId);
        }

        c.close();

        return habit;
    }

    /**
     * @param categoryId The category to fetch habits from
     * @return an array list containing the habits
     */
    public ArrayList<Habit> getHabits(long categoryId){
        ArrayList<Habit> habits = new ArrayList<>((int)getNumberOfHabits(categoryId));

        for(int i = 0; i < getNumberOfHabits(categoryId); i++){
            habits.add(getHabit(getHabitIdFromIndex(categoryId, i)));
        }

        return habits;
    }

    /**
     * @param habitId The row id to be updated
     * @param name The new name to replace the old one
     * @return The number of rows changed, -1 if error
     */
    public long updateHabitName(long habitId, String name){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_NAME, name);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The row id to be updated
     * @param description The new description to replace the old one
     * @return The number of rows changed, -1 if error
     */
    public long updateHabitDescription(long habitId, String description){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_DESCRIPTION, description);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The row id to be updated
     * @param categoryId The new categoryId to replace the old one
     * @return The number of rows changed, -1 if error
     */
    public long updateHabitCategory(long habitId, @Nullable Long categoryId){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_CATEGORY, categoryId);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The row id to be updated
     * @param iconResId The new iconResId to replace the old one
     * @return The number of rows changed, -1 if error
     */
    public long updateHabitIconResId(long habitId, String iconResId){
        // TODO TEST
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put(DatabaseHelper.HABIT_ICON_RES_ID, iconResId);

        String whereClause = DatabaseHelper.HABIT_ID + " =?";
        String whereArgs[] = {String.valueOf(habitId)};

        return db.update(DatabaseHelper.HABITS_TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * @param habitId The id of the habit to edit
     * @param habit A new habit to replace the old one
     * @return The number of rows removed, -1 if error
     */
    public long updateHabit(long habitId, Habit habit){
        long count = -1;

        if(getHabit(habitId) != null){
            count = updateHabitName(habitId, habit.getName());
            updateHabitDescription(habitId, habit.getDescription());
            HabitCategory category = habit.getCategory();
            if(category != null){
                updateHabitCategory(habitId, category.getDatabaseId());
            }
            updateHabitIconResId(habitId, habit.getIconResId());
        }

        return count;
    }

    /**
     * @param habitId The row id of the habit to be removed
     * @return The number of rows removed, -1 if error
     */
    public long deleteHabit(long habitId){
        // TODO TEST

        // Delete all of the habit's entries
        for(int i = 0; i < getNumberOfEntries(habitId); i++){
            SessionEntry entry = getEntry(habitId, i);
            if(entry != null) {
                Long entryId = entry.getDatabaseId();
                if(entryId != null) {
                    deleteEntry(entryId);
                }
            }
        }

        // Delete the habit itself
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.HABIT_ID + "=?";
        String whereArgs[] = {String.valueOf(habitId)};

        return db.delete(DatabaseHelper.HABITS_TABLE_NAME, whereClause, whereArgs);
    }

    public void resetDatabase(){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        databaseHelper.resetDatabase(db);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper{

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_HABITS_TABLE);
            sqLiteDatabase.execSQL(CREATE_CATEGORIES_TABLE);
            sqLiteDatabase.execSQL(CREATE_ENTRIES_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            resetDatabase(sqLiteDatabase);
        }

        public void resetDatabase(SQLiteDatabase sqLiteDatabase){
            sqLiteDatabase.execSQL(DROP_HABITS_TABLE);
            sqLiteDatabase.execSQL(DROP_CATEGORIES_TABLE);
            sqLiteDatabase.execSQL(DROP_ENTRIES_TABLE);
            this.onCreate(sqLiteDatabase);
        }

        private static final String DATABASE_NAME = "habit_logger_database";
        private static final int DATABASE_VERSION = 2;

        // CREATE TABLE HABITS_TABLE (ID INTEGER PRIMARY KEY, NAME TEXT, DESCRIPTION TEXT, CATEGORY LONG, ICON_RES_ID TEXT)
        private static final String HABITS_TABLE_NAME = "HABITS_TABLE";
        private static final String HABIT_ID          = "ID";
        private static final String HABIT_NAME        = "NAME";
        private static final String HABIT_DESCRIPTION = "DESCRIPTION";
        private static final String HABIT_CATEGORY    = "CATEGORY";
        private static final String HABIT_ICON_RES_ID = "ICON_RES_ID";

        private static final String CREATE_HABITS_TABLE =
                "CREATE TABLE " + HABITS_TABLE_NAME + " (" +
                HABIT_ID          + " INTEGER PRIMARY KEY, " +
                HABIT_NAME        + " TEXT,"    +
                HABIT_DESCRIPTION + " TEXT,"    +
                HABIT_CATEGORY    + " LONG," +
                HABIT_ICON_RES_ID + " TEXT" +
                ");";

        // DROP TABLE IF EXISTS HABITS_TABLE
        private static final String DROP_HABITS_TABLE = "DROP TABLE IF EXISTS " + HABITS_TABLE_NAME;

        // CREATE TABLE CATEGORIES_TABLE (ID INTEGER PRIMARY KEY, NAME TEXT, COLOR TEXT);
        private static final String CATEGORIES_TABLE_NAME = "CATEGORIES_TABLE";
        private static final String CATEGORY_ID    = "ID";
        private static final String CATEGORY_NAME  = "NAME";
        private static final String CATEGORY_COLOR = "COLOR";

        private static final String CREATE_CATEGORIES_TABLE =
                "CREATE TABLE " + CATEGORIES_TABLE_NAME + " (" +
                CATEGORY_ID    + " INTEGER PRIMARY KEY," +
                CATEGORY_NAME  + " TEXT,"  +
                CATEGORY_COLOR + " TEXT"  +
                ");";

        // DROP TABLE IF EXISTS CATEGORIES_TABLE
        private static final String DROP_CATEGORIES_TABLE = "DROP TABLE IF EXISTS " + CATEGORIES_TABLE_NAME;

        // CREATE TABLE ENTRIES_TABLE (ID INTEGER PRIMARY KEY, HABIT_ID LONG, START_TIME LONG, DURATION LONG, NOTE TEXT)
        private static final String ENTRIES_TABLE_NAME = "ENTRIES_TABLE";
        private static final String ENTRY_ID         = "ID";
        private static final String ENTRY_HABIT_ID   = "HABIT_ID";
        private static final String ENTRY_START_TIME = "START_TIME";
        private static final String ENTRY_DURATION   = "DURATION";
        private static final String ENTRY_NOTE       = "NOTE";

        private static final String CREATE_ENTRIES_TABLE =
                "CREATE TABLE " + ENTRIES_TABLE_NAME + " (" +
                ENTRY_ID          + " INTEGER PRIMARY KEY," +
                ENTRY_HABIT_ID    + " LONG," +
                ENTRY_START_TIME  + " LONG,"  +
                ENTRY_DURATION    + " LONG,"  +
                ENTRY_NOTE        + " TEXT"    +
                ");";

        // DROP TABLE IF EXISTS ENTRIES_TABLE
        private static final String DROP_ENTRIES_TABLE = "DROP TABLE IF EXISTS " + ENTRIES_TABLE_NAME;
    }
}