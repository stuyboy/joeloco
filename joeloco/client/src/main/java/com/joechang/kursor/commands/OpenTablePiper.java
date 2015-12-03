package com.joechang.kursor.commands;

import com.joechang.kursor.CliBodyPiper;
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

/**
 * Author:    joechang
 * Created:   7/29/15 11:22 AM
 * Purpose:   So far the most complex piper in that it must take in the output of a yelp, for example, and then
 * find the name of the restaurant, and send it to opentable.  Should we think about returning the JSON vs. the text
 * output?
 */
public class OpenTablePiper extends CliBodyPiper {
    public static final String PIPE_PHRASE = OpenTableCliBodyProcessor.REZ_PHRASE;

    private ThirdPartyClient tpc;

    public OpenTablePiper(CommandLineProcessor clp) {
        super(clp, PIPE_PHRASE);
        tpc = RestClientFactory.getInstance().getThirdPartyClient();
    }

    @Override
    public boolean isConsumesResponse() {
        //Once the phone number comes to this piper, we let Opentable handle the rest.
        return true;
    }

    @Override
    public void doOnFind(final String[] address, String body, final CliProcessedCallback cb) {
        //A lot of faith here.
        if (body == null || body.indexOf('\n') <= 0) {
            getCommandLineProcessor().getCliOutputWriter().outputNotFound(address, null);
        }

        //Wow wow, this is ugly.  Need to figure out a different way of passing values. TODO
        String hopefullyName = body.substring(body.indexOf("] ") + 2, body.indexOf('\n'));
        String city = "san francisco";

        tpc.searchOpentable(hopefullyName, city, 2, new ThirdPartyClient.Callback() {
            @Override
            public void success(Search s, Response response) {
                String[] resps = s.getResponse();
                //This is where we need to generalize with writers, or senders of some sort.
                if (cb == null || !cb.isConsumesResponse()) {
                    Logger.getLogger(getClass().getName()).info("Sending text " + Arrays.toString(s.getResponse()) + " to " + address);
                    getCommandLineProcessor().doOutput(address, s, resps);
                }


                if (cb != null) {
                    cb.onCommandProcessed(address, StringUtils.join("\n", resps));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                RestClientExceptionWrapper wrap = RestClientExceptionWrapper.from(error);
                getCommandLineProcessor().doError(address, wrap.getMessage());
            }
        });
    }
}
