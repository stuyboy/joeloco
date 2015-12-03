package com.joechang.kursor;

import android.content.Context;
import android.location.Location;
import com.joechang.kursor.*;
import com.joechang.kursor.commands.*;
import com.joechang.loco.Configuration;
import com.joechang.loco.firebase.AndroidFirebaseManager;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.user.UserManager;
import com.joechang.loco.utils.LocationUtils;

/**
 * Author:    joechang
 * Created:   9/15/15 11:31 AM
 * Purpose:   CommandLineProcessor that can exist on the client side, registering processors that interact
 * with the phone.
 */
public class AndroidCommandLineProcessor extends CommandLineProcessor {

    private Context mContext;

    public AndroidCommandLineProcessor(Context c, CliOutputWriter cow) {
        super(cow);
        mContext = c;
        AndroidFirebaseManager.init(mContext);
        addBlackListedAddress(Configuration.getServerPhoneNumber());
    }

    @Override
    protected void initProcessors() {
        //Add all body processors here.
        addBodyProcessor(new SingularBodyProcessor(this));
        addBodyProcessor(new LocationCliBodyProcessor(this));
        addBodyProcessor(new YelpCliBodyProcessor(this));
        addBodyProcessor(new OpenTableCliBodyProcessor(this));
        addBodyProcessor(new GiphyCliBodyProcessor(this));

        //Redirectors
        addPiper(new PhonePiper(this));
        addPiper(new EmailPiper(this));
        addPiper(new OpenTablePiper(this));
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public Double[] getLatLng() {
        Double[] latlng = super.getLatLng();

        Location ll = LocationUtils.getInstance(getContext()).getCurrentLocation();
        if (ll != null) {
            latlng[0] = ll.getLatitude();
            latlng[1] = ll.getLongitude();
        }

        return latlng;
    }
}
