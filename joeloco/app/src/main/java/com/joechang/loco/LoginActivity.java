package com.joechang.loco;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.client.UserClient;
import com.joechang.loco.contacts.LocoContactHelper;
import com.joechang.loco.firebase.AndroidFirebaseManager;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.fragment.PlaceholderFragment;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.PostWriteAction;
import com.joechang.loco.model.User;
import com.joechang.loco.utils.HttpUtils;
import com.joechang.loco.utils.NetworkErrorUtils;
import com.joechang.loco.utils.UserInfoStore;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;

public class LoginActivity extends Activity implements OnClickListener {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private GitkitClient mGitkitClient;
    private UserInfoStore mUserInfoStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Spinner for initialization
        showLoadingPage();

        this.mUserInfoStore = new UserInfoStore(this);

        mGitkitClient = GitkitClient.newBuilder(this, new GitkitClient.SignInCallbacks() {
            @Override
            public void onSignIn(IdToken idToken, final GitkitUser user) {
                //showProfilePage(idToken, user);
                mUserInfoStore.saveIdTokenAndGitkitUser(idToken, user);
                doLoggedIn();
            }

            @Override
            public void onSignInFailed() {
                signInFailed();
            }
        }).build();

        if (mUserInfoStore.isUserLoggedIn()) {
            //showProfilePage(mUserInfoStore.getSavedIdToken(), mUserInfoStore.getSavedGitkitUser());
            doLoggedIn();
        } else {
            showSignInPage();
        }
    }

    private void signInFailed() {
        Toast.makeText(LoginActivity.this, "Sign in failed", Toast.LENGTH_LONG).show();
    }

    private void signInSuceeded() {
        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(i);
        LoginActivity.this.finish();
    }

    private void showLoadingPage() {
        setContentView(PlaceholderFragment.defaultProgressBarLayout(this));
    }

    private void showSignInPage() {
        setContentView(R.layout.activity_login);
        Button mEmailSignInButton = (Button) findViewById(R.id.start_sign_in_button);
        mEmailSignInButton.setOnClickListener(this);

        if (!mUserInfoStore.wasShown()) {
            //showDialog, a screen that warns, etc.
            mUserInfoStore.saveDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (!mGitkitClient.handleActivityResult(requestCode, resultCode, intent)) {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!mGitkitClient.handleIntent(intent)) {
            super.onNewIntent(intent);
        }
    }

    /**
     * Update the local machine with the username, id of the user,
     **/
    protected void doLoggedIn() {
        updateUserRecord(
                UserInfoStore.getInstance(LoginActivity.this).getUser(),
                new PostQueryAction<User>() {
                    @Override
                    public void doAction(User p) {
                        signInSuceeded();
                    }

                    @Override
                    public void onError(User p) {
                        //not-sure
                        NetworkErrorUtils.handleNetworkError(LoginActivity.this, new NullPointerException());
                    }
                });

        //Message to add Loco contact information into addressbook.
        //LocoContactHelper.newInstance(this).doContactCheck();

        //Authenticate with the server API, and grab token.
        /*
        String gitToken = mUserInfoStore.getTokenId();
        UserClient uc = RestClientFactory.getInstance().getUserClient();
        uc.verifyJwt(gitToken, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                signInSuceeded();
            }

            @Override
            public void failure(RetrofitError error) {
                signInFailed();
            }
        });

        int i=1;
        */
    }

    /**
     * TODO: This should be hosted on the server side.
     * @param user
     * @param pqa
     */
    private void updateUserRecord(final User user, final PostQueryAction<User> pqa) {
        AndroidFirebaseManager.init(this);

        FirebaseManager.getInstance().findUserById(
                user.getUserId(),
                new PostQueryAction<User>() {
                    @Override
                    public void doAction(User p) {
                        if (p != null) {
                            pqa.doAction(p);
                            FirebaseManager.getInstance().updateUserLastLogin(p.getUserId());
                        } else {
                            //If user is not found, then create it on the firebase side!
                            User newUser = UserInfoStore.getInstance(LoginActivity.this).getUser();
                            newUser.setCreatedTime(System.currentTimeMillis());
                            newUser.setLastLoginTime(System.currentTimeMillis());

                            FirebaseManager.getInstance().addUser(
                                    newUser,
                                    new PostWriteAction<User>() {
                                        @Override
                                        public void doAction(User user) {
                                            pqa.doAction(null);
                                        }

                                        @Override
                                        public void onError(User user) {
                                            pqa.doAction(null);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(User p) {
                        int i=1;
                        //Nothing to say.
                    }
                }
        );
    }

    private void showProfilePage(IdToken idToken, GitkitUser user) {
        setContentView(R.layout.profile);
        showAccount(user);
        findViewById(R.id.sign_out).setOnClickListener(this);
    }

    // Step 5: Respond to user actions.
    // If the user clicks sign in, call GitkitClient.startSignIn() to trigger the sign in flow.
    // If the user clicks sign out, call GitkitClient.signOut() to clear state.
    // If the user clicks manage account, call GitkitClient.manageAccount() to show manage
    // account UI.
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_sign_in_button) {
            mGitkitClient.startSignIn();
        } else if (v.getId() == R.id.sign_out) {
            showSignInPage();
        }
    }

    //A nice profile page with picture.
    private void showAccount(final GitkitUser user) {
        ((TextView) findViewById(R.id.account_email)).setText(user.getEmail());

        if (user.getDisplayName() != null) {
            ((TextView) findViewById(R.id.account_name)).setText(user.getDisplayName());
        }

        if (user.getPhotoUrl() != null) {
            final ImageView pictureView = (ImageView) findViewById(R.id.account_picture);
            new AsyncTask<String, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(String... arg) {
                    try {
                        byte[] result = HttpUtils.get(arg[0]);
                        return BitmapFactory.decodeByteArray(result, 0, result.length);
                    } catch (IOException e) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        pictureView.setImageBitmap(bitmap);
                    }
                }
            }.execute(user.getPhotoUrl());
        }

        LinearLayout ll = (LinearLayout) findViewById(R.id.account_block);
        ll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                doLoggedIn();
                return true;
            }
        });

    }

    /**
     * Check if the device supports Google Play Services.  It's best
     * practice to check first rather than handling this as an error case.
     *
     * @return whether the device supports Google Play Services
     */
    private boolean supportsGooglePlayServices() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ==
                ConnectionResult.SUCCESS;
    }
}



