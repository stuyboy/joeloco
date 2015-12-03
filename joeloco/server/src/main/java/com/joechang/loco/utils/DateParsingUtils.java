package com.joechang.loco.utils;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Author:    joechang
 * Created:   9/1/15 1:40 PM
 * Purpose:   Take the messy world of date determined by text and try to find the match?!
 */
public class DateParsingUtils {
    private static Parser mParser = new Parser();

    /**
     * Take text and try to create a date.
     * @param incoming
     * @return date
     */
    public static Date attemptMatch(String incoming) {
        List<DateGroup> dateGroups = mParser.parse(incoming);

        for (DateGroup dg : dateGroups) {
            List<Date> dates = dg.getDates();
            for (Date d : dates) {
                normalizeForMeals(d);
                return d;
            }
        }

        return new Date();
    }

    /**
     * Given a date and time, see if we can guess when the user wants to eat.  Mostly lunch or dinner.
     * @param incoming
     * @return date
     */
    public static void normalizeForMeals(Date incoming) {
        int hourOfDay = incoming.getHours();
        int minutes = incoming.getMinutes();
        int seconds = incoming.getSeconds();

        //Probably no real date supplied.
        if (((seconds != 00) && (minutes%5 != 0)) || hourOfDay <= 9) {
            if (hourOfDay > 12) {
                incoming.setHours(19);
                incoming.setMinutes(0);
            } else {
                incoming.setHours(12);
                incoming.setMinutes(0);
            }
        }
    }

}
