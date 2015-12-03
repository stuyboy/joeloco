package com.joechang.kursor;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   8/12/15 12:51 PM
 * Purpose:
 */
public class CommandLineProcessorTests {
    private Logger log = Logger.getLogger(CommandLineProcessorTests.class.getName());
    private CountDownLatch asyncLock = new CountDownLatch(1);

    public void testCommandLine() throws InterruptedException {
        CommandLineProcessor clp = new CommandLineProcessor(new TestOutputter());
        String id = "" + System.currentTimeMillis();
        String address = "4153992597";
        clp.processMessage(id, address, "@yelp2 aziza in sf,ca & @opentable");

        asyncLock.await(5000, TimeUnit.MILLISECONDS);
    }

    class TestOutputter implements CliOutputWriter {

        @Override
        public void outputCommandResponse(String[] destination, String[] response) {
            //log.info("To " + destination + ":" + response);
            asyncLock.countDown();
        }

        @Override
        public void outputNotFound(String[] destination, String e) {
            outputCommandResponse(destination, new String[]{"Not found."});
        }

        @Override
        public void outputCommandResponse(String[] destinations, URL resource) {
            asyncLock.countDown();
        }
    }
}
