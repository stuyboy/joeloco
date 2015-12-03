package com.joechang.loco;

import android.content.Intent;
import android.os.Bundle;

import com.joechang.loco.fragment.ProfileFragment;
import com.joechang.loco.model.User;

/**
 * Author:  joechang
 * Date:    12/30/14
 * Purpose: Activity holding profile setting fragments.
 */
public class ProfileActivity extends BaseDrawerActionBarActivity implements User.LogoutProvider {

    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, ProfileFragment.newInstance())
                .commit();
    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.PROFILE;
    }

    public void logout() {
        getUserInfoStore().clearLoggedInUser();
        Intent loginIntent = new Intent(this, LoginActivity.class);

        //clear the backstack
        loginIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.finish();
        startActivity(loginIntent);
    }

}


