package com.joechang.kursor;

import java.net.URL;

/**
 * Author:    joechang
 * Created:   8/12/15 10:44 AM
 * Purpose:
 */
public interface CliOutputWriter {
    //Write out to the interface (sms, app, email, whatever) the response.  Send to destination.
    public void outputCommandResponse(String[] destinations, String[] response);

    //Write out to the interface that we didn't get any results.
    public void outputNotFound(String[] destinations, String message);

    //What happens to a media item that needs to be output?
    public void outputCommandResponse(String[] destinations, URL resource);

}
