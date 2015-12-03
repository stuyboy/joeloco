package com.joechang.loco.client;

import com.squareup.okhttp.Credentials;
import org.apache.commons.codec.binary.Base64;
import retrofit.RequestInterceptor;

/**
 * Author:    joechang
 * Created:   7/8/15 2:13 PM
 * Purpose:   Interceptor for use with restclients to provide authorization
 */
public class AuthInterceptor implements RequestInterceptor {

    public static final String AUTH_HEADER_KEY = "Authorization";

    @Override
    public void intercept(RequestFacade request) {
        String tec = getCredentials();
        request.addHeader(AUTH_HEADER_KEY, tec);
    }

    public static String getCredentials() {
        return Credentials.basic("user", "password");
    }
}
