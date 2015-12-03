package com.joechang.loco.listener;

import com.firebase.client.DataSnapshot;
import com.joechang.loco.response.AbstractDeferredResult;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Author:    joechang
 * Created:   6/25/15 12:01 PM
 * Purpose:   Same as for single result, but multiple
 */
public class DeferredSetResultValueListener extends DeferredResultValueListener {

    private AbstractDeferredResult.Set resultSet;

    public static DeferredSetResultValueListener instance(AbstractDeferredResult.Set adr) {
        DeferredSetResultValueListener d = new DeferredSetResultValueListener();
        d.resultSet = adr;
        d.clz = adr.getBase().baseClass();
        return d;
    }

    protected DeferredSetResultValueListener() {}

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Collection c = new ArrayList();
        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
            for (DataSnapshot d : dataSnapshot.getChildren()) {
                c.add(d.getValue(clz));
            }
            resultSet.setResult(c);
        } else {
            //We need to throw this exception from within the web server thread.
            resultSet.notFound();
        }
    }

}
