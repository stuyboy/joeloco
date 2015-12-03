package com.joechang.kursor;

import com.joechang.kursor.commands.*;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.Search;
import com.joechang.loco.user.UserManager;
import com.joechang.loco.utils.AddressUtils;
import com.joechang.loco.utils.Stopwatch;
import com.joechang.loco.utils.StringUtils;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   8/12/15 10:17 AM
 * Purpose:   Breaking out the commandline functionality into a new class, separate from the SMS stuff.
 * That way we can use an app, sms, or even email to peform the functions.
 */
public class CommandLineProcessor {
    private Logger log = Logger.getLogger(CommandLineProcessor.class.getName());

    private CliOutputWriter mWriter;
    private Set<AbstractCliBodyProcessor> bodyProcessors = new LinkedHashSet<>();
    private Set<CliBodyPiper> redirectors = new LinkedHashSet<>();
    private Set<String> blackListedAddresses = new LinkedHashSet<>();

    //Special
    private HistoryBodyProcessor mHistoryProcessor;
    private UserManager mUserManager;

    private Map<Integer, Stack<String>> responseHistory = new HashMap<Integer, Stack<String>>();

    public CommandLineProcessor(CliOutputWriter cow) {
        mWriter = cow;

        //Special use of history
        mHistoryProcessor = new HistoryBodyProcessor(this);
        addBodyProcessor(mHistoryProcessor);

        mUserManager = new UserManager();

        initProcessors();
    }


    public CliOutputWriter getCliOutputWriter() {
        return mWriter;
    }

    protected void initProcessors() {
        //Add all body processors here.
        //bodyProcessors.add(new LocationCliBodyProcessor(this));
        addBodyProcessor(new SingularBodyProcessor(this));
        addBodyProcessor(new YelpCliBodyProcessor(this));
        addBodyProcessor(new OpenTableCliBodyProcessor(this));
        addBodyProcessor(new GiphyCliBodyProcessor(this));

        //Redirectors
        //addPiper(new PhonePiper(this));
        //addPiper(new EmailPiper(this));
        addPiper(new OpenTablePiper(this));
    }

    /**
     * The primary method which iterates through all registered processors, seeing if their key phrase is
     * contained in the outgoing/incoming message.  Also layered in is the pipe directive, so if we find a pipe phrase,
     * then insert it into processing of the main handler.
     *
     * @param id
     * @param address
     * @param body
     */
    public boolean processMessage(String id, String address, String body) {
        return processMessage(id, new String[]{address}, body);
    }

    public boolean processMessage(String id, String[] addresses, String body) {
        boolean wasProcessed = false;

        String[] cleanAddrs = AddressUtils.cleanPhoneNumbers(addresses);
        String[] prunedAddrs = removeBlackListedAddresses(cleanAddrs);

        //Handle creating any new users in a separate thread.
        handleUsers(prunedAddrs);

        Stopwatch.log("Processing message: " + id + ", assumed from: " + Arrays.toString(addresses));

        if (prunedAddrs.length <= 0) {
            log.info("No numbers found after pruning.");
            return wasProcessed;
        }

        CliProcessedCallback cb = getPipeCallback(body);
        if (cb != null) {
            body = cb.removeFromBody(body);
            log.info(String.format("Found pipe %s, removed resulting msg: %s", cb, body));
        }

        Stopwatch.start("BodyProcessing");
        for (AbstractCliBodyProcessor bp : bodyProcessors) {
            boolean isToDo = bp.processBody(prunedAddrs, body, cb);
            if (isToDo) {
                wasProcessed = true;
            }
        }
        Stopwatch.stop("BodyProcessing");

        return wasProcessed;
    }

    protected String[] removeBlackListedAddresses(String[] addrs) {
        ArrayList<String> ret = new ArrayList<>();
        for (String s : addrs) {
            boolean addNumber = true;
            for (String bl : blackListedAddresses) {
                if (bl.contains(s)) {
                    addNumber = false;
                    log.info("Abandon: Server number is involved: " + s);
                    return new String[]{};
                }
            }
            if (addNumber) {
                ret.add(s);
            }
        }
        return ret.toArray(new String[]{});
    }

    /**
     * Currently only one pipe accepted per entry.
     *
     * @param body
     * @return null if no pipe found, otherwise a full callback!
     */
    protected CliProcessedCallback getPipeCallback(String body) {
        for (final CliBodyPiper pipe : redirectors) {
            if (pipe.bodyHasPhrase(body)) {
                return pipe;
            }
        }

        return null;
    }

    public void doOutput(String[] address, Search s, String[] body) {
        if (mWriter != null) {
            if (body == null || body.length == 0 || (s != null && s.getResponse() == null)) {
                mWriter.outputNotFound(address, CliOutputHelper.formatNotFound(s));
                return;
            }

            //No blank line
            body = StringUtils.stripLastNewLine(body);

            mWriter.outputCommandResponse(address, body);
        }
    }

    public void doOutput(String[] address, Search s, String body) {
        doOutput(address, s, new String[]{body});
    }

    public void doOutput(String[] address, Search s, URL resource) {
        if (mWriter != null) {
            if (resource == null) {
                mWriter.outputNotFound(address, CliOutputHelper.formatNotFound(s));
                return;
            }

            mWriter.outputCommandResponse(address, resource);
        }
    }

    public void doError(String[] address, String errorMessage) {
        mWriter.outputNotFound(address, errorMessage);
    }

    //These methods should be persisting to the cloud.
    public void pushHistoryStack(String[] addresses, String json) {
        int h = historyHash(addresses);
        if (responseHistory.get(h) == null) {
            responseHistory.put(h, new Stack<String>());
        }
        responseHistory.get(h).push(json);
    }

    public String popHistoryStack(String[] addresses) {
        int h = historyHash(addresses);
        if (responseHistory.get(h) == null) {
            return null;
        }
        return responseHistory.get(h).peek();
    }

    protected int historyHash(String[] addresses) {
        SortedSet<String> ss = new TreeSet<>(Arrays.asList(addresses));
        return ss.hashCode();
    }

    /**
     * See if the incoming search string is a number.  If so, try to process history.  Otherwise just return the orig.
     * @param addresses
     * @param searchString
     * @return
     */
    public String handleHistory(String[] addresses, String searchString) {
        try {
            int historyInt = Integer.parseInt(searchString.trim()) - 1;
            return mHistoryProcessor.processHistoryResult(addresses, historyInt);
        } catch (Exception nfe) {
            //no-op
        }

        return searchString;
    }

    /**
     * In the background, add any new users that we may have missed.  Possible caching in the future.
     * @param addresses
     */
    public void handleUsers(final String[] addresses) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mUserManager.addNewUsers(addresses);
            }
        });
    }

    //Platform specific
    public Double[] getLatLng() {
        return new Double[]{null, null};
    }

    public void addBlackListedAddress(String address) {
        blackListedAddresses.add(address);
    }

    public void addBodyProcessor(AbstractCliBodyProcessor bp) {
        this.bodyProcessors.add(bp);
    }

    public void addPiper(CliBodyPiper cbp) {
        this.redirectors.add(cbp);
    }
}
