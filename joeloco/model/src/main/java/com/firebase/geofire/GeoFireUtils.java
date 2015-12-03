package com.firebase.geofire;

import com.firebase.client.DataSnapshot;

/**
 * Author:  joechang
 * Date:    3/19/15
 * Purpose: This is a very hacky little class that lives in the local source but in the geofire
 * package.  So that we can reach the methods, but also make them available publicly to outside
 * classes.  So sort of a wrapper.
 */
public class GeoFireUtils {

    public static GeoLocation getLocationValue(DataSnapshot dataSnapshot) {
        return GeoFire.getLocationValue(dataSnapshot);
    }

}
