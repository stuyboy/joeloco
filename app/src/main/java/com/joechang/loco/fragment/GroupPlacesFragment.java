package com.joechang.loco.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import com.joechang.loco.BaseDrawerActionBarActivity;
import com.joechang.loco.R;
import com.joechang.loco.firebase.FirebaseAdapter;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.imageslider.adapter.FirebaseGridViewImageAdapter;
import com.joechang.loco.imageslider.helper.Utils;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.ImageUpload;
import com.joechang.loco.ui.GroupSelector;
import com.joechang.loco.utils.CameraUtils;

/**
 * Author:  joechang
 * Date:    2/25/15
 * Purpose: A list of pictures that signify the places where a group goes.  Filtered by the
 * group Id.
 */
public class GroupPlacesFragment extends GridViewFragment implements
        View.OnClickListener {

    private int CAPTURE_PLACE_ACTIVITY_CODE = 1022;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imgWidth how many pictures go across
     * @return A new instance of fragment GridViewFragment.
     */
    public static GroupPlacesFragment newInstance(int imgWidth) {
        GroupPlacesFragment fragment = new GroupPlacesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMG_WIDTH, imgWidth);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_groups_places, container, false);
        createGridView(inflater, v);

        Button addPlace = (Button)v.findViewById(R.id.places_add);
        addPlace.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.places_add:
                launchCameraForPlaces();
        }
    }

    protected void launchCameraForPlaces() {
        startActivityForResult(
                CameraUtils.getCameraIntent(),
                CAPTURE_PLACE_ACTIVITY_CODE
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_PLACE_ACTIVITY_CODE) {
            ImageUpload i = CameraUtils.doCameraIntentResult(getActivity(), resultCode, data);
            if (i != null) {
                i.setGroupId(GroupSelector.getSelectedGroupId(this));
                FirebaseManager.getInstance().uploadImage(i);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public FirebaseAdapter getAdapter(Context cxt, LayoutInflater inflater, int gridWidth) {
        String groupId = GroupSelector.getSelectedGroupId(cxt);

        if (groupId == null) {
            return null;
        }

        return new FirebaseGridViewImageAdapter(
                FirebaseManager.getInstance().getImageUploadFirebase().orderByChild(Group.ID).equalTo(groupId),
                cxt,
                inflater,
                gridWidth,
                groupId);
    }

}
