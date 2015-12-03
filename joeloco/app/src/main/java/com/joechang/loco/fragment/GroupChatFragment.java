package com.joechang.loco.fragment;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.joechang.loco.R;
import com.joechang.loco.model.ChatSession;
import com.joechang.loco.ui.GroupSelector;

/**
 * Author:    joechang
 * Created:   5/29/15 5:49 PM
 * Purpose:   Dedicated to using the groupId as the chatsessionid!
 */
public class GroupChatFragment extends ChatFragment implements AsyncLoadingFragment {
    private ReadyAction readyAction;

    public static GroupChatFragment newInstance() {
        GroupChatFragment f = new GroupChatFragment();
        return f;
    }

    @Override
    protected String findChatId() {
        String groupId = GroupSelector.getSelectedGroupId(this);

        if (groupId == null) {
            throw new IllegalArgumentException("GroupChatFragment not being used within Group Activity");
        }

        return groupId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        View flingBar = v.findViewById(R.id.flingBar);
        flingBar.setVisibility(View.GONE);
        return v;
    }

    @Override
    public ReadyAction getOnFinishedLoadingAction() {
        return readyAction;
    }

    @Override
    public void onFinishedLoading(ReadyAction ra) {
        this.readyAction = ra;
    }

    @Override
    protected ListAdapter getAdapter() {
        ListAdapter la = super.getAdapter();

        la.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (getOnFinishedLoadingAction() != null) {
                    getOnFinishedLoadingAction().doAction(0);
                }
            }
        });

        return la;
    }
}