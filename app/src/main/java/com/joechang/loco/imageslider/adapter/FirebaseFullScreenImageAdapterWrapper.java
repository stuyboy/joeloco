package com.joechang.loco.imageslider.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.firebase.client.Query;
import com.google.android.gms.maps.model.LatLng;
import com.joechang.loco.R;
import com.joechang.loco.firebase.FirebasePagerAdapter;
import com.joechang.loco.imageslider.helper.TouchImageView;
import com.joechang.loco.model.ImageUpload;
import com.joechang.loco.utils.BitmapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Author:  joechang
 * Date:    12/3/14
 * Purpose: data access adapter through firebase for full-screen images.
 */
public class FirebaseFullScreenImageAdapterWrapper extends PagerAdapter {
    private Activity _activity;
    FirebasePagerAdapter pagerAdapter;
    FirebaseChangeListener firebaseChangeListener;
    FirebaseLoadListener firebaseLoadListener;

    @SuppressLint("UseSparseArrays")
    private Map<Integer, LatLng> latLngMap = new HashMap<Integer, LatLng>();

    public FirebaseFullScreenImageAdapterWrapper(Query ref, Activity activity) {
        this.pagerAdapter = new FirebasePagerAdapter(ref, activity.getBaseContext(), activity.getLayoutInflater());
        this.pagerAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                FirebaseFullScreenImageAdapterWrapper.super.notifyDataSetChanged();
                doOnChanged();
            }

            @Override
            public void onInvalidated() {
                FirebaseFullScreenImageAdapterWrapper.super.notifyDataSetChanged();
                doOnInvalidated();
            }
        });
        this._activity = activity;
    }

    protected void doOnChanged() {
        //clearLatLngForPosition();
        if (this.firebaseChangeListener != null) {
            this.firebaseChangeListener.doOnChange(null);
        }
    }

    protected void doOnInvalidated() {
        //clearLatLngForPosition();
        if (this.firebaseChangeListener != null) {
            this.firebaseChangeListener.doOnChange(null);
        }
    }

    public void onFirebaseChange(FirebaseChangeListener fcl) {
        this.firebaseChangeListener = fcl;
    }

    public void onFirebaseLoad(FirebaseLoadListener fll) {
        this.firebaseLoadListener = fll;
    }

    @Override
    public int getCount() {
        return this.pagerAdapter.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;
        Button btnClose;

        LayoutInflater inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);

        btnClose = (Button) viewLayout.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _activity.finish();
            }
        });

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        ImageUpload iu = this.pagerAdapter.getItem(position);
        imgDisplay.setImageBitmap(BitmapUtils.toBitmap(iu));

        ((ViewPager) container).addView(viewLayout);

        LatLng store = new LatLng(iu.getLocTime().getLatitude(), iu.getLocTime().getLongitude());
        this.latLngMap.put(position, store);

        //Data has been loaded for this item/position.
        if (this.firebaseLoadListener != null) {
            this.firebaseLoadListener.doOnLoad(position);
        }

        return viewLayout;
    }

    public LatLng getLatLngForPosition(int position) {
        return this.latLngMap.get(position);
    }

    protected void clearLatLngForPosition() {
        this.latLngMap.clear();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }

    public interface FirebaseChangeListener {
        public void doOnChange(Integer positionChanged);
    }

    public interface FirebaseLoadListener {
        public void doOnLoad(Integer positionLoaded);
    }
}
