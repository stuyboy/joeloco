package com.joechang.loco.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.joechang.loco.ChooseContactActivity;
import com.joechang.loco.R;
import com.joechang.loco.service.SendLocationService;

/**
 * Author:  joechang
 * Date:    3/6/15
 * Purpose: Quick fragment that is used to interact with LocationPublishService, outputting onscreen.
 */
public class ShareNowFragment extends Fragment implements View.OnClickListener {

    private NearbyFragment mNearbyFragment = NearbyFragment.newInstance();

    // TODO: Rename and change types and number of parameters
    public static ShareNowFragment newInstance() {
        ShareNowFragment fragment = new ShareNowFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sharenow, container, false);

        getFragmentManager().beginTransaction()
                .add(R.id.sharenow_content_frame, mNearbyFragment)
                .commit();

        Button b = (Button) v.findViewById(R.id.button_sharenow);
        b.setOnClickListener(this);

        return v;
    }

    public void onClick(View v) {
        //Only one button, so not checking id.
        Intent i = new Intent(getActivity(), ChooseContactActivity.class);
        startActivityForResult(i, ChooseContactActivity.CHOOSE_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (ChooseContactActivity.CHOOSE_CONTACT == requestCode) {
                SendLocationService.launchIntent(getActivity(), data);
            }
        }
    }
}
