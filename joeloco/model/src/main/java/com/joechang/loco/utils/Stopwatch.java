package com.joechang.loco.utils;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   9/30/15 10:56 AM
 * Purpose:
 */
public class Stopwatch {
    private static final Logger log = Logger.getLogger(Stopwatch.class.getName());
    private static final ThreadLocal<HashMap<String, Long>> map = new ThreadLocal<HashMap<String, Long>>();
    static {
        map.set(new HashMap<String, Long>());
    }

    public static void start(String key) {
        log.info("Starting timer for: " + key);
        map.get().put(key, now());
    }

    public static long split(String key) {
        Long prev = map.get().get(key);
        if (prev == null) {
            throw new IllegalArgumentException("Starting key not found.");
        }
        long now = now();
        long elapsed = now - prev;
        map.get().put(key, now);
        log.info(key + " duration: " + elapsed + "ms.");
        return elapsed;
    }

    public static long stop(String key) {
        Long prev = map.get().get(key);
        if (prev == null) {
            throw new IllegalArgumentException("Starting key not found.");
        }
        long elapsed = now() - prev;
        log.info("Ending timer for: " + key + " duration: " + elapsed + "ms.");
        map.get().remove(key);
        return elapsed;
    }

    public static void log(String msg) {
        log.info(now() + ":" + msg);
    }

    public static long now() {
        return System.currentTimeMillis();
    }
}
