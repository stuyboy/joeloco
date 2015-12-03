package com.joechang.loco.config;

import com.joechang.loco.model.ChatSession;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.User;

/**
 * Created by joechang on 5/13/15.
 * Small class with the string literals that translate into REST API Calls.
 */
public class Routes {

    //PRIMARY
    public static final String EVENT_RESOURCE = "/events";
    public static final String USER_RESOURCE = "/users";
    public static final String CHAT_RESOURCE = "/chats";
    public static final String GROUP_RESOURCE = "/groups";

    //Chat
    public static final String CHAT_BY_ID = CHAT_RESOURCE + "/{" + ChatSession.ID + "}";
    public static final String CHAT_MESSAGES = CHAT_BY_ID + "/messages";

    //Users
    public static final String USER_AUTH = USER_RESOURCE + "/auth";
    public static final String USERS_ALL = USER_RESOURCE + "/all";
    public static final String USER_BY_ID = USER_RESOURCE + "/{" + User.ID + "}";
    public static final String EVENTS_FOR_USER = USER_BY_ID + EVENT_RESOURCE;
    public static final String USER_LOCATION = USER_BY_ID + "/location";
    public static final String USER_AVATAR = USER_BY_ID + "/avatar";
    public static final String USER_MAP_POINTER = USER_BY_ID + "/mapPointer";

    //Groups
    public static final String GROUP_BY_ID = GROUP_RESOURCE + "/{" + Group.ID + "}";

    //Events
    public static final String EVENT_BY_ID = EVENT_RESOURCE + "/{" + Event.ID + "}";
    public static final String EVENT_USERS = EVENT_BY_ID + USER_RESOURCE;

    //Third Party Search
    public static final String YELP_SEARCH = "/yelp";
    public static final String OPENTABLE_SEARCH = "/opentable";

    //Redirect
    public static final String OPENTABLE_REDIRECT = "/o/";
}
