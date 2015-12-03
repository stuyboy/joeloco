package com.joechang.loco.firebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import com.firebase.client.Query;
import com.joechang.loco.R;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.ImageUpload;

/**
 * Author:  joechang
 * Date:    1/30/15
 * Purpose: A backing data adapter for groups operations.
 */
public class FirebaseGroupsAdapter extends FirebaseAdapter<Group> {

    public FirebaseGroupsAdapter(Query ref, int layout, Context context, LayoutInflater inflater) {
        super(ref, Group.class, layout, context, inflater);
    }

    @Override
    protected void populateView(View v, Group model, int position) {
        TextView tv = (TextView)v;
        ViewParent vp = tv.getParent();
        tv.setText(model.getName());
    }
}
