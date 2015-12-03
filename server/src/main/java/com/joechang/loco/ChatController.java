package com.joechang.loco;

import com.joechang.loco.config.Routes;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.listener.DeferredPostQueryAction;
import com.joechang.loco.listener.DeferredPostWriteAction;
import com.joechang.loco.listener.DeferredResultValueListener;
import com.joechang.loco.model.*;
import com.joechang.loco.response.ChatSessionResult;
import com.joechang.loco.response.PutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Author:    joechang
 * Created:   5/23/15 9:47 AM
 * Purpose:   Easily little endpoint to retrieve and post messages to a group, or to an event.
 */
@RestController
public class ChatController {

    private FirebaseManager firebaseManager;

    @RequestMapping(Routes.CHAT_BY_ID)
    public ChatSessionResult getChatSessionById(
            @PathVariable(value = ChatSession.ID) String chatId
    ) {
        ChatSessionResult csr = new ChatSessionResult();
        firebaseManager.getChatFirebase(chatId)
                .addListenerForSingleValueEvent(DeferredResultValueListener.instance(csr, chatId));
        return csr;
    }

    @RequestMapping(value = Routes.CHAT_RESOURCE, method = RequestMethod.PUT)
    public PutResponse createChatSession(
            @RequestParam(value = Event.ID, required = false) String eventId,
            @RequestParam(value = Group.ID, required = false) String groupId
    ) {
        PutResponse pr = new PutResponse(ChatSession.class);
        String idType = Event.ID;
        String idParam = eventId;

        if (groupId != null) {
            idType = Group.ID;
            idParam = groupId;
        }

        firebaseManager.addChatSession(idType, idParam, DeferredPostWriteAction.instance(pr));
        return pr;
    }

    @RequestMapping(value = Routes.CHAT_RESOURCE, method = RequestMethod.GET)
    public ChatSessionResult findChat(
            @RequestParam(value = Event.ID, required = false) String eventId,
            @RequestParam(value = Group.ID, required = false) String groupId
    ) {
        ChatSessionResult csr = new ChatSessionResult();
        if (eventId != null) {
            firebaseManager.findChatSession(Event.ID, eventId, DeferredPostQueryAction.instance(csr, eventId));
        } else if (groupId != null) {
            firebaseManager.findChatSession(Group.ID, groupId, DeferredPostQueryAction.instance(csr, groupId));
        }
        return csr;
    }

    @RequestMapping(value = Routes.CHAT_MESSAGES, method = RequestMethod.PUT)
    public DeferredResult postMessage(
            @PathVariable(value = ChatSession.ID) String chatId,
            @RequestBody(required = true) Message message
    ) {
        PutResponse pr = new PutResponse(Message.class);
        firebaseManager.addMessage(chatId, message, DeferredPostWriteAction.instance(pr));
        return pr;
    }

    @Autowired
    public void setFirebaseManager(FirebaseManager fm) {
        this.firebaseManager = fm;
    }
}
