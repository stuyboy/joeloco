package com.joechang.loco.config;

import com.firebase.client.Firebase;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.response.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;

/**
 * Created by joechang on 5/13/15.
 */

@Configuration
public class ServerConfig {
    static {
        FirebaseManager.init();
    }

    @Bean
    public static FirebaseManager getFirebaseManager() {
        FirebaseManager fm = new FirebaseManager();
        return fm;
    }

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
                Map<String, Object> errorAttr = super.getErrorAttributes(requestAttributes, includeStackTrace);
                Throwable err = getError(requestAttributes);
                if (err instanceof ResourceNotFoundException) {
                    ResourceNotFoundException rn = (ResourceNotFoundException)err;
                    errorAttr.put("errorCode", rn.getErrorCode());
                    errorAttr.put("query", rn.getQuery());

                    //Override the default error if possible
                    if (rn.getMessage() != null) {
                        errorAttr.put("error", rn.getMessage());
                    }
                }
                return errorAttr;
            }
        };
    }
}
