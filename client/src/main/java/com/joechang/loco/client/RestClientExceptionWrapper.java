package com.joechang.loco.client;

import retrofit.RetrofitError;

/**
 * Author:    joechang
 * Created:   9/12/15 12:34 PM
 * Purpose:   Quick class to translate the exception we get back from Server, bringing any messages and/or codes.
 */
public class RestClientExceptionWrapper {

    private int status;
    private long timestamp;
    private String error;
    private String exception;
    private String message;
    private String path;
    private String query;
    private int errorCode;

    public RestClientExceptionWrapper() {}

    public static RestClientExceptionWrapper from(RetrofitError re) {
        try {
            Object x = re.getBodyAs(RestClientExceptionWrapper.class);
            if (x != null) {
                return (RestClientExceptionWrapper)x;
            }
        } catch (Exception x) {
            //no-op
        }

        RestClientExceptionWrapper ret = new RestClientExceptionWrapper();
        ret.setMessage(re.getMessage());
        ret.setPath(re.getUrl());

        if (re.getResponse() != null) {
            ret.setErrorCode(re.getResponse().getStatus());
            ret.setError(re.getResponse().getReason());
        }

        return ret;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
