package com.joechang.loco.utils;

import android.content.Context;
import android.content.SharedPreferences;

import android.telephony.TelephonyManager;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;
import com.joechang.loco.R;
import com.joechang.loco.model.User;

/*
 * Stores data that needs to persist using Android SharedPreferences
 */

public class UserInfoStore {
    private SharedPreferences mPrefs;
    private Context mContext;

    public static String PRIVATE_STORAGE_NAME = "userinfostorer";
    public String ID_TOKEN_KEY = "idTokenKey";
    public String GITKIT_USER_KEY = "gitkitUserKey";

    public String ACC_TOKEN = "accessToken";
    public String HELPFUL = "helpfulThing";
    public String dialogShown = "dialogShown";

    //Quick settings for configuring what to say in an event send, and how long, in minutes.
    public static final String QUICKSEND_MESSAGE = "quickSendMeSSage";
    public static final String QUICKSEND_DURATION = "quickSendDuraTION";
    public static final String CHAT_ENABLED = "chatEnABLED";
    public final static Integer DEFAULT_DURATION = 30;

    public UserInfoStore(Context ctx) {
        mContext = ctx;
        mPrefs = ctx.getSharedPreferences(PRIVATE_STORAGE_NAME, Context.MODE_PRIVATE);
    }

    public static UserInfoStore getInstance(Context ctx) {
        return new UserInfoStore(ctx);
    }

    public void saveToken(String AccToken, String helpful) {
        mPrefs.edit().putString(ACC_TOKEN, AccToken)
                .putString(HELPFUL, helpful).apply();
    }

    //wasShown() and saveDialog() are used to display the splash dialog once and only once
    public boolean wasShown() {
        boolean wasShown = mPrefs.getBoolean(dialogShown, false);
        if (wasShown) {
            return  true;
        } else {
            return false;
        }
    }

    public void saveDialog() {
        mPrefs.edit().putBoolean(dialogShown, true).apply();
    }

    public String getHelpful() {
        return mPrefs.getString(HELPFUL, null);
    }

    public String getTokenId() {
        return getSavedIdToken().getTokenString();
    }

    public boolean isChatEnabled() {
        return mPrefs.getBoolean(CHAT_ENABLED, false);
    }

    public String getQuickSendMessage() {
        return mPrefs.getString(QUICKSEND_MESSAGE, mContext.getString(R.string.text_quickSendMessage));
    }

    public int getQuickSendDuration() {
        return mPrefs.getInt(QUICKSEND_DURATION, DEFAULT_DURATION);
    }

    public void saveQuickSendSettings(String message, int duration, boolean chatEnabled) {
        mPrefs.edit()
                .putString(QUICKSEND_MESSAGE, message)
                .putInt(QUICKSEND_DURATION, duration)
                .putBoolean(CHAT_ENABLED, chatEnabled)
                .apply();
    }

    public void saveIdTokenAndGitkitUser(IdToken idToken, GitkitUser user) {
        mPrefs.edit()
                .putString(ID_TOKEN_KEY, idToken.getTokenString())
                .putString(GITKIT_USER_KEY, user.toString())
                .apply();
    }

    private IdToken getSavedIdToken() {
        String tokenString = mPrefs.getString(ID_TOKEN_KEY, null);
        if (tokenString != null) {
            IdToken idToken = IdToken.parse(tokenString);
            if (idToken != null && !idToken.isExpired()) {
                return idToken;
            }
        }
        return null;
    }

    public GitkitUser getSavedGitkitUser() {
        String userString = mPrefs.getString(GITKIT_USER_KEY, null);
        if (userString != null) {
            return GitkitUser.fromJsonString(userString);
        }
        return null;
    }

    public String getUserId() {
        return this.getSavedGitkitUser().getLocalId();
    }

    public String getName() {
        return this.getSavedGitkitUser().getDisplayName();
    }

    public String getUsername() {
        return this.getSavedGitkitUser().getEmail();
    }


    public User getUser() {
        GitkitUser user = getSavedGitkitUser();

        User newUser = new User();
        newUser.setUserId(user.getLocalId());
        newUser.setUsername(user.getEmail());
        newUser.setFullname(user.getDisplayName());
        newUser.setPhotoUrl(user.getPhotoUrl());
        newUser.setPhoneNumber(PhoneUtils.getTelephoneNumber(mContext));

        return newUser;
    }

    public void refreshUser(User u) {
        u.setPhoneNumber(PhoneUtils.getTelephoneNumber(mContext));
    }

    public void clearLoggedInUser() {
        mPrefs.edit()
                .remove(ID_TOKEN_KEY)
                .remove(GITKIT_USER_KEY)
                .remove(ACC_TOKEN)
                .apply();
    }

    public boolean isUserLoggedIn() {
        return getSavedIdToken() != null && getSavedGitkitUser() != null;
    }
}
