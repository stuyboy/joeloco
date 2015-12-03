package com.joechang.loco.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import com.joechang.loco.utils.AddressUtils;
import com.joechang.loco.utils.BitmapUtils;
import com.joechang.loco.utils.StringUtils;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   9/3/15 5:35 PM
 * Purpose:   An attempt at making a single service that handles all mms sending, to avoid problems.
 */
public class MMSSenderService extends Service {
    public enum MediaType {
        BITMAP("image/png"),
        GIF("image/gif");

        protected String mimeType;
        MediaType(String mimeType) {
            this.mimeType = mimeType;
        }
    }

    private Logger log = Logger.getLogger(MMSSenderService.class.getName());

    public final static String PARAM_ADDRESS = "TO_ADDRESS";
    public final static String PARAM_BODY = "TEXT_BODY";
    public final static String PARAM_MIME_TYPE = "MIME_TYPE";
    public final static String PARAM_MEDIA_LOCATION = "MEDIA_LOC";

    private final static int MAX_SMS_RESULTS = 5;

    private final static Map<Integer, Long> sentMessages = Collections.synchronizedMap(new HashMap<Integer, Long>());
    private final IBinder mBinder = new Binder();

    private Context mContext;
    private static String carrierName;
    private static String phoneNumber;

    public static void sendMMS(Context mContext, String[] numbers, String response) {
        sendMMS(mContext, numbers, response, null, null);
    }

    public static void sendMMS(Context mContext, String[] numbers, String response, MediaType type, String mediaLocation) {
        Intent ii = new Intent(mContext, MMSSenderService.class);
        ii.putExtra(MMSSenderService.PARAM_ADDRESS, numbers);
        ii.putExtra(MMSSenderService.PARAM_BODY, response);
        ii.putExtra(MMSSenderService.PARAM_MIME_TYPE, type == null ? null : type.toString());
        ii.putExtra(MMSSenderService.PARAM_MEDIA_LOCATION, mediaLocation);

        mContext.startService(ii);
    }

    public static void sendMMS(Context mContext, String number, String response) {
        sendMMS(mContext, new String[] { number }, response);
    }

    public static void sendSMS(String number, String response) {
        processSMS(new String[] { number }, response);
    }

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        carrierName = manager.getNetworkOperatorName();
        phoneNumber = manager.getLine1Number();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String[] numbers = intent.getStringArrayExtra(PARAM_ADDRESS);
            String response = intent.getStringExtra(PARAM_BODY);

            String mediaType = intent.getStringExtra(PARAM_MIME_TYPE);
            String mediaLocation = intent.getStringExtra(PARAM_MEDIA_LOCATION);

            //Pictures & Media
            Bitmap bm = null;
            String mimetype = null;
            byte[] media = null;

            if (!StringUtils.isEmpty(mediaType) && !StringUtils.isEmpty(mediaLocation)) {
                //WTF is this different? the AndroidSMSMMS Library handles images differently.
                if (MediaType.BITMAP.name().equals(mediaType)) {
                    bm = handleBitmap(mediaType, mediaLocation);
                }
                if (MediaType.GIF.name().equals(mediaType)) {
                    mimetype = MediaType.GIF.mimeType;
                    media = handleMedia(mediaLocation);
                }
            }

            //Normalize the numbers
            numbers = AddressUtils.cleanPhoneNumbers(numbers);

