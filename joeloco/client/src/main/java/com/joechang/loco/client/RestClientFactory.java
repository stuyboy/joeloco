package com.joechang.loco.client;

import com.google.gson.*;
import com.joechang.loco.Configuration;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by joechang on 5/14/15.
 * Factory for creating rest clients, based on Retrofit.
 */
public class RestClientFactory {

    private RestAdapter restAdapter;
    private static RestAdapter.LogLevel mlogLevel = RestAdapter.LogLevel.BASIC;
    private static boolean isDev = false;

    //Clients
    private UserClient logLocationClient;
    private GroupClient groupClient;
    private EventClient eventClient;
    private ThirdPartyClient tpcClient;

    private static class SingletonHelper {
        private static final RestClientFactory INSTANCE = new RestClientFactory();
    }

    private RestClientFactory() {
        String serverAddress = isDev ? Configuration.getDevServerAddress() : Configuration.getProdServerAddress();
        restAdapter = createRestAdapter(serverAddress);

        logLocationClient = restAdapter.create(UserClient.class);
        groupClient = restAdapter.create(GroupClient.class);
        eventClient = restAdapter.create(EventClient.class);
        tpcClient = restAdapter.create(ThirdPartyClient.class);
    }

    public static RestAdapter createRestAdapter(String endpoint) {
        //Use OKHttpClient
        OkHttpClient okHttpClient = createOkHttpClient();
        Executor apiExecutors = Executors.newCachedThreadPool();

        //Clean this up.  Need to extract dates via milliseconds
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = builder.create();

        RestAdapter a = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(new GsonConverter(gson))
                .setLogLevel(mlogLevel)
                .setClient(new OkClient(okHttpClient))
                .setRequestInterceptor(new AuthInterceptor())
                .setExecutors(apiExecutors, apiExecutors)
                .build();

        return a;
    }

    public static RestClientFactory getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public static void useDebugServer(boolean t) {
        isDev = t;
    }

    public static void noLogging() {
        mlogLevel = RestAdapter.LogLevel.NONE;
    }

    public UserClient getUserClient() {
        return logLocationClient;
    }

    public GroupClient getGroupClient() {
        return groupClient;
    }

    public EventClient getEventClient() {
        return eventClient;
    }

    public ThirdPartyClient getThirdPartyClient() {
        return tpcClient;
    }

    protected static OkHttpClient createOkHttpClient() {
        Long timeout = Long.valueOf(Configuration.REST_TIMEOUT.get());

        OkHttpClient c = new OkHttpClient();
        c.setConnectTimeout(timeout, TimeUnit.MILLISECONDS);
        c.setWriteTimeout(timeout, TimeUnit.MILLISECONDS);
        c.setReadTimeout(timeout, TimeUnit.MILLISECONDS);

        File cacheDir = new File(System.getProperty("java.io.tmpdir"), "okhttp-cache");
        c.setCache(new Cache(cacheDir, 5 * 1024 * 1024)); //5MB Cache
        return c;
    }
}
