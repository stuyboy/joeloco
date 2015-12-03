package com.joechang.kursor.sms;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import com.joechang.kursor.AndroidCommandLineProcessor;
import com.joechang.kursor.CliOutputWriter;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.loco.service.MMSSenderService;
import com.joechang.loco.utils.Stopwatch;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   6/13/15 7:17 PM
 * Purpose:   Stay in the background, listen for SMS & MMS Keywords that will trigger joeloco services.
 */
public class MmsCommandLineService extends Service implements CliOutputWriter {
    public static final String PARAM_THREAD_ID = "thread_id";
    public static final String PARAM_MSG_ID = "_id";
    public static final String PARAM_ADDRESS = "address";
    public static final String PARAM_BODY = "body";
    public static final String PARAM_TYPE = "type";

    private static long MESSAGE_STALE_TIMEOUT_MILLIS = 2 * 60 * 1000;

    private static Logger log = Logger.getLogger(MmsCommandLineService.class.getName());
    private static LruCache<MmsEnvelope, Long> mmsEnvelopeSendTime = new LruCache<MmsEnvelope, Long>(100);
    private static LruCache<Long, String> addressCache = new LruCache<Long, String>(100);
    private final Uri MMS_SMS_CONVERSATION_URI = Telephony.MmsSms.CONTENT_URI;
    private final IBinder mBinder = new Binder();
    private CommandLineProcessor mCli;
    private Context mContext;
    private String[] ALL_THREADS_PROJECTION = {
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

    private String[] PROJECTION = new String[]{
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

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mCli = new AndroidCommandLineProcessor(mContext, this);

        this.getContentResolver().registerContentObserver(
                MMS_SMS_CONVERSATION_URI,
                false,
                new MmsCommandLineService.Observer(new Handler())
        );

        //ConsoleHandler handler = new ConsoleHandler();
        //handler.setLevel(Level.ALL);
        //log.addHandler(handler);
        //log.setLevel(Level.FINEST);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String id = intent.getStringExtra(PARAM_MSG_ID);
            String address = intent.getStringExtra(PARAM_ADDRESS);
            String body = intent.getStringExtra(PARAM_BODY);

            String[] addressParsed = {address};

            if (address != null && address.indexOf(',') > 0) {
                addressParsed = address.split(",");
            }

            if (id != null && address != null && body != null) {
                mCli.processMessage(id, addressParsed, body);
            }
        }

        return START_STICKY;
    }

    /**
     * VERY ANNOYING.  HOW DO WE SEND GROUP MMS?!?!
     *
     * @param numbers
     * @param resp
     */
    @Override
    public void outputCommandResponse(String[] numbers, String[] resp) {
        String joinedResponse = TextUtils.join("\n", resp);
        MMSSenderService.sendMMS(mContext, numbers, joinedResponse);
    }

    @Override
    public void outputNotFound(String[] dest, String message) {
        outputCommandResponse(dest, new String[]{message});
    }

    @Override
    public void outputCommandResponse(String[] destinations, URL resource) {
        String d = getFilesDir().getAbsolutePath();
        //Download a gif into a bytearray, into a file.
        try {
            String randomFilename = UUID.randomUUID().toString() + ".gif";
            String completePath = d + "/" + randomFilename;
            FileUtils.copyURLToFile(resource, new File(completePath));
            MMSSenderService.sendMMS(mContext, destinations, "", MMSSenderService.MediaType.GIF, completePath);
        } catch (Exception e) {
            outputNotFound(destinations, resource.toString());
        }
    }

    public void fillContactCache() {
        Stopwatch.start("FillContactCache");
        Uri contacts = Uri.parse("content://mms-sms/canonical-addresses");
        Cursor c = mContext.getContentResolver().query(
                contacts,
                null,
                null,
                null,
                null
        );
        try {
            if (c != null) {
                while (c.moveToNext()) {
                    // TODO: don't hardcode the column indices
                    long id = c.getLong(0);
                    String number = c.getString(1);
                    addressCache.put(id, number);
                }
            }
        } finally {
            c.close();
        }
        Stopwatch.stop("FillContactCache");
    }

    public String findRecipient(Long id) {
        if (addressCache.get(id) == null) {
            fillContactCache();
        }
        return addressCache.get(id);
    }

    public void debug(String msg) {
        Log.d(MmsCommandLineService.class.toString(), msg);
    }

    public class Binder extends android.os.Binder {
        public MmsCommandLineService getService() {
            return MmsCommandLineService.this;
        }
    }

    /**
     * Private class for handling the outgoing text message.
     */
    public class Observer extends ContentObserver {
        private static final int ID = 0;
        private static final int DATE = 1;
        private static final int MESSAGE_COUNT = 2;
        private static final int RECIPIENT_IDS = 3;
        private static final int SNIPPET = 4;
        private static final int SNIPPET_CS = 5;
        private static final int READ = 6;
        private static final int ERROR = 7;
        private static final int HAS_ATTACHMENT = 8;
        private Handler handler;


        public Observer(Handler h) {
            super(h);
            this.handler = h;
        }

        public void debugThread(Cursor c) {
            debug("ThId: " + c.getString(c.getColumnIndex("_id")));
            debug("Date: " + c.getString(c.getColumnIndex("date")));
            debug("count: " + c.getString(c.getColumnIndex("message_count")));
            debug("rIds: " + c.getString(c.getColumnIndex("recipient_ids")));
            debug("snip: " + c.getString(c.getColumnIndex("snippet")));
            debug("snip_cs: " + c.getString(c.getColumnIndex("snippet_cs")));
            debug("read: " + c.getString(c.getColumnIndex("read")));
            debug("error: " + c.getString(c.getColumnIndex("error")));
            debug("hatch: " + c.getString(c.getColumnIndex("has_attachment")));
        }