            //See if this is a double send.
            if (!sentAlready(numbers, response, mediaLocation)) {
                //If we're the server, send MMS.  Otherwise, send SMS.
                if (isMMSWorthy(numbers, response) || bm != null || media != null) {
                    processMMS(mContext, numbers, response, bm, mimetype, media);
                } else {
                    processSMS(numbers, response);
                }
            }
        }

        return START_STICKY;
    }

    /**
     * Wow, read the file written onto the device into a bytearray.  Hope it's not too long.
     * @param mediaLocation
     * @return
     */
    private byte[] handleMedia(String mediaLocation) {
        byte[] arr = null;
        try {
            arr = FileUtils.readFileToByteArray(new File(mediaLocation));
        } catch (IOException ioe) {
            log.severe("Could not read media attachment");
        }
        return arr;
    }

    /**
     * For now, just return bitmap.  Maybe future, return other byte arrays.
     * @param mediaType
     * @param mediaLocation
     * @return
     */
    private Bitmap handleBitmap(String mediaType, String mediaLocation) {
        if (MediaType.BITMAP.name().equals(mediaType) && mediaLocation != null) {
            return BitmapUtils.toBitmap(this, mediaLocation);
        }
        return null;
    }

    private boolean sentAlready(String[] numbers, String response, String mediaLocation) {
        boolean sentAlready = true;

        //If we've seen this message, don't send.
        int hashMsg = (hash(numbers, response, mediaLocation));
        synchronized (sentMessages) {
            cleanSentMessages();
            if (sentMessages.get(hashMsg) == null) {
                sentMessages.put(hashMsg, System.currentTimeMillis());
                sentAlready = false;
                log.info(hashMsg + " not found. Sending.");
            } else {
                log.info(hashMsg + " was previously sent.  Skipping.");
            }
        }

        return sentAlready;
    }

    private int hash(String[] numbers, String response, String mediaLocation) {
        int hash = 5;
        if (numbers != null) {
            hash = 31 * hash + Arrays.deepHashCode(numbers);
        }
        if (response != null) {
            hash = 31 * hash + response.hashCode();
        }
        if (mediaLocation != null) {
            hash = 31 * hash + mediaLocation.hashCode();
        }
        return hash;
    }

    private void cleanSentMessages() {
        //180 Second timeout
        Long exp = System.currentTimeMillis() - (180 * 1000);
        Iterator<Map.Entry<Integer, Long>> i = sentMessages.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<Integer, Long> e = i.next();
            if (e.getValue() <= exp)  {
                i.remove();
            }
        }
    }

    private boolean isMMSWorthy(String[] numbers, String response) {
        if ((numbers.length <= 1) && (SmsManager.getDefault().divideMessage(response).size() <= 1)) {
            return false;
        }

        /**
        if (!phoneNumber.contains(Configuration.getServerPhoneNumber())) {
            return false;
        }
        **/

        return true;
    }

    protected static void processMMS(Context cxt, String[] numbers, String response) {
        processMMS(cxt, numbers, response, null, null, null);
    }

    protected static void processMMS(Context cxt, String[] numbers, String response, Bitmap bm) {
        processMMS(cxt, numbers, response, bm, null, null);
    }

    protected static void processMMS(Context cxt, String[] numbers, String response, String mimetype, byte[] media) {
        processMMS(cxt, numbers, response, null, mimetype, media);
    }

    protected static void processMMS(Context cxt, String[] numbers, String response, Bitmap bm, String mimeType, byte[] media) {
        String[] cleanedNumbers = AddressUtils.cleanPhoneNumbers(numbers);
        Settings sendSettings = new Settings();
        sendSettings.setGroup(true);
        sendSettings.setDeliveryReports(false);
        sendSettings.setSplit(false);
        sendSettings.setSplitCounter(false);
        sendSettings.setStripUnicode(false);
        sendSettings.setSignature("");
        sendSettings.setSendLongAsMms(true);
        sendSettings.setSendLongAsMmsAfter(0);

        //Determine carrier, etc.
        setMmsc(sendSettings);

        //com.klinker.android.logger.Log.setDebug(true);

        Transaction sendTransaction = new Transaction(cxt, sendSettings);

        Message mm = new Message(response, cleanedNumbers);
        if (bm != null) {
            mm.setImage(bm);
        }

        if (mimeType != null && media != null) {
            mm.setMedia(media, mimeType);
        }

        sendTransaction.sendNewMessage(mm, 0);
        //com.klinker.android.logger.Log.setDebug(false);
    }

    protected static void processSMS(String[] numbers, String r) {
        int count = 1;
        ArrayList<String> arrSMS = SmsManager.getDefault().divideMessage(r);
        for (String n : numbers) {
            SmsManager.getDefault().sendMultipartTextMessage(
                    n,
                    null,
                    arrSMS,
                    null,
                    null
            );
            if (++count > MAX_SMS_RESULTS) {
                break;
            }
        }
    }

    public static void setMmsc(Settings sendSettings) {
        switch (carrierName.toUpperCase()) {
            case "VERIZON WIRELESS":
                sendSettings.setMmsc("http://mms.vtext.com/servlets/mms");
                break;
            case "AT&T":
                sendSettings.setMmsc("http://mmsc.mobile.att.net");
                sendSettings.setProxy("proxy.mobile.att.net");
                sendSettings.setPort("80");
                break;
        }
    }
}
