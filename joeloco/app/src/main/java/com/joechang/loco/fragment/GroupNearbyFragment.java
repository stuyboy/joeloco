package com.joechang.loco.fragment;

import android.app.Activity;
import android.location.Location;
import android.net.Network;
import android.os.Bundle;
import android.util.Pair;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.MapFragment;
import com.joechang.loco.BaseDrawerActionBarActivity;
import com.joechang.loco.R;
import com.joechang.loco.UsersNearbyFragment;
import com.joechang.loco.client.GroupClient;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.firebase.GeoFire;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.LocTime;
import com.joechang.loco.ui.GroupSelector;
import com.joechang.loco.utils.LocationUtils;
import com.joechang.loco.utils.MapUtils;
import com.joechang.loco.utils.NetworkErrorUtils;
import com.joechang.loco.utils.RealtimeLocation;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.*;

/**
 * Author:  joechang
 * Date:    3/19/15
 * Purpose: Plots nearby points, but does so for only the members of a specific group.
 */
public class GroupNearbyFragment extends UsersNearbyFragment implements AsyncLoadingFragment {


    public static GroupNearbyFragment newInstance() {
        GroupNearbyFragment fragment = new GroupNearbyFragment();
        return fragment;
    }

    @Override
    public UserListFragment getUserListFragment() {
        return GroupUserListFragment.newInstance();
    }

    @Override
    protected void registerListeners() {
        String groupId = GroupSelector.getSelectedGroupId(this);
        if (groupId != null) {
            GroupClient gc = RestClientFactory.getInstance().getGroupClient();
            gc.getGroup(
                    groupId,
                    new GroupClient.Callback() {
                        @Override
                        public void success(Group group, Response response) {
                            final Map<String, String> existingUsers = group.getMembers();
                            if (existingUsers != null) {
                                registerListenerForUsers(new ArrayList<String>(existingUsers.keySet()));
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            NetworkErrorUtils.handleNetworkError(GroupNearbyFragment.this, error);
                        }
                    }
            );
        }
    }

}
