package com.joechang.loco.logging;

/**
 * Created by joechang on 5/12/15.
 */
public class LogLocationEntry {

    private double latitude;
    private double longitude;
    private String userId;
    private long timeStamp;

    public LogLocationEntry(String userId, double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.timeStamp = System.currentTimeMillis();
    }

    //Required for JSON Marshalling.
    public LogLocationEntry() {}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
