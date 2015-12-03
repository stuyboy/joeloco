package com.joechang.android.mms.pdu;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.Telephony;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * Author:    joechang
 * Created:   8/20/15 10:55 AM
 * Purpose:   A quick class derived from the Android source that makes sense of the byteArray coming in.
 */
public class PduHelper {

    public static final int MESSAGE_TYPE_DELIVERY_IND       = 0x86;
    public static final int MESSAGE_TYPE_NOTIFICATION_IND   = 0x82;
    public static final int MESSAGE_TYPE_READ_ORIG_IND      = 0x88;

    //PDU Header indexes
    public static final int MESSAGE_TYPE                    = 0x8C;
    public static final int MESSAGE_TYPE_SEND_REQ           = 0x80;
    public static final int MESSAGE_ID                      = 0x8B;
    public static final int FROM                            = 0x89;

    //Some constants
    private static final int TEXT_MIN = 32;
    private static final int TEXT_MAX = 127;
    private static final int QUOTE = 127;

    //Members
    private HashMap<Integer, Object> headerMap;
    private byte[] originalData;

    /**
     * Takes in the byteArray that comes in as key "data" from the Intent received.
     * @param pduData
     * @return
     */
    public PduHelper(byte[] pduData) {
        originalData = pduData;
        headerMap = processByteArray(pduData);
    }

    public int getMessageType() {
        Object typeInt = headerMap.get(MESSAGE_TYPE);
        return typeInt == null ? null : (int)typeInt;
    }

    public String getMessageId() {
        Object messageId = headerMap.get(MESSAGE_ID);
        return messageId == null ? null : new String((byte[])messageId);
    }

    public String getThreadId(Context context) {
        Long msgId = findThreadId(context, getMessageId());
        return msgId == -1 ? null : Long.toString(msgId);
    }

    //TODO: Able to get this from the header more neatly.  Very crude.
    public String getFromNumber() {
        String incomingNumber = new String(originalData);
        int indx = incomingNumber.indexOf("/TYPE");
        if (indx > 0 && (indx - 15) > 0) {
            int newIndx = indx - 15;
            incomingNumber = incomingNumber.substring(newIndx, indx);
            indx = incomingNumber.indexOf("+");
            if (indx > 0) {
                incomingNumber = incomingNumber.substring(indx);
                return incomingNumber;
            }
        }

        return null;
    }

    protected static HashMap<Integer, Object> processByteArray(byte[] byteArray) {
        HashMap<Integer, Object> headerMap = new HashMap<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);

        if (bais == null) {
            return null;
        }

        boolean keepParsing = true;
        while (keepParsing && (bais.available() > 0)) {
            bais.mark(1);
            int headerField = extractByteValue(bais);

            if ((headerField >= TEXT_MIN) && (headerField <= TEXT_MAX)) {
                bais.reset();
                byte[] bval = parseWapTextString(bais);
                //Log.d(PduHelper.class.toString(), "FOUND Text Header: " + new String(bval));
                continue;
            }

            switch (headerField) {
                case MESSAGE_TYPE:
                    int messageType = extractByteValue(bais);
                    switch (messageType) {
                        case MESSAGE_TYPE_DELIVERY_IND:
                        case MESSAGE_TYPE_NOTIFICATION_IND:
                        case MESSAGE_TYPE_READ_ORIG_IND:
                            headerMap.put(MESSAGE_TYPE, messageType);
                            break;
                         default:
                            //no-op
                    }
                    break;
                case MESSAGE_ID:
                    byte[] value = parseWapTextString(bais);
                    headerMap.put(MESSAGE_ID, value);
                    break;
            }
        }
        return headerMap;
    }

    protected static byte[] parseWapTextString(ByteArrayInputStream bais) {
        assert(null != bais);

        bais.mark(1);

        int temp = bais.read();
        assert (-1 != temp);

        if (temp == QUOTE) {
            bais.mark(1);
        }

        return getWapTextString(bais);
    }

    protected static byte[] getWapTextString(ByteArrayInputStream bais) {
        assert (null != bais);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int temp = bais.read();
        assert (-1 != temp);

        while ((-1 != temp) && ('\0' != temp)) {
            if (isText(temp)) {
                bos.write(temp);
            }
            temp = bais.read();
            assert (-1 != temp);
        }

        if (bos.size() > 0) {
            return bos.toByteArray();
        }

        return null;
    }

    protected static boolean isText(int ch) {
        return (((ch >= 32) && (ch <= 126)) || ((ch >= 128) && (ch <= 255)) || ch == '\t' || ch == '\n' || ch == '\r');
    }

    protected static long findThreadId(Context context, String messageId) {
        StringBuilder sb = new StringBuilder('(');
        sb.append(Telephony.Mms.MESSAGE_ID);
        sb.append('=');
        sb.append(DatabaseUtils.sqlEscapeString(messageId));
        sb.append(" AND ");
        sb.append(Telephony.Mms.MESSAGE_TYPE);
        sb.append('=');
        sb.append(MESSAGE_TYPE_SEND_REQ);

        // TODO ContentResolver.query() appends closing ')' to the selection argument
        // sb.append(')');

        Cursor cursor = context.getContentResolver().query(
                Telephony.Mms.CONTENT_URI,
                new String[] { Telephony.Mms.THREAD_ID },
                sb.toString(),
                null,
                null
        );

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }

        return -1;
    }

    protected static int extractByteValue(ByteArrayInputStream bais) {
        assert(null != bais);
        int temp = bais.read();
        assert (-1 != temp);
        return temp & 0xFF;
    }

}
