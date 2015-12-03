package com.joechang.loco.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.LruCache;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.client.UserClient;
import com.joechang.loco.model.User;
import com.joechang.kursor.sms.MmsCommandLineService;
import com.joechang.kursor.sms.SmsCommandLineService;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Author:    joechang
 * Created:   7/28/15 3:21 PM
 * Purpose:   Class which will read incoming messages and throw it to the SmsCommandLineService or MmsCommandLineService
 * Also recognizes if this user is already on the system, so we won't answer the incoming text.
 */
public class SmsMessageReceiver extends BroadcastReceiver {

    public static final String MMS_MESSAGE = "application/vnd.wap.mms-message";
    public static final String INTENT_DATA = "data";
    public static final String INTENT_PDUS = "pdus";

    private LruCache<String, Boolean> foundUserCache = new LruCache<>(20);

    public void onReceive(final Context context, Intent intent) {
        if (!MMS_MESSAGE.equals(intent.getType())) {
            onReceiveSMS(context, intent);
        }
    }

    protected void onReceiveSMS(final Context context, Intent intent) {
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get(INTENT_PDUS);
        SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);

        final String body = messages.getMessageBody();
        final String address = messages.getOriginatingAddress();
        final String id = Long.toString(messages.getTimestampMillis());

        userOnSystem(address, new ForwardToServiceCallback(context, id, address, body) {
            @Override
            public void doOnReturn() {
                forwardToSmsCommandLineService(context, id, address, body);
            }
        });
    }

    protected void userOnSystem(final String address, final ForwardToServiceCallback cb) {
        //First check cache.  If user has the app, rely on client-side to do work.
        Boolean bb = foundUserCache.get(address);

        if (bb == null) {
            //Here, check the address, and see if the user is on the system.  If so, then rely on their app to do the work?
            UserClient uc = RestClientFactory.getInstance().getUserClient();
            uc.findUser(address, new UserClient.Callback() {
                @Override
                public void success(User user, Response response) {
                    Boolean userOnSystem = (user != null);
                    foundUserCache.put(address, userOnSystem);
                    if (!userOnSystem) {
                        cb.doOnReturn();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    //just do it
                    cb.doOnReturn();
                }
            });
        } else if (!bb) {
            cb.doOnReturn();
       }
    }

    protected void forwardToSmsCommandLineService(Context context, String id, String address, String body) {
        Intent i = new Intent(context, SmsCommandLineService.class);
        i.putExtra(SmsCommandLineService.PARAM_SMS_ID, id);
        i.putExtra(SmsCommandLineService.PARAM_ADDRESS, address);
        i.putExtra(SmsCommandLineService.PARAM_BODY, body);
        context.startService(i);
    }

    protected void forwardToMmsCommandLineService(Context context, String threadId, String msgId, String address) {
        Intent i = new Intent(context, MmsCommandLineService.class);
        i.putExtra(MmsCommandLineService.PARAM_THREAD_ID, threadId);
        i.putExtra(MmsCommandLineService.PARAM_MSG_ID, msgId);
        i.putExtra(MmsCommandLineService.PARAM_ADDRESS, address);
        context.startService(i);
    }

    protected abstract class ForwardToServiceCallback {
        Context context;
        String id, address, body;

        public  ForwardToServiceCallback(Context context, String id, String address, String body) {
            this.context = context;
            this.id = id;
            this.address = address;
            this.body = body;
        }

        public abstract void doOnReturn();
    }
}
