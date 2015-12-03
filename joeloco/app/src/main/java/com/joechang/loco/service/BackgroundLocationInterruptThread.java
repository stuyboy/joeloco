package com.joechang.loco.service;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.joechang.loco.utils.LocationUtils;
import com.joechang.loco.utils.SimpleLocationListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author:  joechang
 * Date:    3/31/15
 * Purpose: A separate light-weight thread that listens for any location changes and may
 * interrupt our primary loop in LocationPublishService
 */
public class BackgroundLocationInterruptThread extends Thread {
    private Logger log = Logger.getLogger(getClass().getSimpleName());
    private LocationPublishService lps;
    private LocationListener currentLL;
    private Handler mHandler;

    public static String ARG_PROVIDER = "ARG_PROVIDER";
    public static String ARG_INTERVAL = "ARG_INTERVAL";
    public static String ARG_DISTANCE = "ARG_DISTANCE";

    public BackgroundLocationInterruptThread(LocationPublishService lps) {
        this.lps = lps;
    }

    public void run() {
        Looper.prepare();
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                try {
                    Bundle b = msg.getData();
                    String provider = b.getString(ARG_PROVIDER);
                    Long interval = b.getLong(ARG_INTERVAL);
                    Float distance = b.getFloat(ARG_DISTANCE);
                    resetListener(provider, interval, distance);
                    return true;
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Cannot start BackgroundLocationInterruptThread", e);
                }
                return false;
            }
        });
        Looper.loop();
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void exit() {
        removeActiveListener();
        Looper.myLooper().quitSafely();
    }

    private LocationManager getLocationManager() {
        return LocationUtils.getInstance(this.lps).getLocationManager();
    }

    public LocationListener getActiveListener() {
        return this.currentLL;
    }

    public void removeActiveListener() {
        if (currentLL != null) {
            getLocationManager().removeUpdates(currentLL);
        }
        this.currentLL = null;
    }

    //Removes the current listener, creates a new one with the appropriate specs, and relaunches
    //the looping mechanism.
    public void resetListener(String provider, long interval, float distance) {
        log.fine("Swapping out background listener for " + provider + " and " + (interval/1000) + "s.");
        removeActiveListener();

        currentLL = new LocationListener(provider, interval, distance);
        getLocationManager().requestLocationUpdates(
                currentLL.getProvider(),
                currentLL.getSampleInterval(),
                currentLL.getMinimumDistance(),
                currentLL,
                Looper.myLooper());
    }

    class LocationListener extends SimpleLocationListener {
        public LocationListener(String gpsProvider, long sampleInterval, float distance) {
            super(gpsProvider, sampleInterval, distance);
        }

        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);

            LocationPublishService l = BackgroundLocationInterruptThread.this.lps;

            if (l.resetLastLocation(location)) {
                log.fine("Listener noted location change, interrupting sleep.");
                l.setLastInterruptedMillis(System.currentTimeMillis());
                l.publishLocation(l.mUserId, location);
                l.primaryLoopThread.interrupt();
            }

            l.launchListeners();
        }
    };

}
