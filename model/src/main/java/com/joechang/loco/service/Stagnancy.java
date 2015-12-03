package com.joechang.loco.service;

import java.util.EnumSet;

/**
 * Author:  joechang
 * Date:    3/30/15
 * Purpose: Some time intervals that are then mapped to what we think the user is doing at the time.
 * Intervals INCLUDE the minimum time, run UNTIL the defined maximum time (does not include max time).
 */
public enum Stagnancy {
    //The lower and higher threshold for a stagnancy step, the maximum time we will sleep for movement
    SHUTDOWN     ( Integer.MAX_VALUE, Integer.MAX_VALUE, 60, 60), //For Override
    DEAD         ( 9 * 60 * 60, Integer.MAX_VALUE, 1800, 3600),     //Long period of inactivity
    SLEEPING     ( 5 * 60 * 60, 9 * 60 * 60,        900, 3600),
    WORKING      ( 1 * 60 * 60, 5 * 60 * 60,        600, 1800),
    IDLE         ( 5 * 60,      60 * 60,             30,  300),
    SETTLING     ( 60,           5 * 60,              5,   60),     //Transitioning period
    WALKING      ( 10,          60,                   2,   25),
    DRIVING      (  3,          10,                   1,    5),
    FLYING       (  1,           3,                   0,    1),
    REALTIME     (  0,           0,                  60,   60);     //Usually for override

    private int min;
    private int max;
    private int waitSpread;
    private int minWait;
    private int maxWait;

    private static final int MAX_WAIT = 3600;

    Stagnancy(int minSeconds, int maxSeconds, int minWait, int maxWait) {
        this.min = minSeconds;
        this.max = maxSeconds;
        this.minWait = minWait;
        this.maxWait = maxWait;

        //diff between min and max waits.
        this.waitSpread = this.maxWait - this.minWait;
    }

    //Yes, expensive.
    public static Stagnancy fromSeconds(int seconds) {
        for (Stagnancy s : values()) {
            if (seconds >= s.getMin() && seconds < s.getMax()) {
                return s;
            }
        }

        //If no match, then take middle of road.
        return WALKING;
    }

    public static EnumSet<Stagnancy> OVERRIDES = EnumSet.of(SHUTDOWN, REALTIME);
    public static EnumSet<Stagnancy> HIGH = EnumSet.range(DEAD, SETTLING);
    public static EnumSet<Stagnancy> LOW = EnumSet.range(WALKING, FLYING);

    //Typical waitSpread interval
    private int waitSpread() {
        return this.waitSpread;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getMinWait() {
        return minWait;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public long getMaxWaitInMillis() {
        return getMaxWait() * 1000;
    }

    public boolean isMoreStagnantThan(Stagnancy comp) {
        return this.compareTo(comp) < 0;
    }

    public boolean isLessStagnantThan(Stagnancy comp) {
        return this.compareTo(comp) > 0;
    }

    //As we are closer to certain intervals, vary the waitTime.  Ask peters?
    public int waitInterval(int secondsPassed) {
        double retSeconds = this.getMaxWait();
        int numerator = Math.max(secondsPassed - this.min, 1);

        //where is midpoint?
        int mid = Math.round((this.max - this.min) / 2 + this.min);

        //Are we past that or on the way there?
        if (secondsPassed > mid) {
            numerator = secondsPassed - mid;
        }

        float spread = (this.max - this.min) / 2;
        float prorate = Math.min(numerator / spread, 1.0f);
        retSeconds = getMinWait() + (prorate * waitSpread());

        if (secondsPassed > mid) {
            retSeconds = getMaxWait() - (int)retSeconds;
        }

        return Math.max(Math.min((int) retSeconds, this.getMaxWait()), this.getMinWait());
    }

    @Override
    public String toString() {
        return this.name() + " (" +
                min +
                " - " + max +
                "), sl [" + minWait +
                " - " + maxWait + "]";
    }
}

