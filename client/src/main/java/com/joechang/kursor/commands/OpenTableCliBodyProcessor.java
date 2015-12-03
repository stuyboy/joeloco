package com.joechang.kursor.commands;

import com.joechang.kursor.AbstractCliBodyProcessor;
import com.joechang.kursor.CliProcessedCallback;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.loco.client.RestClientExceptionWrapper;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.client.ThirdPartyClient;
import com.joechang.loco.model.Search;
import com.joechang.loco.utils.ArrayUtils;
import com.joechang.loco.utils.StringUtils;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Author:    joechang
 * Created:   6/14/15 5:17 PM
 * Purpose:   Able to use joeloco via @rez within text messages.  Sends request to joeloco server, which proxies.
 */
public class OpenTableCliBodyProcessor extends AbstractCliBodyProcessor {
    public static final String KEYWORDS = "(?:"
                                        + "\\s+on|"
                                        + "\\s+near|"
                                        + "\\s+in|"
                                        + "\\s+for|"
                                        + "\\s+today|"
                                        + "\\s+tonight|"
                                        + "\\s+tomorrow|"
                                        + "\\s+this|"
                                        + "\\s+next|"
                                        + "$)";

    public static final String REZ_PHRASE = "(?:@rez|@opentable)(\\d*)";

    //What do we want to eat.
    private static final String SEARCH_WHAT = REZ_PHRASE + "\\s+(.+?)" + KEYWORDS; //(?:\\s+(?:in|near)\\s+|$)(.+)?";
    private static final Pattern mWhatMatcher = Pattern.compile(SEARCH_WHAT);

    //Where do we want to eat
    private static final String SEARCH_WHERE = "(?:in|near)\\s+(.+?)" + KEYWORDS;
    private static final Pattern mWhereMatcher = Pattern.compile(SEARCH_WHERE);

    //How many in party
    private static final String SEARCH_HOWMANY = "(?:for)\\s+(\\d+?)" + KEYWORDS;
    private static final Pattern mManyMatcher = Pattern.compile(SEARCH_HOWMANY);

    //What date
    private static final String SEARCH_WHEN =
            "\\son\\s+(.+?)" + KEYWORDS
                    + "|(today.*?)" + KEYWORDS
                    + "|(tonight.*?)" + KEYWORDS
                    + "|(tomorrow.*?)" + KEYWORDS
                    + "|(this.*?)" + KEYWORDS
                    + "|(next.*?)" + KEYWORDS;

    private static final Pattern mWhenMatcher = Pattern.compile(SEARCH_WHEN);


    private static Logger log = Logger.getLogger(OpenTableCliBodyProcessor.class.getName());

    public OpenTableCliBodyProcessor(CommandLineProcessor clp) {
        super(clp, REZ_PHRASE);
    }

    @Override
    public void doOnFind(final String[] cleaned, String body, final CliProcessedCallback cb) {
        String[] search = regexForSearchString(body);

        //Result size
        String sizeStr = search[0];

        //Search phrase, which could be a number referring to previous entry.
        String what = getCommandLineProcessor().handleHistory(cleaned, search[1]);

        //If we're handling a numeric, then set results to 1 for a detailed view.
        if (!what.equalsIgnoreCase(search[1])) {
            sizeStr = "1";
        }

        String where = regexForWhere(body)[0];
        String when = regexForWhen(body)[0];
        Integer howmany = regexForPartySize(body);

        log.info(String.format("Sending into opentable %s and size %s", Arrays.toString(search), sizeStr));

        //If we're handling a numeric, then set results to 1 for a detailed view.
        final Integer requestedSize = StringUtils.isEmpty(sizeStr) ? null : Integer.parseInt(sizeStr);

        ThirdPartyClient tpc = RestClientFactory.getInstance().getThirdPartyClient();
        tpc.searchOpentable(
                null,       //what
                where,
                when,
                howmany,
                what,
                requestedSize,
                new ThirdPartyClient.Callback() {
                    @Override
                    public void success(Search s, Response response) {
                        String[] resps = s.getResponse();

                        //This is where we need to generalize with writers, or senders of some sort.
                        if (cb == null || !cb.isConsumesResponse()) {
                            Logger.getLogger(getClass().getName()).info("Sending text " + Arrays.toString(s.getResponse()) + " to " + Arrays.toString(cleaned));
                            if (requestedSize == null || requestedSize > 1) {
                                getCommandLineProcessor().pushHistoryStack(cleaned, s.getJson());
                            }
                            String[] header = { s.getQuery() };
                            getCommandLineProcessor().doOutput(cleaned, s, ArrayUtils.addAll(header, resps));
                        }

                        if (cb != null) {
                            cb.onCommandProcessed(cleaned, StringUtils.join("\n", resps));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        RestClientExceptionWrapper wrap = RestClientExceptionWrapper.from(error);
                        getCommandLineProcessor().doError(cleaned, wrap.getMessage());
                    }
                });
    }

        protected String[] regexForSearchString(String body) {
            return doRegex(mWhatMatcher, body);
        }

    protected String[] regexForWhere(String body) {
        return doRegex(mWhereMatcher, body);
    }

    protected String[] regexForWhen(String body) {
        String[] candidates = doRegex(mWhenMatcher, body);
        for (String c : candidates) {
            if (c != null)
                return new String[]{c};
        }
        return candidates;
    }

    protected Integer regexForPartySize(String body) {
        String[] r = doRegex(mManyMatcher, body);
        if (r[0] == null)
            return null;
        return Integer.parseInt(r[0]);
    }
}
