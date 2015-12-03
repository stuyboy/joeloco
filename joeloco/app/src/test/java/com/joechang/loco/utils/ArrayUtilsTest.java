package com.joechang.loco.utils;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  joechang
 * Date:    2/11/15
 * Purpose:
 */
public class ArrayUtilsTest extends ApplicationTestCase<Application> {
    public ArrayUtilsTest() {
        super(Application.class);
    }

    @SmallTest
    public void testToPrimitive() {
        //test empty list of booleans
        List<Boolean> ll = new ArrayList<Boolean>();
        boolean[] r = ArrayUtils.toPrimitiveArray(ll);
        Assert.assertEquals(r.length, ll.size());

        //test list of one
        List<Boolean> ll2 = new ArrayList<Boolean>();
        ll2.add(new Boolean(true));
        boolean[] r2 = ArrayUtils.toPrimitiveArray(ll2);
        Assert.assertEquals(r2.length, ll2.size());

    }
}
