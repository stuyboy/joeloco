package com.joechang.loco.user;

import com.firebase.client.Firebase;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.PostWriteAction;
import com.joechang.loco.model.User;
import com.joechang.loco.utils.AddressUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Author:    joechang
 * Created:   10/2/15 12:06 PM
 * Purpose:   Small class to be used to identify new users, allow saving things to specific user profiles, etc.
 */
public class UserManager {
    private Cache<String, Long> foundNumbers = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();

    public UserManager() {
        //no-op
    }

    /**
     * Given a list of phone numbers, make sure that we record new ones in the DB.
     * @param phoneNumbers
     */
    public void addNewUsers(String[] phoneNumbers) {
        for (String pn : phoneNumbers) {
            final String clean = AddressUtils.cleanPhoneNumber(pn);
            if (foundNumbers.getIfPresent(clean) != null) {
                continue;
            }
            FirebaseManager.getInstance().findUserByPhoneNumber(pn, new PostQueryAction<User>() {
                @Override
                public void doAction(User p) {
                    if (p == null) {
                        addNewUserViaPhoneNumber(clean);
                    } else {
                        foundNumbers.put(p.getPhoneNumber(), System.currentTimeMillis());
                    }
                }

                @Override
                public void onError(User p) {

                }
            });
        }
    }

    protected void addNewUserViaPhoneNumber(final String phoneNumber) {
        User user = new User();
        user.setUserId(phoneNumber);
        user.setPhoneNumber(phoneNumber);
        user.setCreatedTime(System.currentTimeMillis());

        FirebaseManager.getInstance().addUser(user, new PostWriteAction() {
            @Override
            public void doAction(Object objectWritten) {
                foundNumbers.put(phoneNumber, System.currentTimeMillis());
            }

            @Override
            public void onError(Object objectNotWritten) {

            }
        });
    }
}
