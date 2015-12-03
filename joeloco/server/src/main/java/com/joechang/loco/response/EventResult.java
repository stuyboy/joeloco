package com.joechang.loco.response;

import com.joechang.loco.model.Event;

/**
 * Created by joechang on 5/13/15.
 */
public class EventResult extends AbstractDeferredResult<Event> {

    @Override
    public Class baseClass() {
        return Event.class;
    }

}
