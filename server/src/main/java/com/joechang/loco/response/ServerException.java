package com.joechang.loco.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Author:    joechang
 * Created:   5/22/15 10:11 PM
 * Purpose:
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServerException extends RuntimeException {

    public ServerException(String msg) {
        super(msg);
    }

}
