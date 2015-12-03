package com.joechang.kursor.sms;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.LruCache;
import com.joechang.kursor.AndroidCommandLineProcessor;
import com.joechang.kursor.CliOutputWriter;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.service.MMSSenderService;
import com.joechang.loco.utils.Stopwatch;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   6/13/15 7:17 PM
 * Purpose:   Stay in the background, listen for SMS Keywords that will trigger joeloco services.
 */
public class SmsCommandLineService extends Service implements CliOutputWriter {
    private static Logger log = Logger.getLogger(SmsCommandLineService.class.getName());

    private CommandLineProcessor mCli;
    private Context mContext;
    private final IBinder mBinder = new Binder();
    private final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    private static final int SENT_TYPE = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT;

    private static final long MESSAGE_STALE_TIMEOUT_MILLIS = 2 * 60 * 1000;

    public static final String PARAM_SMS_ID = "_id";
    public static final String PARAM_ADDRESS = "address";
    public static final String PARAM_BODY = "body";
    public static final String PARAM_TYPE = "type";

    public static final int MAX_RESULTS = 5;

    private LruCache<String, Long> messageIdSendTime = new LruCache<String, Long>(100);

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mCli = new AndroidCommandLineProcessor(mContext, this);

        /*
        this.getContentResolver().registerContentObserver(
                SMS_CONTENT_URI,
                true,
                new SmsCommandLineService.Observer(new Handler())
        );
        */
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String id = intent.getStringExtra(PARAM_SMS_ID);
            String address = intent.getStringExtra(PARAM_ADDRESS);
            String body = intent.getStringExtra(PARAM_BODY);

            if (id != null && address != null && body != null) {
                mCli.processMessage(id, address, body);
            }
        }

        return START_STICKY;
    }

    @Override
    public void outputCommandResponse(String[] numbers, String[] resp) {
        String joinedResponse = TextUtils.join("\n", resp);
        MMSSenderService.sendMMS(mContext, numbers, joinedResponse);
    }

    @Override
    public void outputNotFound(String[] dest, String message) {
        outputCommandResponse(dest, new String[] { message });
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

    public class Binder extends android.os.Binder {
        public SmsCommandLineService getService() {
            return SmsCommandLineService.this;
        }
    }

    /**
     * Private class for handling the outgoing text message via content observer.  Good for outgoing SMS
     * from an Android phone.
     */

    /**
    class Observer extends ContentObserver {
        private Handler handler;

        public Observer(Handler h) {
            super(h);
            this.handler = h;
        }

        public void debugOutput(Cursor c) {
            log.setLevel(Level.FINEST);

//            ConsoleHandler handler = new ConsoleHandler();
//            handler.setLevel(Level.ALL);
//            log.addHandler(handler);
//
            log.finest("Id : " + c.getString(c.getColumnIndex(PARAM_SMS_ID)));
            log.finest("Thread Id : " + c.getString(c.getColumnIndex("thread_id")));
            log.finest("Address : " + c.getString(c.getColumnIndex(PARAM_ADDRESS)));
            log.finest("Person : " + c.getString(c.getColumnIndex("person")));
            log.finest("Date : " + c.getLong(c.getColumnIndex("date")));
            log.finest("Read : " + c.getString(c.getColumnIndex("read")));
            log.finest("Status : " + c.getString(c.getColumnIndex("status")));
            log.finest("Type : " + c.getString(c.getColumnIndex(PARAM_TYPE)));
            log.finest("Rep Path Present : " + c.getString(c.getColumnIndex("reply_path_present")));
            log.finest("Subject : " + c.getString(c.getColumnIndex("subject")));
            log.finest("Body : " + c.getString(c.getColumnIndex(PARAM_BODY)));
            log.finest("Err Code : " + c.getString(c.getColumnIndex("error_code")));

//            log.removeHandler(handler);
        }

        public void debugColumns(Cursor c) {
            String[] colNames = c.getColumnNames();
            if(colNames != null){
                for(int k=0; k<colNames.length; k++){
                    log.finest("column[" + k + "] : " + colNames[k]);
                }
            }
        }

        public void lookForLoco(Cursor c) {
            String protocol = c.getString(c.getColumnIndex("protocol"));

            log.info("Wuzzup: " + protocol);

            if(protocol == null){
                int type = c.getInt(c.getColumnIndex(PARAM_TYPE));
                String id = c.getString(c.getColumnIndex(PARAM_SMS_ID));

                //If too old, abandon
                Long timestamp = c.getLong(c.getColumnIndex("date"));
                if (Math.abs(System.currentTimeMillis() - timestamp) > MESSAGE_STALE_TIMEOUT_MILLIS) {
                    log.info("Skipping msg " + id + ", stale message.");
                    return;
                }

                if(type == SENT_TYPE){
                    if (messageIdSendTime.get(id) == null) {
                        String address, body;

                        try {
                            address = c.getString(c.getColumnIndex(PARAM_ADDRESS));
                            body = c.getString(c.getColumnIndex(PARAM_BODY));

                            log.info("Found message: " + c);

                            mCli.processMessage(id, address, body);
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "Could not process msg " + id, e);
                        }

                        messageIdSendTime.put(id, System.currentTimeMillis());
                    } else {
                        log.info("Skipping msg " + id + ", type " + type + ". Previously done.");
                    }
                }
            }
        }

        public void querySms(SmsProcessor sp) {
            Cursor c = mContext.getContentResolver().query(SMS_CONTENT_URI, null, null, null, null);
            try {
                if (c != null) {
                    if (c.moveToFirst()) {
                        sp.doWithCursor(c);
                    }
                }
            } finally {
                c.close();
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            try{
                querySms(new SmsProcessor() {
                    @Override
                    public boolean doWithCursor(Cursor c) {
                        //debugColumns(c);
                        //debugOutput(c);
                        Stopwatch.start("SMSOnChange");
                        lookForLoco(c);
                        Stopwatch.stop("SMSOnChange");
                        return true;
                    }
                });
            }
            catch(Exception e){
                log.log(Level.SEVERE, "Error in SmsCommandLineService", e);
            }
        }

    }

    private interface SmsProcessor {
        boolean doWithCursor(Cursor c);
    }

    **/
}
