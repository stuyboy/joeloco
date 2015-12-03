package com.joechang.loco;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.firebase.client.DataSnapshot;
import com.joechang.loco.adapter.GroupsSpinnerAdapter;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.client.UserClient;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.firebase.SimpleChildEventListener;
import com.joechang.loco.fragment.*;
import com.joechang.loco.model.*;
import com.joechang.loco.utils.NetworkErrorUtils;
import com.joechang.loco.utils.UserInfoStore;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.Map;

/**
 * Author:  joechang
 * Date:    12/30/14
 * Purpose: Activity holding profile setting fragments.
 */
public class GroupsActivity extends BaseDrawerActionBarActivity implements
        NewEntryDialogFragment.NewEntryDialogListener,
        ListSelectDialogFragment.ListSelectDialogListener,
        ActionBar.OnNavigationListener,
        Group.GroupSelector {

    //For now, keep the index and the array synchronized manually.
    private static final int MEMBERS = 0;
    private static final int PLACES = 1;
    private static final int TIMES = 2;
    private static final int NEARBY = 3;
    private static final int CHAT = 4;

    private PageContainer[] subPages = {
            new PageContainer(MEMBERS, "Members"),
            new PageContainer(PLACES, "Places"),
            new PageContainer(TIMES, "Times"),
            new PageContainer(NEARBY, "Nearby"),
            new PageContainer(CHAT, "Chat")
    };
    private GroupsSpinnerAdapter groupsSpinnerAdapter;
    private String selectedGroupId;
    private int selectedTab;
    private PagerAdapter pagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Too many bugs with orientation switching and Google Maps
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Create the list of groups at the top.
        ActionBar ab = getActionBar();
        ab.setDisplayShowTitleEnabled(false);

        createGroupsSpinner();
        //createGroupFragments();

        //Create the tabs
        setContentView(R.layout.activity_tabbed);

        //When user adds a new group, let's update spinner, select it!
        FirebaseManager.getInstance().getUserFirebase(getUserInfoStore().getUserId()).addChildEventListener(
                new SimpleChildEventListener() {
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        createGroupsSpinner();
                    }
                }
        );
    }

    public PageContainer getPageContainer(int tab) {
        return this.subPages[tab];
    }

    protected void createGroupFragments() {
        createMembersFragment();
        createPlacesFragment();
        createTimesFragment();
        createNearbyFragment();
        createChatFragment();
    }

    protected void createFragmentPager() {
        pagerAdapter = PageContainer.getPagerAdapter(getFragmentManager(), subPages);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(this.pagerAdapter);

        //Swiping to a new tab means we create a new item.
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                GroupsActivity.this.selectedTab = position;
                //createFragments(position);
            }
        });
    }

    protected void createGroupsSpinner() throws IllegalArgumentException {
        UserClient uc = RestClientFactory.getInstance().getUserClient();
        uc.getUser(
                getUserInfoStore().getUserId(),
                new UserClient.Callback() {
                    @Override
                    public void success(User user, Response response) {
                        final Map<String, String> p = user.getGroups();
                        updateElements(p);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        NetworkErrorUtils.handleNetworkError(GroupsActivity.this, error);
                    }
                }
        );
    }

    protected void updateElements(final Map<String, String> p) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (p.isEmpty()) {
                    Toast.makeText(GroupsActivity.this, "No groups configured!", Toast.LENGTH_LONG).show();
                    return;
                }

                GroupsSpinnerAdapter sa = new GroupsSpinnerAdapter(GroupsActivity.this, p);
                GroupsActivity.this.groupsSpinnerAdapter = sa;

                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                getActionBar().setListNavigationCallbacks(
                        sa,
                        GroupsActivity.this
                );

                //If we already have a selected group, then set spinner to that.  Otherwayround.
                if (getSelectedGroupId() == null) {
                    setSelectedGroupId(sa.getGroupIdForIndex(0));
                } else {
                    getActionBar().setSelectedNavigationItem(sa.getIndexForGroupId(getSelectedGroupId()));
                }

                //Only create fragments after we've established we've got a selected group!
                createGroupFragments();
                createFragmentPager();
            }
        });
    }

    /**
     * This can get pretty complicated.  Both the placeholderfragment AND the groupmembersfragment
     * have readyActions, because they both have asynchronous loading events!
     */
    protected void createMembersFragment() {
        final PageContainer members = getPageContainer(MEMBERS);
        if (members.getFragment() == null) {
            final GroupMembersFragment gFrag = new GroupMembersFragment();
            members.setFragment(gFrag);
            gFrag.onFinishedLoading(new AsyncLoadingFragment.ReadyAction() {
                @Override
                public void doAction(int insertViewId) {
                    members.hideLoadingProgressBar();
                }
            });

            members.onFinishedLoading(
                    new AsyncLoadingFragment.ReadyAction() {
                        @Override
                        public void doAction(final int insertViewId) {
                            members.showLoadingProgressBar();
                            FirebaseManager fm = FirebaseManager.getInstance();
                            fm.findGroupIdsForUser(
                                    getUserInfoStore().getUserId(),
                                    new PostQueryAction<Map<String, String>>() {
                                        @Override
                                        public void doAction(Map<String, String> p) {
                                            gFrag.setGroupMap(p);
                                            getFragmentManager().beginTransaction()
                                                    .add(insertViewId, members.getFragment())
                                                    .commit();
                                        }

                                        @Override
                                        public void onError(Map<String, String> p) {

                                        }
                                    }
                            );
                        }
                    });
        }
    }

    protected void createPlacesFragment() {
        final PageContainer places = getPageContainer(PLACES);
        if (places.getFragment() == null) {
            final GroupPlacesFragment gpf = GroupPlacesFragment.newInstance(4);
            places.setFragment(gpf);
            gpf.onFinishedLoading(new AsyncLoadingFragment.ReadyAction() {
                @Override
                public void doAction(int insertViewId) {
                    places.hideLoadingProgressBar();
                }
            });
            places.onFinishedLoading(
                    new AsyncLoadingFragment.ReadyAction() {
                        @Override
                        public void doAction(final int insertViewId) {
                            places.showLoadingProgressBar();
                            getFragmentManager().beginTransaction()
                                    .add(insertViewId, places.getFragment())
                                    .commit();
                        }
                    });
        }
    }

    protected void createTimesFragment() {
        final PageContainer times = getPageContainer(TIMES);
        if (times.getFragment() == null) {
            final GroupTimesFragment gtf = GroupTimesFragment.newInstance();
            times.setFragment(gtf);
            gtf.onFinishedLoading(new AsyncLoadingFragment.ReadyAction() {
                @Override
                public void doAction(int insertViewId) {
                    times.hideLoadingProgressBar();
                }
            });
            times.onFinishedLoading(
                    new AsyncLoadingFragment.ReadyAction() {
                        @Override
                        public void doAction(final int insertViewId) {
                            times.showLoadingProgressBar();
                            getFragmentManager().beginTransaction()
                                    .add(insertViewId, times.getFragment())
                                    .commit();
                        }
                    });
        }
    }

    protected void createNearbyFragment() {
        final PageContainer nearby = getPageContainer(NEARBY);
        if (nearby.getFragment() == null) {
            GroupNearbyRestrictedFragment gnf = GroupNearbyRestrictedFragment.newInstance();
            nearby.setFragment(gnf);
            gnf.onFinishedLoading(
                    new AsyncLoadingFragment.ReadyAction() {
                        @Override
                        public void doAction(int insertViewId) {
                            nearby.hideLoadingProgressBar();
                        }
                    }
            );
            nearby.onFinishedLoading(
                    new AsyncLoadingFragment.ReadyAction() {
                        @Override
                        public void doAction(final int insertViewId) {
                            nearby.showLoadingProgressBar();
                            getFragmentManager().beginTransaction()
                                    .add(insertViewId, nearby.getFragment())
                                    .commit();
                        }
                    }
            );
        }
    }

    protected void createChatFragment() {
        final PageContainer chat = getPageContainer(CHAT);
        if (chat.getFragment() == null) {
            GroupChatFragment cf = GroupChatFragment.newInstance();
            Bundle b = new Bundle();
            b.putString(ChatSession.ID, getSelectedGroupId());
            cf.setArguments(b);
            chat.setFragment(cf);
            cf.onFinishedLoading(
                    new AsyncLoadingFragment.ReadyAction() {
                        @Override
                        public void doAction(int insertViewId) {
                            chat.hideLoadingProgressBar();
                        }
                    }
            );
            chat.onFinishedLoading(
                    new AsyncLoadingFragment.ReadyAction() {
                        @Override
                        public void doAction(int insertViewId) {
                            chat.showLoadingProgressBar();
                            getFragmentManager().beginTransaction()
                                    .add(insertViewId, chat.getFragment())
                                    .commit();
                        }
                    }
            );
        }
    }

    @Override
    public int getActionBarMenu() {
        return R.menu.groups;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_newGroup:
                startNewGroupIntent();
                break;
            case R.id.action_deleteGroup:
                if (getSelectedGroupId() != null) {
                    FirebaseManager.getInstance().deleteGroup(getSelectedGroupId());
                }
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Throw up a dialog where user can enter a new group name *
     */
    public void startNewGroupIntent() {
        NewEntryDialogFragment nedf = new NewEntryDialogFragment();
        nedf.show(getFragmentManager(), "NewGroupFragment");
    }

    @Override
    public void onDialogSave(DialogFragment df) {
        //Save the new group
        EditText et = (EditText) df.getDialog().findViewById(R.id.newDialogEdit);
        Group newGroup = new Group(et.getText().toString());
        FirebaseManager.getInstance().addGroup(
                UserInfoStore.getInstance(this).getUserId(),
                newGroup,
                new PostWriteAction<Group>(newGroup) {
                    @Override
                    public void doAction(Group newGroup) {
                        Toast.makeText(GroupsActivity.this, "Created new group " + newGroup, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Group newGroup) {
                        Toast.makeText(GroupsActivity.this, "Error in creating new group " + newGroup, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public void onDialogCancel(DialogFragment df) {
        //No need for anything.
    }

    @Override
    public void onDialogSave(String groupId, Map<String, String> selectedUsers, Map<String, String> removedUsers) {
        FirebaseManager.getInstance().editGroupUsers(groupId, selectedUsers, removedUsers, false);
    }

    @Override
    public void onDialogCancel(String groupId) {
        //no-op!
    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.GROUPS;
    }

    @Override
    public String getSelectedGroupId() {
        return this.selectedGroupId;
    }

    @Override
    public void setSelectedGroupId(String groupId) {
        this.selectedGroupId = groupId;
    }

    /**
     * For the ActionBar for selecting which group to show.
     *
     * @param i
     * @param l
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        if (this.groupsSpinnerAdapter != null) {
            String id = this.groupsSpinnerAdapter.getGroupIdForIndex(i);
            if (id != null) {
                setSelectedGroupId(id);
                return refreshFragments();
            }
        }
        return false;
    }

    private boolean refreshFragments() {
        GroupMembersFragment gmf = (GroupMembersFragment) getPageContainer(MEMBERS).getFragment();
        GroupPlacesFragment gpf = (GroupPlacesFragment) getPageContainer(PLACES).getFragment();
        GroupNearbyFragment gnf = (GroupNearbyFragment) getPageContainer(NEARBY).getFragment();
        GroupTimesFragment gtf = (GroupTimesFragment) getPageContainer(TIMES).getFragment();
        GroupChatFragment gcf = (GroupChatFragment) getPageContainer(CHAT).getFragment();

        return (gmf != null && gmf.refresh()) &&
                (gpf != null && gpf.refresh()) &&
                (gnf != null && gnf.refresh()) &&
                (gtf != null && gtf.refresh()) &&
                (gcf != null && gcf.refresh());
    }
}


