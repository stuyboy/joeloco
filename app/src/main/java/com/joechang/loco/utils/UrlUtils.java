package com.joechang.loco.utils;

import android.net.Uri;

import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;

import java.util.Set;

/**
 * Author:  joechang
 * Date:    4/29/15
 * Purpose: For linking, encrypting, obfuscating, etc.
 */
public class UrlUtils {

    /**
     * Possibly to be replaced later with something more complex.
     * @param data
     * @return the groupId
     */
    public static String extractGroupId(Uri data) {
        return extractString(data, Group.ID);
    }

    public static String extractEventId(Uri data) {
        return extractString(data, Event.ID);
    }

    private static String extractString(Uri data, String param) {
        Set<String> params = data.getQueryParameterNames();
        if (params.contains(param)) {
            return Uri.decode(data.getQueryParameter(param));
        }

        return null;

    }


}
