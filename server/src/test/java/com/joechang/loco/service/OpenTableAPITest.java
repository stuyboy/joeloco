package com.joechang.loco.service;

import com.joechang.loco.model.RestaurantResult;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Author:    joechang
 * Created:   8/31/15 2:25 PM
 * Purpose:
 */
public class OpenTableAPITest extends TestCase {

    public void testSearchRestaurants() throws Exception {
        OpenTableAPI ota = new OpenTableAPI();

        List<RestaurantResult> dd = ota.searchRestaurants(getTomorrow(), 2, 14, 32, 10);
        for (RestaurantResult s : dd) {
            System.out.println(s);
        }
    }

    public void testSearchRestaurantsWithCuisine() throws Exception {
        OpenTableAPI ota = new OpenTableAPI();

        List<RestaurantResult> dd = ota.searchRestaurants(getTomorrow(), 2, "sushi", "financial district", 10);
        for (RestaurantResult s : dd) {
            System.out.println(s);
        }
    }

    private Date getTomorrow() {
        Calendar cc = Calendar.getInstance();
        cc.add(Calendar.DATE, 1);
        cc.set(Calendar.HOUR_OF_DAY, 20);
        cc.set(Calendar.MINUTE, 00);

        return cc.getTime();
    }
}