package com.joechang.loco.firebase;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.joechang.loco.model.LocTime;
import com.joechang.loco.utils.RealtimeLocation;

/**
 * Author:  joechang
 * Date:    3/19/15
 * Purpose: Joeloco take on Geofire, extending it for use in the app.
 */
public class GeoFire extends com.firebase.geofire.GeoFire {

    private static double DEFAULT_SEARCH_RADIUS_KM = 200;

    public GeoFire(Firebase firebase) {
        super(firebase);
    }

    //** FIREBASE LOCATION ie GEOFIRE METHODS **//
    public void writeRealTimeLocation(String username, GeoLocation location) {
        this.setLocation(username, location);
    }

    /**
     * This method registers a callback object based on the KEY, or in our case, userId.
     *
     * @param userId
     * @param olu
     */
    public ListenerPair registerRealtimeLocationUpdate
    (final String userId, final RealtimeLocation.OnLocationUpdate olu) {
        Firebase keyFirebase = getFirebase().child(userId);
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    olu.execute(userId, null);
                } else {
                    GeoLocation location = GeoFireUtils.getLocationValue(dataSnapshot);
                    if (location != null) {
                        LocTime lt = new LocTime(location.latitude, location.longitude);
                        olu.execute(userId, lt);
                    } else {
                        String message = "GeoFire data has invalid format: " + dataSnapshot.getValue();
                        olu.cancel();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                olu.cancel();
            }
        };

        keyFirebase.addValueEventListener(vel);

        return new ListenerPair(keyFirebase, vel);
    }

    /**
     * This method registers a callback object based on DISTANCE.  It will query for all points that have
     * entered this LocTime within DEFAULT_SEARCH_RADIUS_KM.  This is ALL points.
     *
     * @param lt
     * @param olu
     */
    public GeoQuery registerRealtimeLocationUpdate(LocTime lt, final RealtimeLocation.OnLocationUpdate olu) {
        GeoLocation gl = new GeoLocation(lt.getLatitude(), lt.getLongitude());
        GeoQuery gq = this.queryAtLocation(gl, DEFAULT_SEARCH_RADIUS_KM);
        gq.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                olu.execute(key, new LocTime(location.latitude, location.longitude));
            }

            @Override
            public void onKeyExited(String key) {
                olu.remove(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                olu.execute(key, new LocTime(location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                //no-op
            }

            @Override
            public void onGeoQueryError(FirebaseError error) {

            }
        });
        return gq;
    }

    public class ListenerPair {
        private Firebase firebase;
        private ValueEventListener listener;

        public ListenerPair(Firebase firebase, ValueEventListener listener) {
            this.firebase = firebase;
            this.listener = listener;
        }

        public Firebase getFirebase() {
            return firebase;
        }

        public ValueEventListener getListener() {
            return listener;
        }
    }

}
