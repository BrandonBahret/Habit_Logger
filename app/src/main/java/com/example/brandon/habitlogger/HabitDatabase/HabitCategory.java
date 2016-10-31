package com.example.brandon.habitlogger.HabitDatabase;

import android.graphics.Color;
import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the category object.
 * This object is used to give habits categorical organization.
 */

public class HabitCategory {
    @NonNull private String color = "";
    @NonNull private String name  = "";

    private long databaseId = -1;

    /**
     * @param color A color in hexadecimal format. Ex: "#ffffff"
     * @param name The name of the category
     */
    public HabitCategory(@NonNull String color, @NonNull String name){
        this.color = color;
        this.name  = name;
    }

    /**
     * @param color A color represented by an int
     * @param name The name of the category
     */
    public HabitCategory(int color, @NonNull String name){
        // Todo test this function
        setColor("#" + Integer.toHexString(color).toUpperCase());
        setName(name);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HabitCategory){
            HabitCategory compare = (HabitCategory)obj;
            return compare.getName().equals(getName()) &&
                    compare.getColor().equals(getColor());
        }
        else{
            return false;
        }
    }

    public String toString(){
        String format = "%s {\n\tColor: %s\n}\n";
        return String.format(Locale.US, format, getName(), getColor());
    }

    /**
     * @param color A color in hexadecimal form Ex: "#ffffff"
     */
    public void setColor(@NonNull String color){
        this.color = color;
    }

    /**
     * @return A color in hexadecimal form Ex: "#ffffff"
     */
    @NonNull
    public String getColor(){
        return this.color;
    }

    /**
     * @return A color stored as an int
     */
    public int getColorAsInt(){
        return Color.parseColor(getColor());
    }

    /**
     * @param name The name of the category
     */
    public void setName(@NonNull String name){
        this.name = name;
    }

    /**
     * @return The name of the category
     */
    @NonNull
    public String getName(){
        return this.name;
    }

    /**
     * @param databaseId The row id of the category in the database, -1 if not available.
     */
    protected void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    /**
     * @return The row id of the category in the database, -1 if not available.
     */
    public long getDatabaseId() {
        return databaseId;
    }
}
