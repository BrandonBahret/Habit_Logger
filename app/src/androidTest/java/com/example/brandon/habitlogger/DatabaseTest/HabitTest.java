package com.example.brandon.habitlogger.DatabaseTest;

import android.support.test.runner.AndroidJUnit4;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class HabitTest {

    @Test
    public void testToString() throws Exception{
        Habit test = new Habit("Name", "description", new HabitCategory("color", "name"), null, "icon");

        String testString = test.toString();
        String expected   = "Name{\n\tDescription: description\n\tCategory: {\n\t\tName: name,\n\t\tColor: color\n\t}" +
                "\n\tIconResId: icon\n\tNumber of entries: 0\n}\n";;

        assertEquals(testString, expected);
    }
}
