package com.joechang.loco.model;

import java.io.Serializable;
import java.util.List;

/**
 * Author:    joechang
 * Created:   5/23/15 9:49 AM
 * Purpose:
 */
public class ChatSession implements Serializable {

    public final static String ID = "chatId";

    //Keeping this twice, once again.  Could just be the eventId, I suppose.
    private String chatId;

    //Should we support a chatsession between groups AND events?
    private String eventId;
    private String groupId;

    private Long createdTime;

    private List<Message> messages;

    public ChatSession() {
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
