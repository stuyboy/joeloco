package com.joechang.loco.fragment;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.joechang.loco.R;
import com.joechang.loco.avatar.AvatarUrlBuilder;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.LocTime;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.User;
import com.joechang.loco.service.LocationPublishService;
import com.joechang.loco.utils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * Displays a map for what's nearby, who's nearby?!?! for a specific group.
 */
public class NearbyFragment extends Fragment implements RealtimeLocation.OnLocationUpdate {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_STARTLONGITUDE = "startLongitude";
    private static final String ARG_STARTLATITUDE  = "startLatitude";
    private static final int DEFAULT_MAP_ZOOM = 10;

    // TODO: Rename and change types of parameters
    private double mStartLongitude;
    private double mStartLatitude;

    protected MapFragment mMapFragment;
    protected CompassFragment mCompassFragment;
    protected UserListFragment mUserFragment;

    private String userId;
    private GeoQuery geoQuery;

    private Map<String, Collection<Marker>> trackedMarkers;

    public static NearbyFragment newInstance() {
        NearbyFragment fragment = new NearbyFragment();
        return fragment;
    }

    public NearbyFragment() {
        // Required empty public constructorgu
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.trackedMarkers = new HashMap<>();

        Location ll = LocationUtils.getInstance(getActivity()).getCurrentLocation();
        mStartLatitude = ll.getLatitude();
        mStartLongitude = ll.getLongitude();

        if (getArguments() != null) {
            mStartLatitude = getArguments().getDouble(ARG_STARTLATITUDE);
            mStartLongitude = getArguments().getDouble(ARG_STARTLONGITUDE);
        }

        this.userId = UserInfoStore.getInstance(getActivity()).getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nearby, container, false);

        mMapFragment = getMapFragment();
        mCompassFragment = getCompassFragment();
        mUserFragment = getUserListFragment();

        getFragmentManager().beginTransaction()
                .add(R.id.nearbyMapContainer, mMapFragment)
                .add(R.id.nearbyMapContainer, mCompassFragment)
                .add(R.id.nearbyMapContainer, mUserFragment)
                .commit();

