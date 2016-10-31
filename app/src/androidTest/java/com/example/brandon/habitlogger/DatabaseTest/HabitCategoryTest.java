package com.example.brandon.habitlogger.DatabaseTest;

import android.support.test.runner.AndroidJUnit4;

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
public class HabitCategoryTest {
    @Test
    public void testToString() throws Exception{
        HabitCategory test = new HabitCategory("color", "name");

        String testString = test.toString();
        String expected   = "name {\n\tColor: color\n}\n";

        assertEquals(testString, expected);
    }

    @Test
    public void testConstructorWithIntColor() throws Exception{
        HabitCategory test = new HabitCategory(0xFFEF9A9A, "name");

        assertEquals("#FFEF9A9A", test.getColor());
    }

    @Test
    public void testGetColorAsInt() throws Exception{
        HabitCategory test = new HabitCategory(0xFFEF9A9A, "name");

        assertEquals(0xFFEF9A9A, test.getColorAsInt());
    }
}
