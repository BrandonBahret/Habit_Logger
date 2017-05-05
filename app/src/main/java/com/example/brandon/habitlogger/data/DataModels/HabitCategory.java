package com.example.brandon.habitlogger.data.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
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

public class HabitCategory implements Serializable, Parcelable {

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

    //region Methods responsible for making this object parcelable
    public HabitCategory(Parcel in) {
        HabitCategory category = (HabitCategory) in.readSerializable();
        HabitCategory.copy(this, category);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Creator<HabitCategory> CREATOR = new Creator<HabitCategory>() {
        @Override
        public HabitCategory createFromParcel(Parcel in) {
            return new HabitCategory(in);
        }

        @Override
        public HabitCategory[] newArray(int size) {
            return new HabitCategory[size];
        }
    };
    //endregion -- end --

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HabitCategory) {
            HabitCategory compare = (HabitCategory) obj;
            return compare.getName().equals(getName()) &&
                    compare.getColor().equals(getColor());
        }
        else return false;
    }

    public static void copy(HabitCategory dest, HabitCategory source){
        dest.setName(source.getName());
        dest.setColor(source.getColor());
        dest.setDatabaseId(source.getDatabaseId());
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
