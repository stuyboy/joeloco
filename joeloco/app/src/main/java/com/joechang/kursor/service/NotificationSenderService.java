package com.joechang.kursor.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.joechang.loco.Configuration;
import com.joechang.loco.firebase.AndroidFirebaseManager;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.Notification;
import com.joechang.loco.service.MMSSenderService;
import com.joechang.loco.utils.AddressUtils;
import com.joechang.loco.utils.PhoneUtils;

import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   10/2/15 3:33 PM
 * Purpose:   Based on the notification tables within Firebase, when a new one comes along that has not been SENT,
 * send it using the MMSSenderService!
 */
public class NotificationSenderService extends Service implements ValueEventListener {
    private Logger logger = Logger.getLogger(NotificationSenderService.class.getName());
    private final IBinder mBinder = new Binder();

    @Override
    public void onCreate() {
        AndroidFirebaseManager.init(getApplicationContext());

        //If this is not the server, do not enable.
        if (PhoneUtils.getTelephoneNumber(this).contains(Configuration.getServerPhoneNumber())) {
            //Setup a listener
            FirebaseManager.getInstance().getNotificationsFirebase().addValueEventListener(this);
            logger.info("Registering a notification sender as " + PhoneUtils.getTelephoneNumber(this));
        }

        super.onCreate();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null && dataSnapshot.exists() && dataSnapshot.hasChildren()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                Notification n = ds.getValue(Notification.class);
                if (Notification.Status.QUEUED.equals(n.getStatus())) {
                    sendNotification(n);
                }
            }
        }
    }

    private void sendNotification(Notification n) {
        MMSSenderService.sendMMS(
                getApplicationContext(),
                n.getPhoneNumber(),
                n.getMessage()
        );
        //MMSSenderService.sendSMS(n.getPhoneNumber(), n.getMessage());
        n.setStatus(Notification.Status.SENT);
        FirebaseManager.getInstance().getNotificationsFirebase().child(n.getNotificationId()).setValue(n);
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
