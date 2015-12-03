package com.joechang.loco.listener;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;
import com.joechang.loco.model.User;
import com.joechang.loco.response.AbstractDeferredResult;
import com.joechang.loco.response.ResourceNotFoundException;
import com.joechang.loco.response.ServerException;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Author:    joechang
 * Created:   5/22/15 6:09 PM
 * Purpose:   General value listener for use with firebase callbacks.  Should generally work.
 */
public class DeferredResultValueListener implements ValueEventListener {

    protected AbstractDeferredResult result;
    protected Object id;
    protected Class clz;

    public static DeferredResultValueListener instance(AbstractDeferredResult adr, Object id) {
        DeferredResultValueListener d = new DeferredResultValueListener();
        d.result = adr;
        d.id = id;
        d.clz = adr.baseClass();
        return d;
    }

    protected DeferredResultValueListener() {}

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            //Wow this is bad.  In cases where we do a search, the results come back with Ids as key, object as value.
            //So we have to see what is what.
            try {
                result.setResult(dataSnapshot.getValue(clz));
            } catch (FirebaseException fe) {
                result.setResult(dataSnapshot.getChildren().iterator().next().getValue(clz));
            }
        } else {
            //We need to throw this exception from within the web server thread.
            result.notFound(id);
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        result.setErrorResult(new ServerException(firebaseError.getMessage()));
    }
}
