package com.joechang.loco.firebase;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.joechang.loco.utils.NetworkErrorUtils;

/**
 * Author:  joechang
 * Date:    5/12/15
 * Purpose:
 */
public class AndroidFirebaseManager extends FirebaseManager {

    private static final String CONNECTION_URL = ".info/connected";
    private static Firebase connectedRef;

    public static synchronized void init(Context c) {
        if (!initialized) {
            Firebase.setAndroidContext(c);
        }

        if (NetworkErrorUtils.detectNetwork(c)) {
            setupConnectedHandler(c);
        }

        init();
    }

    protected static void setupConnectedHandler(final Context c) {
        connectedRef = new Firebase(FIREBASE_URL + CONNECTION_URL);
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.getValue(Boolean.class)) {
                    if (c instanceof Activity) {
                        //NetworkErrorUtils.handleNetworkError((Activity) c);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
