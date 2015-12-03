package com.joechang.loco.utils;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.joechang.loco.BaseDrawerActionBarActivity;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   6/9/15 10:23 AM
 * Purpose:   Some generic methods for handling network errors.  Ie, log it, alert user, etc.
 */
public class NetworkErrorUtils {
    private static Logger log = Logger.getLogger(NetworkErrorUtils.class.getName());

    public static void handleNetworkError(Fragment ff, Exception e) {
        handleNetworkError(ff.getActivity(), e);
    }

    public static void handleNetworkError(Activity ba, Exception e) {
        logWarning(ba, e);
        alertNetworkNotAvailable(ba);
    }

    public static void handleNetworkError(Activity ba) {
        handleNetworkError(ba, new NetworkErrorException());
    }

    protected static void logWarning(Object c, Exception e) {
        log.log(Level.WARNING, "Network error while in " + c.getClass().getName(), e);
    }

    //Handle network errors gracefully?!
    protected static void alertNetworkNotAvailable(final Activity a) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle("Network Not Available")
                .setMessage("Please try again once you are in range.")
                .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        a.finish();
                    }
                });
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    /**
     * Do a basic test of connectivity, although wifi connected does not mean we have path to internet.
     * @param context
     * @return boolean with the result.
     */
    public static boolean detectNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            if (context instanceof Activity) {
                NetworkErrorUtils.handleNetworkError((Activity) context);
                return false;
            }
        }
        return true;
    }
}
