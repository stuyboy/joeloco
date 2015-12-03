package com.joechang.loco;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import com.joechang.loco.client.EventClient;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.fragment.GroupNearbyFragment;
import com.joechang.loco.fragment.GroupNearbyRestrictedFragment;
import com.joechang.loco.fragment.NearbyChatFragment;
import com.joechang.loco.fragment.NearbyFragment;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.User;
import com.joechang.loco.utils.UrlUtils;
import com.joechang.loco.utils.UserInfoStore;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Author:  joechang
 * Date:    12/12/14
 * Purpose: Serves as an activity for the map view of the current event or group.  Primary sharing screen that
 * mixes chat with map.
 */
public class RealtimeActivity extends BaseDrawerActionBarActivity implements
        Group.GroupSelector, NearbyChatFragment.FragmentSupplier, User.UserSelector {

    private String selectedUserId;
    private String selectedGroupId;
    private String selectedEventId;

    private NearbyFragment mNearbyFragment;

    private Event selectedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reconcileArguments();

    }

    protected void reconcileArguments() {
        String groupId = null;
        String eventId = null;

        Uri data = getIntent().getData();
        Bundle bb = getIntent().getExtras();

        if (data != null) {
            groupId = UrlUtils.extractGroupId(data);
            eventId = UrlUtils.extractEventId(data);
        } else if (bb != null) {
            groupId = bb.getString(Group.ID);
            eventId = bb.getString(Event.ID);
        }

        determineNearbyFragment(groupId, eventId);
    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.NEARBY;
    }

    private void launch() {
        //Do some validation.
        if ((selectedEventId != null && selectedEvent == null) || (selectedEvent != null && !selectedEvent.isValidNow())) {
            alertExpiration();
        } else if (mNearbyFragment != null) {
            Fragment f;

            if (UserInfoStore.getInstance(this).isChatEnabled()) {
                f = NearbyChatFragment.newInstance();
                Bundle b = new Bundle();
                b.putString(Event.ID, selectedEventId);
                b.putString(Group.ID, selectedGroupId);
                f.setArguments(b);
            } else {
                f = getNearbyFragment();
            }

            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, f)
                    .commit();
        }
    }

    private void alertExpiration() {
        new AlertDialog.Builder(this).setTitle("Event has Expired").setMessage("Event timeframe is not valid")
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        RealtimeActivity.this.finish();
                    }
                }).show();
    }

    @Override
    public NearbyFragment getNearbyFragment() {
        return mNearbyFragment;
    }

    protected void determineNearbyFragment(final String groupId, final String eventId) {
        if (eventId != null) {
            selectedEventId = eventId;

            //when eventId comes in, just get the group associate, async.
            EventClient gc = RestClientFactory.getInstance().getEventClient();
            gc.getEvent(
                    eventId,
                    new EventClient.Callback() {
                        @Override
                        public void success(Event event, Response response) {
                            if (event != null && event.getGroupId() != null) {
                                selectedEvent = event;
                                selectedGroupId = event.getGroupId();
                                mNearbyFragment = GroupNearbyRestrictedFragment.newInstance();

                                //Unfortunate, but needed since we're in a callback, and working with the UI
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        launch();
                                    }
                                });
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    }
            );
        } else if (groupId != null) {
            selectedGroupId = groupId;
            mNearbyFragment = GroupNearbyFragment.newInstance();
            launch();
            return;
        } else {
            mNearbyFragment = NearbyFragment.newInstance();
            launch();
        }
    }

    @Override
    public String getSelectedGroupId() {
        if (selectedGroupId == null) {
            throw new IllegalArgumentException("Could not determine id from URI");
        }

        return selectedGroupId;
    }

    @Override
    public void setSelectedGroupId(String groupId) {
        selectedGroupId = groupId;
    }

    @Override
    public String getSelectedUserId() {
        return selectedUserId;
    }

    @Override
    public void setSelectedUserId(String userId) {
        selectedUserId = userId;
    }
}
