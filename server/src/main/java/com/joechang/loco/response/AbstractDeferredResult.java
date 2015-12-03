package com.joechang.loco.response;

import com.joechang.loco.model.User;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.Collections;

/**
 * Author:    joechang
 * Created:   5/22/15 5:49 PM
 * Purpose:
 */
public abstract class AbstractDeferredResult<T> extends DeferredResult<T> {

    public static final int DEFAULT_TIMEOUT = 10000;

    public abstract Class baseClass();

    public AbstractDeferredResult() {
        super(DEFAULT_TIMEOUT);
    }

    /**
     * Just returns(setResult) an exception indicating that resource is not found, and causes return 404.
     */
    public void notFound(Object id) {
        ResourceNotFoundException rn;
        if (id == null) {
            rn = new ResourceNotFoundException();
        } else {
            rn = new ResourceNotFoundException(baseClass(), id.toString());
        }
        setErrorResult(rn);
    }

    public void canceled() {
        setErrorResult(new ServerException("Server error"));
    }

    public Set set() {
        return new Set(this);
    }

    public static void notFound(DeferredResult dr, String id) {
        dr.setErrorResult(new ResourceNotFoundException(id));
    }

    public class Set extends DeferredResult<Collection<T>> {
        private AbstractDeferredResult<T> baseResult;

        public Set(AbstractDeferredResult<T> baseResult) {
            super(DEFAULT_TIMEOUT);
            this.baseResult = baseResult;
        }

        public <X extends DeferredResult<Collection<T>>> X empty() {
            setResult(Collections.EMPTY_SET);
            return (X)this;
        }

        public void setSingleResult(T e) {
            setResult(Collections.singletonList(e));
        }

        public void notFound() {
            setErrorResult(new ResourceNotFoundException(baseClass()));
        }

        public AbstractDeferredResult<T> getBase() {
            return baseResult;
        }
    }

}
