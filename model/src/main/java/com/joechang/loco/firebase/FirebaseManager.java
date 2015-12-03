package com.joechang.loco.firebase;

import com.firebase.client.*;
import com.joechang.loco.model.*;
import com.joechang.loco.utils.AddressUtils;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by joechang on 11/29/14.
 * This is an enum to try to make it a threadsafe singleton.  May change in the future.
 */
public class FirebaseManager {

    private static final Logger LOGGER = Logger.getLogger(FirebaseManager.class.getSimpleName());

    protected static final String FIREBASE_URL = "https://joeloco.firebaseio.com/";
    private String GEOFIRE_NAME = "rtLocations";

    private static String IMAGEUPLOAD = "imageUpload";
    private static String GROUPS = "groups";
    private static String USER = "users";
    private static String MEMBERS = "members";
    private static String EVENTS = "events";
    private static String CHATS = "chats";
    private static String MESSAGES = "messages";
    private static String NOTIFICATIONS = "notifications";

    private Firebase fb;
    private GeoFire gf;

    private static String authToken = "OAK57XvJoJPNx4oIiaQmY1BAYEXyD6tURKwvkiYG";

    protected static volatile boolean initialized = false;

    //Required as a way to make this lazy initialized, as we need to call Firebase.setAndroidContext first.
    private static class FirebaseManagerInit {
        public static final FirebaseManager instance = new FirebaseManager();
    }

