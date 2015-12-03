package com.joechang.loco.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.joechang.loco.R;
import com.joechang.loco.contacts.ContactsUtils;
import com.joechang.loco.firebase.FirebaseForeignKeyAdapter;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.User;
import com.joechang.loco.ui.GroupSelector;

import java.util.List;
import java.util.Map;

/**
 * Author:  joechang
 * Date:    1/29/15
 * Purpose: GroupsFragment lists all the users within a group, and has a facility to drop down
 * all groups available to this user
 * TODO: When the group is deleted, or users are changed, we need to update the screen in real-time.
 */
public class GroupMembersFragment extends AbstractUserFragment
        implements View.OnClickListener, AsyncLoadingFragment {

    private FirebaseForeignKeyAdapter adapter;
    private Map<String, String> groupMap;
    private ReadyAction postLoadAction;

    public GroupMembersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param groupMap Pass in the group names.  Maybe a map in the future?
     * @return A new instance of fragment GroupsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupMembersFragment newInstance(Map<String, String> groupMap) {
        GroupMembersFragment fragment = new GroupMembersFragment();
        fragment.groupMap = groupMap;
        return fragment;
    }

    public Map<String, String> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(Map<String, String> groupMap) {
        this.groupMap = groupMap;
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    public boolean refresh() {
        //Redraw the list based on likely a new group selected.
        if (GroupSelector.getSelectedGroupId(this) != null) {
            populateGroupsListView(GroupSelector.getSelectedGroupId(this));
            return true;
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_groups_members, container, false);

        Button c = (Button) v.findViewById(R.id.groups_add);
        c.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.groups_add:
                //doLaunchContactPicker();
                doLaunchInterimUserPicker();
                break;
        }
    }

    private void deleteGroup(String groupId) {
        FirebaseManager.getInstance().deleteGroup(groupId);
        this.groupMap.remove(groupId);
        //populateSpinner(getActivity());
    }

    /**
     * Quickly get a list of all users and put into dialog box with multiselect *
     */
    public void doLaunchInterimUserPicker() {
        final String groupId = GroupSelector.getSelectedGroupId(this);

        //Existing users
        if (groupId != null) {
            Firebase ff = FirebaseManager.getInstance().getGroupMembersFirebase(groupId);
            ff.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Map<String, String> existingUsers = (Map<String, String>) dataSnapshot.getValue();
                    FirebaseManager.getInstance().findUsers(new PostQueryAction<List<User>>() {
                        @Override
                        public void doAction(List<User> p) {
                            //shit so this gets called anytime we add anything new!!!
                            ListSelectDialogFragment lsdf = ListSelectDialogFragment.getInstance(groupId, p);
                            if (existingUsers != null) {
                                lsdf.setExistingUsers(existingUsers.keySet());
                            }
                            lsdf.show(getActivity().getFragmentManager(), "addUserFragment");
                        }

                        @Override
                        public void onError(List<User> p) {

                        }
                    });
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    /**
     * This lives with the next method.  No currently utilized.
     */
    public void doLaunchContactPicker() {
        Intent contactPickerIntent = ContactsUtils.intentForPicker();
        startActivityForResult(contactPickerIntent, ContactsUtils.ADD_CONTACT_TO_GROUP);
    }

    /**
     * Overriden here only when we utilize the contacts intent to do our dirty work.  For now,
     * we just query the list of users from the database.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ContactsUtils.ADD_CONTACT_TO_GROUP:
                if (resultCode == Activity.RESULT_OK) {
                    ContactsUtils.retrieveEmails(getActivity(), data);
                }
                break;
        }
    }

    /**
     * Based on selected drop-down item, populate the list of users in that group
     *
     * @param groupId
     */
    private void populateGroupsListView(String groupId) {
        Activity a = getActivity();

        if (a == null) {
            return;
        }

        //Retrieve group and list people in the group.
        this.adapter = new FirebaseForeignKeyAdapter(
                FirebaseManager.getInstance().getGroupMembersFirebase(groupId),
                android.R.layout.simple_list_item_1,
                a,
                a.getLayoutInflater()
        );

        final ListView lv = (ListView) a.findViewById(R.id.groups_listView);
        if (lv != null) {
            lv.setAdapter(adapter);
            //When finished loading, this will get called.
            this.adapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    postLoadAction.doAction(lv.getId());
                }
            });
        }
    }

    @Override
    public ReadyAction getOnFinishedLoadingAction() {
        return postLoadAction;
    }

    @Override
    public void onFinishedLoading(ReadyAction ra) {
        postLoadAction = ra;
    }
}
