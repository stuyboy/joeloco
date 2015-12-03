package com.joechang.loco.utils;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Author:    joechang
 * Created:   9/1/15 1:52 PM
 * Purpose:
 */
public class DateParsingUtilsTest {

    @Test
    public void testAttemptMatch() throws Exception {
            DateParsingUtils dp = new DateParsingUtils();

            o(dp.attemptMatch("next tuesday at noon"));
            o(dp.attemptMatch("this thursday"));
            o(dp.attemptMatch("April 4"));
            o(dp.attemptMatch("Oct 15"));

            //System.out.println("Nothing");
            o(dp.attemptMatch("wrong"));
    }

    public void o(Date d) {
        System.out.println(SimpleDateFormat.getInstance().format(d));
    }
}