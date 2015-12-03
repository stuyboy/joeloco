package com.joechang.kursor.commands;

import com.joechang.kursor.CliBodyPiper;
import com.joechang.kursor.CliProcessedCallback;
import com.joechang.kursor.AndroidCommandLineProcessor;
import com.joechang.loco.utils.EmailUtils;

import java.util.regex.Pattern;

/**
 * Author:    joechang
 * Created:   7/29/15 11:22 AM
 * Purpose:
 */
public class EmailPiper extends CliBodyPiper<AndroidCommandLineProcessor> {
    public static final String EMAIL_PHRASE = "@email";

    private static final String PHONE_NUMBER_REGEX = "((\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4})(?:\\s|$)";
    private static final Pattern numberMatcher = Pattern.compile(PHONE_NUMBER_REGEX);

    public EmailPiper(AndroidCommandLineProcessor clp) {
        super(clp, EMAIL_PHRASE);
    }

    @Override
    public boolean isConsumesResponse() {
        //Once the phone number comes to this piper, we email and DO NOT SEND A RESPONSE TEXT.
        return true;
    }

    @Override
    public void doOnFind(String[] address, String body, CliProcessedCallback cb) {
        EmailUtils.launchEmailDialog(
                getCommandLineProcessor().getContext(),
                null,
                null,
                body
        );
    }

}