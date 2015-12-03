package com.joechang.loco.model;

/**
 * Author:  joechang
 * Date:    2/3/15
 * Purpose: A simple little class that I hope firebase maps to an ID and name
 */
public class ForeignKey {
    private String id;
    private String name;

    //FB required
    public ForeignKey() {}

    public ForeignKey(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ForeignKey(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
