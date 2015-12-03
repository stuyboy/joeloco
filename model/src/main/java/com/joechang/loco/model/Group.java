package com.joechang.loco.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author:  joechang
 * Date:    1/30/15
 * Purpose: A group is a collection of users that have banded together for some reason.
 * There can be multiple groups with multiple members, and each user may be a member of many groups.
 */
public class Group implements Serializable {

    private String groupId;
    private String name;
    private Map<String, String> members = new HashMap<String, String>();
    private Map<String, String> administrators = new HashMap<String, String>();

    //Quickly identifies uniqueness of groups based on members.
    private int membersHash;

    public static final String ID = "groupId";

    public Group() {
    }

    public Group(String groupName) {
        this.name = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public void setMembers(Map<String, String> members) {
        this.members = members;
    }

    public int getMembersHash() {
        return membersHash;
    }

    public Map<String, String> getAdministrators() {
        return administrators;
    }

    public void setAdministrators(Map<String, String> administrators) {
        this.administrators = administrators;
    }

    /**
     * Called when group is updated or saved.
     */
    public void updateMembersHash() {
        Set<String> ss = getMembers().keySet();
        this.membersHash = ss.hashCode();
    }

    public interface GroupSelector {
        public String getSelectedGroupId();
        public void setSelectedGroupId(String groupId);
    }
}
