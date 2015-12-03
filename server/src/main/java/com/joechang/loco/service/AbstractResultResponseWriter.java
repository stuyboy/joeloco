package com.joechang.loco.service;

import com.google.gson.Gson;
import com.joechang.loco.model.BusinessResult;
import com.joechang.loco.utils.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Author:    joechang
 * Created:   9/22/15 1:36 PM
 * Purpose:
 */
public class AbstractResultResponseWriter<T extends BusinessResult> {
    public enum Format {
        TEXT
    }

    public enum Length {
        SHORT,
        FULL
    }

    public static final Gson gson = new Gson();
    public static final DateFormat mDf = new SimpleDateFormat("EEE M/d h:mma");

    public String toJson(T rr) {
        return gson.toJson(rr);
    }

    public String toJson(Collection<T> rrs) {
        return gson.toJson(rrs);
    }

    public String[] toText(Collection<T> rrs, Length l, boolean enumerate) {
        List<String> ret = new ArrayList<>();
        int counter = 1;

        for (T r : rrs) {
            String number = enumerate ? numberBullet(counter) : "";
            String body = toText(r, l, number);
            if (enumerate) {
                body = StringUtils.indent(body, number.length()).trim();
            }
            ret.add(body);
            counter++;
        }

        return ret.toArray(new String[]{});
    }

    public String[] toText(Collection<T> rrs) {
        return toText(rrs, Length.SHORT, true);
    }

    public String toText(T br) {
        return toText(br, Length.SHORT);
    }

    public String toText(T br, Length length) {
        return toText(br, length, "");
    }

    protected String toText(T br, Length length, String prefix) {
        StringBuilder sb = new StringBuilder(prefix);

        //Business name
        appendLine(sb, br.getName());

        //Rating and review count
        appendLine(sb, ratingStars(br.getRating()), " ", br.getNumReviews(), " reviews");

        //Rating and name
//        append(sb, "[", br.getRating(), "] ");
//        appendLine(sb, br.getName());

        //Now build address
        appendLine(sb, br.getAddress());

        //Phone
        appendLine(sb, br.getPhoneNumber());

        if (Length.SHORT.equals(length)) {
            return sb.toString();
        }

        //Review number
        appendLine(sb, br.getNumReviews(), " reviews");

        //Is it closed
        appendLine(sb, br.getOpenStatus());

        //Link to more
        appendLine(sb, br.getReviewUrl());

        //Quote
        appendLine(sb, "\"", br.getSnippet(), "\"");

        return sb.toString();
    }

    protected void appendLine(StringBuilder sb, String... attr) {
        append(sb, true, attr);
    }

    protected void append(StringBuilder sb, String... attr) {
        append(sb, false, attr);
    }

    private void append(StringBuilder sb, boolean newLine, String... attr) {
        StringBuilder internalSB = new StringBuilder();

        for (String s : attr) {
            if (s == null || s.isEmpty()) {
                //If any piece of this is empty, don't append!
                return;
            } else {
                internalSB.append(s);
            }
        }

        sb.append(internalSB);

        if (newLine) {
            sb.append(StringUtils.NEWLINE);
        }
    }

    public String numberBullet(int idx) {
        return idx + ". ";
    }

    public String ratingStars(String rating) {
        if (rating == null) {
            return null;
        }

        String stars = "";
        float numrating = Float.parseFloat(rating);
        for (int i = 0; i < numrating; i++) {
            if ((i+1) > numrating) {
                stars += "½";
                break;
            } else {
                stars += "★";
            }
        }
        return stars;
    }

    public String summarizeQuery(String name, String cuisine, String where, Date when, int numPeople) {
        if (name == null && cuisine == null) {
            throw new IllegalArgumentException("Cannot summarize query");
        }

        StringBuilder sb = new StringBuilder();

        //One of these has to be in there.
        sb.append(name != null ? name : "");
        sb.append(cuisine != null ? cuisine : "");

        //Prettify with upper case.
        sb.replace(0, 1, sb.substring(0, 1).toUpperCase());

        sb.append(where != null ? " in " + where : "");

        if (numPeople > 0) {
            sb.append(" for ").append(numPeople);
        }

        if (when != null) {
            sb.append(" on " + mDf.format(when));
        }

        return sb.toString();
    }
}
