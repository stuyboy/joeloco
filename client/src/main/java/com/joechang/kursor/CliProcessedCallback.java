package com.joechang.kursor;

/**
 * Author:    joechang
 * Created:   7/29/15 11:43 AM
 * Purpose:
 */
public interface CliProcessedCallback {

    //Once line is processed, this method is called, with message and address passed in.
    public void onCommandProcessed(String[] addresses, String message);

    //Remove the callback/pipe from the body of the message.
    public String removeFromBody(String body);

    //Should this callback consume the preceding message?  ie, should we still send a text?
    public boolean isConsumesResponse();

}
