package com.joechang.loco.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joechang.loco.R;

/**
 * Author:  joechang
 * Date:    3/6/15
 * Purpose: Quick fragment that is used to interact with LocationPublishService, outputting onscreen.
 */
public class LocationDebugFragment extends Fragment{

    // TODO: Rename and change types and number of parameters
    public static LocationDebugFragment newInstance() {
        LocationDebugFragment fragment = new LocationDebugFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lps_debug, container, false);
        return v;
    }

}
