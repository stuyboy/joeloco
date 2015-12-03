package com.joechang.loco.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Author:    joechang
 * Created:   5/22/15 6:04 PM
 * Purpose:   When a call is made looking for a specific id, and it does not exist.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceNotFoundException extends RuntimeException {

    private int errorCode;
    private String query;

    public ResourceNotFoundException(String id) {
        super("Could not locate resource with id: " + id);
        query = id;
    }

    public ResourceNotFoundException(Class clz, String id) {
        super("Could not locate " + clz.getSimpleName().toLowerCase() + " '" + id + "'");
        query = id;
    }

    public ResourceNotFoundException(Class clz) {
        super("Could not locate any instances of " + clz.getSimpleName().toLowerCase());
    }

    public ResourceNotFoundException() {
        super("Could not identify search");
    }

    public ResourceNotFoundException(int errorCode, String reason) {
        super(reason);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
