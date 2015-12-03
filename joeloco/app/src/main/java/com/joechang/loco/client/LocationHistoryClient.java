package com.joechang.loco.client;

import com.joechang.loco.Configuration;
import com.joechang.loco.logging.LogLocationEntry;
import com.loopj.android.http.*;

import java.util.logging.Logger;

/**
 * Author:  joechang
 * Date:    1/21/15
 * Purpose: A small class that uses asynchttprequests to log the user's locaiton.
 */
public class LocationHistoryClient  {
    private static final Logger LOGGER = Logger.getLogger(LocationHistoryClient.class.getSimpleName());

    private static final String BASE_URL = Configuration.getProdServerAddress();

    private static final String API_LOGLOCATION = "loglocation";

    private static AsyncHttpClient mAhc = new AsyncHttpClient();

    protected static void get(String url, RequestParams params, AsyncHttpResponseHandler handler) {
        mAhc.get(getUrl(url), params, handler);
    }

    protected static void post(String url, RequestParams params, AsyncHttpResponseHandler handler) {
        mAhc.post(getUrl(url), params, handler);
    }

    private static String getUrl(String url) {
        return BASE_URL + "/" + url;
    }

    public static void postLocation(final String userId, final Double latitude, final Double longitude, UserClient.StatusResponseCallback cb) {
        LogLocationEntry lle = new LogLocationEntry(userId, latitude, longitude);
        UserClient llc = RestClientFactory.getInstance().getUserClient();
        llc.postLocation(userId, lle, cb);
    }
}
