package com.joechang.loco.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.*;
import com.joechang.loco.R;
import com.joechang.loco.model.ChatSession;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Author:    joechang
 * Created:   5/28/15 2:30 PM
 * Purpose:   The current main screen for an event or group.  Map is shown, and chat screen at bottom.
 */
public class NearbyChatFragment extends Fragment {

    private ChatFragment mChatFragment;
    private NearbyFragment mNearbyFragment;
    private FragmentSupplier mFragmentSupplier;

    public static NearbyChatFragment newInstance() {
        return new NearbyChatFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mFragmentSupplier = (FragmentSupplier)activity;
        } catch (ClassCastException e) {
            throw e;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.generic_sliding_drawer_layout, container, false);

        String chatId = "openChat";

        //This logic could be moved out to API
        if (getArguments() != null) {
            String eventId = getArguments().getString(Event.ID);
            String groupId = getArguments().getString(Group.ID);

            if (groupId != null) {
                chatId = groupId;
            }

            if (eventId != null) {
                chatId = eventId;
            }
        }

        if (savedInstanceState == null) {
            Bundle bb = new Bundle();
            bb.putString(ChatSession.ID, chatId);
            mChatFragment = ChatFragment.newInstance();
            mChatFragment.setArguments(bb);

            mNearbyFragment = mFragmentSupplier.getNearbyFragment();

            getFragmentManager().beginTransaction()
                    .add(R.id.map_frame, mNearbyFragment)
                    .add(R.id.chat_frame, mChatFragment, "NearbyChatFragmentChat")
                    .commit();
        }

        //Although fragment rebuilds its UI pieces on rotation, doesn't rebuild the listeners.
        setupSlideFrame(v);

        return v;
    }

    protected void setupSlideFrame(View v) {
        SlidingUpPanelLayout spl = (SlidingUpPanelLayout)v.findViewById(R.id.sliding_layout);
        spl.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                hackBottomFiller(v < 0.60f && v > .25f);
                rotateArrow(view, v * 180f);
            }

            @Override
            public void onPanelCollapsed(View view) {
                rotateArrow(view, 0);
            }

            @Override
            public void onPanelExpanded(View view) {
                hackBottomFiller(false);
                rotateArrow(view, 180);
            }

            @Override
            public void onPanelAnchored(View view) {
                hackBottomFiller(true);
                rotateArrow(view, 90);
            }

            @Override
            public void onPanelHidden(View view) {

            }
        });
    }

    private void rotateArrow(View view, float rot) {
        ImageView tv = (ImageView)view.findViewById(R.id.flingArrow);
        tv.setRotation(rot);
    }

    private void hackBottomFiller(boolean b) {
        if (mChatFragment == null) {
            mChatFragment = (ChatFragment)getFragmentManager().findFragmentByTag("NearbyChatFragmentChat");
        }

        if (mChatFragment != null) {
            mChatFragment.doBottomFillerHack(b);
        }
    }

    public interface FragmentSupplier {
        public NearbyFragment getNearbyFragment();
    }
}
