package com.example.brandon.habitlogger.DatabaseTest;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.example.brandon.habitlogger.HabitDatabase.DataExportManager;
import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class DatabaseExportLocalTest extends AndroidTestCase{

    private HabitDatabase db;
    private DataExportManager dataExportManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new HabitDatabase(context);
        dataExportManager = new DataExportManager(context);

        // Add some junk data to the database
        HabitCategory mainCategory = new HabitCategory("color", "name");

        SessionEntry entries[] = new SessionEntry[10];

        for(int i = 0; i < entries.length; i++){
            entries[i] = new SessionEntry(0, 0, "note");
        }

        for(int i = 0; i < 10; i++){
            db.addHabit(new Habit("name " + i, "description", mainCategory, entries, "none"));
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


    public void testBackupDatabase(){
        int numberOfCategories = db.getNumberOfCategories();
        dataExportManager.exportDatabase(true);

        db.resetDatabase();
        assertEquals(0, db.getNumberOfCategories());

        dataExportManager.importDatabase(true);
        assertEquals(numberOfCategories, db.getNumberOfCategories());
    }

    public void testRestoreDatabase(){
        dataExportManager.exportDatabase(true);

        db.resetDatabase();
        assertEquals(0, db.getNumberOfCategories());
        dataExportManager.importDatabase(true);

        assertNotSame(0, db.getNumberOfCategories());
    }

    public void testBackupRestoreHabit(){
        Habit expectedHabit = db.getHabit(
                db.getHabitIdFromIndex(
                        db.getCategoryIdFromIndex(0), 0
                ));
        assertNotNull(expectedHabit);

        boolean result = dataExportManager.importHabit(dataExportManager.getFilePathFromHabit(expectedHabit, true), true);
        assertEquals(true , result);

        Habit actualHabit = dataExportManager.getHabit(
                dataExportManager.getFilePathFromHabit(expectedHabit, true),
                true
        );

        assertEquals(expectedHabit.toString(), actualHabit.toString());
    }

    public void testDeleteHabit(){
        Habit habit = db.getHabit(
                db.getHabitIdFromIndex(
                        db.getCategoryIdFromIndex(0), 0
                ));

        dataExportManager.exportHabit(habit, true);

        String filepath = dataExportManager.getFilePathFromHabit(habit, true);
        assertNotNull(dataExportManager.getHabit(filepath, true));

        dataExportManager.deleteHabit(habit.getCategory().getName(), habit.getName(), true);
        assertEquals(null, dataExportManager.getHabit(filepath, true));
    }
}
