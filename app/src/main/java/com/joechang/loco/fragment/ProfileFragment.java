package com.joechang.loco.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.joechang.loco.R;
import com.joechang.loco.avatar.AvatarUrlBuilder;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.client.UserClient;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.User;
import com.joechang.loco.utils.BitmapUtils;
import com.joechang.loco.utils.NetworkErrorUtils;
import com.joechang.loco.utils.UserInfoStore;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends AbstractUserFragment implements View.OnClickListener {

    private User mUser;

    public static ProfileFragment newInstance() {
        ProfileFragment p = new ProfileFragment();
        return p;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        Button b = (Button)v.findViewById(R.id.profile_button);
        b.setOnClickListener(this);

        Button c = (Button)v.findViewById(R.id.logout_button);
        c.setOnClickListener(this);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getUserId() != null) {
            UserClient uc = RestClientFactory.getInstance().getUserClient();
            uc.getUser(getUserId(), new UserClient.Callback() {
                @Override
                public void success(User user, Response response) {
                    mUser = user;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateProfileForm(mUser);
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    NetworkErrorUtils.handleNetworkError(ProfileFragment.this, error);
                }
            });
        }
    }

    private void populateProfileForm(User u) {
        Activity a = getActivity();

        TextView tv = (TextView)a.findViewById(R.id.profile_full_name);
        tv.setText(u.getFullname());

        ImageView iv = (ImageView)a.findViewById(R.id.profile_picture_view);
        String avatarUrl = AvatarUrlBuilder.squareUrl(getUserId(), 60);
        BitmapUtils.cacheAsyncImage(iv, avatarUrl);

        TextView em = (TextView)a.findViewById(R.id.profile_email);
        em.setText(u.getUsername());

        TextView ph = (TextView)a.findViewById(R.id.profile_phone_number);
        em.setText(u.getPhoneNumber());

        TextView joined = (TextView)a.findViewById(R.id.profile_member_since);
        Date ds = new Date(u.getCreatedTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        joined.setText(sdf.format(ds));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout_button:
                ((User.LogoutProvider)getActivity()).logout();
                break;
            case R.id.profile_button:
            default:
                saveProfileForm(mUser);
                getActivity().finish();
        }
    }

    private void saveProfileForm(User u) {
        FirebaseManager fm = FirebaseManager.getInstance();
        u.setLastUpdated(System.currentTimeMillis());
        UserInfoStore.getInstance(getActivity()).refreshUser(u);
        fm.updateUser(u);
    }
}
