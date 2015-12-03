package com.joechang.loco;

import android.os.Bundle;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.firebase.GeoFire;
import com.joechang.loco.fragment.AsyncLoadingFragment;
import com.joechang.loco.fragment.GroupUserListFragment;
import com.joechang.loco.fragment.NearbyFragment;
import com.joechang.loco.fragment.UserListFragment;
import com.joechang.loco.model.LocTime;
import com.joechang.loco.model.User;
import com.joechang.loco.ui.UserSelector;
import com.joechang.loco.utils.RealtimeLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author:    joechang
 * Created:   6/25/15 3:18 PM
 * Purpose:   Using a list of ids sent in as arguments, show a nearbyfragment.
 */
public class UsersNearbyFragment extends NearbyFragment implements AsyncLoadingFragment {

    private Map<Firebase, ValueEventListener> listenerMap = new HashMap<Firebase, ValueEventListener>();
    private ReadyAction readyAction;
    private int numberOfUsers = 0;

    public static UsersNearbyFragment newInstance() {
        UsersNearbyFragment fragment = new UsersNearbyFragment();
        return fragment;
    }

    @Override
    public UserListFragment getUserListFragment() {
        return UserListFragment.newInstance();
    }

    @Override
    protected void registerListeners() {
        List<String> l = UserSelector.getSelectedUsersIds(this);
        if (l != null) {
            registerListenerForUsers(l);
        }
    }

    protected void registerListenerForUsers(List<String> userIds) {
        if (userIds != null) {
            numberOfUsers = userIds.size();
            for (String userId : userIds) {
                registerListenerForUser(userId);
            }
        }
    }

    protected void registerListenerForUser(final String userId) {
        //Plot other users that are within range by adding listeners for each group member.
        GeoFire.ListenerPair fbVel = FirebaseManager.getInstance().getGeoFire().registerRealtimeLocationUpdate(
                userId,
                new RealtimeLocation.OnLocationUpdate() {
                    @Override
                    public void execute(String key, LocTime location) {
                        //But is this contact within your circles?
                        if (getMap() != null) {
                            plotContact(key, location);
                        }
                        if (getOnFinishedLoadingAction() != null) {
                            getOnFinishedLoadingAction().doAction(0);
                        }
                    }

                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void remove(String key) {
                        removeContact(key);
                    }
                }
        );
        addListener(fbVel);
    }

    protected void addListener(GeoFire.ListenerPair pfv) {
        if (pfv != null) {
            this.listenerMap.put(pfv.getFirebase(), pfv.getListener());
        }
    }

    @Override
    protected void removeListeners() {
        if (this.listenerMap != null) {
            for (Map.Entry<Firebase, ValueEventListener> lmE : this.listenerMap.entrySet()) {
                lmE.getKey().removeEventListener(lmE.getValue());
            }
        }
        super.removeListeners();
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    @Override
    public ReadyAction getOnFinishedLoadingAction() {
        return this.readyAction;
    }

    @Override
    public void onFinishedLoading(ReadyAction ra) {
        this.readyAction = ra;
    }
}
