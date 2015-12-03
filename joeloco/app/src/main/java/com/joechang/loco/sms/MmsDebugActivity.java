package com.joechang.loco.sms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Telephony;
import com.joechang.loco.BaseDrawerActionBarActivity;
import com.joechang.loco.NavigationEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Author:    joechang
 * Created:   9/8/15 3:25 PM
 * Purpose:
 */
public class MmsDebugActivity extends BaseDrawerActionBarActivity {

    public static final String PARAM_THREAD_ID = "thread_id";
    public static final String PARAM_MSG_ID = "_id";
    public static final String PARAM_ADDRESS = "address";
    public static final String PARAM_BODY = "body";
    public static final String PARAM_TYPE = "type";

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        testMmsMessages();
    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.DEBUG;
    }

    public void testMmsMessages() {
        queryMms();
    }

    public void queryMms() {
        //Cursor c = mContext.getContentResolver().query(MMS_SMS_CONVERSATION_URI, null, null, null, null);
        Uri uu = Telephony.Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();

        String[] ALL_THREADS_PROJECTION = {
                Telephony.Threads._ID,
                Telephony.Threads.DATE,
                Telephony.Threads.MESSAGE_COUNT,
                Telephony.Threads.RECIPIENT_IDS,
                Telephony.Threads.SNIPPET,
                Telephony.Threads.SNIPPET_CHARSET,
                Telephony.Threads.READ,
                Telephony.Threads.ERROR,
                Telephony.Threads.HAS_ATTACHMENT
        };

        Cursor c = mContext.getContentResolver().query(
                uu,
                ALL_THREADS_PROJECTION, null, null, "date desc");

        try {
            if (c != null && c.moveToFirst()) {
                debugColumns(c);

                Long lastMsgId = queryLastMessage(c.getLong(0));

                if (lastMsgId == null) {
                    return;
                }

                String message = queryMmsText(lastMsgId);
            }
        } finally {
            c.close();
        }
    }

    public Long queryLastMessage(Long threadId) {
        Uri uu = Telephony.Threads.CONTENT_URI.buildUpon().appendEncodedPath(threadId.toString()).build();
        String[] PROJECTION = new String[]{
                // TODO: should move this symbol into com.android.mms.telephony.Telephony.
                Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN,
                BaseColumns._ID,
                Telephony.Sms.Conversations.THREAD_ID,
                // For SMS
                //Telephony.Sms.ADDRESS,
                //Telephony.Sms.BODY,
                //Telephony.Sms.DATE,
                //Telephony.Sms.DATE_SENT,
                //Telephony.Sms.READ,
                //Telephony.Sms.TYPE,
                //Telephony.Sms.STATUS,
                //Telephony.Sms.LOCKED,
                //Telephony.Sms.ERROR_CODE,
                // For MMS
                //Telephony.Mms.SUBJECT,
                //Telephony.Mms.SUBJECT_CHARSET,
                Telephony.Mms.DATE,
                Telephony.Mms.DATE_SENT,
                Telephony.Mms.READ,
                Telephony.Mms.MESSAGE_TYPE,
                //Telephony.Mms.MESSAGE_BOX,
                //Telephony.Mms.DELIVERY_REPORT,
                //Telephony.Mms.READ_REPORT,
                //Telephony.MmsSms.PendingMessages.ERROR_TYPE,
                //Telephony.Mms.LOCKED,
                //Telephony.Mms.STATUS,
                Telephony.Mms.TEXT_ONLY
        };
        Cursor c = mContext.getContentResolver().query(
                uu,
                PROJECTION, null, null, "date desc");

        try {
            if (c != null && c.moveToFirst()) {

                debugColumns(c);

                Long msgId = c.getLong(c.getColumnIndex(BaseColumns._ID));
                return msgId;
                /*
                debug(String.format("MID: %d : tdc: %s, type: %s",
                        msgId,
                        c.getString(c.getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN)),
                        c.getString(c.getColumnIndex(Telephony.Mms.MESSAGE_TYPE))
                ));
                */
            }
        } finally {
            c.close();
        }

        return null;
    }

    public String queryMmsText(Long mmsId) {
        Uri uri = Uri.parse("content://mms/part");
        String selection = Telephony.Mms.Part.MSG_ID + "=" + mmsId;
        Cursor cursor = mContext.getContentResolver().query(uri, null, selection, null, null);
        try {
            if (cursor.moveToFirst()) {

                do {
                    String partId = cursor.getString(cursor.getColumnIndex("_id"));
                    String type = cursor.getString(cursor.getColumnIndex("ct"));

                    debugColumns(cursor);

                    if ("text/plain".equals(type)) {
                        String data = cursor.getString(cursor.getColumnIndex("_data"));
                        String body;
                        if (data != null) {
                            // implementation of this method below
                            body = getMmsText(partId);
                        } else {
                            body = cursor.getString(cursor.getColumnIndex("text"));
                        }
                        return body;
                    }

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    private String getMmsText(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = mContext.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return sb.toString();
    }


    public void debugOutput(Cursor c) {
        debug("Id : " + c.getString(c.getColumnIndex(PARAM_MSG_ID)));
        debug("Thread Id : " + c.getString(c.getColumnIndex("thread_id")));
        debug("Address : " + c.getString(c.getColumnIndex(PARAM_ADDRESS)));
        debug("Person : " + c.getString(c.getColumnIndex("person")));
        debug("Date : " + c.getLong(c.getColumnIndex("date")));
        debug("Read : " + c.getString(c.getColumnIndex("read")));
        debug("Status : " + c.getString(c.getColumnIndex("status")));
        debug("Type : " + c.getString(c.getColumnIndex(PARAM_TYPE)));
        debug("Rep Path Present : " + c.getString(c.getColumnIndex("reply_path_present")));
        debug("Subject : " + c.getString(c.getColumnIndex("subject")));
        debug("Body : " + c.getString(c.getColumnIndex(PARAM_BODY)));
        debug("Err Code : " + c.getString(c.getColumnIndex("error_code")));
        debug("Mime Type : " + c.getString(c.getColumnIndex("ct_t")));
    }

    public void debugColumns(Cursor c) {
        String[] colNames = c.getColumnNames();
        if (colNames != null) {
            for (int k = 0; k < colNames.length; k++) {
                debug("column[" + k + "] : " + colNames[k] + " -> " + c.getString(k));
            }
        }
    }

    public static void debug(String x) {
        System.out.println(x);
    }
}
