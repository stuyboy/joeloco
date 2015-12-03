package com.joechang.loco.service;

import org.junit.Test;

import java.util.Date;

/**
 * Author:    joechang
 * Created:   8/10/15 2:19 PM
 * Purpose:
 */
public class OpenTableAPITests {

    @Test
    public void testReadingOTPage() {
        OpenTableAPI ota = new OpenTableAPI();
        ota.findOpenTimes(44104, 2, new Date());

    }
}
