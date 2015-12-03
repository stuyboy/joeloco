package com.joechang.loco.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import com.joechang.loco.BaseDrawerActionBarActivity;
import com.joechang.loco.R;

import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   6/8/15 12:57 PM
 * Purpose:   As you're looking for another destination, provide an interface that gives you an arrow and approximate
 * distance away.  Destination could be a person's lat/lng or some other lat/lng.  Can phone do this?
 */
public class CompassFragment extends Fragment implements SensorEventListener {
    private static Logger log = Logger.getLogger(CompassFragment.class.getName());

    private static final float MIN_DEGREES_TO_MOVE = 3.0f;
    private static final float MAX_DEGREES_TO_MOVE = 45.0f;

    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];

    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    private float mCurrentDegree = 0f;
    private volatile int largeMoveCount = 0;

    private boolean mClickable = false;

    public static CompassFragment newInstance() {
        return new CompassFragment();
    }

    public CompassFragment() {
        setClickable(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_magnet, container, false);

        mPointer = (ImageView) v.findViewById(R.id.compass);

        if (mClickable) {
            mPointer.setOnClickListener((BaseDrawerActionBarActivity)getActivity());
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            if (SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer)) {
                SensorManager.getOrientation(mR, mOrientation);
                float azimuthInRadians = mOrientation[0];
                float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
                float targetDegrees = -azimuthInDegrees + 360;
                if (animateArrow(mPointer, getCompassCurrentDegree(), targetDegrees)) {
                    mCurrentDegree = targetDegrees;
                }
            }
        }
    }

    /**
     * Takes the imageview (Assumed to be an arrow), and animates the rotation to degreesTo.
     * @param arrow
     * @param degreesTo
     * @return whether we performed the action
     */
    protected boolean animateArrow(ImageView arrow, float degreesFrom, float degreesTo) {
        float[] rectifiedDegrees = determineDirection(degreesFrom, degreesTo);

        float startPos = rectifiedDegrees[0];
        float endPos = rectifiedDegrees[1];

        //Diff
        float diff = Math.abs(endPos - startPos);

        //dampen somewhat.
        if (diff <= MIN_DEGREES_TO_MOVE) {
            return false;
        }

        //If we get a rotten movement, outside of range, sample a few times before moving.
        if (diff > MAX_DEGREES_TO_MOVE) {
            largeMoveCount++;
            if (largeMoveCount < 10) {
                return false;
            }
            largeMoveCount = 0;
        }

        RotateAnimation ra = new RotateAnimation(
                startPos,
                endPos,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setInterpolator(new OvershootInterpolator(1f));
        ra.setDuration(2000);
        ra.setFillAfter(true);

        arrow.startAnimation(ra);

        return true;
    }

    //Consider the circle more -180 to 180 versus 0 to 360.
    //Return the new "toDegrees", within a -180 to +180 value.
    private float findDirectional(float degrees) {
        if (degrees > 180) {
            return -(360-degrees);
        }

        return degrees;
    }

    private float[] determineDirection(float startDegrees, float endDegrees) {
        float newStart = startDegrees;
        float newEnd = endDegrees;

        if ((startDegrees > 180 && endDegrees <= 180) && ((360 - startDegrees + endDegrees) < 180)) {
            newStart = findDirectional(startDegrees);
            newEnd = endDegrees;
        } else if ((startDegrees <= 180 && endDegrees > 180) && ((360 - endDegrees + startDegrees) < 180)) {
            newStart = startDegrees;
            newEnd = findDirectional(endDegrees);
        }

        return new float[] { newStart, newEnd };
    }

    public float getCompassCurrentDegree() {
        return mCurrentDegree;
    }

    protected void setClickable(boolean b) {
        mClickable = b;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }
}
