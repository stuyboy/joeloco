package com.joechang.loco.utils;

import java.util.Calendar;

/**
* Author:  joechang
* Date:    4/21/15
* Purpose:
*/
public class DateBag {
    public int year;
    public int month;
    public int day;

    private DateBag(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static DateBag getInstance(Calendar c) {
        return new DateBag(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        );
    }
}
