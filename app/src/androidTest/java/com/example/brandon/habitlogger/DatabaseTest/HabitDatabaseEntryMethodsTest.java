package com.example.brandon.habitlogger.DatabaseTest;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

import java.util.Arrays;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class HabitDatabaseEntryMethodsTest extends AndroidTestCase {
    private HabitDatabase db;
    private long mainHabitId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new HabitDatabase(context);

        HabitCategory mainCategory = new HabitCategory("color", "name");
        Habit mainHabit = new Habit("name", "", mainCategory, null, "");
        mainHabitId = db.addHabit(mainHabit);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddEntry(){
        SessionEntry expectedEntry = new SessionEntry(0, 0, "");

        long id = db.addEntry(mainHabitId, expectedEntry);

        SessionEntry actualEntry = db.getEntry(id);

        assertNotNull(actualEntry);
        assertEquals(expectedEntry, actualEntry);
        assertEquals(id, actualEntry.getDatabaseId());
    }

    public void testGetEntryIdFromIndex(){
        SessionEntry expectedEntry = new SessionEntry(0, 0, "");

        long expectedId = db.addEntry(mainHabitId, expectedEntry);
        long actualId   = db.getEntryIdFromIndex(mainHabitId, 0);

        assertEquals(expectedId, actualId);
    }

    public void testGetEntryIdByObject(){
        SessionEntry expectedEntry = new SessionEntry(0, 0, "");

        long expectedId = db.addEntry(mainHabitId, expectedEntry);
        long actualId   = db.getEntryIdByObject(expectedEntry);

        assertEquals(expectedId, actualId);
    }

    public void testSearchEntryIdsByComment(){
        db.addEntry(mainHabitId, new SessionEntry(0, 0, "1"));
        db.addEntry(mainHabitId, new SessionEntry(0, 0, "2"));
        db.addEntry(mainHabitId, new SessionEntry(0, 0, "3"));
        long expectedId  = db.addEntry(mainHabitId, new SessionEntry(0, 0, "4"));
        long actualIds[] = db.searchEntryIdsByComment(mainHabitId, "4");

        assertNotNull(actualIds);
        assertEquals(expectedId, actualIds[0]);
    }

    public void testSearchEntriesWithTimeRangeForAHabit(){
        long expectedIds[] = new long[2];

        db.addEntry(mainHabitId, new SessionEntry(0, 0, "1"));
        expectedIds[0] = db.addEntry(mainHabitId, new SessionEntry(600, 0, "4"));
        expectedIds[1] = db.addEntry(mainHabitId, new SessionEntry(600, 0, "5"));
        db.addEntry(mainHabitId, new SessionEntry(0, 0, "7"));

        long actualIds[] = db.searchEntriesWithTimeRangeForAHabit(mainHabitId, 500, 1000);

        assertNotNull(expectedIds);
        assertNotNull(actualIds);
        assertTrue(Arrays.equals(expectedIds, actualIds));
    }

    public void testSearchAllEntriesWithTimeRange(){
        long expectedIds[] = new long[4];
        int idInd = 0;

        long newHabitId = db.addHabit(new Habit("new habit", "", new HabitCategory("color", "name"), null, ""));

        db.addEntry(mainHabitId, new SessionEntry(0, 0, "1"));
        db.addEntry(mainHabitId, new SessionEntry(0, 0, "2"));
        expectedIds[idInd++] = db.addEntry(newHabitId, new SessionEntry(600, 0, "3"));
        expectedIds[idInd++] = db.addEntry(newHabitId, new SessionEntry(600, 0, "4"));
        expectedIds[idInd++] = db.addEntry(mainHabitId, new SessionEntry(600, 0, "5"));
        expectedIds[idInd] = db.addEntry(mainHabitId, new SessionEntry(600, 0, "6"));
        db.addEntry(mainHabitId, new SessionEntry(0, 0, "7"));

        long actualIds[] = db.searchAllEntriesWithTimeRange(500, 1000);

        assertNotNull(expectedIds);
        assertNotNull(actualIds);
        assertTrue(Arrays.equals(expectedIds, actualIds));
    }

    public void testGetEntryId(){
        SessionEntry expectedEntry = new SessionEntry(0, 0, "");

        long expectedId = db.addEntry(mainHabitId, expectedEntry);
        long actualId   = db.getEntryId(mainHabitId, 0);

        assertEquals(expectedId, actualId);
    }

    public void testGetEntryOnId(){
        SessionEntry expectedEntry = new SessionEntry(0, 0, "");
        long expectedId = db.addEntry(mainHabitId, expectedEntry);

        SessionEntry actualEntry = db.getEntry(expectedId);

        assertNotNull(actualEntry);
        assertEquals(expectedEntry, actualEntry);
        assertEquals(expectedEntry.getDatabaseId(), actualEntry.getDatabaseId());
    }

    public void testUpdateEntryStartTime(){
        SessionEntry origEntry = new SessionEntry(0, 0, "");
        long expectedId = db.addEntry(mainHabitId, origEntry);

        SessionEntry expectedEntry = new SessionEntry(50, 0, "");
        expectedEntry.setHabitId(mainHabitId);

        db.updateEntryStartTime(expectedId, 50);

        SessionEntry actualEntry = db.getEntry(expectedId);

        assertNotNull(actualEntry);
        assertEquals(expectedEntry, actualEntry);
    }

    public void testUpdateEntryDuration(){
        SessionEntry origEntry = new SessionEntry(0, 0, "");
        long expectedId = db.addEntry(mainHabitId, origEntry);

        SessionEntry expectedEntry = new SessionEntry(0, 50, "");
        expectedEntry.setHabitId(mainHabitId);

        db.updateEntryDuration(expectedId, 50);

        SessionEntry actualEntry = db.getEntry(expectedId);

        assertNotNull(actualEntry);
        assertEquals(expectedEntry, actualEntry);
    }

    public void testUpdateEntryNote(){
        SessionEntry origEntry = new SessionEntry(0, 0, "");
        long expectedId = db.addEntry(mainHabitId, origEntry);

        SessionEntry expectedEntry = new SessionEntry(0, 0, "New Note");
        expectedEntry.setHabitId(mainHabitId);

        db.updateEntryNote(expectedId, "New Note");

        SessionEntry actualEntry = db.getEntry(expectedId);

        assertNotNull(actualEntry);
        assertEquals(expectedEntry, actualEntry);
    }

    public void testUpdateEntry(){
        SessionEntry origEntry = new SessionEntry(0, 0, "");
        long expectedId = db.addEntry(mainHabitId, origEntry);

        SessionEntry expectedEntry = new SessionEntry(50, 50, "New Note");
        expectedEntry.setHabitId(mainHabitId);

        db.updateEntry(expectedId, expectedEntry);

        SessionEntry actualEntry = db.getEntry(expectedId);

        assertNotNull(actualEntry);
        assertEquals(expectedEntry, actualEntry);
    }

    public void testDeleteEntry(){
        SessionEntry entry = new SessionEntry(0, 0, "");
        long expectedId = db.addEntry(mainHabitId, entry);

        db.deleteEntry(expectedId);

        assertEquals(null, db.getEntry(expectedId));
    }

    public void testDeleteEntriesForHabit(){
        long expectedIds[] = new long[2];

        expectedIds[0] = db.addEntry(mainHabitId, new SessionEntry(0, 0, "3"));
        expectedIds[1] = db.addEntry(mainHabitId, new SessionEntry(0, 0, "4"));

        for(long id : expectedIds){
            assertNotNull(db.getEntry(id));
        }

        db.deleteEntriesForHabit(mainHabitId);

        for(long id : expectedIds){
            assertEquals(null, db.getEntry(id));
        }
    }
}