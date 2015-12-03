package com.joechang.kursor.commands;

import com.joechang.kursor.AbstractCliBodyProcessor;
import com.joechang.kursor.CliProcessedCallback;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.loco.client.RestClientExceptionWrapper;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.client.ThirdPartyClient;
import com.joechang.loco.model.Search;
import com.joechang.loco.utils.StringUtils;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Author:    joechang
 * Created:   6/14/15 5:17 PM
 * Purpose:   Able to use joeloco via #yelp within text messages.  Sends request to joeloco server, which proxies.
 */
public class YelpCliBodyProcessor extends AbstractCliBodyProcessor {
    public static final String YELP_PHRASE = "(?:@yelp|@review)(\\d*)";
    private static final String SEARCH_YELP_REGEX = YELP_PHRASE + " (.+?)(?:\\s+(?:in|near)\\s+|$)(.+)?";
    private static final Pattern mMatcher = Pattern.compile(SEARCH_YELP_REGEX);
    private static Logger log = Logger.getLogger(YelpCliBodyProcessor.class.getName());

    public YelpCliBodyProcessor(CommandLineProcessor clp) {
        super(clp, YELP_PHRASE);
    }

    @Override
    public void doOnFind(final String[] cleaned, String body, final CliProcessedCallback cb) {
        String[] search = doRegex(body);
        Double[] latLng = getCommandLineProcessor().getLatLng();

        String where = search[2];
        String searchStr = search[1];
        String sizeStr = search[0];

        //Support the history functionality.
        String what = getCommandLineProcessor().handleHistory(cleaned, searchStr);

        //If we're using a history result, limit to 1 to get the detail output
        if (!what.equalsIgnoreCase(searchStr)) {
            sizeStr = "1";
        }

        //If we're handling a numeric, then set results to 1 for a detailed view.
        final Integer requestedSize = StringUtils.isEmpty(sizeStr) ? null : Integer.parseInt(sizeStr);

        //log.info(String.format("Sending into yelp %s and ll %s and size %s", Arrays.toString(search), Arrays.toString(latLng), size));
        ThirdPartyClient tpc = RestClientFactory.getInstance().getThirdPartyClient();
        tpc.searchYelp(
                what,
                where,
                latLng[0],  //lat
                latLng[1],  //lng
                requestedSize,
                new ThirdPartyClient.Callback() {
                    @Override
                    public void success(Search s, Response response) {
                        String[] resps = s.getResponse();

                        //This is where we need to generalize with writers, or senders of some sort.
                        if (cb == null || !cb.isConsumesResponse()) {
                            log.info("Sending text " + Arrays.toString(s.getResponse()) + " to " + Arrays.toString(cleaned));
                            if (requestedSize == null || requestedSize > 1) {
                                getCommandLineProcessor().pushHistoryStack(cleaned, s.getJson());
                            }
                            getCommandLineProcessor().doOutput(cleaned, s, resps);
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

    public String[] doRegex(String body) {
        return super.doRegex(mMatcher, body);
    }
}
