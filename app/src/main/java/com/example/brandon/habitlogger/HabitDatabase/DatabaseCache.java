package com.example.brandon.habitlogger.HabitDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Brandon on 11/28/2016.
 * A place to store database records in ram.
 */

public class DatabaseCache implements Serializable {

    Map<Long, Habit> habits = new HashMap<>(20);
    Map<Long, HabitCategory> categories = new HashMap<>(20);

    public DatabaseCache(){

    }

    public void clearCache(){
        habits.clear();
    }

    public void cacheHabits(Habit habits[]){
        for(Habit habit : habits){
            Long databaseId = habit.getDatabaseId();
            if(!this.habits.containsKey(databaseId)){
                this.habits.put(databaseId, habit);
            }
        }
    }

    public void cacheHabit(Habit habit){
        this.habits.put(habit.getDatabaseId(), habit);
    }

    public void cacheCategories(HabitCategory categories[]){
        for(HabitCategory category: categories){
            Long databaseId = category.getDatabaseId();
            if(!this.categories.containsKey(databaseId)){
                this.categories.put(databaseId, category);
            }
        }
    }

    public void cacheCategory(HabitCategory category){
        this.categories.put(category.getDatabaseId(), category);
    }
}
