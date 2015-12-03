package com.joechang.kursor.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.joechang.kursor.AbstractCliBodyProcessor;
import com.joechang.kursor.CliProcessedCallback;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.loco.config.Params;

/**
 * Author:    joechang
 * Created:   9/18/15 2:17 PM
 * Purpose:
 */
public class HistoryBodyProcessor extends AbstractCliBodyProcessor<CommandLineProcessor> {

    public static final String HISTORY_PHRASE = "@history\\s*(\\d*)";

    public HistoryBodyProcessor(CommandLineProcessor cli) {
        super(cli, HISTORY_PHRASE);
    }

    @Override
    public void doOnFind(String[] address, String body, CliProcessedCallback callback) {
        //getCommandLineProcessor().doOutput(address, null, getCommandLineProcessor().popHistoryStack());
        String[] n = doRegex(mPattern, body);
        try {
            Integer idx = Integer.parseInt(n[0]) - 1;
            getCommandLineProcessor().doOutput(address, null, processHistoryResult(address, idx));
        } catch (Exception e) {
            getCommandLineProcessor().doError(address, e.getMessage());
        }
    }

    public String processHistoryResult(String[] address, Integer idx) throws Exception {
        String lastEntry = getCommandLineProcessor().popHistoryStack(address);
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(lastEntry);
        if (je.isJsonArray()) {
            je = je.getAsJsonArray().get(idx);
        }
        return je.getAsJsonObject().get(Params.NAME).getAsString();
    }
}
