package com.joechang.loco.utils;

import java.util.ArrayList;

/**
 * Author:    joechang
 * Created:   8/31/15 12:35 PM
 * Purpose:   A place to hold some functions that clean phone numbers.
 */
public class AddressUtils {


    /**
     * Maybe move out at some point, most efficient way to strip the characters we don't want.
     *
     * @param in
     * @return
     */
    public static String cleanPhoneNumber(String in) {
        //Hackeroo
        in = in.replace("+1","");

        StringBuilder sb = new StringBuilder();

        char[] charArr = in.toLowerCase().toCharArray();

        for (char c : charArr) {
            switch (c) {
                case '(':
                case ')':
                case '-':
                case ' ':
                    break;
                default:
                    sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String[] cleanPhoneNumbers(String[] ins) {
        ArrayList<String> ret = new ArrayList<>();
        for (String i : ins) {
            ret.add(cleanPhoneNumber(i));
        }
        return ret.toArray(new String[]{});
    }

}
