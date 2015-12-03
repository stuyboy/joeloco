package com.joechang.loco.service;

import com.joechang.loco.model.BusinessResult;
import com.joechang.loco.security.TwoStepOAuth;
import com.joechang.loco.utils.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Code sample for accessing the Yelp API V2.
 * <p/>
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 * <p/>
 * <p/>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 */
public class YelpAPI {
    private static final Logger log = Logger.getLogger(YelpAPI.class.getName());

    private static final String API_HOST = "api.yelp.com";
    private static final String DEFAULT_TERM = "dinner";
    private static final String DEFAULT_LOCATION = "San Francisco, CA";
    private static final String SEARCH_PATH = "/v2/search";
    private static final String BUSINESS_PATH = "/v2/business";

    /*
     * Update OAuth credentials below from the Yelp Developers API site:
     * http://www.yelp.com/developers/getting_started/api_access
     */
    private static final String CONSUMER_KEY = "vABkqOfEUQLcLlClLUlq0w";
    private static final String CONSUMER_SECRET = "mJmH6aRyHpX7wSQSlqkgqmPDP6Q";
    private static final String TOKEN = "-ZdA5Wtn1GNwBowSKx0afpzl-b5qC2wy";
    private static final String TOKEN_SECRET = "k4Si-pxBJrBWx08VrbKB0Xt82ts";
    OAuthService service;
    Token accessToken;
    private int searchLimit = 2;

    /**
     * Setup the Yelp API OAuth credentials.
     *
     * @param consumerKey    Consumer key
     * @param consumerSecret Consumer secret
     * @param token          Token
     * @param tokenSecret    Token secret
     */
    public YelpAPI(String consumerKey, String consumerSecret, String token, String tokenSecret) {
        this.service =
                new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(consumerKey)
                        .apiSecret(consumerSecret).build();
        this.accessToken = new Token(token, tokenSecret);
    }

    public static YelpAPI getInstance() {
        return new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
    }

    /**
     * Creates and sends a request to the Search API by term and location.
     * <p/>
     * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
     * for more info.
     *
     * @param term     <tt>String</tt> of the search term to be queried
     * @param location <tt>String</tt> of the location
     * @return <tt>String</tt> JSON Response
     */
    public String searchForBusinessesByLocation(String term, String location, Double lat, Double lng) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("limit", String.valueOf(searchLimit));

        if (lat != null && lng != null) {
            request.addQuerystringParameter("cll", lat.toString() + ',' + lng.toString());
        }

        return sendRequestAndGetResponse(request);
    }

    public String searchForBusinessesByLatLng(String term, Double lat, Double lng) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("ll", lat.toString() + ',' + lng.toString());
        request.addQuerystringParameter("limit", String.valueOf(searchLimit));
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and sends a request to the Business API by business ID.
     * <p/>
     * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
     * for more info.
     *
     * @param businessID <tt>String</tt> business ID of the requested business
     * @return <tt>String</tt> JSON Response
     */
    public String searchByBusinessId(String businessID) {
        OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return <tt>OAuthRequest</tt>
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
        return request;
    }

    public List<BusinessResult> searchBusinesses(String what, String where, Double lat, Double lng) {
        String rawJson = queryAPI(what, where, lat, lng);
        JSONArray ja = extractBusinesses(rawJson);
        List<BusinessResult> ret = new ArrayList<BusinessResult>();

        for (int i = 0; i < ja.size(); i++) {
            Object job = ja.get(i);
            if (job instanceof JSONObject) {
                JSONObject jo = (JSONObject) job;
                BusinessResult br = jsonToBusinessResult(jo);
                ret.add(br);
            }
        }

        return ret;
    }

    protected BusinessResult jsonToBusinessResult(JSONObject jo) {
        String name = safeGet(jo, "name");
        String rating = safeGet(jo, "rating");
        String phone = safeGet(jo, "display_phone");
        String reviewCount = safeGet(jo, "review_count");
        String closedNow = safeGet(jo, "is_closed");
        String link = safeGet(jo, "mobile_url");
        String snippet = safeGet(jo, "snippet_text").replace('\n', ' ');
        String street = "";
        JSONObject location = (JSONObject) jo.get("location");
        if (location != null) {
            JSONArray addrArray = (JSONArray) location.get("address");
            if (addrArray != null && addrArray.size() > 0) {
                street = addrArray.get(0).toString();
            }
        }
        String cross = safeGet(location, "cross_streets");

        //Now build address
        StringBuilder sbA = new StringBuilder();
        sbA.append(street);
        if (!"".equals(cross)) {
            String acr = cross;
            if (cross.indexOf('&') > 0) {
                acr = cross.substring(0, cross.indexOf('&')).trim();
            }
            sbA.append(" & ").append(acr);
        }

        String address = sbA.toString();

        BusinessResult br = new BusinessResult(name);
        br.setRating(rating);
        br.setPhoneNumber(StringUtils.stripPhone(phone));
        br.setAddress(address);
        br.setNumReviews(reviewCount);
        br.setReviewUrl(link);
        br.setSnippet(snippet);
        br.setOpenStatus("true".equals(closedNow) ? "Closed" : "Open");

        return br;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     *
     * @param request {@link OAuthRequest} corresponding to the API request
     * @return <tt>String</tt> body of API response
     */
    private String sendRequestAndGetResponse(OAuthRequest request) {
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    public String queryAPI(String what, String where, Double lat, Double lng) {
        if (what == null || what.isEmpty()) {
            what = DEFAULT_TERM;
        }

        if (where == null || where.isEmpty()) {
            where = DEFAULT_LOCATION;
        }

        return searchForBusinessesByLocation(what, where, lat, lng);
    }

    protected String ratingToEmoticon(String rating) {
        switch (rating) {
            case "5.0":
                return "\uD83D\uDE07";
            case "4.5":
                return "\uD83D\uDE07";
            case "4.0":
                return "\uD83D\uDE0A";
            case "3.5":
                return "\uD83D\uDE00";
            case "3.0":
                return "\uD83D\uDE10";
            case "2.5":
                return "\uD83D\uDE26";
            case "2.0":
                return "\uD83D\uDE26";
            default:
                return "\uD83D\uDE26";
        }
    }

    private JSONArray extractBusinesses(String yelpJsonResponse) {
        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(yelpJsonResponse);
        } catch (ParseException pe) {
            log.warning("Error: could not parse JSON response:");
            log.fine(yelpJsonResponse);
        }

        if (response.get("error") == null) {
            return (JSONArray) response.get("businesses");
        }

        return new JSONArray();
    }

    private String safeGet(JSONObject jo, String attr) {
        if (jo.get(attr) == null) {
            return "";
        }

        return jo.get(attr).toString();
    }

    public void setSearchLimit(int searchLimit) {
        this.searchLimit = searchLimit;
    }

}
