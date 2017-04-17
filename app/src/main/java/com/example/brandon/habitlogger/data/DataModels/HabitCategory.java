package com.example.brandon.habitlogger.data.DataModels;

import android.support.annotation.NonNull;

import com.example.brandon.habitlogger.common.MyColorUtils;

import java.io.Serializable;
import java.util.Locale;

import static com.example.brandon.habitlogger.common.MyColorUtils.stringifyColor;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the category object.
 * This object is used to give habits categorical organization.
 */

public class HabitCategory implements Serializable {

    //region (Member attributes)
    @NonNull private String mColor = "#ff000000";
    @NonNull private String mName = "NAME_NOT_SET";

    private long mDatabaseId = -1;
    //endregion

    //region Constructors {}
    public HabitCategory(@NonNull String color, @NonNull String name) {
        mColor = color;
        mName = name;
    }

    public HabitCategory(int color, @NonNull String name) {
        this(stringifyColor(color), name);
    }

    public HabitCategory() {}
    //endregion

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HabitCategory) {
            HabitCategory compare = (HabitCategory) obj;
            return compare.getName().equals(getName()) &&
                    compare.getColor().equals(getColor());
        }
        else return false;
    }

    @Override
    public String toString() {
        String format = "%s {\n\tColor: %s\n}\n";
        return String.format(Locale.US, format, getName(), getColor());
    }

    //region Setters {}
    public HabitCategory setColor(@NonNull String color) {
        mColor = color;
        return this;
    }

    public HabitCategory setColor(int color) {
        mColor = MyColorUtils.stringifyColor(color);
        return this;
    }

    public HabitCategory setName(@NonNull String name) {
        mName = name;
        return this;
    }

    public HabitCategory setDatabaseId(long databaseId) {
        mDatabaseId = databaseId;
        return this;
    }
    //endregion -- end --

    //region Getters {}
    @NonNull
    public String getColor() {
        return mColor;
    }

    public int getColorAsInt() {
        return MyColorUtils.parseColor(mColor);
    }

    @NonNull
    public String getName() {
        return mName;
    }

    public long getDatabaseId() {
        return mDatabaseId;
    }
    //endregion -- end --

}
