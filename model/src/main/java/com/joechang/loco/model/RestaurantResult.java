package com.joechang.loco.model;

import java.util.Date;

/**
 * Author:    joechang
 * Created:   9/1/15 5:55 PM
 * Purpose:
 */
public class RestaurantResult extends BusinessResult {
    //Reservation Detail
    private String[] availableTimes;
    private String reserveUrl;
    private Date reserveDate;
    private int numPeople;

    public RestaurantResult(String name) {
        super(name);
    }

    public String getReserveUrl() {
        return reserveUrl;
    }

    public void setReserveUrl(String reserveUrl) {
        this.reserveUrl = reserveUrl;
    }

    public String[] getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(String[] availableTimes) {
        this.availableTimes = availableTimes;
    }

    public int getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public Date getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(Date reserveDate) {
        this.reserveDate = reserveDate;
    }

    public void setReserveUrlById(String base, Integer id) {
        setReserveUrl(base + id);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getName()).append('\n');
        for (int i = 0; i < availableTimes.length; i++) {
            b.append(String.valueOf(availableTimes[i]).trim());
            if (i == availableTimes.length - 1)
                return b.append('\n').toString();
            b.append(", ");
        }
        return "";
    }
}
