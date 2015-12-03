package com.joechang.loco;

import com.firebase.client.Query;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.imageslider.adapter.FirebaseFullScreenImageAdapterWrapper;
import com.joechang.loco.imageslider.adapter.FirebaseGridViewImageAdapter;
import com.joechang.loco.imageslider.helper.Utils;
import com.joechang.loco.model.Group;
import com.joechang.loco.utils.MapUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.Collection;

public class  FullScreenViewActivity extends Activity {

	private Utils utils;
	private FirebaseFullScreenImageAdapterWrapper adapter;
	private ViewPager viewPager;
    private Collection<Circle> mapCircles = new ArrayList<Circle>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen_view);

        Intent i = getIntent();

        int position = i.getIntExtra(FirebaseGridViewImageAdapter.ARG_POSITION, 0);
        String groupId = i.getStringExtra(Group.ID);

		viewPager = (ViewPager) findViewById(R.id.pager);
		utils = new Utils(getApplicationContext());

        setupImagePager(position, groupId);

        MapFragment map = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        MapUtils.setupMapFragment(map.getMap());
	}

    protected void setupImagePager(final int position, final String groupId) {
        Query query = FirebaseManager.getInstance().getImageUploadFirebase();

        if (groupId != null) {
            query = query.orderByChild(Group.ID).equalTo(groupId);
        }

        adapter = new FirebaseFullScreenImageAdapterWrapper(
                query,
                FullScreenViewActivity.this
        );
        viewPager.setAdapter(adapter);

        adapter.onFirebaseChange(new FirebaseFullScreenImageAdapterWrapper.FirebaseChangeListener() {
            @Override
            public void doOnChange(Integer positionChanged) {
                viewPager.setCurrentItem(position);
            }
        });

        adapter.onFirebaseLoad(new FirebaseFullScreenImageAdapterWrapper.FirebaseLoadListener() {
            @Override
            public void doOnLoad(Integer positionLoaded) {
                if (positionLoaded == viewPager.getCurrentItem()) {
                    synchronizeMap(adapter.getLatLngForPosition(position));
                }
            }
        });

        viewPager.setOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int i, float v, int i2) {
                    }

                    @Override
                    public void onPageSelected(int i) {
                        synchronizeMap(adapter.getLatLngForPosition(i));
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {
                    }
                }
        );
    }

    private void synchronizeMap(LatLng ll) {
        MapFragment mf = (MapFragment)this.getFragmentManager().findFragmentById(R.id.map);
        if (ll != null && mf != null) {
            MapUtils.clearCircles(this.mapCircles);
            mapCircles.add(MapUtils.circleMap(mf.getMap(), ll));
        }
    }

}
