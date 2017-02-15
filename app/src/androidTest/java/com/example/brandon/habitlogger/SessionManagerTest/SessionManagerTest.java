package com.example.brandon.habitlogger.SessionManagerTest;

import android.os.SystemClock;
import android.test.AndroidTestCase;

import com.example.brandon.habitlogger.HabitSessions.SessionManager;

/**
 * Created by Brandon on 12/5/2016.
 * Test session manager
 */

public class SessionManagerTest extends AndroidTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
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