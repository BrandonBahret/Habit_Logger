package com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.DatabaseSchema.SQL_TYPES;

/**
 * Created by Brandon on 2/17/2017.
 * Schema defining categories table.
 */

public class CategoriesTableSchema {
    public static final String TABLE_NAME = "CATEGORIES_TABLE";
    public static final String CATEGORY_ID = "ID";
    public static final String CATEGORY_NAME = "NAME";
    public static final String CATEGORY_COLOR = "COLOR";

    public static String getCreateTableStatement(){
        // CREATE TABLE CATEGORIES_TABLE (ID INTEGER PRIMARY KEY, NAME TEXT NOT NULL, COLOR TEXT NOT NULL);
        return "CREATE TABLE " + TABLE_NAME + " (" +
                CATEGORY_ID + SQL_TYPES.PRI_INT_KEY + ", " +
                CATEGORY_NAME + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL + ", " +
                CATEGORY_COLOR + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL +
                ");";
    }

    public static String getDropTableStatement(){
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String getInsertStatement() {
        return "INSERT INTO " + CategoriesTableSchema.TABLE_NAME +
                " (" + CategoriesTableSchema.CATEGORY_NAME +
                ", " + CategoriesTableSchema.CATEGORY_COLOR +
                ") VALUES(?, ?)";
    }

    public static HabitCategory getObjectFromContentValues(@NonNull ContentValues contentValues) {
        String categoryName = contentValues.getAsString(CATEGORY_NAME);
        String categoryColor = contentValues.getAsString(CATEGORY_COLOR);
        long databaseId = contentValues.getAsLong(CATEGORY_ID);

        HabitCategory category = new HabitCategory(categoryColor, categoryName);
        category.setDatabaseId(databaseId);

        return category;
    }
}
