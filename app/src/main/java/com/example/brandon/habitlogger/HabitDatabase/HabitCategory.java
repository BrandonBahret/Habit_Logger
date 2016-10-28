package com.example.brandon.habitlogger.HabitDatabase;

import android.graphics.Color;
import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * Created by Brandon on 10/26/2016.
 * This is a class that defines the category object.
 * This object is used to give habits categorical organization.
 */

public class HabitCategory {
    private String hexColor;
    private String categoryName;

    private Long databaseId = null;

    /**
     * @param color A color in hexadecimal format. Ex: "#ffffff"
     * @param name The name of the category
     */
    public HabitCategory(String color, String name){
        setColor(color);
        setName(name);
    }

    /**
     * @param color A color represented by an int
     * @param name The name of the category
     */
    public HabitCategory(int color, String name){
        // Todo test this function
        setColor(Integer.toHexString(color));
        setName(name);
    }

    public String toString(){
        String format = "Category {\n\tName: %s\n\tColor: %s\n}\n";
        return String.format(Locale.US, format, getName(), getColor());
    }

    /**
     * @param color A color in hexadecimal form Ex: "#ffffff"
     */
    public void setColor(String color){
        this.hexColor = color;
    }

    /**
     * @param name The name of the category
     */
    public void setName(String name){
        this.categoryName = name;
    }

    /**
     * @return A color in hexadecimal form Ex: "#ffffff"
     */
    public String getColor(){
        return this.hexColor;
    }

    /**
     * @return A color stored as an int
     */
    public int getColorAsInt(){
        return Color.parseColor(getColor());
    }

    /**
     * @return The name of the category
     */
    public String getName(){
        return this.categoryName;
    }

    @Nullable
    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(@Nullable Long databaseId) {
        this.databaseId = databaseId;
    }
}
