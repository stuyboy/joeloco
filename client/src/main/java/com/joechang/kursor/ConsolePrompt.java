package com.joechang.kursor;

import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.firebase.FirebaseManager;

import java.net.URL;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

/**
 * Author:    joechang
 * Created:   9/17/15 1:48 PM
 * Purpose:
 */
public class ConsolePrompt implements CliOutputWriter {
    private Scanner scanner = new Scanner(System.in).useDelimiter("\n");
    private CommandLineProcessor clp;
    private CountDownLatch cdl;

    public ConsolePrompt() {
        //Possibly put these somewhere else?
        LogManager.getLogManager().reset();
        FirebaseManager.init();
        RestClientFactory.useDebugServer(true);
        RestClientFactory.noLogging();

        clp = new CommandLineProcessor(this);
    }

    public static void main(String[] args) {
        ConsolePrompt cp = new ConsolePrompt();
        try {
            cp.doInput();
        } catch (InterruptedException ie) {
            //no-op
        }
    }

    public void doInput() throws InterruptedException {
        String input = "";
        boolean didSomething = false;

        while (!input.equalsIgnoreCase("exit")) {
            System.out.print("> ");
            input = scanner.next();
            cdl = new CountDownLatch(1);
            didSomething = clp.processMessage(UUID.randomUUID().toString(), "KursorConsole", input);
            if (didSomething) {
                cdl.await(30, TimeUnit.SECONDS);
            }
        }

        System.exit(0);
    }

    public void releaseLatch() {
        if (cdl != null) {
            while (cdl.getCount() > 0) {
                cdl.countDown();
            }
        }
    }

    @Override
    public void outputCommandResponse(String[] destinations, String[] response) {
        for (String s : response) {
            System.out.println(s);
        }
        releaseLatch();
    }

    @Override
    public void outputNotFound(String[] destinations, String message) {
        System.out.println(message);
        releaseLatch();
    }

    @Override
    public void outputCommandResponse(String[] destinations, URL resource) {
        System.out.println(resource.toString());
        releaseLatch();
    }
}
