package com.joechang.loco.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.firebase.geofire.GeoLocation;
import com.joechang.loco.client.LocationHistoryClient;
import com.joechang.loco.client.UserClient;
import com.joechang.loco.firebase.AndroidFirebaseManager;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.logging.StatusResponse;
import com.joechang.loco.utils.LocationUtils;
import com.joechang.loco.utils.SimpleLocationListener;
import com.joechang.loco.utils.UserInfoStore;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that takes care of the background publishing to cloud about phone's location.
 * Also does a separate request to joelo.co servers to note historical locations in files.
 */

public class LocationPublishService extends Service implements UserClient.StatusResponseCallback {

    private static Logger log = Logger.getLogger(LocationPublishService.class.getSimpleName());
    private static String DEFAULT_USERNAME = "UNKNOWN";

    private ExecutorService es = Executors.newCachedThreadPool();

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new Binder();

    // Stats variables that we don't care about missing an update.  So use volatile.
    private volatile Location lastLocation;
    private volatile Stagnancy currentStagnancy;
    private volatile boolean stagnancyOverride = false;

    //Last time we sent location to server
    private volatile long lastPublish;

    //Last time that location was polled via GPS, WiFi, etc.
    private volatile long lastEvaluatedLocation;

    //Last time we changed the current location.
    private volatile long lastLocationChangeMillis = System.currentTimeMillis();

    //Last time that the listener chimed in with a location change.
    private volatile long lastInterruptedMillis;

    //How long the poll interval is.
    private volatile int currentSleepTime;

    //When is the next time we're polling.
    private volatile long nextLoopTime;

    //The sleeping looper thread
    protected Thread primaryLoopThread;

    //The background interrupting thread
    protected BackgroundLocationInterruptThread backgroundInterruptThread;

    protected String mUserId = null;
    private volatile boolean running = false;
    private boolean loop = true;

    private Set<Listener> listeners = new HashSet<Listener>();

    public final static float MINIMUM_DISTANCE = 3f;    //3M distanceTo change for publish.

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidFirebaseManager.init(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!this.running) {
            //Initialize
            LocationUtils.getInstance(this);

            // Assing a userID
            this.mUserId = safeFindUserId();

            this.backgroundInterruptThread = new BackgroundLocationInterruptThread(this);
            this.backgroundInterruptThread.start();
            log.info("Started BackgroundLocationInterruptThread");

            // We want this service to continue running until it is explicitly
            // stopped, so return sticky.
            this.primaryLoopThread = new Thread(getRunnable(intent));
            this.primaryLoopThread.start();
            log.info("Started LocationPublishService Thread");

            this.running = true;
        }

