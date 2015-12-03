package com.joechang.kursor;

import com.joechang.loco.model.Search;

/**
 * Author:    joechang
 * Created:   9/14/15 3:05 PM
 * Purpose:   Small class to refactor things
 */
public class CliOutputHelper {

    public static final String NO_RESULTS = "No results";

    public static String formatNotFound(String originalQuery) {
        StringBuilder sb = new StringBuilder(NO_RESULTS);
        formatNotFoundQuery(sb, originalQuery);
        return sb.toString();
    }

    public static String formatNotFound(Search s) {
        if (s.getSource() == null) {
            return formatNotFound(s.getQuery());
        }

        StringBuilder sb = new StringBuilder("No ").append(s.getSource()).append(" results");
        formatNotFoundQuery(sb, s.getQuery());
        return sb.toString();
    }

    protected static void formatNotFoundQuery(StringBuilder sb, String originalQuery) {
        if (originalQuery != null && !originalQuery.isEmpty()) {
            sb.append(" for ");
            sb.append(originalQuery.trim());
        }
    }

}
