package com.joechang.loco.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.joechang.loco.utils.IdUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Author:  joechang
 * Date:    4/3/15
 * Purpose: A group decides to gather.  There is an associated calendar item.  This is our copy,
 * which is tied to a groupId, which also includes the date and times to share location.
 */
public class Event implements Serializable {
    public static final String ID = "eventId";

    //Keeping this twice
    private String eventId;

    //Events are tied to a group, and are named something.
    private String groupId;
    private String name;

    //They typically have a start and end time, which we use to share location
    private Date dateStart;
    private Date dateEnd;

    //There is some buffer time before and after the event where we want to share location
    private int previewSeconds;
    private int postviewSeconds;

    //Finally, where is the event?
    private double latitude;
    private double longitude;

    //Required for Firebase instantiation
    public Event() {}

    public Event(String groupId, Date dateStart, Date dateEnd, String name) {
        this.groupId = groupId;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.name = name;
    }

    /**
     * Once given a group, then create a unique event to hold this shareNow.  But should we pass in the ID
     * so we are not dependent on creating things first in order to generate a unique Id?
     *
     * @param groupId
     */
    public static Event newTempInstance(String groupId) {
        Event e = new Event();
        e.setEventId(IdUtils.uniqueId());
        e.setDateStart(new Date(System.currentTimeMillis()));
        e.setDateEnd(new Date(System.currentTimeMillis() + (30 * 60 * 1000)));
        e.setName("Temporary Event");
        e.setGroupId(groupId);
        return e;
    }

    @JsonIgnore
    public boolean isValidNow(Date timeNow) {
        return (getDateStart().before(timeNow) && timeNow.before(getDateEnd()));
    }

    @JsonIgnore
    public boolean isValidNow() {
        return isValidNow(new Date());
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String id) {
        this.eventId = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public void setRelativeDateEnd(int minutesPastStart) {
        setDateEnd(new Date(getDateStart().getTime() + (minutesPastStart * 60 * 1000)));
    }

    public int getPreviewSeconds() {
        return previewSeconds;
    }

    public void setPreviewSeconds(int previewSeconds) {
        this.previewSeconds = previewSeconds;
    }

    public int getPostviewSeconds() {
        return postviewSeconds;
    }

    public void setPostviewSeconds(int postviewSeconds) {
        this.postviewSeconds = postviewSeconds;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