        return START_STICKY;
    }

    /**
     * Return null should no user id be found.  If for some reason we are not logged in, and this service runs.
     * @return
     */
    private String safeFindUserId() {
        UserInfoStore uis = UserInfoStore.getInstance(getApplicationContext());
        if (uis != null && uis.getSavedGitkitUser() != null) {
            return uis.getUserId();
        }
        return null;
    }

    /**
     * Idea here is that depending on our current Stagnancy, we want different strategies for the
     * interrupt driven location changed listener.  In consideration of battery, etc.  Very much the
     * opposite logic of publishLocation provider.  Call only when the stagnancy changes.
     */
    public synchronized void resetBackgroundListener() {
        //Defaults are for low stagnancy case.
        String gpsProvider = LocationUtils.getInstance(this).determineHighAccuracyProvider();
        long sampleInterval = Stagnancy.SETTLING.getMaxWaitInMillis();
        float sampleDistance = MINIMUM_DISTANCE;

        if (Stagnancy.OVERRIDES.contains(getCurrentStagnancy())) {
            //If we're SHUTDOWN or REALTIME, set polling time accordingly.
            sampleInterval = getCurrentStagnancy().getMax() * 1000;
            sampleDistance = 0;
        } else if (Stagnancy.HIGH.contains(getCurrentStagnancy())) {
            //If we haven't moved in a while, use the lower power provider, but sample FREQUENTLY.
            gpsProvider = LocationUtils.getInstance(this).determineLowBatteryProvider();
            sampleInterval = Stagnancy.WALKING.getMaxWaitInMillis();
        } else if (lastLocation != null) {
            //Otherwise, with low stagnancy, sample less, but get a second opinion as well.
            gpsProvider = LocationUtils.getInstance(this).determineAlternateProvider(lastLocation.getProvider());
        }

        Message mm = Message.obtain();
        Bundle bb = new Bundle();
        bb.putString(BackgroundLocationInterruptThread.ARG_PROVIDER, gpsProvider);
        bb.putLong(BackgroundLocationInterruptThread.ARG_INTERVAL, sampleInterval);
        bb.putFloat(BackgroundLocationInterruptThread.ARG_DISTANCE, sampleDistance);
        mm.setData(bb);
        this.backgroundInterruptThread.getHandler().sendMessage(mm);
    }

    /**
     * Confusing method because it resets the location and millis within this method, and returns
     * boolean of whether it was done or not.
     *
     * @param loc
     * @return Did we actually resetTheLastLocation based on qualifications?
     */
    protected synchronized boolean resetLastLocation(Location loc) {
        this.lastEvaluatedLocation = System.currentTimeMillis();

        if (!LocationUtils.isBetterLocation(loc, lastLocation)) {
            return false;
        }

        if (!isMovedEnough(loc, lastLocation)) {
            return false;
        }

        //If no current location, then last chg is last fix.
        if (lastLocation == null) {
            this.lastLocationChangeMillis = loc.getTime();
        } else {
            this.lastLocationChangeMillis = System.currentTimeMillis();
        }

        this.lastLocation = loc;

        return true;
    }

    private boolean isMovedEnough(Location nowLocation, Location lastLocation) {
        if (lastLocation != null && nowLocation != null) {
            float locDelta = Math.abs(lastLocation.distanceTo(nowLocation));
            if (locDelta <= MINIMUM_DISTANCE) {
                return false;
            }
        }

        return true;
    }

    private int secondsSinceLastLocationChange() {
        return Math.round((System.currentTimeMillis() - lastLocationChangeMillis) / 1000);
    }

    protected int publishLocation(final String userId) {
        return publishLocation(userId, LocationUtils.getInstance(this).getHighAccuracyLocation());
    }

    /**
     * Publish the location if our user has moved beyond the MINIMUM_DISTANCE.  Return the amount
     * of time that has passed since user has been stagnant.  If we're on the move constantly, then
     * return 0!
     * <p/>
     * This is called by the loop.  So as the loop slows down over time, we tend to use less power.
     * We count on our interrupt thread to alert us to any movement, resetting this loop.
     *
     * @param userId
     * @return timeStagnant
     */
    protected int publishLocation(final String userId, Location location) {
        if (location != null && userId != null) {
            //If this is determined to be a better location... SET IT.
            if (resetLastLocation(location) || Stagnancy.REALTIME.equals(getCurrentStagnancy())) {
                //No worries, this is async!
                FirebaseManager.getInstance().
                        getGeoFire().
                        writeRealTimeLocation(
                                userId,
                                new GeoLocation(
                                        location.getLatitude(),
                                        location.getLongitude()
                                )
                        );

                //This is async too..
                //Don't publish location history for now..
                LocationHistoryClient.postLocation(userId, location.getLatitude(), location.getLongitude(), this);

                //Log when we last posted location to server
                this.lastPublish = System.currentTimeMillis();
            }

            //This would have changed as resetLastLocation would have done work.
            return secondsSinceLastLocationChange();
        }

        return Stagnancy.WALKING.getMaxWait();
    }

    /**
     * As part of the UserClient.statusResponseCallback, when we postLocation via the LocationHistoryClient,
     * we previously then also got the get Stagnancy, that would turn on location tracking to REALTIME.
     * @param statusResponse
     * @param response
     */
    @Override
    public void success(StatusResponse statusResponse, Response response) {
        if (Stagnancy.REALTIME.equals(statusResponse.getRequestedStagnancy())) {
            overrideRealtimeMode();
            return;
        }

        //For this implementation, shutdown when we're not called upon.
        //removeStagnancyOverride();
        overrideShutdown();
    }

    @Override
    public void failure(RetrofitError error) {
        log.info(error.toString());
    }

    public Stagnancy getCurrentStagnancy() {
        return currentStagnancy;
    }

    public void setCurrentStagnancy(Stagnancy currentStagnancy) {
        this.currentStagnancy = currentStagnancy;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public boolean isRunning() {
        return running;
    }

    public int getCurrentSleepTime() {
        return currentSleepTime;
    }

    public void setCurrentSleepTime(int currentSleepTime) {
        this.currentSleepTime = currentSleepTime;
        this.nextLoopTime = System.currentTimeMillis() + (currentSleepTime * 1000);
    }

    public long getLastInterruptedMillis() {
        return lastInterruptedMillis;
    }

    public void setLastInterruptedMillis(long lastInterruptedMillis) {
        this.lastInterruptedMillis = lastInterruptedMillis;
    }

    public long getLastLocationChangeMillis() {
        return lastLocationChangeMillis;
    }

    public long getLastEvaluatedLocation() {
        return lastEvaluatedLocation;
    }

    public long getNextLoopTime() {
        return nextLoopTime;
    }

    public long getLastPublish() {
        return lastPublish;
    }

    public void setStagnancyOverride(Stagnancy newStagnancy) {
        stagnancyOverride = true;
        setCurrentStagnancy(newStagnancy);
    }

    public void removeStagnancyOverride() {
        stagnancyOverride = false;
        setCurrentStagnancy(Stagnancy.fromSeconds((int) (System.currentTimeMillis() - lastLocationChangeMillis) / 1000));
    }

    public void overrideRealtimeMode() {
        setStagnancyOverride(Stagnancy.REALTIME);
    }

    public void overrideShutdown() {
        setStagnancyOverride(Stagnancy.SHUTDOWN);
    }

    public SimpleLocationListener getActiveLocationListener() {
        return backgroundInterruptThread.getActiveListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.loop = false;
        backgroundInterruptThread.exit();
    }

    /**
     * We use a separate thread here that samples the lastLocation periodically to get a fix on the user
     * This prevents over using battery, and we don't see the GPS icon in the notification area.
     *
     * @param intent
     * @return
     */
    protected Runnable getRunnable(final Intent intent) {
        return new Runnable() {
            @Override
            public void run() {
                log.log(Level.INFO, "Starting joelo.co location services");

                //Tricky here, but just need a looper in this thread for asyncresponsehandler in AsyncHttpClient
                Looper.prepare();

                int sleep = 10;
                final LocationPublishService lps = LocationPublishService.this;
                Stagnancy lastLoopStagnancy = getCurrentStagnancy();

                while (loop) {
                    try {
                        if (mUserId == null) {
                            throw new IllegalArgumentException("UserId cannot be null.  Restart once logged in.");
                        }

                        if (LocationUtils.isInitialized()) {
                            //Periodically publish lastKnownLocation, if it has changed.
                            int stagnantTime = publishLocation(mUserId);

                            //If we currently have an override in place, then do not reset.  See #setStagnancyOverride
                            Stagnancy nStag = stagnancyOverride ? getCurrentStagnancy() : Stagnancy.fromSeconds(stagnantTime);

                            if (lastLoopStagnancy != nStag || getCurrentStagnancy() == null) {
                                setCurrentStagnancy(nStag);
                                lastLoopStagnancy = nStag;
                                resetBackgroundListener();
                            }

                            sleep = nStag.waitInterval(stagnantTime);
                            setCurrentSleepTime(sleep);

                            log.fine(Thread.currentThread().getName() + ": " +
                                    "No location chg in " + stagnantTime + " seconds. " +
                                    "Assume " + getCurrentStagnancy() + ".  " +
                                    "Sleeping " + sleep + " seconds.");
                        }

                        launchListeners();
                        TimeUnit.SECONDS.sleep(sleep);

                    } catch (NullPointerException npe) {
                        //Something really wrong here, let's not continue
                        log.log(Level.SEVERE, "Unable to create LocationPublishService.", npe);
                        loop = false;
                    } catch (InterruptedException intE) {
                        log.fine("Noted an interrupt.  Moving on.");
                    } catch (Exception ie) {
                        //what to do here.
                        log.log(Level.SEVERE, "Service stopped.", ie);
                        loop = false;
                    }
                }

                LocationPublishService.this.running = false;
                Looper.myLooper().quitSafely();
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class Binder extends android.os.Binder {
        public LocationPublishService getService() {
            return LocationPublishService.this;
        }

        public boolean isRunning() {
            return LocationPublishService.this.running;
        }
    }

    public interface Listener {
        public void doOnSample(LocationPublishService lps);
    }

    public void addListener(Listener l) {
        this.listeners.add(l);
    }

    public void removeListener(Listener l) {
        this.listeners.remove(l);
    }

    protected boolean launchListeners() {
        for (Listener l : LocationPublishService.this.listeners) {
            l.doOnSample(LocationPublishService.this);
        }
        return true;
    }

}
