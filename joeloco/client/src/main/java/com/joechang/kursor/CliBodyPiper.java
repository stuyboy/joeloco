package com.joechang.kursor;

import java.util.regex.Matcher;

/**
 * Author:    joechang
 * Created:   7/29/15 5:35 PM
 * Purpose:
 */
public abstract class CliBodyPiper<T extends CommandLineProcessor> extends AbstractCliBodyProcessor<T> implements CliProcessedCallback {

    public static final String PIPE_REGEX = "\\s*\\&\\s*";

    public CliBodyPiper(T clp, String locoPhrase) {
        super(
                clp,
                "(" +
                PIPE_REGEX +
                locoPhrase +
                ")"
        );
    }

    public String getMatchedPhrase(String body) {
        Matcher m = mPattern.matcher(body);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    @Override
    public void onCommandProcessed(String[] address, String message) {
        doOnFind(address, message, null);
    }

    @Override
    public String removeFromBody(String body) {
        String f = getMatchedPhrase(body);
        if (f != null) {
            return body.replace(f, "");
        }
        return body;
    }
}
