package com.joechang.loco.model;

import java.io.Serializable;
import java.util.*;

/**
 * Author:  joechang
 * Date:    12/23/14
 * Purpose: Generic pojo for users in the joelo.co system
 */
public class User implements Serializable {

    public static final String ID = "userId";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String TOKEN_ID = "gitToken";

    private String username;
    private String userId;
    private String fullname;
    private String photoUrl;
    private String phoneNumber;
    private long lastLoginTime;
    private long createdTime;
    private long lastUpdated;

    //Index of groups this user belongs to, key is groupId, value is groupName
    private Map<String, String> groups = new HashMap<String, String>();

    //List of notifications or acknowledgements, and timestamp occurred.
    private Map<String, Long> milestones = new HashMap<String, Long>();

    //Required
    public User() {}

    public User(String username, String userId) {
        this.username = username;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, String> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, String> groups) {
        this.groups = groups;
    }

    public Map<String, Long> getMilestones() {
        return milestones;
    }

    public void setMilestones(Map<String, Long> milestones) {
        this.milestones = milestones;
    }

    @Override
    public String toString() {
        return fullname;
    }

    public interface LogoutProvider {
        public void logout();
    }

    public interface UserSelector {
        public String getSelectedUserId();
        public void setSelectedUserId(String userId);
    }

    public interface UserListSelector {
        public List<String> getSelectedUsersIds();
        public void setSelectedUsersIds(List<String> userIds);
    }
}
