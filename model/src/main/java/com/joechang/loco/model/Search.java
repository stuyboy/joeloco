package com.joechang.loco.model;

import java.io.Serializable;

/**
 * Author:    joechang
 * Created:   7/21/15 3:24 PM
 * Purpose:
 */
public class Search implements Serializable {

    public String source;
    public String query;
    public String json;
    public String[] response;

    public Search() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] getResponse() {
        return response;
    }

    public void setResponse(String[] response) {
        this.response = response;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
