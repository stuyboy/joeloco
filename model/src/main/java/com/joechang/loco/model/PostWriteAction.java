package com.joechang.loco.model;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.logging.Logger;

/**
 * Author:  joechang
 * Date:    12/23/14
 * Purpose: what happens when we finish writing to firebase?
 */
public abstract class PostWriteAction<T extends Object> implements Firebase.CompletionListener {
    private T object;
    private String id;

    public abstract void doAction(T objectWritten);
    public abstract void onError(T objectNotWritten);

    public PostWriteAction(T objectToWrite) {
        this.object = objectToWrite;
    }

    public PostWriteAction() {
        //Make sure to set an object in place.
    }

    public void setObject(T object) {
        this.object = object;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
        if (firebaseError != null) {
            onError(this.object);
        } else {
            doAction(this.object);
        }
    }

    public static PostWriteAction defaultInstance(final Object c) {
        return new PostWriteAction(c) {
            Logger log = Logger.getLogger(c.getClass().getSimpleName());

            @Override
            public void doAction(Object c) {
                log.info("Successfully completed write of " + c.toString());
            }

            @Override
            public void onError(Object c) {
                log.severe("Could not finish write of " + c.toString());
            }
        };
    }
}
