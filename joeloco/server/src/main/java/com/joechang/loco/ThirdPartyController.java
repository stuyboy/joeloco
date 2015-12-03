package com.joechang.loco;

import com.joechang.loco.config.Params;
import com.joechang.loco.config.Routes;
import com.joechang.loco.model.BusinessResult;
import com.joechang.loco.model.Search;
import com.joechang.loco.response.SearchResult;
import com.joechang.loco.service.*;
import com.joechang.loco.model.RestaurantResult;
import com.joechang.loco.utils.DateParsingUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.*;

/**
 * Author:    joechang
 * Created:   7/17/15 3:49 PM
 * Purpose:   Proxy API for integrating with other services, such as Yelp, etc,
 */
@RestController
public class ThirdPartyController {

    private OpenTableAPI otApi;
    private OpenTableResponseWriter otWriter;

    private YelpAPI yApi;
    private YelpResponseWriter yWriter;

    public ThirdPartyController() {
        otApi = new OpenTableAPI();
        otWriter = new OpenTableResponseWriter();

        yApi = YelpAPI.getInstance();
        yWriter = new YelpResponseWriter();
    }

    @RequestMapping(Routes.OPENTABLE_SEARCH)
    public SearchResult searchOpenTable(
        @RequestParam(value = Params.NAME, required = false) String name,
        @RequestParam(value = Params.CITY, required = false, defaultValue = "San Francisco") String city,
        @RequestParam(value = Params.WHEN, required = false, defaultValue = "tonight 7:30pm") String when,
        @RequestParam(value = Params.PARTY_SIZE, required = false, defaultValue = "2") final Integer numPeople,
        @RequestParam(value = Params.CUISINE, required = false) String cuisine,
        @RequestParam(value = Params.RESULT_SIZE, required = false, defaultValue = "5") Integer size
    ) {
        final SearchResult sr = new SearchResult();
        final Search res = new Search();
        res.setSource("Opentable");

        //Empty cases
        if (name == null && cuisine == null) {
            sr.notFound(null);
            return sr;
        }

        //Date is never passed null, we attempt match or default it.
        final Date dd = DateParsingUtils.attemptMatch(when);

        //Set the query so we can use it for messaging later.
        res.setQuery(otWriter.summarizeQuery(name, cuisine, city, dd, numPeople));

        //Quick determination if the cuisine may be a restaurant name.  So see if there's no match.  If not, assume
        //restaurant name.
        if (cuisine != null) {
            if (otApi.lookupCuisineId(cuisine) <= 0) {
                name = cuisine;
            }
        }

        //Looking for a specific restaurant, then just go for it, such as yelp redirection.
        if (name != null) {
            final String searchName = name;
            otApi.getClient().findRestaurant(searchName, city, null, new Callback<JSONObject>() {
                @Override
                public void success(JSONObject o, Response response) {
                    RestaurantResult rr = otApi.fromJSON(o);

                    if (rr == null) {
                        res.setQuery(searchName);
                        sr.setResult(res);
                    } else {
                        rr.setReserveDate(dd);
                        rr.setNumPeople(numPeople);
                        rr.setAvailableTimes(otApi.findOpenTimes(rr));
                        res.setResponse(new String[]{otWriter.toText(rr)});
                        res.setJson(otWriter.toJson(rr));
                        sr.setResult(res);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    int i = 1;
                }
            });
        } else {
            //What happens when no restaurants match? TODO
            List<RestaurantResult> rrs = otApi.searchRestaurants(dd, numPeople, cuisine, city, size);

            if (rrs.size() > 1) {
                res.setResponse(otWriter.toText(rrs));
            } else {
                res.setResponse(otWriter.toText(rrs, AbstractResultResponseWriter.Length.FULL, false));
            }
            res.setJson(otWriter.toJson(rrs));
            sr.setResult(res);
        }

        return sr;
    }

    @RequestMapping(Routes.YELP_SEARCH)
    public SearchResult searchYelp(
            @RequestParam(value = Params.WHAT, required = false)  String what,
            @RequestParam(value = Params.WHERE, required = false) String where,
            @RequestParam(value = Params.LATITUDE, required = false) Double lat,
            @RequestParam(value = Params.LONGITUDE, required = false) Double lng,
            @RequestParam(value = Params.RESULT_SIZE, required = false, defaultValue = "3") Integer size
    ) {
        SearchResult sr = new SearchResult();
        Search res = new Search();
        res.setSource("Yelp");

        yApi.setSearchLimit(size);

        if (what == null) {
            sr.notFound(what);
            return sr;
        }

        res.setQuery(yWriter.summarizeQuery(what, null, where, null, -1));

        List<BusinessResult> brs = yApi.searchBusinesses(what, where, lat, lng);

        if (brs.size() > 1) {
            res.setResponse(yWriter.toText(brs));
        } else {
            res.setResponse(yWriter.toText(brs, AbstractResultResponseWriter.Length.FULL, false));
        }
        res.setJson(yWriter.toJson(brs));

        sr.setResult(res);
        return sr;
    }
}
