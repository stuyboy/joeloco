package com.joechang.loco.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Property;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.joechang.loco.R;

/**
 * Author:  joechang
 * Date:    4/22/15
 * Purpose: Random methods to help with rendering, etc.
 */
public class ViewUtils {

    public static void animateField(Context c, final TextView v, Integer currentTextColor) {
        Integer colorFrom = c.getResources().getColor(R.color.orange);
        Integer colorTo = currentTextColor;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(1500);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.setTextColor((Integer) animation.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    public static void animateMarker(Marker m, LatLng newLatLng) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                double lat = (endValue.latitude - startValue.latitude) * fraction + startValue.latitude;
                double lng = (endValue.longitude - startValue.longitude) * fraction + startValue.longitude;
                return new LatLng(lat, lng);
            }
        };

        LatLng oldLatLng = m.getPosition();
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator mapAnimation = ObjectAnimator.ofObject(m, property, typeEvaluator, newLatLng);
        mapAnimation.setDuration(1000);
        mapAnimation.start();
    }
}
