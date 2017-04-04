package com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema;

import android.content.ContentValues;

import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.HabitDatabase.DatabaseSchema.DatabaseSchema.SQL_TYPES;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;

/**
 * Created by Brandon on 2/17/2017.
 * Schema defining habits table
 */

public class HabitsTableSchema {
    public static final String TABLE_NAME = "HABITS_TABLE";
    public static final String HABIT_ID = "ID";
    public static final String HABIT_IS_ARCHIVED = "ARCHIVED";
    public static final String HABIT_NAME = "NAME";
    public static final String HABIT_DESCRIPTION = "DESCRIPTION";
    public static final String HABIT_CATEGORY_ID = "CATEGORY";
    public static final String HABIT_ICON_RES_ID = "ICON_RES_ID";

    public static String getCreateTableStatement() {
        // CREATE TABLE HABITS_TABLE (ID INTEGER PRIMARY KEY, ARCHIVED INTEGER, NAME TEXT NOT NULL, DESCRIPTION TEXT NOT NULL,
        // CATEGORY INTEGER NOT NULL, ICON_RES_ID TEXT NOT NULL)
        return "CREATE TABLE " + TABLE_NAME + " (" +
                HABIT_ID + SQL_TYPES.PRI_INT_KEY + ", " +
                HABIT_IS_ARCHIVED + SQL_TYPES.INTEGER + ", " +
                HABIT_NAME + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL + ", " +
                HABIT_DESCRIPTION + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL + ", " +
                HABIT_CATEGORY_ID + SQL_TYPES.INTEGER + SQL_TYPES.NOT_NULL + ", " +
                HABIT_ICON_RES_ID + SQL_TYPES.TEXT + SQL_TYPES.NOT_NULL +
                ");";
    }

    public static String getDropTableStatement() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static Habit getObjectFromContentValues(ContentValues contentValues, HabitDatabase dao) {
        boolean isArchived = contentValues.getAsLong(HABIT_IS_ARCHIVED) == 1;
        String name = contentValues.getAsString(HABIT_NAME);
        String description = contentValues.getAsString(HABIT_DESCRIPTION);
        long categoryId = contentValues.getAsLong(HABIT_CATEGORY_ID);
        String iconResId = contentValues.getAsString(HABIT_ICON_RES_ID);

        long habitId = contentValues.getAsLong(HABIT_ID);

        HabitCategory category = dao.getCategory(categoryId);

        if (category != null) {
            return new Habit(name, description, category, iconResId, null)
                    .setDatabaseId(habitId)
                    .setIsArchived(isArchived);
        }
        else return null;
    }

    public static ContentValues getContentValuesFromObject(Habit habit) {
        ContentValues values = new ContentValues();
        values.put(HABIT_IS_ARCHIVED, habit.getIsArchived() ? 1 : 0);
        values.put(HABIT_NAME, habit.getName());
        values.put(HABIT_DESCRIPTION, habit.getDescription());
        values.put(HABIT_CATEGORY_ID, habit.getCategory().getDatabaseId());
        values.put(HABIT_ICON_RES_ID, habit.getIconResId());

        return values;
    }
}
