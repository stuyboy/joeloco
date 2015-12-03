package com.joechang.loco.fragment;

import android.app.Fragment;
import android.os.Bundle;

import com.joechang.loco.model.User;
import com.joechang.loco.utils.UserInfoStore;

/**
 * Author:  joechang
 * Date:    1/29/15
 * Purpose: Abstract Fragment from which to extend.  Just uses the user.
 */
public abstract class AbstractUserFragment extends Fragment {

    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(User.ID);
        } else {
            userId = UserInfoStore.getInstance(getActivity()).getUserId();
        }

        if (userId == null) {
            throw new IllegalArgumentException("Fragment expects a user argument " + User.ID);
        }
    }

    public String getUserId() {
        return userId;
    }

}
