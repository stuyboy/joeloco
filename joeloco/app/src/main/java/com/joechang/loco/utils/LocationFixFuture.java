package com.joechang.loco.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author:  joechang
 * Date:    4/1/15
 * Purpose: A subclass of future that can help block threads when we're asking for a location fix.
 */
public class LocationFixFuture extends SimpleLocationListener implements Future<Location> {

    private volatile Location result = null;
    private volatile boolean canceled = false;
    private final CountDownLatch countDownLatch;

    public LocationFixFuture() {
        countDownLatch = new CountDownLatch(1);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        } else {
            countDownLatch.countDown();
            canceled = true;
            return !isDone();
        }
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public boolean isDone() {
        return countDownLatch.getCount() == 0;
    }

    @Override
    public Location get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return result;
    }

    @Override
    public Location get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout, unit);
        return result;
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        this.result = location;
        countDownLatch.countDown();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        super.onProviderDisabled(provider);
    }
}
