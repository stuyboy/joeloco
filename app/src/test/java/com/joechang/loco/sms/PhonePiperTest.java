package com.joechang.loco.sms;

import android.app.Application;
import android.test.ApplicationTestCase;
import com.joechang.kursor.AndroidCommandLineProcessor;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.kursor.commands.PhonePiper;
import org.junit.Assert;

/**
 * Author:    joechang
 * Created:   8/11/15 2:55 PM
 * Purpose:
 */
public class PhonePiperTest extends ApplicationTestCase<Application> {
    private AndroidCommandLineProcessor clp;

    public PhonePiperTest() {
        super(Application.class);
        clp = new AndroidCommandLineProcessor(getContext(), null);
    }

    public void testPhonePiperRegex() {
        PhonePiper pp = new PhonePiper(clp);
        Assert.assertFalse(pp.bodyHasPhrase("what the foo | @phone"));
        Assert.assertTrue(pp.bodyHasPhrase("what the foo & @phone"));
        Assert.assertTrue(pp.bodyHasPhrase("what the foo& @phone"));
        Assert.assertTrue(pp.bodyHasPhrase("what the foo&@phone"));
        Assert.assertTrue(pp.bodyHasPhrase("what the foo   &   @phone"));
    }

    public void testRemoveFromBody() {
        PhonePiper pp = new PhonePiper(clp);
        Assert.assertEquals("what the foo", pp.removeFromBody("what the foo & @phone"));
        Assert.assertEquals("@yelp poo", pp.removeFromBody("@yelp poo & @phone"));
    }

}
