package com.joechang.loco.client;

import org.json.simple.JSONObject;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Author:    joechang
 * Created:   10/5/15 4:52 PM
 * Purpose:   Quick little class to encapsulate the giphy api
 */
public interface GiphyClient {
    public enum Rating {
        Y("y"),
        G("g"),
        PG("pg"),
        PG13("pg-13"),
        R("r");

        private String val;
        Rating(String value) {
            val = value;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public static final String API_KEY = "dc6zaTOxFJmzC";

    @GET("/v1/gifs/search")
    public void searchImages(
            @Query("q") String what,
            @Query("limit") int size,
            @Query("rating") Rating rating,
            @Query("api_key" ) String apiKey,
            Callback<JSONObject> callback
    );

    @GET("/v1/stickers/search")
    public void searchStickers(
            @Query("q") String what,
            @Query("limit") int size,
            @Query("rating") Rating rating,
            @Query("api_key" ) String apiKey,
            Callback<JSONObject> callback
    );

}
