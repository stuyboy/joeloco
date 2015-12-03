package com.joechang.loco.utils;

import java.util.Arrays;

/**
 * Author:    joechang
 * Created:   8/12/15 2:35 PM
 * Purpose:   Because I don't want to include all of apache commons, or rely on Android Libraries.
 */
public class StringUtils {
    public static final String NEWLINE = "\n";

    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token: tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    public static boolean isEmpty(String u) {
        return (u == null) || (u.length() <= 0);
    }

    public static String stripPhone(String phone) {
        String hackRemove = "+1-";
        return phone.replace(hackRemove, "");
    }

    public static String stripAddress(String address) {
        String addressStr = address;

        //Turn off for now since we have MMS
        if (addressStr.length() > 220) {
            addressStr = address
                    .replace(" St", "")
                    .replace(" Ave", "")
                    .replace(" Blvd", "")
                    .replace(" Way", "");
        }
        return addressStr;
    }

    /**
     * Each time we see a "\n", indent by a set number of spaces.
     * First line is not included as there is no "\n"!
     * @param incoming
     * @return
     */
    public static String indent(String incoming, int spaces) {
        if (incoming != null) {
            return incoming.replace("\n", "\n" + StringUtils.repeat(" ", spaces));
        }
        return incoming;
    }

    public static String repeat(String in, int n) {
        return new String(new char[n]).replace("\0", in);
    }

    public static String stripLastNewLine(String in) {
        if (in != null && in.endsWith(NEWLINE)) {
            in = in.substring(0, in.length() - NEWLINE.length());
        }
        return in;
    }

    public static String[] stripLastNewLine(String[] in) {
        if (in != null && in.length > 0) {
            String[] ret = new String[in.length];
            for (int i = 0; i < in.length; i++) {
                ret[i] = stripLastNewLine(in[i]);
            }
            in = ret;
        }

        return in;
    }

    public static String autoBreak(String... lines) {
        StringBuilder sb = new StringBuilder();
        for (String s : lines) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }
}
