package com.joechang.loco.response;

import com.joechang.loco.model.User;

/**
 * Author:    joechang
 * Created:   5/22/15 5:52 PM
 * Purpose:
 */
public class UserResult extends AbstractDeferredResult<User> {

    @Override
    public Class baseClass() {
        return User.class;
    }

}
