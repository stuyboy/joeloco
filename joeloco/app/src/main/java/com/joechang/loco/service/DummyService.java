package com.joechang.loco.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Author:    joechang
 * Created:   8/25/15 10:38 AM
 * Purpose:
 */
public class DummyService extends IntentService {

    public DummyService() {
        super(DummyService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //none
    }
}
