package com.joechang.loco.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.joechang.loco.R;
import com.joechang.loco.model.LocTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author:  joechang
 * Date:    2/3/15
 * Purpose: Central spot where we try to determine location.  The Google Api Client is not working, though.
 * TODO: Should probably make this an enum-based singleton.
 */
public class LocationUtils implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static volatile LocationUtils INSTANCE = null;

    private volatile Context context;
    private volatile LocationManager locationManager;

    //Variables that may change from execution to execution
    private Location lastKnownLocation = null;
    private static final String DEFAULT_PROVIDER = LocationManager.PASSIVE_PROVIDER;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    //real?
    private GoogleApiClient gac = null;

    //Prevent instantiation
    private LocationUtils() {}

    public static LocationUtils getInstance(Context c) {
        if (INSTANCE == null) {
            synchronized (LocationUtils.class) {
                try {
                    if (INSTANCE == null) {
                        INSTANCE = new LocationUtils();
                        INSTANCE.context = c;
                        INSTANCE.locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
                    }
                } catch (Exception e) {
                    //do nothing, return null;
                }
            }
        }
        return INSTANCE;
    }

    public static boolean isInitialized() {
        return INSTANCE != null &&
               INSTANCE.locationManager != null;
    }

    /**
     * The wholy grail criteria, but battery intensive!
     * @return
     */
    private static Criteria getHighAccuracyCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        criteria.setBearingRequired(true);

        //API level 9 and up
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);

        return criteria;
    }

    private static Criteria getLowBatteryCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);

        return criteria;
    }

    public String determineBestProvider(Criteria a) {
        if (getLocationManager() != null) {
            return getLocationManager().getBestProvider(a, true);
        }

        return DEFAULT_PROVIDER;
    }

    public String determineLowBatteryProvider() {
        return determineBestProvider(getLowBatteryCriteria());
    }

    public String determineHighAccuracyProvider() {
        return determineBestProvider(getHighAccuracyCriteria());
    }

    /**
     * Just give me a provider that doesn't match this one!
     * @param notThisOne
     * @return
     */
    public String determineAlternateProvider(String notThisOne) {
        List<String> ss = getLocationManager().getProviders(true);
        ss.remove(notThisOne);
        if (ss.size() == 0) {
            return notThisOne;
        }
        int rand = (int)(Math.random() * (ss.size() - 0));
        return ss.get(rand);
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public Location getLastKnownLocation() {
        if (getLocationManager() != null) {
            Location gpsCandidate = getLocationManager().getLastKnownLocation(determineBestProvider(getHighAccuracyCriteria()));
            Location netCandidate = getLocationManager().getLastKnownLocation(determineBestProvider(getLowBatteryCriteria()));
            if (isBetterLocation(gpsCandidate, netCandidate)) {
                return gpsCandidate;
            }

            return netCandidate;
        }

        return null;
    }

    /**
     * Cause us to make a request using the highaccuracyprovider.  Warning this is BLOCKING.
     * @return location, or the lastknownone.
     */
    public Location getHighAccuracyLocation() {

/* TAKES TOO LONG */
        /*
        final LocationFixFuture lff = new LocationFixFuture();
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new Runnable() {
            @Override
            public void run() {
                getLocationManager().requestSingleUpdate(
                        getHighAccuracyCriteria(),
                        lff,
                        Looper.myLooper()
                );
            }
        });

        try {
            Location ll = lff.get(105, TimeUnit.SECONDS);
            if (ll != null) {
                return ll;
            }
            es.shutdown();
        } catch (InterruptedException ie) {
            //no biggie.  Swallow.
        } catch (ExecutionException ee) {
            //shoudl log this.
        } catch (TimeoutException te) {
            //why
            int i=1;
        }
*/
        return getLastKnownLocation();
    }

    public Location getCurrentLocation() {
        Location ll = getLastKnownLocation();

        //Sometimes the location manager will not get us a real coordinate.
        if (ll == null) {
            ll = MapUtils.getDefaultLocation();
        }

        return ll;
    }


    /**
     * SHAMEFULLY PULLED FROM Android Documentation
     * Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null && location != null) {
            // A new location is always better than no location
            return true;
        } else if (location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location, currentBestLocation);

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    public static boolean isSameProvider(Location provider1, Location provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }

        if (provider2 == null) {
            return false;
        }

        return isSameProvider(provider1.getProvider(), provider2.getProvider());
    }

    public static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }

        return provider1.equals(provider2);
    }

    public Location getLastKnownLocationPlay() {
        if (gac == null) {
            setupGoogleApiClient(this.context);
        } else {
            onConnected(null);
        }

        return lastKnownLocation;
    }

    private GoogleApiClient setupGoogleApiClient(Context c) {
        if (gac == null) {
            synchronized (LocationUtils.class) {
                if (gac == null) {
                    gac = new GoogleApiClient.Builder(c)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();
                }
            }
        }

        return gac;
    }

    public static String toFriendlyString(Location ll) {
        if (ll == null) {
            return "Null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ll.getProvider().toUpperCase() + ": " + ll.getLatitude() + ", " + ll.getLongitude() + "\n")
          .append("FixTime: " + SimpleDateFormat.getDateTimeInstance().format(new Date(ll.getTime())));
        return sb.toString();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(gac);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        gac = null;
    }

    @Override
    public void onConnectionSuspended(int i) {
        gac = null;
    }

    public static double distance(Location l, Location k) {
        return LocTime.distance(l.getLatitude(), l.getLongitude(), k.getLatitude(), l.getLongitude());
    }

    public static LocTime fromLocation(Location ll) {
        return new LocTime(ll.getLatitude(), ll.getLongitude(), ll.getTime(), ll.getProvider());
    }
}
