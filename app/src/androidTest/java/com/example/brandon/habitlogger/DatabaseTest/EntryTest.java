package com.example.brandon.habitlogger.DatabaseTest;

import android.support.test.runner.AndroidJUnit4;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class EntryTest {

    @Test
    public void testToString() throws Exception{
        SessionEntry test = new SessionEntry(100, 200, "note");

        String testString = test.toString();
        String expected   = "{\n" +
                "\tStarting Time: 100,\n" +
                "\tDuration: 200\n" +
                "\tNote: note\n" +
                "}\n";

        assertEquals(testString, expected);
    }
}
