package com.joechang.kursor.commands;

import android.content.Intent;
import android.net.Uri;
import com.joechang.kursor.CliBodyPiper;
import com.joechang.kursor.CliProcessedCallback;
import com.joechang.kursor.AndroidCommandLineProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author:    joechang
 * Created:   7/29/15 11:22 AM
 * Purpose:
 */
public class PhonePiper extends CliBodyPiper<AndroidCommandLineProcessor> {
    public static final String PHONE_PHRASE = "@phone";

    private static final String PHONE_NUMBER_REGEX = "((\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4})(?:\\s|$)";
    private static final Pattern numberMatcher = Pattern.compile(PHONE_NUMBER_REGEX);

    public PhonePiper(AndroidCommandLineProcessor clp) {
        super(clp, PHONE_PHRASE);
    }

    @Override
    public boolean isConsumesResponse() {
        //Once the phone number comes to this piper, we dial the number and DO NOT SEND A RESPONSE TEXT.
        return true;
    }

    @Override
    public void doOnFind(String[] address, String body, CliProcessedCallback cb) {
        Matcher m = numberMatcher.matcher(body);

        if (m.find()) {
            String number = m.group();

            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getCommandLineProcessor().getContext().startActivity(intent);
        }
    }

}
