package com.joechang.kursor;

import com.joechang.loco.utils.StringUtils;
import org.junit.Test;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Author:    joechang
 * Created:   9/17/15 1:10 PM
 * Purpose:
 */
public class CommandLineProcessorTest {
    CommandLineProcessor clp = new CommandLineProcessor(new TestOutputWriter());
    CountDownLatch asyncHold;

    @Test
    public void testCommandLine() throws InterruptedException {
        asyncHold = new CountDownLatch(1);
        clp.processMessage("" + System.currentTimeMillis(),
                           "415-504-6106",
                           "@yelp haircut in mission"
        );
        asyncHold.await(100, TimeUnit.SECONDS);
    }

    class TestOutputWriter implements CliOutputWriter {
        @Override
        public void outputCommandResponse(String[] destinations, String[] response) {
            System.out.println("Sending to " + StringUtils.join(",", destinations) + ":");
            System.out.println(StringUtils.join("\n", response));
            System.out.println("\n");
            asyncHold.countDown();
        }

        @Override
        public void outputNotFound(String[] destinations, String message) {
            System.out.println("Error to " + StringUtils.join(",", destinations) + ":");
            System.out.println(message);
            System.out.println("\n");
            asyncHold.countDown();
        }

        @Override
        public void outputCommandResponse(String[] destinations, URL resource) {
            outputCommandResponse(destinations, new String[] { resource.toString() });
        }
    }
}
