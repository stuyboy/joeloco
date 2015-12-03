package com.joechang.loco.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.listener.DeferredResultValueListener;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.response.EventResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by joechang on 5/13/15.
 * Background Service keeping track of relevant events.  If triggered, we issue a StatusResponse that turns on the
 * high accuracy GPS tracking.
 * <p/>
 * Based on the caching mechanism, the userEventCache always populates asynchrnously, meaning the SECOND call results
 * in data.  TODO!!!!
 */
@Service
public class EventMonitoringService {
    //Map userIDs to the events that are retrieved.  These are refreshed as necessary.
    LoadingCache<String, Collection<Event>> userEventsCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build(new UserEventLoader());

    private FirebaseManager fbManager;

    @Autowired
    public void setFirebaseManager(FirebaseManager fm) {
        this.fbManager = fm;
    }

    public EventResult.Set getEventsForUserId(String userId) throws ExecutionException {
        EventResult.Set er = new EventResult().set();
        er.setResult(userEventsCache.get(userId));
        return er;
    }

    public EventResult getEvent(String id) {
        final EventResult er = new EventResult();
        fbManager.getEventFirebase(id).addListenerForSingleValueEvent(
                DeferredResultValueListener.instance(er, id)
        );
        return er;
    }

    public Collection<Event> getActiveEvents(String userId, Date time) {
        Collection<Event> ret = new HashSet<Event>();
        try {
            Collection<Event> userEvents = userEventsCache.get(userId);
            for (Event e : userEvents) {
                if (e.isValidNow(time)) {
                    ret.add(e);
                }
            }
        } catch (ExecutionException ee) {
            //woah
        }
        return ret;
    }

    class UserEventLoader extends CacheLoader<String, Collection<Event>> {
        @Override
        public Collection<Event> load(final String key) throws Exception {
            //Show existing if exists, but schedule for refresh.
            Collection<Event> temp = userEventsCache.getIfPresent(key);
            final CountDownLatch cdl = new CountDownLatch(1);

            final EventResult.Set def = new EventResult().set();
            def.setResultHandler(new DeferredResult.DeferredResultHandler() {
                @Override
                public void handleResult(Object result) {
                    if (result instanceof Collection) {
                        EventMonitoringService.this.userEventsCache.put(key, (Collection<Event>) result);
                    }
                    cdl.countDown();
                }
            });

            getEventsForUser(key, def);

            if (temp == null) {
                //Not healthy, but if empty, we must lock to populate, and try again.
                cdl.await(5, TimeUnit.SECONDS);
                temp = userEventsCache.getIfPresent(key);
            }

            return temp;
        }

        private void getEventsForUser(String userId, final EventResult.Set df) {
            fbManager.findUserEvents(userId, new PostQueryAction<Collection<Event>>() {
                @Override
                public void doAction(Collection<Event> p) {
                    if (p == null) {
                        df.empty();
                    } else {
                        df.setResult(p);
                    }
                }

                @Override
                public void onError(Collection<Event> p) {

                }
            });
        }
    }
}
