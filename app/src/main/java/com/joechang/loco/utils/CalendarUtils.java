package com.joechang.loco.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Author:  joechang
 * Date:    4/21/15
 * Purpose: Strange little class with helpers to initialize stuff.
 */
public class CalendarUtils {

    //The primary formats.
    private static DateFormat defaultTimeFormat() {
        return SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
    }

    private static DateFormat defaultDateFormat() {
        return SimpleDateFormat.getDateInstance(DateFormat.LONG);
    }

    public static DateBag getDateBag() {
        return DateBag.getInstance(Calendar.getInstance());
    }

    public static String getDateString(Calendar c) {
        return defaultDateFormat().format(c.getTime());
    }

    public static String getTimeString(Calendar c) {
        return defaultTimeFormat().format(c.getTime());
    }

    public static Calendar fromDateString(String s) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(defaultDateFormat().parse(s));
            return c;
        } catch (ParseException pe) {
            //oops, not good.
        }

        return getNextStart();
    }

    public static Calendar fromTimeString(String s) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(defaultTimeFormat().parse(s));
            return c;
        } catch (ParseException pe) {
            //ugh
        }

        return getNextStart();
    }

    public static Calendar fromDateTimeString(String date, String time) {
        Calendar d = fromDateString(date);
        Calendar t = fromTimeString(time);

        d.set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY));
        d.set(Calendar.MINUTE, t.get(Calendar.MINUTE));
        d.set(Calendar.SECOND, t.get(Calendar.SECOND));

        return d;
    }

    public static String getNextStartDateString() {
        return getDateString(getNextStart());
    }

    public static String getNextEndDateString() {
        return getDateString(getLogicalEnd(getNextStart()));
    }


    /**
     * Using current time, find entry for a calendar item, namely, the date, but next hour
     * @return date
     */
    public static Date getNextCalendarDate() {
        return getNextStart().getTime();
    }

    public static Calendar getNextStart() {
        Calendar cc = Calendar.getInstance();
        cc.roll(Calendar.HOUR, 1);
        cc.set(Calendar.MINUTE, 0);
        cc.set(Calendar.SECOND, 0);
        return cc;
    }

    public static Calendar getNextEnd() {
       return getLogicalEnd(getNextStart());
    }

    /**
     * Given a calendar date, find a logical end time for an event.  Most likely one hour later.
     * @param fromCalendar
     * @return endCalendar
     */
    public static Calendar getLogicalEnd(Calendar fromCalendar) {
        Calendar endCalendar = (Calendar)fromCalendar.clone();
        endCalendar.add(Calendar.HOUR_OF_DAY, 1);
        return endCalendar;
    }

    /**
     * Keep from having end before from by resetting as appropriate.
     * @param fromCalendar
     * @param endCalendar
     * @return the modified endCalendar date, or the original endCalendar if not changed.
     */
    public static Calendar normalizeCalendars(Calendar fromCalendar, Calendar endCalendar) {
        if (fromCalendar.after(endCalendar)) {
            return getLogicalEnd(fromCalendar);
        }

        return endCalendar;
    }
}
