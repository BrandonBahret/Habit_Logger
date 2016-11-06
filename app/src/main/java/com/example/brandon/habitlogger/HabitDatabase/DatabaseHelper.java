package com.example.brandon.habitlogger.HabitDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "habit_logger_database";
    protected static final int DATABASE_VERSION = 4;

    // CREATE TABLE HABITS_TABLE (ID INTEGER PRIMARY KEY, ARCHIVED INTEGER, NAME TEXT NOT NULL, DESCRIPTION TEXT NOT NULL,
    // CATEGORY INTEGER NOT NULL, ICON_RES_ID TEXT NOT NULL)
    protected static final String HABITS_TABLE_NAME = "HABITS_TABLE";
    protected static final String HABIT_ID          = "ID";
    protected static final String HABIT_IS_ARCHIVED = "ARCHIVED";
    protected static final String HABIT_NAME        = "NAME";
    protected static final String HABIT_DESCRIPTION = "DESCRIPTION";
    protected static final String HABIT_CATEGORY    = "CATEGORY";
    protected static final String HABIT_ICON_RES_ID = "ICON_RES_ID";

    private static final String CREATE_HABITS_TABLE =
            "CREATE TABLE " + HABITS_TABLE_NAME + " (" +
                    HABIT_ID          + " INTEGER PRIMARY KEY, " +
                    HABIT_IS_ARCHIVED + " INTEGER, " +
                    HABIT_NAME        + " TEXT NOT NULL,"    +
                    HABIT_DESCRIPTION + " TEXT NOT NULL,"    +
                    HABIT_CATEGORY    + " INTEGER NOT NULL," +
                    HABIT_ICON_RES_ID + " TEXT NOT NULL" +
                    ");";

    // DROP TABLE IF EXISTS HABITS_TABLE
    private static final String DROP_HABITS_TABLE = "DROP TABLE IF EXISTS " + HABITS_TABLE_NAME;

    // CREATE TABLE CATEGORIES_TABLE (ID INTEGER PRIMARY KEY, NAME TEXT NOT NULL, COLOR TEXT NOT NULL);
    protected static final String CATEGORIES_TABLE_NAME = "CATEGORIES_TABLE";
    protected static final String CATEGORY_ID    = "ID";
    protected static final String CATEGORY_NAME  = "NAME";
    protected static final String CATEGORY_COLOR = "COLOR";

    private static final String CREATE_CATEGORIES_TABLE =
            "CREATE TABLE " + CATEGORIES_TABLE_NAME + " (" +
                    CATEGORY_ID    + " INTEGER PRIMARY KEY," +
                    CATEGORY_NAME  + " TEXT NOT NULL,"  +
                    CATEGORY_COLOR + " TEXT NOT NULL"  +
                    ");";

    // DROP TABLE IF EXISTS CATEGORIES_TABLE
    private static final String DROP_CATEGORIES_TABLE = "DROP TABLE IF EXISTS " + CATEGORIES_TABLE_NAME;

    // CREATE TABLE ENTRIES_TABLE (ID INTEGER PRIMARY KEY, HABIT_ID INTEGER NOT NULL,
    // START_TIME INTEGER NOT NULL, DURATION INTEGER NOT NULL, NOTE TEXT NOT NULL)
    protected static final String ENTRIES_TABLE_NAME = "ENTRIES_TABLE";
    protected static final String ENTRY_ID         = "ID";
    protected static final String ENTRY_HABIT_ID   = "HABIT_ID";
    protected static final String ENTRY_START_TIME = "START_TIME";
    protected static final String ENTRY_DURATION   = "DURATION";
    protected static final String ENTRY_NOTE       = "NOTE";

    private static final String CREATE_ENTRIES_TABLE =
            "CREATE TABLE " + ENTRIES_TABLE_NAME + " (" +
                    ENTRY_ID          + " INTEGER PRIMARY KEY," +
                    ENTRY_HABIT_ID    + " INTEGER NOT NULL," +
                    ENTRY_START_TIME  + " INTEGER NOT NULL," +
                    ENTRY_DURATION    + " INTEGER NOT NULL," +
                    ENTRY_NOTE        + " TEXT NOT NULL"     +
                    ");";

    // DROP TABLE IF EXISTS ENTRIES_TABLE
    private static final String DROP_ENTRIES_TABLE = "DROP TABLE IF EXISTS " + ENTRIES_TABLE_NAME;

    private  Context context;
    DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_HABITS_TABLE);
        sqLiteDatabase.execSQL(CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(CREATE_ENTRIES_TABLE);
    }

    public void resetDatabase(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL(DROP_HABITS_TABLE);
        sqLiteDatabase.execSQL(DROP_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(DROP_ENTRIES_TABLE);
        this.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        resetDatabase(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }
}