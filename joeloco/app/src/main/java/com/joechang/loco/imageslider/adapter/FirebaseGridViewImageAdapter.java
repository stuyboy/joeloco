package com.joechang.loco.imageslider.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.firebase.client.Query;
import com.joechang.loco.FullScreenViewActivity;
import com.joechang.loco.R;
import com.joechang.loco.firebase.FirebaseAdapter;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.ImageUpload;
import com.joechang.loco.utils.BitmapUtils;

/**
 * Author:  joechang
 * Date:    12/3/14
 * Purpose:
 */
public class FirebaseGridViewImageAdapter extends FirebaseAdapter<ImageUpload> {

    public static final String ARG_POSITION = "_mPosition";
    private int imageWidth;
    private String groupId;

    public FirebaseGridViewImageAdapter(Query ref, Context context, LayoutInflater inflater, int imageWidth) {
        super(ref, ImageUpload.class, R.layout.fragment_grid_view, context, inflater);
        this.imageWidth = imageWidth;
    }

    public FirebaseGridViewImageAdapter(Query ref, Context context, LayoutInflater inflater, int gridWidth, String groupId) {
        this(ref, context, inflater, gridWidth);
        this.groupId = groupId;
    }

    /**
     * Overridden to create imageview and add to the gridView.
     * @param i
     * @param view
     * @param viewGroup
     * @return the imageview
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageUpload model = getItem(i);
        view = new ImageView(this.getContext());
        populateView(view, model, i);
        return view;
    }

    @Override
    protected void populateView(View v, ImageUpload model, final int position) {
        ImageView iv = (ImageView)v;

        Bitmap image;
        image = BitmapUtils.toBitmap(model);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setLayoutParams(new GridView.LayoutParams(imageWidth, imageWidth));
        iv.setImageBitmap(image);

        // image view click listener
        iv.setOnClickListener(
                new View.OnClickListener() {
                    private int mPos = position;

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getContext(), FullScreenViewActivity.class);
                        i.putExtra(ARG_POSITION, mPos);
                        i.putExtra(Group.ID, groupId);
                        getContext().startActivity(i);
                    }
                }
        );
    }
}
