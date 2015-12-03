package com.joechang.loco.response;

import com.joechang.loco.model.ChatSession;
import com.joechang.loco.model.Group;

/**
 * Created by joechang on 5/20/15.
 */
public class ChatSessionResult extends AbstractDeferredResult<ChatSession> {
    @Override
    public Class baseClass() {
        return ChatSession.class;
    }
}
