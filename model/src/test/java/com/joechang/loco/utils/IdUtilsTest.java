package com.joechang.loco.utils;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by joechang on 5/19/15.
 */

public class IdUtilsTest {

    @Test
    public void testIdGeneration() {
        String ss = IdUtils.uniqueId();

        Assert.assertFalse(ss.contains("="));
        Assert.assertFalse(ss.contains("+"));
        Assert.assertFalse(ss.contains("/"));
    }

}
