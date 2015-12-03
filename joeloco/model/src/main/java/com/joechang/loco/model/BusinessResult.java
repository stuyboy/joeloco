package com.joechang.loco.model;

/**
 * Author:    joechang
 * Created:   9/22/15 12:52 PM
 * Purpose:
 */
public class BusinessResult {
    private Integer id;
    private String name;

    //Review Detail
    private String rating;
    private String reviewUrl;
    private String address;
    private String phoneNumber;
    private String numReviews;
    private String openStatus;
    private String snippet;

    public BusinessResult(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNumReviews() {
        return numReviews;
    }

    public void setNumReviews(String numReviews) {
        this.numReviews = numReviews;
    }

    public String getOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(String openStatus) {
        this.openStatus = openStatus;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