    public FirebaseManager() {
        try {
            this.fb = new Firebase(FIREBASE_URL);
            this.gf = new GeoFire(this.fb.child(GEOFIRE_NAME));
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public static FirebaseManager getInstance() {
        if (!initialized) {
            throw new IllegalStateException("FirebaseManager needs to init()");
        }

        return FirebaseManagerInit.instance;
    }

    public static synchronized void init() {
        //Place any intialization code before this.
        initialized = true;

        if (FirebaseManager.getInstance() == null ||
                FirebaseManager.getInstance().fb == null ||
                FirebaseManager.getInstance().gf == null) {
            throw new IllegalStateException("FirebaseManager is null? How can this be?");
        }

        FirebaseManager.getInstance().performAuth();
    }

    public synchronized void performAuth() {
        this.fb.authWithCustomToken(getAuthToken(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                LOGGER.info("Authenticated with firebase.");
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                initialized = false;
                throw new RuntimeException("Unable to auth firebase");
            }
        });
    }

    private static String getAuthToken() {
        return authToken;
    }

    //** FIREBASE IMAGE METHODS **//

    public Firebase getImageUploadFirebase() {
        return this.fb.child(IMAGEUPLOAD);
    }

    public GeoFire getGeoFire() {
        return this.gf;
    }

    public String uploadImage(ImageUpload i) {
        Firebase pushRef = getImageUploadFirebase().push();
        pushRef.setValue(i, PostWriteAction.defaultInstance(i));
        return pushRef.getKey();
    }

    //** USER FIREBASE METHODS **//

    public Firebase getUserFirebase() {
        return this.fb.child(USER);
    }

    public Firebase getUserFirebase(String userId) {
        return this.fb.child(USER).child(userId);
    }

    public Firebase getGroupFirebase() {
        return this.fb.child(GROUPS);
    }

    public Firebase getGroupFirebase(String groupId) {
        return this.fb.child(GROUPS).child(groupId);
    }

    public Firebase getGroupMembersFirebase(String groupId) {
        return this.fb.child(GROUPS).child(groupId).child(MEMBERS);
    }

    public Firebase getEventFirebase() {
        return this.fb.child(EVENTS);
    }

    public Firebase getEventFirebase(String eventId) {
        return this.fb.child(EVENTS).child(eventId);
    }

    public Firebase getChatFirebase() {
        return this.fb.child(CHATS);
    }

    public Firebase getChatFirebase(String chatId) {
        return this.fb.child(CHATS).child(chatId);
    }

    public Firebase getChatMessagesFirebase(String chatId) {
        return this.fb.child(CHATS).child(chatId).child(MESSAGES);
    }

    public Firebase getNotificationsFirebase() {
        return this.fb.child(NOTIFICATIONS);
    }

    /**
     * Add a user to the firebase user endpoint.  Record name, the unique ID, etc.
     *
     * @param newUser
     * @return the ID assigned to the user.
     */
    public String addUser(User newUser, final PostWriteAction pwa) {
        Firebase fb = getUserFirebase();
        if (newUser == null || newUser.getUserId() == null) {
            throw new RuntimeException("Adding user cannot be null");
        }
        Firebase newFb = fb.child(newUser.getUserId());
        pwa.setObject(newUser);
        newFb.setValue(newUser, newUser.getUsername(), pwa);
        return newFb.getKey();
    }

    public void findUsers(final PostQueryAction<List<User>> pqa) {
        getUserFirebase().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> ret = new ArrayList<User>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ret.add(ds.getValue(User.class));
                }
                pqa.doAction(ret);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void findUserById(String userId, final PostQueryAction<User> pqa) {
        getUserFirebase(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            pqa.doAction(dataSnapshot.getValue(User.class));
                            return;
                        }

                        pqa.doAction(null);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        LOGGER.info(firebaseError.toString());
                        pqa.onError(null);
                    }
                }
        );
    }

    public void findUserByEmail(String email, final PostQueryAction<User> pqa) {
        getUserFirebase()
                .startAt(email)
                .endAt(email)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    pqa.doAction(dataSnapshot.getValue(User.class));
                                }
                                pqa.doAction(null);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                LOGGER.info(firebaseError.toString());
                                pqa.onError(null);
                            }
                        }
                );
    }

    public void findUserByPhoneNumber(String phoneNumber, final PostQueryAction<User> pqa) {
        String cleaned = AddressUtils.cleanPhoneNumber(phoneNumber);
        Query q = getUserFirebase().orderByChild("phoneNumber").equalTo(cleaned).limitToFirst(1);
        q.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User u = null;
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            u = (dataSnapshot.getChildren().iterator().next().getValue(User.class));
                        }
                        pqa.doAction(u);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        LOGGER.info(firebaseError.toString());
                        pqa.onError(null);
                    }
                }
        );
    }

    public void findGroupById(String id, final PostQueryAction<Group> pqa) {
        getGroupFirebase(id)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                pqa.doAction(dataSnapshot.getValue(Group.class));
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                LOGGER.info(firebaseError.toString());
                                pqa.onError(null);
                            }
                        }
                );
    }

    public void addGroup(final String userId, final Group group, final PostWriteAction pwa) {
        Firebase fb = getGroupFirebase();
        if (userId == null || group == null) {
            throw new RuntimeException("Adding group missing information.");
        }

        Firebase newPushRef = fb.push();
        group.setGroupId(newPushRef.getKey());
        newPushRef.setValue(group, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                addUserToGroup(userId, group.getGroupId(), true);
                pwa.doAction(group);
            }
        });
    }

    public void addUserToGroup(final String userId, final String groupId, final boolean admin) {
        findUserById(userId, new PostQueryAction<User>() {
            @Override
            public void doAction(User p) {
                Map<String, String> j = new HashMap<String, String>();
                j.put(p.getUserId(), p.getFullname());
                editGroupUsers(groupId, j, null, admin);
            }

            @Override
            public void onError(User p) {

            }
        });
    }

    /**
     * Add and Remove users from the group
     *
     * @param groupId     - the groupId to manipulate
     * @param addUsers    map of userids and full names to ADD, null is ok
     * @param removeUsers map of userids and full names to REMOVE, null is ok
     * @param admin       whether to add or remove these users from group admins, if they happen to be.
     */
    public void editGroupUsers(
            final String groupId,
            Map<String, String> addUsers,
            Map<String, String> removeUsers,
            final boolean admin) {

        //Handle null case.
        final Map<String, String> addUsersCopy =
                (addUsers == null) ? new HashMap<String, String>() : addUsers;
        final Map<String, String> removeUsersCopy =
                (removeUsers == null) ? new HashMap<String, String>() : removeUsers;

        final Map<String, String> all = new HashMap<String, String>();
        all.putAll(addUsersCopy);
        all.putAll(removeUsersCopy);

        findGroupById(groupId, new PostQueryAction<Group>() {
            @Override
            public void doAction(final Group g) {
                g.setGroupId(groupId);
                for (final String userId : all.keySet()) {
                    findUserById(userId, new PostQueryAction<User>() {
                        @Override
                        public void doAction(User u) {
                            if (addUsersCopy.containsKey(userId)) {
                                u.getGroups().put(groupId, g.getName());
                            } else if (removeUsersCopy.containsKey(userId)) {
                                u.getGroups().remove(groupId);
                            }
                            updateUser(u);
                        }

                        @Override
                        public void onError(User u) {

                        }
                    });
                }

                //Removal of users from group copy
                for (String userId : removeUsersCopy.keySet()) {
                    if (admin) {
                        g.getAdministrators().remove(userId);
                    }
                    g.getMembers().remove(userId);
                }

                //Addition of users to group copy
                if (admin) {
                    g.getAdministrators().putAll(addUsersCopy);
                }
                g.getMembers().putAll(addUsersCopy);
                updateGroup(g);
            }

            @Override
            public void onError(Group g) {

            }
        });
    }

    /**
     * For some userId, return all the groups ids and names that he/she is a part of.
     */
    public void findGroupIdsForUser(String userId, final PostQueryAction<Map<String, String>> gqa) {
        findUserById(userId, new PostQueryAction<User>() {
            @Override
            public void doAction(User p) {
                if (p == null) {
                    gqa.doAction(null);
                    return;
                }

                gqa.doAction(p.getGroups());
            }

            @Override
            public void onError(User p) {
                LOGGER.info("Error occurred getting groups for user");
            }
        });
    }

    /**
     * From a list of userids, find a group that has all those members in it, and only those members
     * This is particularly useful for locating groups of "one" or "two".
     *
     * @param userIds
     * @param gqa
     */
    public void findGroupFromUsers(Set<String> userIds, final PostQueryAction<Group> gqa) {
        int hashCode = userIds.hashCode();
        Query qf = getGroupFirebase().orderByChild("membersHash").equalTo(hashCode);
        qf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group foundGroup = null;
                if (dataSnapshot.exists()) {
                    foundGroup = dataSnapshot.getChildren().iterator().next().getValue(Group.class);
                }
                gqa.doAction(foundGroup);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void findGroupsWithUser(String userId, final PostQueryAction<Collection<Group>> gqa) {
        Query qf = getGroupFirebase().orderByChild("members").equalTo(userId);
        qf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Collection<Group> gCol = new HashSet<Group>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        gCol.add(ds.getValue(Group.class));
                    }
                    gqa.doAction(gCol);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void findUserEvents(String userId, final PostQueryAction<Collection<Event>> eqa) {
        //Get all the user groups, and then find the events with those groupIds.  Do a multiple join via counter.
        findGroupIdsForUser(userId, new PostQueryAction<Map<String, String>>() {
            @Override
            public void doAction(Map<String, String> p) {
                if (p == null) {
                    eqa.doAction(null);
                    return;
                }

                //Keep track of threaded return.  GroupId:#ofEvents
                final Map<String, Long> completionMap = new HashMap<String, Long>();
                final int expectedCount = p.size();
                final Collection<Event> returnEvents = new ArrayList<Event>();

                for (final String groupId : p.keySet()) {
                    getEventFirebase().orderByChild(Group.ID).equalTo(groupId).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    completionMap.put(groupId, dataSnapshot.getChildrenCount());
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            returnEvents.add(child.getValue(Event.class));
                                        }
                                    }
                                    if (completionMap.size() >= expectedCount) {
                                        eqa.doAction(returnEvents);
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    completionMap.put(groupId, null);
                                }
                            });
                }
            }

            @Override
            public void onError(Map<String, String> p) {
                //what to do?
            }
        });
    }

    public void updateUserLastLogin(String userId) {
        getUserFirebase().child(userId).child("lastLoginTime").setValue(System.currentTimeMillis());
    }

    public void updateUser(User u) {
        getUserFirebase(u.getUserId()).setValue(u);
    }

    public void updateGroup(Group g) {
        g.updateMembersHash();
        getGroupFirebase(g.getGroupId()).setValue(g);
    }

    //Not only get rid of the group, but remove the entry from all the users in the group.
    public void deleteGroup(final String groupId) {
        findGroupById(groupId, new PostQueryAction<Group>() {
            @Override
            public void doAction(Group p) {
                if (p != null && p.getMembers() != null) {
                    for (Map.Entry<String, String> me : p.getMembers().entrySet()) {
                        final String userId = me.getKey();
                        findUserById(userId, new PostQueryAction<User>() {
                            @Override
                            public void doAction(User p) {
                                p.getGroups().remove(groupId);
                                updateUser(p);
                            }

                            @Override
                            public void onError(User p) {

                            }
                        });
                    }
                    getGroupFirebase(groupId).removeValue();
                }
            }

            @Override
            public void onError(Group p) {
                //what to do with these?
            }
        });
    }

    public void addEvent(final Event e, final PostWriteAction pwa) {
        Firebase fb = getEventFirebase();

        if (e.getEventId() == null) {
            Firebase newPushRef = fb.push();
            e.setEventId(newPushRef.getKey());
            newPushRef.setValue(e, pwa);
        } else {
            getEventFirebase(e.getEventId()).setValue(e, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    pwa.doAction(e);
                }
            });
        }
    }

    public void updateEvent(Event e) {
        getEventFirebase(e.getEventId()).setValue(e);
    }

    public void deleteEvent(Event e) {
        getEventFirebase(e.getEventId()).removeValue();
    }

    public void addChatSession(final String key, final String id, final PostWriteAction<ChatSession> pwa) {
        findChatSession(key, id, new PostQueryAction<ChatSession>() {
            @Override
            public void doAction(ChatSession p) {
                if (p != null) {
                    pwa.setId(p.getChatId());
                    pwa.doAction(p);
                    return;
                }
                ChatSession cs = new ChatSession();
                cs.setCreatedTime(System.currentTimeMillis());

                if (Event.ID.equals(key)) {
                    cs.setEventId(id);
                } else if (Group.ID.equals(key)) {
                    cs.setGroupId(id);
                }

                Firebase ref = getChatFirebase().push();
                cs.setChatId(ref.getKey());
                pwa.setObject(cs);
                pwa.setId(ref.getKey());
                ref.setValue(cs, pwa);
            }

            @Override
            public void onError(ChatSession p) {
                pwa.onError(null);
            }
        });
    }

    public void addMessage(final String chatId, final Message m, final PostWriteAction<Message> pwa) {
        Firebase newMessageRef = getChatFirebase(chatId).child(MESSAGES).push();
        m.setMessageId(newMessageRef.getKey());
        m.setSendTime(System.currentTimeMillis());
        newMessageRef.setValue(m, pwa);
    }

    public void findChatSession(String key, String id, final PostQueryAction<ChatSession> pqc) {
        Query q = getChatFirebase().orderByChild(key).equalTo(id).limitToFirst(1);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    ChatSession cs = dataSnapshot.getChildren().iterator().next().getValue(ChatSession.class);
                    pqc.doAction(cs);
                } else {
                    pqc.doAction(null);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                pqc.onError(null);
            }
        });
    }

    public void addNotification(final Notification n, final PostWriteAction<Notification> pwa) {
        Firebase newNotificationRef = getNotificationsFirebase().push();
        n.setNotificationId(newNotificationRef.getKey());
        newNotificationRef.setValue(n, pwa);
    }
}
