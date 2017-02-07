package com.example.brandon.habitlogger.HabitDatabase;

import android.os.Parcel;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by Brandon on 2/6/2017.
 */

public class CategoryHabitsContainer extends ExpandableGroup<Habit>  {

    HabitCategory category;

    public CategoryHabitsContainer(HabitCategory category, List<Habit> items) {
        super(category.getName(), items);
        this.category = category;
    }

    protected CategoryHabitsContainer(Parcel in) {
        super(in);
    }

    public HabitCategory getCategory(){
        return category;
    }
}
