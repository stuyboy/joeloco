package com.joechang.loco.listener;

import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.PostWriteAction;
import com.joechang.loco.response.AbstractDeferredResult;
import com.joechang.loco.response.PutResponse;
import com.joechang.loco.response.ServerException;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Author:    joechang
 * Created:   5/23/15 11:00 AM
 * Purpose:
 */
public class DeferredPostWriteAction<T> extends PostWriteAction<T> {
    private PutResponse result;

    public static DeferredPostWriteAction instance(PutResponse pr) {
        DeferredPostWriteAction pdwa = new DeferredPostWriteAction();
        pdwa.result = pr;
        return pdwa;
    }

    @Override
    public void doAction(T p) {
        if (p != null) {
            result.created(getId());
        } else {
            //We need to throw this exception from within the web server thread.
            result.error();
        }
    }

    @Override
    public void onError(T p) {
        result.setErrorResult(new ServerException("Could not create object."));
    }
}
