package com.joechang.loco.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Author:    joechang
 * Created:   5/23/15 9:51 AM
 * Purpose:
 */
public class Message implements Serializable {

    public final static String ID = "messageId";

    private String messageId;
    private String message;
    private String userId;
    private String name;

    private long sendTime;

    public Message() {
    }

    public Message(String userId, String name, String message) {
        this.userId = userId;
        this.name = name;
        this.message = message;
        this.sendTime = System.currentTimeMillis();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }
}
