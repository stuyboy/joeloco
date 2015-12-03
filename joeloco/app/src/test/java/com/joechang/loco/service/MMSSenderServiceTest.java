package com.joechang.loco.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Author:    joechang
 * Created:   9/24/15 12:43 PM
 * Purpose:
 */
public class MMSSenderServiceTest {

    @Test
    public void testConcurrencyCache() {
        Cache<Integer, Long> sentMessages = CacheBuilder.newBuilder()
                .weakKeys()
                .concurrencyLevel(1)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        sentMessages.put(new Integer(32322), System.currentTimeMillis());

        Object isIncl = sentMessages.getIfPresent(new Integer(32322));
        Assert.assertTrue(isIncl == null);
    }

}