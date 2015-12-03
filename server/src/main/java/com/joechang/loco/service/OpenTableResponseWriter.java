package com.joechang.loco.service;

import com.google.gson.Gson;
import com.joechang.loco.model.BusinessResult;
import com.joechang.loco.model.RestaurantResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Author:    joechang
 * Created:   8/10/15 12:16 PM
 * Purpose:
 */
public class OpenTableResponseWriter extends AbstractResultResponseWriter<RestaurantResult> {
    public static final int MAX_TIME_RESULTS = 5;

    public String openTimesToText(RestaurantResult rr) {
        StringBuilder timesAvailable = new StringBuilder();
        String[] times = rr.getAvailableTimes();
        if (times.length > 0) {
            int totalToShow = Math.min(MAX_TIME_RESULTS, times.length);
            for (int i = 0; i < totalToShow; i++) {
                timesAvailable.append(times[i].trim());
                if (i < totalToShow - 1) {
                    timesAvailable.append(", ");
                }
            }
            if (times.length > MAX_TIME_RESULTS) {
                timesAvailable.append(" +" + (times.length - MAX_TIME_RESULTS) + " more");
            }
        } else {
            //TODO: Better way to handle this?
            timesAvailable.append("No tables for " + rr.getNumPeople() + " on " + mDf.format(rr.getReserveDate()));
        }
        return timesAvailable.toString();
    }

    @Override
    public String toText(RestaurantResult rr, Length l, String prefix) {
        StringBuilder sb = new StringBuilder(super.toText(rr, l, prefix));
        appendLine(sb, rr.getReserveUrl());
        appendLine(sb, openTimesToText(rr));
        return sb.toString();
    }

}
