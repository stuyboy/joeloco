package com.joechang.loco.model;

/**
 * Author:  joechang
 * Date:    12/23/14
 * Purpose: Simple wrap to hold what to do with asynchronous data access, and what to do after.
 */
public interface PostQueryAction<T> {

    public void doAction(T p);
    public void onError(T p);
}
