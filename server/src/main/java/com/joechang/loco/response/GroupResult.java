package com.joechang.loco.response;

import com.joechang.loco.model.Group;

/**
 * Created by joechang on 5/20/15.
 */
public class GroupResult extends AbstractDeferredResult<Group> {
    @Override
    public Class baseClass() {
        return Group.class;
    }
}