        return v;
    }

    public MapFragment getMapFragment() {
        return MapFragment.newInstance();
    }

    public CompassFragment getCompassFragment() {
        return CompassFragment.newInstance();
    }

    public UserListFragment getUserListFragment() {
        return UserListFragment.newInstance();
    }

    public boolean refresh() {
        if (getMap() != null) {
            UiSettings us = MapUtils.setupMapFragment(getMap());
            us.setZoomGesturesEnabled(true);
            us.setScrollGesturesEnabled(true);

            MapUtils.moveMap(getMap(), getCurrentLatLng(), false);
            getMap().clear();
        }

        //The UserList refreshes itself on activity creation, but within GroupActivity, may need a nudge as group changes.
        if (mUserFragment.isInitialized()) {
            mUserFragment.refresh();
        }

        removeListeners();
        registerListeners();

        return true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }


    protected void registerListeners() {
        //Plot other users that are within range.
        this.geoQuery = FirebaseManager.getInstance().getGeoFire().registerRealtimeLocationUpdate(
                MapUtils.toLocTime(getCurrentLatLng()),
                this);
    }

    private LatLng getCurrentLatLng() {
        return new LatLng(mStartLatitude, mStartLongitude);
    }

    protected GoogleMap getMap() {
        return this.mMapFragment.getMap();
    }

    public void plotContact(final String id, final LocTime lt) {
        final LookupCache lc = LookupCache.getInstance();
        String username = lc.getUsernameById(id);
        plot(id, username, lt);
    }

    private void plot(final String id, final String username, final LocTime lt) {
        MapUtils.PlotType pt = MapUtils.PlotType.CONTACT;
        if (id.equals(this.userId)) {
            pt = MapUtils.PlotType.ME;
        }

        //The switcheroo.
        asyncPlot(id, username, AvatarUrlBuilder.mapPointerUrl(id), lt);
        //plotImpl(id, username, pt, lt);
    }

    /*
    private void plotImpl(final String id, final String username, final MapUtils.PlotType pt, final LocTime lt) {
        if (lt != null) {
            LatLng ll = new LatLng(lt.getLatitude(), lt.getLongitude());
            Collection<Marker> mFind = this.trackedMarkers.get(id);

            if (mFind == null || mFind.size() > 1 || mFind.size() <= 0) {
                Marker m = MapUtils.plot(pt, getMap(), ll);
                m.setTitle(username);
                updateMarker(id, m);

                //Do this only on initialization.
                autoZoomToIncludeMarkers();
            } else {
                ViewUtils.animateMarker(mFind.iterator().next(), ll);
            }
        }
    }
    */

    public void asyncPlot(final String id, final String username, final String imageUrl, final LocTime lt) {
        if (lt == null) {
            return;
        }

        final LatLng ll = new LatLng(lt.getLatitude(), lt.getLongitude());

        Collection<Marker> mFind = this.trackedMarkers.get(id);
        if (mFind == null || mFind.size() != 1) {
            DownloadImageTask dit = new DownloadImageTask(new DownloadImageTask.PostDownloadActor() {
                @Override
                public void doOnFinish(Bitmap result) {
                    Marker m = (result != null) ?
                            MapUtils.plot(BitmapDescriptorFactory.fromBitmap(result), 0, getMap(), ll) :
                            MapUtils.plot(MapUtils.PlotType.CONTACT, getMap(), ll);

                    if (m != null) {
                        m.setTitle(username);

                        //We insert into trackerMarkers in here.
                        updateMarker(id, m);

                        autoZoomToIncludeMarkers();
                    }
                }
            });
            dit.execute(imageUrl);
        } else {
            ViewUtils.animateMarker(mFind.iterator().next(), ll);
        }
    }

    public void removeContact(String username) {
        updateMarker(username, null);
    }

    public void removeAllContacts() {
        for (String s : this.trackedMarkers.keySet()) {
            removeContact(s);
        }
    }

    /**
     * Forcibly update the marker by creating a new one, clearing all old ones.  Forgoes any animation.
     * @param id
     * @param m
     */
    private void updateMarker(String id, Marker m) {
        Collection<Marker> prev = this.trackedMarkers.get(id);
        if (prev == null) {
            prev = new HashSet<Marker>();
        } else {
            MapUtils.clearMarkers(prev);
        }

        if (m != null) {
            this.trackedMarkers.put(id, Arrays.asList(new Marker[]{m}));
        } else {
            this.trackedMarkers.remove(id);
        }
    }

    protected void autoZoomToIncludeMarkers() {
        if (this.trackedMarkers.size() == 0) {
            return;
        }

        CameraUpdate cu = CameraUpdateFactory.zoomTo(DEFAULT_MAP_ZOOM);
        LatLng one = null;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Collection<Marker> mkrC : this.trackedMarkers.values()) {
            one = mkrC.iterator().next().getPosition();
            builder.include(one);
        }
        //builder.include(LocationUtils.getInstance(getActivity()).getCurrentLocation());

        if (this.trackedMarkers.size() > 1) {
            LatLngBounds bounds = builder.build();
            cu = CameraUpdateFactory.newLatLngBounds(bounds, DEFAULT_MAP_ZOOM);
        } else {
            if (one != null) {
                cu = CameraUpdateFactory.newLatLngZoom(one, DEFAULT_MAP_ZOOM);
            }
        }

        GoogleMap m = getMap();
        m.animateCamera(cu);
    }

    //Implements OnLocationUpdate
    @Override
    public void execute(String key, LocTime location) {
        //But is this contact within your circles?
        if (getMap() != null) {
            plotContact(key, location);
        }
    }

    @Override
    public void cancel() {

    }

    public Map<String, Collection<Marker>> getTrackedMarkers() {
        return trackedMarkers;
    }

    @Override
    public void remove(String key) {
        removeContact(key);
    }

    protected void removeListeners() {
        if (this.geoQuery != null) {
            this.geoQuery.removeAllListeners();
        }
    }

}
