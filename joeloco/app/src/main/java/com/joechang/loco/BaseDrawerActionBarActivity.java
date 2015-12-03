package com.joechang.loco;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.joechang.kursor.sms.MmsCommandLineService;
import com.joechang.kursor.sms.SmsCommandLineService;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.firebase.AndroidFirebaseManager;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.fragment.NavigationDrawerFragment;
import com.joechang.loco.model.ImageUpload;
import com.joechang.loco.model.User;
import com.joechang.loco.service.*;
import com.joechang.loco.utils.BitmapUtils;
import com.joechang.loco.utils.CameraUtils;
import com.joechang.loco.utils.UserInfoStore;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * Author:  joechang
 * Date:    12/9/14
 * Purpose: A little abstract base class to serve as something that supports actionBar + drawer.
 */
public abstract class BaseDrawerActionBarActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnClickListener {
    static {
        //If running in emulator, use local server.
        if (Build.FINGERPRINT.startsWith("generic")) {
            RestClientFactory.useDebugServer(true);
        }
    }
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    protected CharSequence lastScreenTitle;
    private UserInfoStore mUserInfoStore;
    private boolean mNonDrawerUpEnabled = false;

    public abstract NavigationEnum getNavigationEnum();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Should not be a problem if called multiple times.
        AndroidFirebaseManager.init(this);

        //Not sure I need to do this?
        if (savedInstanceState == null) {
            StartupReceiver.startServices(this);
        }

        //Setup the string values on the navigation bar titles, etc.
        NavigationEnum.init(this);
        mUserInfoStore = new UserInfoStore(this);

        setupActionBar();
        setContentView(R.layout.activity_main);
        setLastScreenTitle(getTitle());

        // Set up the drawer.
        getNavigationDrawerFragment().setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                getNavigationEnum().getIndex());

        //Control navigation from the enum.  Is this a back-only item?
        setNonDrawerBackOnly(getNavigationEnum().isNonDrawerBackOnly());

        if (!this.mUserInfoStore.isUserLoggedIn()) {
            Intent l = new Intent(this, LoginActivity.class);
            startActivity(l);
            return;
        }
    }

    protected NavigationDrawerFragment getNavigationDrawerFragment() {
        return (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayHomeAsUpEnabled(true);  //Show the back caret
        actionBar.setDisplayShowHomeEnabled(true);  //Show the icon
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!getNavigationDrawerFragment().isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(getActionBarMenu(), menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public int getActionBarMenu() {
        return R.menu.main;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        //Sort of circuitous, but allows for a default value.
        actionBar.setTitle(getNavigationEnum().getName());
    }

    /**
     * When user selects one of the menu items on the left.
     *
     * @param position
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (NavigationEnum.fromIndex(position)) {
            case PROFILE:
                startProfile();
                break;
            case GROUPS:
                startGroups();
                break;
            case NEARBY:
                startNearby();
                break;
            case BROWSE:
                startBrowse();
                break;
            case FIND:
                startFind();
                break;
            case SETTINGS:
                startSettings();
                break;
            case DEBUG:
                startDebug();
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.compass:
                startMagnetView();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_post:
                startCameraIntent();
                break;
            case R.id.action_sharelocation:
                startShareLocationIntent();
                break;
            case R.id.action_changesmsapp:
                startChangeDefaultSmsApp();
                break;
            case R.id.action_testGroupMMS:
                startSendMMSTest();
                break;
            case android.R.id.home:
                if (this.isNonDrawerBackOnly()) {
                    if (NavUtils.getParentActivityIntent(this) == null) {
                        onBackPressed();
                    } else {
                        NavUtils.navigateUpFromSameTask(this);
                    }
                    return true;
                }
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNonDrawerBackOnly() {
        return mNonDrawerUpEnabled;
    }

    /**
     * Quick method to turn off the usual drawer icon, and replace it with a back button.
     * When done, the navigation will automatically be handled to go back using NavUtils.
     * See onOptionsItemSelected.
     *
     * @param t
     */
    public void setNonDrawerBackOnly(boolean t) {
        mNonDrawerUpEnabled = t;
        getNavigationDrawerFragment().setDrawerIndicatorEnabled(!t);
    }

    // INTENTS AND ACTIONS SECTION -----------------------------------------------------------------
    protected void startProfile() {
        Intent ii = new Intent(this, ProfileActivity.class);
        startActivity(ii);
    }

    protected void startGroups() {
        Intent gg = new Intent(this, GroupsActivity.class);
        startActivity(gg);
    }

    protected void startNearby() {
        //FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.replace(R.id.content_frame, NearbyFragment.newInstance(37.7840371, -122.4806845)).commit();
        Intent ii = new Intent(this, UsersMapActivity.class);
        startActivity(ii);
    }

    protected void startBrowse() {
        Intent i = new Intent(this, GridViewActivity.class);
        startActivity(i);
    }

    protected void startFind() {

    }

    protected void startChangeDefaultSmsApp() {
        final String myPackageName = getPackageName();
        final String defaultApp = Telephony.Sms.getDefaultSmsPackage(this);
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
        startActivity(intent);
    }

    protected void startSettings() {
        Intent s = new Intent(this, SettingsActivity.class);
        startActivity(s);

    }

    protected void startDebug() {
        Intent i = new Intent(this, DebugActivity.class);
        //Intent i = new Intent(this, MmsDebugActivity.class);
        startActivity(i);
    }


    public void startCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void startShareLocationIntent() {
        //Only one button, so not checking id.
        Intent i = new Intent(this, ChooseContactActivity.class);
        startActivityForResult(i, ChooseContactActivity.CHOOSE_CONTACT);
    }

    public void startMagnetView() {
        if (this instanceof User.UserSelector) {
            Intent i = new Intent(this, MagnetActivity.class);
            i.putExtra(User.ID, ((User.UserSelector)this).getSelectedUserId());
            startActivity(i);
        }
    }

    public void startSendMMSTest() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String[] numbers = { "14153104000" };
                String test = new String("what is up, pic!");
                String d = getFilesDir().getAbsolutePath();
                //Download a gif into a bytearray, into a file.
                try {
                    FileUtils.copyURLToFile(new URL("http://media.giphy.com/media/scHQba6DM2rJu/giphy.gif"), new File(d + "/giphy.gif"));
                    MMSSenderService.sendMMS(getApplicationContext(), numbers, test, MMSSenderService.MediaType.GIF, d + "/giphy.gif");
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CameraUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    ImageUpload i = CameraUtils.doCameraIntentResult(this, resultCode, data);
                    if (i != null) {
                        FirebaseManager.getInstance().uploadImage(i);
                    }
                }
                break;
            case ChooseContactActivity.CHOOSE_CONTACT:
                if (resultCode == RESULT_OK) {
                    SendLocationService.launchIntent(this, data);
                    //SendLocationService.launchManualSMSIntent(this, data);
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public UserInfoStore getUserInfoStore() {
        return mUserInfoStore;
    }

    public CharSequence getLastScreenTitle() {
        return lastScreenTitle;
    }

    public void setLastScreenTitle(CharSequence lastScreenTitle) {
        this.lastScreenTitle = lastScreenTitle;
    }

    public void restart() {
        Intent t = getIntent();
        this.finish();
        startActivity(t);
    }
}
