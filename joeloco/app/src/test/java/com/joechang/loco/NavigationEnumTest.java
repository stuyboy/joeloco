package com.joechang.loco;

import android.app.Application;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

/**
 * Author:  joechang
 * Date:    3/11/15
 * Purpose:
 */
public class NavigationEnumTest extends ApplicationTestCase<Application> {
    public NavigationEnumTest() {
        super(Application.class);
    }

    public void testEnabledValues() {
        String[] t = NavigationEnum.getTitles();
        Assert.assertEquals(t.length, 6);
    }
}
