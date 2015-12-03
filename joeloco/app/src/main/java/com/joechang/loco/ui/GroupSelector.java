package com.joechang.loco.ui;

import android.app.Fragment;
import android.content.Context;

import com.joechang.loco.model.Group;

/**
 * Author:  joechang
 * Date:    5/12/15
 * Purpose:
 */
public abstract class GroupSelector {
    /**
     * Not sure this is the best place, but a convenient method to get the groupId from fragments
     * where the activity is a GroupSelector implementer.
     * @param f
     * @return groupId
     */
    public static String getSelectedGroupId(Context f) {
        if (f != null) {
            if (f instanceof Group.GroupSelector) {
                return ((Group.GroupSelector) f).getSelectedGroupId();
            }
        }

        return null;
    }

    public static String getSelectedGroupId(Fragment f) {
        return getSelectedGroupId(f.getActivity());
    }
}
