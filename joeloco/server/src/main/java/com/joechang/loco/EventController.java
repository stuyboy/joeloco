package com.joechang.loco;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.joechang.loco.config.Routes;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.User;
import com.joechang.loco.response.AbstractDeferredResult;
import com.joechang.loco.response.ResourceNotFoundException;
import com.joechang.loco.response.UserResult;
import com.joechang.loco.service.EventMonitoringService;
import com.joechang.loco.response.EventResult;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by joechang on 5/13/15.
 */
@RestController
public class EventController {

    private EventMonitoringService mEMS;
    private FirebaseManager firebaseManager;

    @RequestMapping(Routes.EVENT_BY_ID)
    public EventResult getEvents(
            @PathVariable(value = Event.ID) String eventId
    ) throws ExecutionException {
        EventResult er = new EventResult();

        if (eventId != null) {
            er = mEMS.getEvent(eventId);
        } else {
            er.notFound(eventId);
        }

        return er;
    }

    @RequestMapping(Routes.EVENTS_FOR_USER)
    public EventResult.Set getEventsForUser(
        @PathVariable(value = User.ID) String userId
    ) throws ExecutionException {
        if (userId != null) {
            return mEMS.getEventsForUserId(userId);
        }

        return (new EventResult().set()).empty();
    }

    /**
     * OH MY GOD.
     * @param eventId
     * @return
     * @throws ExecutionException
     */
    @RequestMapping(Routes.EVENT_USERS)
    public DeferredResult<Collection<String>> getUsersForEvent(
            @PathVariable(value = Event.ID) final String eventId
    ) throws ExecutionException {
        final DeferredResult<Collection<String>> result = new DeferredResult<>();
        firebaseManager.getEventFirebase(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Event e = dataSnapshot.getValue(Event.class);
                    if (e.isValidNow()) {
                        String groupId = e.getGroupId();
                        Firebase ff = firebaseManager.getGroupMembersFirebase(groupId);
                        ff.addListenerForSingleValueEvent(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Map<String, String> existingUsers = (Map<String, String>) dataSnapshot.getValue();
                                result.setResult(existingUsers.keySet());
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                result.setErrorResult(null);
                            }
                        });
                    }

                    if (DateTime.now().toDate().before(e.getDateStart())) {
                        result.setErrorResult(new ResourceNotFoundException(50, "Event has not started yet"));
                    } else if (DateTime.now().toDate().after(e.getDateEnd())) {
                        result.setErrorResult(new ResourceNotFoundException(60, "Event has expired"));
                    }

                    return;
                }

                AbstractDeferredResult.notFound(result, eventId);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                AbstractDeferredResult.notFound(result, eventId);
            }
        });

        return result;
    }

    @Autowired
    public void setEventMonitoringService(EventMonitoringService ems) {
        this.mEMS = ems;
    }

    @Autowired
    public void setFirebaseManager(FirebaseManager fm) {
        this.firebaseManager = fm;
    }
}