        public void debugMessage(Cursor c) {
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
                    debug("column[" + k + "] : " + colNames[k]);
                }
            }
        }

        public String queryMmsText(Long mmsId) {
            Uri uri = Uri.parse("content://mms/part");
            String selection = Telephony.Mms.Part.MSG_ID + "=" + mmsId;
            Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String partId = cursor.getString(cursor.getColumnIndex("_id"));
                        String type = cursor.getString(cursor.getColumnIndex("ct"));
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

        /**
         * This doesn't seem to get used much.
         *
         * @param id
         * @return
         */

        private String getMmsText(String id) {
            Uri partURI = Uri.parse("content://mms/part/" + id);
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try {
                is = getContentResolver().openInputStream(partURI);
                if (is != null) {
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (temp != null) {
                        sb.append(temp);
                        temp = reader.readLine();
                        debug("line read: " + temp);
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

            //debug("returning " + sb.toString());

            return sb.toString();
        }

        public Long queryLastMessage(Long threadId) {
            Uri uu = Telephony.Threads.CONTENT_URI.buildUpon().appendEncodedPath(threadId.toString()).build();
            Cursor c = mContext.getContentResolver().query(
                    uu,
                    PROJECTION, null, null, "date desc");

            try {
                if (c != null && c.moveToFirst()) {
                    Long msgId = c.getLong(c.getColumnIndex(BaseColumns._ID));
                    //debugMessage(c);
                    return msgId;
                }
            } finally {
                c.close();
            }

            return null;
        }

        public void queryMms() {
            //Cursor c = mContext.getContentResolver().query(MMS_SMS_CONVERSATION_URI, null, null, null, null);
            Uri uu = Telephony.Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
            Cursor c = mContext.getContentResolver().query(
                    uu,
                    ALL_THREADS_PROJECTION, null, null, "date desc");

            try {
                if (c != null && c.moveToFirst()) {
                    //debugThread(c);

                    //Make sure it's not too old!
                    String dd = c.getString(c.getColumnIndex(Telephony.Threads.DATE));
                    if (dd != null && !dd.isEmpty()) {
                        Long epoch = Long.parseLong(dd);
                        //If message is more than 2 minute old, abandon!
                        if (Math.abs(System.currentTimeMillis() - epoch) > MESSAGE_STALE_TIMEOUT_MILLIS) {
                            log.info("Skipping message as it is stale.");
                            return;
                        }
                    }

                    //Fill up an MMSEnvelope
                    MmsEnvelope mms = new MmsEnvelope(
                            c.getLong(ID),
                            null,
                            null
                    );

                    //Test it for validity, prevent over-sending messages.
                    Long lastMsgId = queryLastMessage(mms.getThreadId());
                    if (lastMsgId == null) {
                        return;
                    }
                    mms.setMessageId(lastMsgId);
                    mms.setRecipients(c.getString(RECIPIENT_IDS));

                    log.info("Handling: " + mms);

                    //See if this is already been processed, by thread and message ids
                    synchronized (mmsEnvelopeSendTime) {
                        if (mmsEnvelopeSendTime.get(mms) != null) {
                            log.info("Previously Processed " + mms);
                            return;
                        }
                        log.info("Passed for Processing " + mms);
                        mmsEnvelopeSendTime.put(mms, System.currentTimeMillis());
                    }

                    String message = queryMmsText(mms.getMessageId());

                    log.info("Processing message: " + message);

                    if (message != null) {
                        mCli.processMessage(mms.getMessageId().toString(), mms.getRecipients(), message);
                    }
                }
            } finally {
                c.close();
            }

        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            try {
                Stopwatch.start("MMSOnChange");
                queryMms();
                Stopwatch.stop("MMSOnChange");
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error in SmsCommandLineService", e);
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange, null);
        }
    }

    class MmsEnvelope {
        Long threadId;
        Long messageId;
        String[] recipients;

        public MmsEnvelope(Long threadId, Long messageId, String[] recipients) {
            this.threadId = threadId;
            this.messageId = messageId;
            this.recipients = recipients;
        }

        public Long getThreadId() {
            return threadId;
        }

        public void setThreadId(Long threadId) {
            this.threadId = threadId;
        }

        public Long getMessageId() {
            return messageId;
        }

        public void setMessageId(Long messageId) {
            this.messageId = messageId;
        }

        public String[] getRecipients() {
            return recipients;
        }

        public void setRecipients(String delimted) {
            List<String> numbers = new ArrayList<String>();
            for (String d : delimted.split(" ")) {
                numbers.add(findRecipient(Long.parseLong(d)));
            }
            this.recipients = numbers.toArray(new String[]{});
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MmsEnvelope that = (MmsEnvelope) o;

            if (threadId != null ? !threadId.equals(that.threadId) : that.threadId != null) return false;
            if (messageId != null ? !messageId.equals(that.messageId) : that.messageId != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(recipients, that.recipients);

        }

        @Override
        public int hashCode() {
            int result = threadId != null ? threadId.hashCode() : 0;
            result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
            result = 31 * result + (recipients != null ? Arrays.hashCode(recipients) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "MmsEnvelope{" +
                    "threadId=" + threadId +
                    ", messageId=" + messageId +
                    ", recipients=" + Arrays.toString(recipients) +
                    '}';
        }
    }
}
