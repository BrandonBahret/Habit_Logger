package com.example.brandon.habitlogger.DatabaseTest;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class HabitDatabaseHabitMethodsTestNonAsync extends AndroidTestCase {
    private HabitDatabase db;

    public Habit getDummyHabit(){
        return new Habit("dummy", "I am a dummy", new HabitCategory("#ffffffff", "dummy"), "", null);
    }

    public Habit getDummyHabit2(){
        return new Habit("dummy2", "I am a dummy2", new HabitCategory("#ffffffff", "dummy"), "", null);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new HabitDatabase(context);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddHabit(){
        Habit dummyHabit = getDummyHabit();

        long id = db.addHabit(dummyHabit);
        Habit actualHabit = db.getHabit(id);

        assertNotNull(actualHabit);
        assertEquals(dummyHabit, actualHabit);
        assertEquals(dummyHabit.getDatabaseId(), actualHabit.getDatabaseId());
    }

    public void testGetNumberOfHabits(){
        Habit dummyHabit = getDummyHabit();
        db.addHabit(dummyHabit);

        assertEquals(1, db.getCountOfAllHabits(dummyHabit.getCategory().getDatabaseId()));
    }

    public void testGetNumberOfEntries(){
        long id = db.addHabit(getDummyHabit());

        db.addEntry(id, new SessionEntry(0, 0, ""));
        db.addEntry(id, new SessionEntry(0, 0, ""));
        db.addEntry(id, new SessionEntry(0, 0, ""));
        db.addEntry(id, new SessionEntry(0, 0, ""));

        assertEquals(4, db.getNumberOfEntries(id));
    }

    public void testGetHabitIdFromIndex(){
        Habit dummyHabit = getDummyHabit();

        long expectedId = db.addHabit(dummyHabit);

        assertEquals(expectedId, db.getHabitIdFromIndex(dummyHabit.getCategory().getDatabaseId(), 0));
    }

    public void testGetHabitIdFromObject(){
        Habit dummyHabit = getDummyHabit();

        long expectedId = db.addHabit(dummyHabit);

        assertEquals(expectedId, db.getHabitIdFromObject(dummyHabit));
    }

    public void testSearchHabitIdsByName(){

        Set<Long> expectedIds = new HashSet<>(2);

        db.addHabit(new Habit("one", "", getDummyHabit().getCategory(), "", null));
        expectedIds.add(db.addHabit(new Habit("two", "", getDummyHabit().getCategory(), "", null)));
        expectedIds.add(db.addHabit(new Habit("three", "", getDummyHabit().getCategory(), "", null)));
        db.addHabit(new Habit("four", "", getDummyHabit().getCategory(), "", null));

        assertTrue(expectedIds.equals(db.findHabitIdsByName("t")));
    }

    public void testGetHabit(){

        Habit expectedHabit = getDummyHabit();

        long id = db.addHabit(expectedHabit);
        Habit actualHabit = db.getHabit(id);

        assertNotNull(actualHabit);
        assertEquals(expectedHabit, actualHabit);
        assertEquals(expectedHabit.getDatabaseId(), actualHabit.getDatabaseId());
    }

    public void testGetHabits(){
        List<Habit> expectedHabits = new ArrayList<>(2);
        expectedHabits.add(getDummyHabit());
        expectedHabits.add(getDummyHabit2());

        db.addHabit(expectedHabits.get(0));
        db.addHabit(expectedHabits.get(1));

        List<Habit> actualHabits = db.getHabits(expectedHabits.get(0).getCategory().getDatabaseId());

        assertTrue(expectedHabits.equals(actualHabits));
    }

    public void testUpdateHabitIsArchived(){
        Habit origHabit = getDummyHabit();
        long id = db.addHabit(origHabit);

        Habit expectedHabit = getDummyHabit();
        expectedHabit.setIsArchived(true);

        long actualId = db.updateHabitIsArchived(id, true);
        Habit actualHabit = db.getHabit(actualId);

        assertTrue(expectedHabit.equals(actualHabit));
    }

    public void testUpdateHabitName(){
        Habit origHabit = getDummyHabit();

        long id = db.addHabit(origHabit);

        Habit expectedHabit = getDummyHabit();
        expectedHabit.setName("New Name");

        db.updateHabitName(id, "New Name");

        Habit actualHabit = db.getHabit(id);

        assertEquals(expectedHabit, actualHabit);
    }

    public void testUpdateHabitDescription(){
        Habit origHabit = getDummyHabit();

        long id = db.addHabit(origHabit);

        Habit expectedHabit = getDummyHabit();
        expectedHabit.setDescription("New Description");

        db.updateHabitDescription(id, "New Description");

        Habit actualHabit = db.getHabit(id);

        assertEquals(expectedHabit, actualHabit);
    }

    public void testUpdateHabitCategory(){
        Habit origHabit = getDummyHabit();
        long id = db.addHabit(origHabit);

        Habit expectedHabit = getDummyHabit();
        expectedHabit.setCategory(new HabitCategory("color", "New Category"));

        long newCategoryId = db.addCategory(new HabitCategory("color", "New Category"));
        db.updateHabitCategory(id, newCategoryId);

        Habit actualHabit = db.getHabit(id);

        assertEquals(expectedHabit, actualHabit);
    }

    public void testUpdateHabitIconResId(){
        Habit origHabit = getDummyHabit();

        long id = db.addHabit(origHabit);

        Habit expectedHabit = getDummyHabit();
        expectedHabit.setIconResId("New Icon");

        db.updateHabitIconResId(id, "New Icon");

        Habit actualHabit = db.getHabit(id);

        assertEquals(expectedHabit, actualHabit);
    }

    public void testUpdateHabit(){
        Habit origHabit = getDummyHabit();
        long id = db.addHabit(origHabit);

        Habit expectedHabit = getDummyHabit2();
        db.updateHabit(id, getDummyHabit2());

        Habit actualHabit = db.getHabit(id);

        assertEquals(expectedHabit, actualHabit);
    }

    public void testDeleteHabit(){
        Habit origHabit = getDummyHabit();
        long id = db.addHabit(origHabit);

        assertNotNull(db.getHabit(id));

        db.deleteHabit(id);

        assertEquals(null, db.getHabit(id));
    }
}