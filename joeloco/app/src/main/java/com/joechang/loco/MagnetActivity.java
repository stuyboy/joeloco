package com.joechang.loco;

import android.app.Activity;
import android.os.Bundle;
import com.joechang.loco.fragment.ContactMagnetFragment;
import com.joechang.loco.fragment.ProfileFragment;
import com.joechang.loco.model.User;

/**
 * Author:    joechang
 * Created:   6/15/15 12:20 PM
 * Purpose:
 */
public class MagnetActivity extends BaseDrawerActionBarActivity {

    private String mTargetUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTargetUserId = getIntent().getStringExtra(User.ID);

        Bundle bb = new Bundle();
        bb.putString(User.ID, mTargetUserId);
        ContactMagnetFragment cmf = ContactMagnetFragment.newInstance();
        cmf.setArguments(bb);

        //Show the magnetometer.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, cmf)
                .commit();

        //Control navigation from the enum.  Is this a back-only item?
        setNonDrawerBackOnly(true);
    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.NEARBY;
    }
}
