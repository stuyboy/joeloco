package com.joechang.loco;

import android.app.Fragment;
import android.os.Bundle;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.client.UserClient;
import com.joechang.loco.fragment.NearbyChatFragment;
import com.joechang.loco.fragment.NearbyFragment;
import com.joechang.loco.model.User;
import com.joechang.loco.ui.UserSelector;
import com.joechang.loco.utils.UserInfoStore;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class UsersMapActivity extends BaseDrawerActionBarActivity
        implements NearbyChatFragment.FragmentSupplier, User.UserSelector, User.UserListSelector {
    private static Logger log = Logger.getLogger(UsersMapActivity.class.getSimpleName());

    private String mSelectedUserId = "11703047802789244131";    //TEST TEST
    private List<String> mSelectedUsersIds;
    private boolean chatEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatEnabled = UserInfoStore.getInstance(UsersMapActivity.this).isChatEnabled();

        if (savedInstanceState == null) {
            queryForUsersAndCreate();
        } else {
            mSelectedUsersIds = savedInstanceState.getStringArrayList(UserSelector.KEY_USER_LIST);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedUsersIds != null) {
            outState.putStringArrayList(UserSelector.KEY_USER_LIST, new ArrayList<String>(mSelectedUsersIds));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //If user just changed this setting, and is coming back, then restart if they enabled chatWindow.
        if (chatEnabled != getUserInfoStore().isChatEnabled()) {
            restart();
        }
    }

    protected void populateFragments() {
        Fragment f = chatEnabled ?
                NearbyChatFragment.newInstance() :
                getNearbyFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, f)
                .commit();
    }

    protected void queryForUsersAndCreate() {
        UserClient userClient = RestClientFactory.getInstance().getUserClient();
        userClient.getAllUsers(new UserClient.SetCallback() {
            @Override
            public void success(Set<User> users, Response response) {
                List<String> ids = new ArrayList<>();
                for (User u : users) {
                    ids.add(u.getUserId());
                }
                setSelectedUsersIds(ids);
                populateFragments();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.NEARBY;
    }

    @Override
    public NearbyFragment getNearbyFragment() {
        return UsersNearbyFragment.newInstance();
    }

    @Override
    public String getSelectedUserId() {
        return mSelectedUserId;
    }

    @Override
    public void setSelectedUserId(String userId) {
        mSelectedUserId = userId;
    }

    @Override
    public List<String> getSelectedUsersIds() {
        return mSelectedUsersIds;
    }

    @Override
    public void setSelectedUsersIds(List<String> userIds) {
        mSelectedUsersIds = userIds;
    }
}
