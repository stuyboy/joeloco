package com.joechang.loco.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.joechang.loco.EventEntryActivity;
import com.joechang.loco.R;
import com.joechang.loco.firebase.FirebaseEventsAdapter;
import com.joechang.loco.firebase.FirebaseForeignKeyAdapter;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.PostWriteAction;
import com.joechang.loco.ui.GroupSelector;

import java.util.Date;

/**
 * Author:  joechang
 * Date:    3/6/15
 * Purpose: Quick fragment that helps keep the times that location should be shared.
 */
public class GroupTimesFragment extends Fragment implements AsyncLoadingFragment {

    private ReadyAction postLoadAction;

    // TODO: Rename and change types and number of parameters
    public static GroupTimesFragment newInstance() {
        GroupTimesFragment fragment = new GroupTimesFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_times, container, false);

        Button b = (Button) v.findViewById(R.id.add_event_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.add_event_button) {
                    Intent i = new Intent(getActivity(), EventEntryActivity.class);
                    i.putExtra(Group.ID, GroupSelector.getSelectedGroupId(GroupTimesFragment.this));
                    startActivity(i);
                }
            }
        });

        createListView(v);
        return v;
    }

    public void createListView(View v) {
        final ListView lv = (ListView) v.findViewById(R.id.events_listView);
        ListAdapter fka = getAdapter(GroupSelector.getSelectedGroupId(this));
        fka.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (getOnFinishedLoadingAction() != null) {
                    getOnFinishedLoadingAction().doAction(lv.getId());
                }
            }
        });
        lv.setAdapter(fka);
    }

    public boolean refresh() {
        //Redraw the list based on likely a new group selected.
        if (GroupSelector.getSelectedGroupId(this) != null) {
            ListView lv = (ListView) getActivity().findViewById(R.id.events_listView);
            lv.setAdapter(getAdapter(GroupSelector.getSelectedGroupId(this)));
            return true;
        }

        return false;
    }

    protected ListAdapter getAdapter(String groupId) {
        FirebaseEventsAdapter fka = new FirebaseEventsAdapter(
                FirebaseManager.getInstance().getEventFirebase().orderByChild(Group.ID).equalTo(groupId),
                R.layout.event_item_simple,
                getActivity(),
                getActivity().getLayoutInflater()
        );

        return fka;
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
