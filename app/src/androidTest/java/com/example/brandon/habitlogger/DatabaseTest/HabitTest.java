package com.example.brandon.habitlogger.DatabaseTest;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;

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

    private SessionEntryCollection getDummyEntries() {
        SessionEntryCollection entries = new SessionEntryCollection(100);
        for (int i = 0; i < entries.size(); i++) {
            entries.add(i, new SessionEntry(i, i,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit."));
        }
        return entries;
    }

    @Test
    public void testCSVMethods() throws Exception {

        Habit expectedHabit = new Habit("Name", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                new HabitCategory("color", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
                "icon", getDummyEntries());

        String csv = expectedHabit.toCSV();
        Log.d("csv", csv);

        Habit actualHabit = Habit.fromCSV(csv);

        assertEquals(expectedHabit, actualHabit);
    }
}
