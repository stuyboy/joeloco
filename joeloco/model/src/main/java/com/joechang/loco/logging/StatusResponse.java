package com.joechang.loco.logging;

import com.joechang.loco.model.Event;
import com.joechang.loco.service.Stagnancy;

import java.util.Collection;

/**
 * Created by joechang on 5/12/15.
 */
public class StatusResponse {
    public static final int OK = 200;
    public static final int MALFORMED = 303;

    private int response = -1;
    private Stagnancy requestedStagnancy = Stagnancy.IDLE;
    private Collection<Event> activeEvents;

    public StatusResponse(int response) {
        this.response = response;
    }

    public StatusResponse(int response, Stagnancy requestedStagnancy) {
        this.response = response;
        this.requestedStagnancy = requestedStagnancy;
    }

    public int getResponse() {
        return response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public Stagnancy getRequestedStagnancy() {
        return requestedStagnancy;
    }

    public void setRequestedStagnancy(Stagnancy requestedStagnancy) {
        this.requestedStagnancy = requestedStagnancy;
    }

    public Collection<Event> getActiveEvents() {
        return activeEvents;
    }

    public void setActiveEvents(Collection<Event> activeEvents) {
        this.activeEvents = activeEvents;
    }
}
