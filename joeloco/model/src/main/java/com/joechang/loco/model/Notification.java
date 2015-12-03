package com.joechang.loco.model;

/**
 * Author:    joechang
 * Created:   10/2/15 10:58 AM
 * Purpose:   Quick class for notifications to a user.  Can include email, phone number, userid, and the message itself.
 */
public class Notification {
    public final static String WELCOME_MESSAGE = "WelcomeMessage";

    public enum Status {
        QUEUED,
        SENT,
        ERROR
    }

    private String notificationId;
    private String type;
    private String message;
    private String userId;
    private Status status;
    private String email;
    private String phoneNumber;

    public Notification() {
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "userId='" + userId + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", notificationId='" + notificationId + '\'' +
                '}';
    }
}
