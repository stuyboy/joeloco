package com.joechang.loco.fragment;

import android.app.Activity;
import com.joechang.loco.client.GroupClient;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.model.Group;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;

/**
 * Author:    joechang
 * Created:   6/25/15 2:46 PM
 * Purpose:
 */
public class GroupUserListFragment extends UserListFragment {

    public static GroupUserListFragment newInstance() {
        return new GroupUserListFragment();
    }

    public void refresh() {
        String groupId = getGroupId();
        if (groupId != null) {
            populateUserListByGroup(groupId);
            return;
        }
    }

    public void populateUserListByGroup(String groupId) {
        GroupClient gc = RestClientFactory.getInstance().getGroupClient();
        gc.getGroup(groupId, new GroupClient.Callback() {
                    @Override
                    public void success(Group group, Response response) {
                        populateAvatarArray(new ArrayList<String>(group.getMembers().keySet()));
                        refreshAvatarView();
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }

    public String getGroupId() {
        Activity a = getActivity();
        if (a instanceof Group.GroupSelector) {
            String g = ((Group.GroupSelector) a).getSelectedGroupId();
            if (g != null) {
                return g;
            }
        }

        return null;
    }

}
