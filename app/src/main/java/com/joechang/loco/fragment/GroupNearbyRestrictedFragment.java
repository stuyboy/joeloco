package com.joechang.loco.fragment;

import android.util.Pair;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.LocTime;
import com.joechang.loco.ui.GroupSelector;
import com.joechang.loco.utils.RealtimeLocation;

import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author:  joechang
 * Date:    3/19/15
 * Purpose: Plots nearby points, but does so for only the members of a specific group, for specific times!
 */
public class GroupNearbyRestrictedFragment extends GroupNearbyFragment implements ValueEventListener {

    private Query eventQuery;
    private Map<String, Event> events = new HashMap<String, Event>();

    public static GroupNearbyRestrictedFragment newInstance() {
        GroupNearbyRestrictedFragment fragment = new GroupNearbyRestrictedFragment();
        return fragment;
    }

    @Override
    protected void registerListeners() {
        registerEventListeners();
        super.registerListeners();
    }

    /**
     * Overridden to take into account the events/times where location is shareable.
     * @param id
     * @param lt
     */
    @Override
    public void plotContact(String id, LocTime lt) {
        boolean plotOk = false;

        //This is hackable if time is reset.
        //TODO: Get this from the server, not the phone.
        Date timeNow = new Date();

        String groupId = GroupSelector.getSelectedGroupId(this);

        for (Event e : events.values()) {
            if (!groupId.equals(e.getGroupId())) {
                continue;
            }

            if (e.isValidNow(timeNow)) {
                plotOk = true;
                break;
            }
        }

        if (plotOk) {
            super.plotContact(id, lt);
        }

        return;
    }

    protected void registerEventListeners() {
        String groupId = GroupSelector.getSelectedGroupId(this);
        if (groupId != null) {
            eventQuery = FirebaseManager.getInstance().getEventFirebase().orderByChild(Group.ID).equalTo(groupId);
            eventQuery.addValueEventListener(this);
        }
    }

    @Override
    public boolean refresh() {
        this.events.clear();
        return super.refresh();
    }

    /**
     * This is for the event listener for event changes only.
     * @param dataSnapshot
     */
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Event e = (Event)ds.getValue(Event.class);
            this.events.put(e.getEventId(), e);
        }
        this.refreshMap();
    }

    private void refreshMap() {
        //When this changes, we need to redraw the nearbyMap, but not ourselves, as that
        //would re-register the event listeners as well.
        super.removeListeners();
        super.registerListeners();
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }

    @Override
    protected void removeListeners() {
        if (eventQuery != null) {
            eventQuery.removeEventListener(this);
        }
        super.removeListeners();
    }
}
