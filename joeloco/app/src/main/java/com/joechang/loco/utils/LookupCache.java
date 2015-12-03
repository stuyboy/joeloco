package com.joechang.loco.utils;

import android.util.LruCache;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.User;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Author:  joechang
 * Date:    12/29/14
 * Purpose: A quick cache to hold lookup items.  How it stays in memory, not sure.  Problem in here as the first
 * hit will always be null.
 */
public enum LookupCache {
    INSTANCE;

    private Logger logger = Logger.getLogger(LookupCache.class.getName());

    private LruCache<String, String> usernameCache = new LruCache<String, String>(100);

    public static LookupCache getInstance() {
        return INSTANCE;
    }

    public String getUsernameById(String id) {
        if (usernameCache.get(id) == null || usernameCache.get(id).equals(id)) {
            populateCache(id);
        }
        return usernameCache.get(id);
    }

    public void setUsernameById(String id, String username) {
        synchronized (usernameCache) {
            usernameCache.put(id, username);
        }
    }

    public void populateCache(final String id) {
        FirebaseManager.getInstance().findUserById(id, new PostQueryAction<User>() {
            @Override
            public void doAction(User p) {
                if (p != null) {
                    setUsernameById(id, p.getFullname());
                } else {
                    setUsernameById(id, id);
                }
            }

            @Override
            public void onError(User p) {

            }
        });
    }
}
