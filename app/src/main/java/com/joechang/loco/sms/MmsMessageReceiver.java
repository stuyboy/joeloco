package com.joechang.loco.sms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.joechang.android.mms.pdu.PduHelper;

/**
 * Author:    joechang
 * Created:   8/25/15 10:21 AM
 * Purpose:
 */
public class MmsMessageReceiver extends SmsMessageReceiver {

    public void onReceive(final Context context, Intent intent) {
        if (MMS_MESSAGE.equals(intent.getType())) {
            //Rely on content://mms-sms/conversaions instead.
            //onReceiveMMS(context, intent);
        }
    }


    /**
     * Reception of MMS doesn't seem to help, as MMSs need to be forwarded to a TransactionService that actually
     * does the download.  So this is just play code for understanding what's in the PDU.
     * @param context
     * @param intent
     */
    protected void onReceiveMMS(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                byte[] buffer = bundle.getByteArray(INTENT_DATA);
                assert (buffer != null);
                PduHelper ph = new PduHelper(buffer);

                final String msgId = ph.getMessageId();
                final String address = ph.getFromNumber();
                final String threadId = ph.getThreadId(context);

                userOnSystem(address, new ForwardToServiceCallback(context, null, address, null) {
                    @Override
                    public void doOnReturn() {
                        forwardToMmsCommandLineService(context, threadId, msgId, address);
                    }
                });
            }
        } catch (Exception e) {
            Log.d("MMS Exception caught", e.getMessage());
            e.printStackTrace();
        }
    }

}
