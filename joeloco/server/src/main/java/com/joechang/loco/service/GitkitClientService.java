package com.joechang.loco.service;

import com.google.identitytoolkit.GitkitClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Author:    joechang
 * Created:   7/7/15 2:57 PM
 * Purpose:
 */
public class GitkitClientService {

    GitkitClient gc;

    public GitkitClientService() {
        try {
            gc = GitkitClient.newBuilder()
                    .setGoogleClientId("953002196823-88vddtd0m3f0u84vm2kh1a7rmu4sagr0.apps.googleusercontent.com")
                    .setServiceAccountEmail("953002196823-mfp94kh0rqt7mckhucabe4dsvkpm8b3q@developer.gserviceaccount.com")
                    .setKeyStream(new FileInputStream("./c93b9ec08f4001bb3d6d279ce67b9fac71f72e8d-privatekey.p12"))
                    .setCookieName("gtoken")
                    .setWidgetUrl("https://localhost/callback")
                    .build();
            ;
        } catch (FileNotFoundException nfne) {
            //no-op
        }

    }

    public GitkitClient getClient() {
        return gc;
    }
}
