package com.example.brandon.habitlogger.SessionManagerTest;

import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.SessionManager.SessionManager;

/**
 * Created by Brandon on 12/5/2016.
 */

public class SessionManagerTest extends AndroidTestCase {
    private SessionManager mng;
    RenamingDelegatingContext context;

    private final long habitId = 238;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = new RenamingDelegatingContext(getContext(), "test_");
        mng = new SessionManager(context);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetCurrentTime(){
        long currentTime = mng.getCurrentTime();
        SystemClock.sleep(100);
        long nextTime = mng.getCurrentTime();
        assertTrue((nextTime - currentTime) >= 100);
    }

    public void testStartSession() {
        mng.startSession(habitId);
        SessionEntry entry = mng.getSession(habitId);
        assertTrue(habitId == entry.getHabitId());
    }

    public void testPausePlaySession(){
        // Start session
        mng.startSession(habitId);

        // Pause session for 250 ms
        mng.pauseSession(habitId);
        SystemClock.sleep(100);

        // Play the session
        mng.playSession(habitId);

        long pauseTime = mng.getTotalPauseTime(habitId);
        assertTrue(pauseTime >= 100);
    }

    public void testCancelSession(){
        mng.startSession(habitId);
        SessionEntry entry = mng.getSession(habitId);
        assertTrue(habitId == entry.getHabitId());

        mng.cancelSession(habitId);
        assertTrue(!mng.isSessionActive(habitId));
    }

    public void testFinishSession(){
        mng.startSession(habitId);
        SystemClock.sleep(100);
        SessionEntry entry = mng.finishSession(habitId);

        assertTrue(entry.getDuration() >= 100);
    }
}