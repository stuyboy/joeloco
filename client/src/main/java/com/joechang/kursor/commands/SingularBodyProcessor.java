package com.joechang.kursor.commands;

import com.joechang.kursor.AbstractCliBodyProcessor;
import com.joechang.kursor.CliProcessedCallback;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.loco.model.Search;
import com.joechang.loco.utils.StringUtils;

import java.util.logging.Logger;

/**
 * Created by thadhwang on 10/2/15.
 */
public class SingularBodyProcessor extends AbstractCliBodyProcessor {
    public static final String EXP_HELP = "@help";
    public static final String EXP_WELCOME = "@welcome";
    public static final String EXP_YELP = "@yelp";
    public static final String EXP_OPENTABLE = "@opentable";
    public static final String EXP_OPENTABLE_REZ = "@rez";
    public static final String ASSISTANT_PHRASE = "(?:"+EXP_HELP+"|"+EXP_WELCOME+"|"+EXP_YELP+"|"+EXP_OPENTABLE+"|"+EXP_OPENTABLE_REZ+"|"+")";
    private static Logger log = Logger.getLogger(SingularBodyProcessor.class.getName());

    public SingularBodyProcessor(CommandLineProcessor clp) {
        super(clp, ASSISTANT_PHRASE);
    }

    @Override
    public void doOnFind(final String[] cleaned, String body, final CliProcessedCallback cb) {
        Search emptySearch = new Search();
        emptySearch.response = new String[0];
        String message = "";

        switch (body) {
            case EXP_HELP:
                message = StringUtils.autoBreak(
                    "Kursor commands:",
                    "@yelp <restaurant name or category> (in or near) <location>",
                    "e.g. @yelp pizza in SOMA",
                    "",
                    "@yelp <number choice> (after a list of results)",
                    "e.g. @yelp 1",
                    "",
                    "@opentable <restaurant name or category> <day: today, tonight, or tomorrow> for <# of people>",
                    "e.g. @opentable marlowe tonight for 4"
                );

                break;
            case EXP_WELCOME:
                message = StringUtils.autoBreak(
                    "Hello from Kursor!",
                    "",
                    "Kursor is a smart assistant that helps you quickly find information directly within your text messages. All you need to do is include the following phone number in your messages for Kursor to help out: 415-312-2379.",
                    "",
                    "Tap and hold the number to add it to your contact list!",
                    "",
                    "You can do things like get yelp reviews.",
                    "@yelp pizza in SOMA",
                    "@yelp delfina",
                    "",
                    "Text @help for a full list of what you can do. Give it a try!"
                );
                break;
            case EXP_YELP:
                message = StringUtils.autoBreak(
                    "Yelp commands:",
                    "@yelp <restaurant name or category> (in or near) <location>",
                    "e.g. @yelp pizza in SOMA",
                    "",
                    "After you see a list of results, to receive detailed Yelp information about the restaurant, you can text:",
                    "@yelp <number choice>",
                    "e.g. @yelp 1",
                    "",
                    "Text @help for a full list of what you can do."
                );
                break;
            case EXP_OPENTABLE:
                message = StringUtils.autoBreak(
                    "OpenTable commands:",
                    "@opentable <restaurant name or category> <day: today, tonight, or tomorrow> for <# of people>",
                    "e.g. @opentable marlowe tonight for 4",
                    "",
                    "Text @help for a full list of what you can do."
                );
                break;
            case EXP_OPENTABLE_REZ:
                message = StringUtils.autoBreak(
                    "OpenTable commands:",
                    "@rez <restaurant name or category> <day: today, tonight, or tomorrow> for <# of people>",
                    "e.g. @rez marlowe tonight for 4",
                    "",
                    "Text @help for a full list of what you can do."
                );
                break;
            default:
                break;
        }

        if (!StringUtils.isEmpty(message)) {
            getCommandLineProcessor().doOutput(cleaned, emptySearch, message);

            //Not quite sure what a callback would do here.  But avoid NPE.
            if (cb != null) {
                cb.onCommandProcessed(cleaned, message);
            }
        }
    }
}
