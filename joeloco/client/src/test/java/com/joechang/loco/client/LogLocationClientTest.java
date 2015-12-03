package com.joechang.loco.client;

import com.joechang.loco.logging.LogLocationEntry;
import com.joechang.loco.logging.StatusResponse;
import org.junit.Test;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by joechang on 5/14/15.
 */
public class LogLocationClientTest {

    @Test
    public void testCall() {
        UserClient llc = RestClientFactory.getInstance().getUserClient();
        LogLocationEntry lle = new LogLocationEntry("testUser", 0.1, 0.1);
        final CountDownLatch cdl = new CountDownLatch(1);
        llc.postLocation("testUser", lle, new UserClient.StatusResponseCallback() {
            @Override
            public void success(StatusResponse statusResponse, Response response) {
                int i=1;
                cdl.countDown();
            }

            @Override
            public void failure(RetrofitError error) {
                int i=1;
                cdl.countDown();
            }
        });

        try {
            cdl.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int i=1;
    }
}
