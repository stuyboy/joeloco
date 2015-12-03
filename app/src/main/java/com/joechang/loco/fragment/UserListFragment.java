package com.joechang.loco.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.joechang.loco.R;
import com.joechang.loco.avatar.AvatarUrlBuilder;
import com.joechang.loco.model.User;
import com.joechang.loco.ui.UserSelector;
import com.joechang.loco.utils.BitmapUtils;

import java.util.*;

/**
 * Author:    joechang
 * Created:   6/15/15 6:28 PM
 * Purpose:   Quick little fragment that is a listview of users, either by name or avatar.
 */
public class UserListFragment extends Fragment {

    private static final int DEFAULT_AVATAR_PIXELS = 60;

    private int avatarPixels = DEFAULT_AVATAR_PIXELS;

    private String selectedUserId;

    private ListView mListView;
    private Map<String, String> mUserIdsAvatars = new LinkedHashMap<>();
    private Set<ImageView> createdAvatarImages = new HashSet<>();
    private volatile boolean initialized = false;

    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_userlist, container, false);
        mListView = (ListView) v.findViewById(R.id.userListView);

        if (savedInstanceState != null) {
            if (savedInstanceState.getString(User.ID) != null) {
                selectedUserId = savedInstanceState.getString(User.ID);
            }
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    public void refreshListView() {
        //Got to make this neater.
        final String[] avatarUrlArray = mUserIdsAvatars.values().toArray(new String[]{});
        final String[] avatarIdArray = mUserIdsAvatars.keySet().toArray(new String[]{});

        ArrayAdapter<String> aa =
                new ArrayAdapter<String>(getActivity(), R.layout.image_item_simple, avatarUrlArray) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return createAvatarImageView(
                                avatarIdArray[position],
                                avatarUrlArray[position],
                                parent);
                    }
                };
        mListView.setAdapter(aa);
        initialized = true;
    }

    /**
     * Return all images from highlight, to normal.
     */
    protected void resetAvatarImages() {
        for (ImageView v : createdAvatarImages) {
            highlightAvatarImage(v, 1.0);
        }
    }

    /**
     * When clicked, give this avatar image an extra highlight.
     *
     * @param vv
     * @param factor
     */
    protected void highlightAvatarImage(ImageView vv, double factor) {
        ViewGroup.LayoutParams lp = vv.getLayoutParams();
        lp.height = (int) (getAvatarPixels() * factor);
        lp.width = (int) (getAvatarPixels() * factor);
        vv.setLayoutParams(lp);
    }

    protected void highlightAvatarImage(ImageView vv) {
        highlightAvatarImage(vv, 1.25d);
    }

    protected ImageView createAvatarImageView(final String userId, final String url, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ImageView vv = (ImageView) inflater.inflate(R.layout.image_item_simple, parent, false);
        highlightAvatarImage(vv, 1.0d);

        /**
         * When user clicks on an avatar, do the appropriate action.  Highlight them on the map, possibly only chat,
         * move the magnet to point to them?!  Likely needs to register this with the activity so we have a central
         * spot where the user has been chosen.
         * <p/>
         * What is the best way to build the relationship with the activity again?  Assign it in or just assume cast?
         */
        vv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof User.UserSelector) {
                    //How can I highlight this imageview?
                    if (v instanceof ImageView) {
                        //Make selected avatar a little bigger.  How do we reset the rest?
                        resetAvatarImages();
                        highlightAvatarImage((ImageView) v);
                    }

                    //Self save
                    selectedUserId = userId;

                    //Activity actions
                    if (getActivity() instanceof User.UserSelector) {
                        ((User.UserSelector) getActivity()).setSelectedUserId(userId);
                    }
                }
            }
        });

        BitmapUtils.cacheAsyncImage(vv, url);
        createdAvatarImages.add(vv);

        //If currently selected, highlight it.
        if (selectedUserId != null && selectedUserId.equals(userId)) {
            highlightAvatarImage(vv);
        }

        return vv;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (selectedUserId != null) {
            outState.putString(User.ID, selectedUserId);
        }
    }

    public void refresh() {
        List<String> ui = UserSelector.getSelectedUsersIds(this);
        if (ui != null) {
            populateAvatarArray(ui);
            refreshAvatarView();
        }
    }

    protected void populateAvatarArray(List<String> userIds) {
        mUserIdsAvatars.clear();
        for (String userId : userIds) {
            //Get the avatar
            String avatarUrl = AvatarUrlBuilder.circleUrl(userId, getAvatarPixels());
            mUserIdsAvatars.put(userId, avatarUrl);
        }
    }

    protected void refreshAvatarView() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshListView();
                }
            });
        }
    }

    public int getAvatarPixels() {
        return avatarPixels;
    }

    public void setAvatarPixels(int avatarPixels) {
        this.avatarPixels = avatarPixels;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
