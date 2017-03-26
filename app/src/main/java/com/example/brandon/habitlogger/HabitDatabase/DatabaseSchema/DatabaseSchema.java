package com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@SuppressWarnings({"unused", "WeakerAccess"})
public class DatabaseSchema extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "habit_logger_database";
    public static final int DATABASE_VERSION = 4;
    private Context context;

    class SQL_TYPES {
        public static final String NOT_NULL = " NOT NULL ";
        public static final String PRI_INT_KEY = " INTEGER PRIMARY KEY ";
        public static final String INTEGER = " INTEGER ";
        public static final String TEXT = " TEXT ";
    }

    public DatabaseSchema(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HabitsTableSchema.getCreateTableStatement());
        db.execSQL(CategoriesTableSchema.getCreateTableStatement());
        db.execSQL(EntriesTableSchema.getCreateTableStatement());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    public void resetDatabase(SQLiteDatabase db) {
        db.delete(HabitsTableSchema.TABLE_NAME, null, null);
        db.delete(CategoriesTableSchema.TABLE_NAME, null, null);
        db.delete(EntriesTableSchema.TABLE_NAME, null, null);
    }

    public void resetDatabase() {
        resetDatabase(getWritableDatabase());
    }

    public File getDatabasePath() {
        return context.getDatabasePath(getDatabaseName());
    }

    /**
     * @return A ByteBuffer representing the database.
     */
    public ByteBuffer getBytes() throws IOException {
        FileChannel src = new FileInputStream(getDatabasePath()).getChannel();

        ByteBuffer buffer = ByteBuffer.allocate((int) src.size());
        src.read(buffer);
        return buffer;
    }

    /**
     * @param bytes A ByteBuffer representing the database.
     */
    public void setBytes(ByteBuffer bytes) throws IOException {
        FileChannel dst = new FileOutputStream(getDatabasePath()).getChannel();

        dst.write(bytes);
        dst.close();
    }
}