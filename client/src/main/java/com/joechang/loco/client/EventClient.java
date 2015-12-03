package com.joechang.loco.client;

import com.joechang.loco.config.Routes;
import com.joechang.loco.model.Event;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Author:    joechang
 * Created:   5/29/15 3:55 PM
 * Purpose:
 */
public interface EventClient {

    @GET(Routes.EVENT_BY_ID)
    public void getEvent(
            @Path(Event.ID) String eventId,
            EventClient.Callback callback
    );


    public interface Callback extends retrofit.Callback<Event> {}
}
