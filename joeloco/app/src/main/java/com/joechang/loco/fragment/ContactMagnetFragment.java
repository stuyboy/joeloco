package com.joechang.loco.fragment;

import android.graphics.BitmapFactory;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.joechang.loco.R;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.firebase.GeoFire;
import com.joechang.loco.model.LocTime;
import com.joechang.loco.model.User;
import com.joechang.loco.utils.LocationUtils;
import com.joechang.loco.utils.RealtimeLocation;

/**
 * Author:    joechang
 * Created:   6/15/15 11:59 AM
 * Purpose:
 */
public class ContactMagnetFragment extends CompassFragment {
    private GeoFire.ListenerPair mUserListener;
    private String mTargetUserId;
    private ImageView mTargetPointer;
    private TextView mTargetInfo;
    private boolean mTargetUserNotFound;

    //Where is our arrow currently pointing absolutely?
    private volatile float mTargetCurrentDegree;

    //Where is the target in degrees east of north?
    private volatile float mTargetEastOfNorth;

    //How far are we from this dude?
    private volatile double mDistance;

    public ContactMagnetFragment() {
        setClickable(false);
    }

    public static ContactMagnetFragment newInstance() {
        return new ContactMagnetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTargetUserId = getArguments().getString(User.ID);
        }

        if (savedInstanceState != null && mTargetUserId == null) {
            mTargetUserId = savedInstanceState.getString(User.ID);
        }

        if (mTargetUserId == null) {
            return;
        }

        //Now plot an arrow to that user based on North.
        mUserListener = FirebaseManager.getInstance().getGeoFire().registerRealtimeLocationUpdate(
                mTargetUserId,
                new RealtimeLocation.OnLocationUpdate() {
                    @Override
                    public void execute(String key, LocTime location) {
                        if (location == null) {
                            mTargetUserNotFound = true;
                        } else {
                            mTargetUserNotFound = false;
                            setDirectionalDifferences(location);
                        }
                        updateTextViews();
                    }

                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void remove(String key) {

                    }
                }
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(User.ID, mTargetUserId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        v.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        mTargetPointer = (ImageView) v.findViewById(R.id.targetPointer);
        mTargetPointer.setVisibility(View.VISIBLE);

        mTargetInfo = (TextView) v.findViewById(R.id.targetInfo);
        mTargetInfo.setVisibility(View.VISIBLE);

        ImageView compassPointer = (ImageView) v.findViewById(R.id.compass);
        compassPointer.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.minimalcompass));

        return v;
    }

    /**
     * Per the compass fragment, if we're pointing a new direction for north, move the contact pointer accordingly.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        super.onSensorChanged(event);
        float targetDegree = (getCompassCurrentDegree() + mTargetEastOfNorth) % 360;
        if (animateArrow(mTargetPointer, mTargetCurrentDegree, targetDegree)) {
            mTargetCurrentDegree = targetDegree;
        }
    }

    public void updateTextViews() {
        if (mTargetInfo != null) {
            String msg = "User location not found.";
            if (!mTargetUserNotFound) {
                msg = String.format("%s is %s away %f degrees", mTargetUserId, mDistance, mTargetCurrentDegree);
            }
            mTargetInfo.setText(msg);
        }
    }

    /**
     * The main method called when the target user moves, and the compass changes.
     * Now, ideally, there should be one more event: when WE move.  But I think when the compass moves, which is often
     * will be enough to call the getCurrentLocation and update it.
     * @param themlt
     */
    public void setDirectionalDifferences(LocTime themlt) {
        Location me = LocationUtils.getInstance(getActivity()).getCurrentLocation();
        LocTime melt = new LocTime(me.getLatitude(), me.getLongitude());

        mDistance = melt.distanceTo(themlt);
        mTargetEastOfNorth = (float)melt.bearingTo(themlt);
    }
}
