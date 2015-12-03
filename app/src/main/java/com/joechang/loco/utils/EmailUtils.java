package com.joechang.loco.utils;

import android.content.Context;
import android.content.Intent;
import com.joechang.loco.R;

/**
 * Author:    joechang
 * Created:   7/30/15 3:25 PM
 * Purpose:   Sigh, another bunch of static methods.  Doesn't this get old?
 */
public class EmailUtils {

    public static void launchEmailDialog(Context cxt, String emailAddress, String subject, String message) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("message/rfc822");

        share.putExtra(Intent.EXTRA_EMAIL, new String[] { emailAddress });
        share.putExtra(Intent.EXTRA_SUBJECT, subject);
        share.putExtra(Intent.EXTRA_TEXT, message);

        Intent chooser = Intent.createChooser(share, cxt.getString(R.string.email_provider_title));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        cxt.startActivity(chooser);
    }

}
