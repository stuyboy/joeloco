package com.joechang.loco.service;

import org.json.simple.JSONObject;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Author:    joechang
 * Created:   8/7/15 12:39 PM
 * Purpose:   Retrofit interface for yelp
 */
public interface OpenTableClient {

    @GET("/api/restaurants")
    public void findRestaurant(
            @Query("name") String name,
            @Query("city") String city,
            @Query("zip") String zip,
            Callback<JSONObject> callback
    );

    @GET("/api/restaurants/{rid}")
    public void getRestaurant(
            @Path("rid") String restaurantId,
            Callback<JSONObject> callback
    );

}
