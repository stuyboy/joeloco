package com.joechang.loco.service;

import com.joechang.loco.Configuration;
import com.joechang.loco.RedirectController;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.config.Routes;
import com.joechang.loco.model.RestaurantResult;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author:    joechang
 * Created:   8/7/15 12:37 PM
 * Purpose:   Quick little class that calls the unofficial opentable api
 */
public class OpenTableAPI {
    private Logger log = Logger.getLogger(OpenTableAPI.class.getName());

    private OpenTableClient otRestClient;
    private DateFormat mDf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Map<String, String> opentableCuisines;
    private Map<String, String> opentableSFNeighborhoods;

    private final static int DEFAULT_SIZE = 5;
    public final static String MOBILE_RESERVE_URL = "http://mobile.opentable.com/opentable/?restId=";

    public final static String RESERVE_REDIRECT_URL = RedirectController.BASE_URL + Routes.OPENTABLE_REDIRECT;

    public OpenTableAPI() {
        otRestClient = RestClientFactory.getInstance()
                .createRestAdapter(Configuration.getOpenTableApiEndpoint())
                .create(OpenTableClient.class);

        opentableCuisines = new HashMap<String, String>();
        opentableSFNeighborhoods = new HashMap<String, String>();

        loadPropertiesFile(opentableCuisines, "openTableCuisines.txt");
        loadPropertiesFile(opentableSFNeighborhoods, "openTableSFNeighborhoods.txt");
    }

    private void loadPropertiesFile(Map<String, String> target, String filename) {
        try {
            InputStream is = OpenTableAPI.class.getClassLoader().getResourceAsStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;

            while ((line = br.readLine()) != null) {
                String descr=line.substring(0, line.indexOf('=')).replace("\"","").toLowerCase();
                String id=line.substring(line.indexOf('=')+1).replace("\"","");
                target.put(descr, id);
            }

            br.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Cannot load properties file");
        }
    }

    public OpenTableClient getClient() {
        return otRestClient;
    }

    public String[] findOpenTimes(RestaurantResult rr) {
        return findOpenTimes(rr.getId(), rr.getNumPeople(), rr.getReserveDate());
    }

    public String[] findOpenTimes(int restaurantId, int partySize, Date date) {
        String d = mDf.format(date).replace(' ','T');
        return findOpenTimes(
                "http://m.opentable.com/search/results?" +
                "RestaurantID=" + restaurantId +
                "&PartySize=" + partySize +
                "&TimeInvariantCulture=" + d +
                "&DateInvariantCulture=" + d
        );
    }

    public String[] findOpenTimes(String mobileReserveUrl) {
        List<String> retTimes = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(mobileReserveUrl).get();
            Elements times = doc.select("#ulSlots .btn span");
            for (Element e : times) {
                retTimes.add(e.childNode(0).toString());
            }
        } catch (IOException ioe) {
            //cannot parse!
            int i=1;
        }

        return retTimes.toArray(new String[retTimes.size()]);
    }

    public int lookupCuisineId(String incomingCuisine) {
        String cuisineSearch = opentableCuisines.get(incomingCuisine.toLowerCase());
        if (cuisineSearch != null) {
            return Integer.parseInt(cuisineSearch);
        }

        return -1;
    }

    public int lookupNeighborhood(String neighborhood) {
        String nSearch = opentableSFNeighborhoods.get(neighborhood.toLowerCase());
        if (nSearch != null) {
            return Integer.parseInt(nSearch);
        }

        return -1;
    }

    /**
     * This is very SF specific right now.  Not great.
     * @param date
     * @param partySize
     * @param cuisineName
     * @param neighborhoodName
     * @param size
     * @return
     */
    public List<RestaurantResult> searchRestaurants(Date date, int partySize, String cuisineName, String neighborhoodName, Integer size) {
        return searchRestaurants(date, partySize, lookupCuisineId(cuisineName), lookupNeighborhood(neighborhoodName), size);
    }

    public List<RestaurantResult> searchRestaurants(Date date, int partySize, int cuisineId, int neighborhoodId, int numResults) {
        List<RestaurantResult> retRests = new ArrayList<>();

        try {
            String dStr = URLEncoder.encode(mDf.format(date), "UTF-8");

            String queryUrl = "http://www.opentable.com/s/?"
                    + "datetime=" + dStr + "&"
                    + "covers=" + partySize + "&"
                    + "metroid=" + "4" + "&"
                    + "regionids=" + "5" + "&"
                    + "size=" + ((numResults != -1) ? numResults : DEFAULT_SIZE)
                    + ((cuisineId != -1) ? "&cuisineids=" + cuisineId : "")
                    + ((neighborhoodId != -1) ? "&neighborhoodids=" + neighborhoodId : "")
                    ;

            log.info("Scraping " + queryUrl);

            int resultCount = 0;
            Document doc = Jsoup.connect(queryUrl).get();

            Elements results = doc.select(".result");
            for (Element e: results) {
                if (resultCount >= numResults) {
                    break;
                }

                //Id
                Integer restId = null;
                String restaurantId = e.attr("data-rid");
                try {
                    restId = Integer.parseInt(restaurantId);
                } catch (NumberFormatException nfe) {
                    //no-biggue
                }

                Elements nameE = e.select(".rest-row-name");
                String name = (nameE.get(0).childNode(0).toString());

                List<String> times = new ArrayList<>();
                Elements timesE = e.select(".timeslot");
                for (Element t : timesE) {
                    times.add(t.childNode(0).toString());
                }

                if (name != null) {
                    resultCount++;
                    RestaurantResult rr = new RestaurantResult(name.trim());
                    if (restId != null) {
                        rr.setId(restId);
                        rr.setReserveUrlById(RESERVE_REDIRECT_URL, restId);
                    }
                    rr.setNumPeople(partySize);
                    rr.setAvailableTimes(times.toArray(new String[]{}));
                    retRests.add(rr);
                }
            }

        } catch (IOException ioe) {
            //woah!
        }

        return retRests;
    }

    public static RestaurantResult fromJSON(JSONObject jo) {
        String to = jo.get("total_entries").toString();

        if (Double.parseDouble(to) <= 0) {
            return null;
        }

        List<JSONObject> restaurants = (List<JSONObject>)jo.get("restaurants");
        Map jo1 = (Map)restaurants.get(0);

        String name = jo1.get("name").toString();
        String url = jo1.get("mobile_reserve_url").toString();
        Integer id = (int)Double.parseDouble(jo1.get("id").toString());

        RestaurantResult rr = new RestaurantResult(name);
        rr.setId(id);
        rr.setReserveUrlById(RESERVE_REDIRECT_URL, id);

        return rr;
    }
}
