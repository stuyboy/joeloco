package com.joechang.loco.model;

/**
 * //Goes into imageUpload collection
 */
public class ImageUpload {

    private String userId;
    private String username;
    private String groupId;
    private String filePayload;
    private Long uploadTime;
    private LocTime locTime;

    //Required for firebase
    public ImageUpload() {}

    public ImageUpload(
            String userId,
            String username,
            Long uploadTime,
            Double latitude,
            Double longitude,
            String filePayload) {
        this.userId = userId;
        this.username = username;
        this.filePayload = filePayload;
        this.uploadTime = uploadTime;
        this.locTime = new LocTime(latitude, longitude, uploadTime);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFilePayload() {
        return filePayload;
    }

    public void setFilePayload(String filePayload) {
        this.filePayload = filePayload;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocTime getLocTime() {
        return locTime;
    }

    public void setLoctime(LocTime locTime) {
        this.locTime = locTime;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
