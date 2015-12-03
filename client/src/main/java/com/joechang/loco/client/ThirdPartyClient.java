package com.joechang.loco.client;

import com.joechang.loco.config.Params;
import com.joechang.loco.config.Routes;
import com.joechang.loco.logging.LogLocationEntry;
import com.joechang.loco.model.Search;
import com.joechang.loco.model.User;
import retrofit.http.*;

/**
 * Author:    joechang
 * Created:   7/17/15 4:08 PM
 * Purpose:
 */
public interface ThirdPartyClient {

    @GET(Routes.YELP_SEARCH)
    public void searchYelp(
            @Query(Params.WHAT) String what,
            @Query(Params.WHERE) String where,
            @Query(Params.LATITUDE) Double lat,
            @Query(Params.LONGITUDE) Double lng,
            @Query(Params.RESULT_SIZE) Integer size,
            Callback resp);

    @GET(Routes.OPENTABLE_SEARCH)
    public void searchOpentable(
            @Query(Params.NAME) String name,
            @Query(Params.CITY) String city,
            @Query(Params.WHEN) String when,
            @Query(Params.PARTY_SIZE) Integer numberPeople,
            @Query(Params.CUISINE) String cuisine,
            @Query(Params.RESULT_SIZE) Integer size,
            Callback resp);

    @GET(Routes.OPENTABLE_SEARCH)
    public void searchOpentable(
            @Query(Params.NAME) String name,
            @Query(Params.CITY) String city,
            @Query(Params.RESULT_SIZE) Integer size,
            Callback resp);


    public interface Callback extends retrofit.Callback<Search> {}
}
