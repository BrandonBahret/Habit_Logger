package com.example.brandon.habitlogger.DatabaseTest;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;

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

    private SessionEntry[] getDummyEntries(){
        SessionEntry entries[] = new SessionEntry[100];
        for(int i = 0; i < entries.length; i++){
            entries[i] = new SessionEntry(i, i,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        }
        return entries;
    }

    @Test
    public void testToString() throws Exception{
        Habit test = new Habit("Name", "description", new HabitCategory("color", "name"), "icon", null);

        String testString = test.toString();
        String expected   = "Name{\n\tDescription: description\n\tCategory: {\n\t\tName: name,\n\t\tColor: color\n\t}" +
                "\n\tIconResId: icon\n\tNumber of entries: 0\n\tIsArchived: false\n}\n";

        assertEquals(testString, expected);
    }

    @Test
    public void testCSVMethods() throws Exception{

        Habit expectedHabit = new Habit("Name", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                new HabitCategory("color", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
                "icon", getDummyEntries());

        String csv = expectedHabit.toCSV();
        Log.d("csv", csv);

        Habit actualHabit = Habit.fromCSV(csv);

        assertEquals(expectedHabit, actualHabit);
    }
}
