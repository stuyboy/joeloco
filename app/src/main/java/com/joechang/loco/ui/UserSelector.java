package com.joechang.loco.ui;

import android.app.Fragment;
import android.content.Context;
import com.joechang.loco.fragment.UserListFragment;
import com.joechang.loco.model.User;

import java.util.List;

/**
 * Author:    joechang
 * Created:   6/25/15 10:36 PM
 * Purpose:
 */
public abstract class UserSelector {

    public static final String KEY_USER_LIST = "__USERLISTKEy";

    public static List<String> getSelectedUsersIds(Context a) {
        if (a != null) {
            if (a instanceof User.UserListSelector) {
                return ((User.UserListSelector)a).getSelectedUsersIds();
            }
        }

        return null;
    }

    public static List<String> getSelectedUsersIds(Fragment f) {
        return getSelectedUsersIds(f.getActivity());
    }
}
