package com.example.brandon.habitlogger.SessionManagerTest;

import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;

/**
 * Created by Brandon on 12/5/2016.
 */

public class SessionManagerTest extends AndroidTestCase {
    private SessionManager mng;
    private RenamingDelegatingContext context;
    private long habitId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = new RenamingDelegatingContext(getContext(), "test_");

        HabitDatabase db = new HabitDatabase(context);
        habitId = db.addHabitAndCategory(new Habit("null", "null", new HabitCategory("#FFFFFFFF", "null"), null, "null"));
        mng = new SessionManager(context);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetCurrentTime(){
        long currentTime = SessionManager.getCurrentTime();
        SystemClock.sleep(100);
        long nextTime = SessionManager.getCurrentTime();
        assertTrue((nextTime - currentTime) >= 100);
    }

}