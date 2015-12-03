package com.joechang.kursor.commands;

import com.joechang.kursor.AbstractCliBodyProcessor;
import com.joechang.kursor.CliProcessedCallback;
import com.joechang.loco.contacts.ContactsUtils;
import com.joechang.kursor.AndroidCommandLineProcessor;
import com.joechang.loco.service.SendLocationService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author:    joechang
 * Created:   6/14/15 5:17 PM
 * Purpose:   Able to use joeloco via #loco within text messages
 */
public class LocationCliBodyProcessor extends AbstractCliBodyProcessor<AndroidCommandLineProcessor> {
    public static final String LOCATION_PHRASE = "@loco";
    private static final String LOCATION_DURATION_REGEX = LOCATION_PHRASE + "([0-9]*)";
    private static final Pattern mMatcher = Pattern.compile(LOCATION_DURATION_REGEX);

    public LocationCliBodyProcessor(AndroidCommandLineProcessor clp) {
        super(clp, LOCATION_PHRASE);
    }

    @Override
    public void doOnFind(String[] cleaneds, String body, CliProcessedCallback cb) {
        Integer duration = regexForDuration(body);

        for (String cls : cleaneds) {
            SendLocationService.launchIntent(getCommandLineProcessor().getContext(), ContactsUtils.Type.TEXT, cls, duration);
        }

        if (cb != null) {
            cb.onCommandProcessed(cleaneds, body);
        }
    }

    public Integer regexForDuration(String body) {
        Matcher m = mMatcher.matcher(body);
        if (m.find()) {
            //Group 0 is always the whole regular expression.
            for (int i = 1; i <= m.groupCount(); i++) {
                try {
                    return Integer.valueOf(m.group(i));
                } catch (NumberFormatException nfe) {
                    //no-reason to freak out, keep going.
                }
            }
        }
        return null;
    }

}
