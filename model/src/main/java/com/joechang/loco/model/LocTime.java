package com.joechang.loco.model;

/**
 * Author:  joechang
 * Date:    12/12/14
 * Purpose: Very simple pojo for noting the location (lat long) and time
 */
public class LocTime {

    public static final String PARAM_LATITUDE = "lat";
    public static final String PARAM_LONGITUDE = "lng";

    private double latitude;
    private double longitude;

    //Also known as the "Fix Time".  When this lat/lng was determined by GPS, etc.
    private long timestamp;

    //This is when THIS object was created.
    private long createdTime;

    //Source of the measurement
    private String provider;

    public enum DistanceUnit {
        MILES, KILOMETERS, NAUTICAL_MILES
    }

    //Required for firebase
    public LocTime() {}

    public LocTime(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.createdTime = System.currentTimeMillis();
    }

    public LocTime(double latitude, double longitude) {
        this(latitude, longitude, System.currentTimeMillis());
    }

    public LocTime(double latitude, double longitude, long timestamp, String provider) {
        this(latitude, longitude, timestamp);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public String toString() {
        return  "lat=" + latitude +
                ", long=" + longitude +
                ", ts=" + timestamp +
                ", p=" + provider;
    }

    /**
     * Return the distanceTo between this Loctime and another in Kilometers
     * @param lt
     * @return kilometers
     */
    public double distanceTo(LocTime lt) {
        return distance(this.getLatitude(), this.getLongitude(), lt.getLatitude(), lt.getLongitude());
    }

    public double bearingTo(LocTime lt) {
        return bearing(this.getLatitude(), this.getLongitude(), lt.getLatitude(), lt.getLongitude());
    }

    /**
     * Return the distanceTo between two decimal lat/lng points in kilometers
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return kilometers
     */
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (earthRadius * c);
        return dist;
    }

    public static double bearing(double lat1, double long1, double lat2, double long2) {
        double degToRad = Math.PI / 180.0;
        double phi1 = lat1 * degToRad;
        double phi2 = lat2 * degToRad;
        double lam1 = long1 * degToRad;
        double lam2 = long2 * degToRad;

        return Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)
        ) * 180/Math.PI;
    }
}
