package com.joechang.loco.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Author:    joechang
 * Created:   10/2/15 3:44 PM
 * Purpose:   another util class, ho hum.
 */
public class PhoneUtils {
    public static String getTelephoneNumber(Context cxt) {
        TelephonyManager tMgr = (TelephonyManager)cxt.getSystemService(Context.TELEPHONY_SERVICE);
        String s = tMgr.getLine1Number();
        if (s != null) {
            return AddressUtils.cleanPhoneNumber(s);
        }
        return null;
    }
}
