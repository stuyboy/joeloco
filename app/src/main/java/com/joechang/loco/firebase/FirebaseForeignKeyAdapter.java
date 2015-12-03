package com.joechang.loco.firebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import com.firebase.client.Query;
import com.joechang.loco.model.ForeignKey;
import com.joechang.loco.model.Group;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Author:  joechang
 * Date:    1/30/15
 * Purpose: A backing data adapter for collections where it's just a key/val pair pointing to some object
 */
public class FirebaseForeignKeyAdapter extends FirebaseAdapter<ForeignKey> {

    public FirebaseForeignKeyAdapter(Query ref, int layout, Context context, LayoutInflater inflater) {
        super(ref, ForeignKey.class, layout, context, inflater);
    }

    @Override
    protected void populateView(View v, ForeignKey model, int position) {
        TextView tv = (TextView)v;
        tv.setText(model.getName());
    }
}
