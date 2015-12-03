package com.joechang.loco.response;

import com.joechang.loco.listener.DeferredResultValueListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Author:    joechang
 * Created:   5/23/15 11:18 AM
 * Purpose:   Once user as PUT something into a resource, send this back as a response to indicate success.
 */
public class PutResponse extends AbstractDeferredResult {

    private Class classType;

    public PutResponse(Class clz) {
        super();
        this.classType = clz;
    }

    @Override
    public Class baseClass() {
        return classType;
    }

    public void created() {
        this.setResult(new Created());
    }

    public void created(String newId) {
        this.setResult(new Created(newId));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void error() {
        //Throw exception instead?
        this.setErrorResult(new Error());
    }

    public class Created {

        private String id;

        public Created() {}
        public Created(String newId) {
            this.id = newId;
        }

        public String getId() {
            return id;
        }
    }

    public class Error {

    }

}
