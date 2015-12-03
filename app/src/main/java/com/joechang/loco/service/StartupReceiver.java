package com.joechang.loco.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.joechang.kursor.service.NotificationSenderService;
import com.joechang.kursor.sms.MmsCommandLineService;
import com.joechang.kursor.sms.SmsCommandLineService;

/**
 * Author:  joechang
 * Date:    1/27/15
 * Purpose: A standard broadcast receiver to startup the joelo.co service on boot.
 */
public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        startServices(context);
    }

    public static void startServices(Context context) {
        Intent i = new Intent(context, LocationPublishService.class);
        context.startService(i);

        Intent j = new Intent(context, MmsCommandLineService.class);
        context.startService(j);

        Intent k = new Intent(context, SmsCommandLineService.class);
        context.startService(k);

        Intent l = new Intent(context, NotificationSenderService.class);
        context.startService(l);

        //Wish this would work, but services cannot interact with the UI.
        Toast.makeText(context, "Started joelo.co Services", Toast.LENGTH_LONG).show();
    }

}
