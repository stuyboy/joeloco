package com.joechang.kursor;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author:    joechang
 * Created:   6/14/15 4:57 PM
 * Purpose:   Abstract base class that is parent of all sms body processors.
 */
public abstract class AbstractCliBodyProcessor<T extends CommandLineProcessor> {
    protected Logger log = Logger.getLogger(this.getClass().getName());

    private T mClp;
    private String mLocoPhrase;
    protected Pattern mPattern = null;

    public AbstractCliBodyProcessor(T cli, String locoPhrase) {
        mClp = cli;
        mLocoPhrase = locoPhrase;
        mPattern = Pattern.compile(locoPhrase);
    }

    public boolean bodyHasPhrase(String body) {
        return mPattern.matcher(body).find();
    }

    public boolean processBody(String[] address, String body, CliProcessedCallback cb) {
        boolean foundSomethingTodo = false;

        if (mLocoPhrase == null) {
            throw new IllegalArgumentException("Processor cannot work without phrase.");
        }

        try {
            if (body != null) {
                if (bodyHasPhrase(body)) {
                    foundSomethingTodo = true;
                    doOnFind(address, body, cb);
                } else {
                    log.info("Did not find " + mLocoPhrase + " within " + body + " hasPhrase: " + bodyHasPhrase(body));
                }
            }
        } catch (Exception ee) {
            log.log(
                    java.util.logging.Level.SEVERE,
                    "Could not process body " + body,
                    ee
            );
        }

        return foundSomethingTodo;
    }

    public T getCommandLineProcessor() {
        return mClp;
    }

    protected String[] doRegex(Pattern p, String body) {
        Matcher m = p.matcher(body);

        //Group 0 is always the whole regular expression.
        int totalGroups = m.groupCount();

        String[] r = new String[totalGroups];
        if (totalGroups > 0) {
            if (m.find()) {
                for (int i = 1; i <= totalGroups; i++) {
                    r[i - 1] = m.group(i);
                }
            }
            return r;
        }

        return null;
    }

    /**
     * With the address (phone #) and the body of the text, do what needs to be done, and return what you sent.
     * @param address array of addresses to send to.
     * @param body
     */
    public abstract void doOnFind(String[] address, String body, CliProcessedCallback callback);
}
