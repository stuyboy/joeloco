package com.joechang.loco.utils;

import com.joechang.loco.model.LocTime;

/**
 * Author:  joechang
 * Date:    12/18/14
 * Purpose:
 */
public class RealtimeLocation {

    public static interface OnLocationUpdate {
        //When this key/username updates location, what to do?
        public void execute(String key, LocTime location);

        //When the query doesn't work.
        public void cancel();

        //When the key/username ventures outside or no longer tracked.
        public void remove(String key);
    }

}
