package com.joechang.loco.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.joechang.loco.R;

import java.util.Map;

/**
 * Author:  joechang
 * Date:    3/4/15
 * Purpose: Little adapter for the spinner at the top of the actionBar, lists all the groups
 * that a user may belong to.
 */
public class GroupsSpinnerAdapter extends ArrayAdapter<GroupsSpinnerAdapter.GroupSpinnerEntry> implements SpinnerAdapter {
    private GroupSpinnerEntry[] entries;

    public GroupsSpinnerAdapter(Context c, Map<String, String> groupMap) {
        super(c, R.layout.widget_spinner_item);
        this.addAll(gseArray(groupMap));
    }

    private GroupSpinnerEntry[] gseArray(Map<String, String> m) {
        this.entries = new GroupSpinnerEntry[m.size()];
        int i = 0;
        for (Map.Entry<String, String> e : m.entrySet()) {
            this.entries[i] = new GroupSpinnerEntry(e.getKey(), e.getValue());
            i++;
        }
        return this.entries;
    }

    /**
     * Based on the array index, return back the groupId
     * @param i the index which is 0-based
     */
    public String getGroupIdForIndex(int i) {
        if (this.entries != null) {
            return this.entries[i].groupId;
        }

        return null;
    }

    public int getIndexForGroupId(String groupId) {
        if (this.entries != null) {
            for (int i=0; i<this.entries.length; i++) {
                if (this.entries[i].groupId.equals(groupId)) {
                    return i;
                }
            }
        }
        return 0;
    }

    protected class GroupSpinnerEntry {
        private String groupName;
        private String groupId;

        GroupSpinnerEntry(String id, String name) {
            this.groupId = id;
            this.groupName = name;
        }

        @Override
        public String toString() {
            return groupName;
        }
    }
}
