package com.joechang.loco.firebase;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Query;
import com.joechang.loco.RealtimeActivity;
import com.joechang.loco.R;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;

import java.text.SimpleDateFormat;

/**
 * Author:  joechang
 * Date:    1/30/15
 * Purpose: A backing data adapter for Events operations.
 */
public class FirebaseEventsAdapter extends FirebaseAdapter<Event> {

    public FirebaseEventsAdapter(Query ref, int layout, Context context, LayoutInflater inflater) {
        super(ref, Event.class, layout, context, inflater);
    }

    @Override
    protected void populateView(View v, final Event model, int position) {
        ViewGroup tv = (ViewGroup)v;

        TextView name = (TextView)tv.findViewById(R.id.eventName);
        name.setText(model.getName());

        TextView dtStart = (TextView)tv.findViewById(R.id.eventDateStart);
        dtStart.setText(SimpleDateFormat.getDateTimeInstance().format(model.getDateStart()));

        TextView dtEnd = (TextView)tv.findViewById(R.id.eventDateEnd);
        dtEnd.setText(SimpleDateFormat.getDateTimeInstance().format(model.getDateEnd()));

        //Add a click listener
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent i = new Intent(getContext(), EventEntryActivity.class);
                i.putExtra(Event.ID, model.getEventId());
                i.putExtra(Group.ID, model.getGroupId());
                i.putExtra("EventObject", model);
                getContext().startActivity(i);
                */

                Intent i = new Intent(getContext(), RealtimeActivity.class);
                i.putExtra(Group.ID, model.getGroupId());
                i.putExtra(Event.ID, model.getEventId());
                i.putExtra("EventObject", model);
                getContext().startActivity(i);
            }
        });
    }
}
