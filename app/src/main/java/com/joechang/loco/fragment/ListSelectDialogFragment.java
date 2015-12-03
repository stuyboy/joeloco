package com.joechang.loco.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.joechang.loco.R;
import com.joechang.loco.model.User;
import com.joechang.loco.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author:  joechang
 * Date:    2/9/15
 * Purpose:
 */
public class ListSelectDialogFragment extends DialogFragment {

    private String mGroupId;
    private List<User> mUsers = new ArrayList<User>();              //All users
    private Set<String> mExistingUsers = new HashSet<String>();       //Ones in the group already
    private Set<String> mRemoveUsers = new HashSet<String>();    //Ones to remove from group
    private ListSelectDialogListener mListener;

    public interface ListSelectDialogListener {
        public void onDialogSave(String groupId, Map<String, String> selectedUsers, Map<String, String> removedUsers);
        public void onDialogCancel(String groupId);
    }

    /**
     * Create a dialog and pass in all the possible users
     * @param users all possible, good enough for now
     * @return an instance
     */
    public static ListSelectDialogFragment getInstance(String groupId, List<User> users) {
        ListSelectDialogFragment ls = new ListSelectDialogFragment();
        ls.mGroupId = groupId;
        ls.mUsers = users;
        return ls;
    }

    public ListSelectDialogFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            mListener = (ListSelectDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ListSelectDialogListener");
        }
    }

    /**
     * Set the existing users so that we can put a check-mark next the ones that are already part.
     * We wan't our own copy that we can manipulate.
     * @param existingUserIds
     */
    public void setExistingUsers(Set<String> existingUserIds) {
        this.mExistingUsers.clear();
        this.mExistingUsers.addAll(existingUserIds);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Map<User, Boolean> existingUserCheck = getUsers();
        String[] uArray = ArrayUtils.toStringArray(existingUserCheck.keySet());
        boolean[] bArray = ArrayUtils.toPrimitiveArray(existingUserCheck.values());

        builder.setTitle(R.string.title_users)
                //.setMessage(R.string.dialog_enter_new)
                .setMultiChoiceItems(
                        uArray,
                        bArray,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                //Toast.makeText(getActivity(), "You chose " + which, Toast.LENGTH_LONG).show();
                                if (isChecked) {
                                    ListSelectDialogFragment.this.mExistingUsers.add(
                                            ListSelectDialogFragment.this.mUsers.get(which).getUserId());
                                } else {
                                    String toRemove = ListSelectDialogFragment.this.mUsers.get(which).getUserId();
                                    ListSelectDialogFragment.this.mRemoveUsers.add(toRemove);
                                    ListSelectDialogFragment.this.mExistingUsers.remove(toRemove);
                                }
                            }
                        })
                .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ListSelectDialogFragment.this.mListener.onDialogSave(
                                ListSelectDialogFragment.this.mGroupId,
                                ListSelectDialogFragment.this.getSelectedUsers(),
                                ListSelectDialogFragment.this.getRemovedUsers());
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ListSelectDialogFragment.this.mListener.onDialogCancel(
                                ListSelectDialogFragment.this.mGroupId);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    /**
     * Return a map of the usernames and a boolean whether they should be selected, based on existing
     * users.  damn, usernames collide!!!
     * @return
     */
    private Map<User, Boolean> getUsers() {
        Map<User, Boolean> ret = new LinkedHashMap<User, Boolean>();
        if (this.mUsers != null) {
            for (User u : this.mUsers) {
                boolean check = false;
                if (this.mExistingUsers != null) {
                    check = this.mExistingUsers.contains(u.getUserId());
                }
                ret.put(u, check);
            }
        }
        return ret;
    }

    private Map<String, String> getSelectedUsers() {
        Map<String, String> rMap = new LinkedHashMap<String, String>();
        for (User u : this.mUsers) {
            if (this.mExistingUsers.contains(u.getUserId())) {
                rMap.put(u.getUserId(), u.getFullname());
            }
        }
        return rMap;
    }

    private Map<String, String> getRemovedUsers() {
        Map<String, String> rMap = new LinkedHashMap<String, String>();
        for (User u : this.mUsers) {
            if (this.mRemoveUsers.contains(u.getUserId())) {
                rMap.put(u.getUserId(), u.getFullname());
            }
        }
        return rMap;
    }

}
