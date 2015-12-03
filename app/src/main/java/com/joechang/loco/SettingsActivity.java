package com.joechang.loco;

import android.os.Bundle;
import com.joechang.loco.fragment.ProfileFragment;
import com.joechang.loco.fragment.QuickSendSettingsFragment;
import com.joechang.loco.utils.UserInfoStore;

/**
 * Author:  joechang
 * Date:    12/30/14
 * Purpose: Activity holding settings for app, ie quickSend settings ando therwise.
 */
public class SettingsActivity extends BaseDrawerActionBarActivity {

    private UserInfoStore userInfoStore;
    private QuickSendSettingsFragment quickSendFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.userInfoStore = new UserInfoStore(this);

        quickSendFragment = QuickSendSettingsFragment.newInstance();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, quickSendFragment)
                .commit();

    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.SETTINGS;
    }
}


