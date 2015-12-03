package com.joechang.loco.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Quick cleanup class so we don't have to see all the methods **/
public class SimpleLocationListener implements LocationListener {
    private String provider;
    private long sampleInterval;
    private float minimumDistance;
    private Date lastLocationChangeDate;

    public SimpleLocationListener(String provider, long sampleInterval, float distance) {
        this.provider = provider;
        this.sampleInterval = sampleInterval;
        this.minimumDistance = distance;
    }

    public SimpleLocationListener() {
        //when not used as a bag to hold fields.
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocationChangeDate = new Date();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public String getProvider() {
        return provider;
    }

    public long getSampleInterval() {
        return sampleInterval;
    }

    public float getMinimumDistance() {
        return minimumDistance;
    }

    @Override
    public String toString() {
        return provider.toUpperCase() + ": " + getSampleInterval() + "ms : " + getMinimumDistance() + "m : " +
               ((this.lastLocationChangeDate == null) ? "" : SimpleDateFormat.getTimeInstance().format(this.lastLocationChangeDate));
    }
}
