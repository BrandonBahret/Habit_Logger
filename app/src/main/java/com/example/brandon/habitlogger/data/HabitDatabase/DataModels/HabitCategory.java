package com.example.brandon.habitlogger.data.HabitDatabase.DataModels;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.example.brandon.habitlogger.common.MyColorUtils;

import java.io.Serializable;
import java.util.Locale;

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
        this.mColor = color;
        this.mName = name;
    }

    public HabitCategory(int color, @NonNull String name) {
        this.mColor = MyColorUtils.stringifyColor(color);
        this.mName = name;
    }
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
        this.mColor = color;
        return this;
    }

    public HabitCategory setColor(int color) {
        setColor(MyColorUtils.stringifyColor(color));
        return this;
    }

    public HabitCategory setName(@NonNull String name) {
        this.mName = name;
        return this;
    }

    public HabitCategory setDatabaseId(long databaseId) {
        this.mDatabaseId = databaseId;
        return this;
    }
    //endregion

    //region Getters {}
    @NonNull
    public String getColor() {
        return mColor;
    }

    public int getColorAsInt() {
        int result = 0xFFCCCCCC;
        try {
            result = Color.parseColor(getColor());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    public long getDatabaseId() {
        return mDatabaseId;
    }
    //endregion
}
