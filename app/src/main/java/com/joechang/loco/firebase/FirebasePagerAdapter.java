package com.joechang.loco.firebase;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Query;
import com.joechang.loco.R;
import com.joechang.loco.imageslider.helper.TouchImageView;
import com.joechang.loco.model.ImageUpload;
import com.joechang.loco.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author:  joechang
 * Date:    12/16/14
 * Purpose: Why the fuck do I need to create a complete new adapter for paging?
 * Try making this as a shell that calls a real adapter for this stuff.
 */
public class FirebasePagerAdapter extends FirebaseAdapter<ImageUpload> {

    public FirebasePagerAdapter(Query ref, Context context, LayoutInflater inflater) {
        super(ref, ImageUpload.class, R.layout.layout_fullscreen_image, context, inflater);
    }

    @Override
    protected void populateView(View viewLayout, ImageUpload model, int position) {
        //The parent paging widget
        ViewPager vp = (ViewPager)viewLayout.findViewById(R.id.pager);

        //Items on the layout
        TouchImageView imgDisplay;
        Button btnClose;

        //Create the layout with an inflater.
        View imgLayout = getInflater().inflate(R.layout.layout_fullscreen_image, vp, false);
        imgDisplay = (TouchImageView)imgLayout.findViewById(R.id.imgDisplay);
        imgDisplay.setImageBitmap(BitmapUtils.toBitmap(model));

        //Get the viewpager and add this to it.
        vp.addView(imgLayout);
    }
}
