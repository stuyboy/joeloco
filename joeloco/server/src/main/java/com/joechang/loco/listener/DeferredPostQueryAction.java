package com.joechang.loco.listener;

import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.response.AbstractDeferredResult;
import com.joechang.loco.response.ServerException;

/**
 * Author:    joechang
 * Created:   5/23/15 11:00 AM
 * Purpose:
 */
public class DeferredPostQueryAction<T> implements PostQueryAction<T> {
    private AbstractDeferredResult result;
    private Object id;

    public static DeferredPostQueryAction instance(AbstractDeferredResult adr, Object id) {
        DeferredPostQueryAction d = new DeferredPostQueryAction();
        d.result = adr;
        d.id = id;
        return d;
    }

    @Override
    public void doAction(T p) {
        if (p != null) {
            result.setResult(p);
        } else {
            //We need to throw this exception from within the web server thread.
            result.notFound(id);
        }
    }

    @Override
    public void onError(T p) {
        result.setErrorResult(new ServerException("Could not query for " + result.baseClass() + ":" + id));
    }
}
