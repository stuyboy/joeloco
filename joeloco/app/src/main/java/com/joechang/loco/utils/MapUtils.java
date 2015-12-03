package com.joechang.loco.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.*;
import com.joechang.loco.R;
import com.joechang.loco.model.LocTime;

import java.util.Collection;

/**
 * Author:  joechang
 * Date:    12/10/14
 * Purpose: collection of items to keep map interaction consistent in the app
 */
public class MapUtils {

    public static final double DEFAULT_LATITUDE = 37.7952021;
    public static final double DEFAULT_LONGITUDE = -122.3937843;

    public enum PlotType {
        ME          ("me", "Device Operator", R.drawable.star),
        CONTACT     ("friend", "Someone in my Circles", R.drawable.pin);

        private final String id;
        private final String description;
        private final int iconResource;

        PlotType(String id, String description, int iconResource) {
            this.id = id;
            this.description = description;
            this.iconResource = iconResource;
        }
    }

    public static void setupMapFragment(Context cxt, GoogleMap m) {
        MapsInitializer.initialize(cxt);
        setupMapFragment(m);
    }

    public static UiSettings setupMapFragment(GoogleMap m) {
        if (m != null) {
            UiSettings us = m.getUiSettings();
            us.setAllGesturesEnabled(false);
            us.setZoomControlsEnabled(false);
            us.setCompassEnabled(false);
            us.setMyLocationButtonEnabled(false);
            //animateMap(m, getDefaultLatLng());
            return us;
        }

        return null;
    }

    public static LatLng getDefaultLatLng() {
        return new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
    }

    public static Location getDefaultLocation() {
        Location l = new Location("FAKE");
        l.setLatitude(DEFAULT_LATITUDE);
        l.setLongitude(DEFAULT_LONGITUDE);
        return l;
    }

    public static LocTime toLocTime(LatLng ll) {
        return new LocTime(ll.latitude, ll.longitude);
    }

    public static LocTime toLocTime(Location loc) {
        return new LocTime(loc.getLatitude(), loc.getLongitude());
    }

    public static LocTime toLocTime(GeoLocation gl) {
        return new LocTime(gl.latitude, gl.longitude);
    }

    public static Circle circleMap(GoogleMap m, LatLng latlng) {

        CircleOptions co = new CircleOptions()
                .center(latlng)
                .radius(500)
                .fillColor(R.color.white)
                .strokeWidth(1);
        animateMap(m, latlng);
        return m.addCircle(co);
    }

    public static void clearCircles(Collection<Circle> circles) {
        for (Circle c : circles) {
            c.remove();
        }
    }

    public static void animateMap(GoogleMap m, LatLng latlng) {
        moveMap(m, latlng, true);
    }

    public static void moveMap(GoogleMap m, LatLng latlng, boolean animate) {
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latlng, 14);
        if (animate)
            m.animateCamera(cu);
        else
            m.moveCamera(cu);
    }

    public static Marker plot(PlotType pe, GoogleMap map, LatLng ll) {
        float rotate = 0;

        if (PlotType.ME.equals(pe)) {
            rotate = 180;
        }

        return plot(BitmapDescriptorFactory.fromResource(pe.iconResource), rotate, map, ll);
    }

    public static Marker plot(BitmapDescriptor image, float rotate, GoogleMap map, LatLng ll) {
        MarkerOptions mo = new MarkerOptions()
                .position(ll)
                .rotation(rotate)
                .icon(image);

        if (map != null) {
            return map.addMarker(mo);
        }

        return null;
    }

    public static void clearMarkers(Collection<Marker> markers) {
        for (Marker m : markers) {
            m.remove();
        }
    }

    /**
     * For a particular zoom level, realize that the screen resolution, and calculate approx
     * how many km we're covering at this particular zoom.
     * @param zoomLevel
     * @return km
     */
    public static int zoomToKm(float zoomLevel) {
        double lat = 40.0d; //Average between Ca and Ny
        return (int)(156543.03392 * Math.cos(lat * Math.PI / 180) / Math.pow(2, zoomLevel));
    }

    /**
     * Passed in the km we'd like to cover, what is appropriate zoom level? From 2 to 21, zoom out toward zoom in.
     * @param km
     * @return zoomLevel
     */
    public static float kmToZoom(int km) {
        return 13;   //TODO: Not implemented!
    }
}
