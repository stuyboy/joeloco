package com.joechang.loco.sms;

import android.app.Application;
import android.test.ApplicationTestCase;
import com.joechang.kursor.AndroidCommandLineProcessor;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.kursor.commands.LocationCliBodyProcessor;
import org.junit.Assert;

/**
 * Author:    joechang
 * Created:   6/14/15 6:49 PM
 * Purpose:
 */
public class LocationSmsBodyProcessorTest extends ApplicationTestCase<Application> {
    private AndroidCommandLineProcessor clp;

    public LocationSmsBodyProcessorTest() {
        super(Application.class);
        clp = new AndroidCommandLineProcessor(getContext(), null);
    }

    public void testMatch() {
        LocationCliBodyProcessor lm = new LocationCliBodyProcessor(clp);
        Assert.assertTrue(lm.bodyHasPhrase("@loco"));
    }

    public void testRegex() {
        LocationCliBodyProcessor lm = new LocationCliBodyProcessor(clp);
        int xx = lm.regexForDuration("what the hell dude @loco7");
        Assert.assertEquals(7, xx);
    }

}
