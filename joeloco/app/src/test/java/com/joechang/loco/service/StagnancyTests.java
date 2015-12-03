package com.joechang.loco.service;

import android.app.Application;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

/**
 * Author:  joechang
 * Date:    3/30/15
 * Purpose:
 */
public class StagnancyTests extends ApplicationTestCase<Application> {
    public StagnancyTests() {
        super(Application.class);
    }

    public void testWaitTimeCalculations() {
        Stagnancy c = Stagnancy.SETTLING;   //5s to 300s

        assertEquals(28, c.waitInterval(240));
        assertEquals(60, c.waitInterval(180));
        assertEquals(5, c.waitInterval(60));
        assertEquals(5, c.waitInterval(299));
        assertEquals(10, c.waitInterval(72));
        assertEquals(6, c.waitInterval(288));
        assertEquals(11, c.waitInterval(75));
        assertEquals(7, c.waitInterval(285));

        Stagnancy i = Stagnancy.IDLE;
        assertEquals(30, i.waitInterval(302));

        Stagnancy w = Stagnancy.WALKING;
        assertEquals(w.getMinWait(), w.waitInterval(w.getMin()));

        Stagnancy s = Stagnancy.SETTLING;
        assertEquals(s.getMinWait(), s.waitInterval(s.getMin()));

        Stagnancy r = Stagnancy.REALTIME;
        assertEquals(60, r.waitInterval(100));
    }

    public void testComparison() {
        assertTrue(Stagnancy.SETTLING.isLessStagnantThan(Stagnancy.DEAD));
        assertTrue(Stagnancy.SETTLING.isMoreStagnantThan(Stagnancy.WALKING));
        assertFalse(Stagnancy.SETTLING.isMoreStagnantThan(Stagnancy.SETTLING));
    }
}
