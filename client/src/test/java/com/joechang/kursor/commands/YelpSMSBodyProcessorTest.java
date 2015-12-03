package com.joechang.kursor.commands;

import com.joechang.kursor.CommandLineProcessor;
import org.junit.Assert;
import org.junit.Test;

/**
 * Author:    joechang
 * Created:   7/22/15 3:41 PM
 * Purpose:
 */
public class YelpSMSBodyProcessorTest {
    private CommandLineProcessor clp;

    public YelpSMSBodyProcessorTest() {
        clp = new CommandLineProcessor(null);
    }

    @Test
    public void testMatch() {
        YelpCliBodyProcessor lm = new YelpCliBodyProcessor(clp);
        Assert.assertTrue(lm.bodyHasPhrase("@yelp15 pizza in ny"));
        Assert.assertTrue(lm.bodyHasPhrase("@yelp pizza in ny"));
        Assert.assertTrue(lm.bodyHasPhrase("@yelp1 pizza in ny"));

        Assert.assertFalse(lm.bodyHasPhrase("@yeld pizza in ny"));
        Assert.assertFalse(lm.bodyHasPhrase("pizza in ny"));

    }

    @Test
    public void testRegex() {
        YelpCliBodyProcessor lm = new YelpCliBodyProcessor(clp);

        String[] xx = lm.doRegex("lets get lunch @yelp burgers in new york city");
        Assert.assertNull(Integer.getInteger(xx[0]));
        Assert.assertEquals("burgers", xx[1]);
        Assert.assertEquals("new york city", xx[2]);

        String[] xy = lm.doRegex("lets get lunch @yelp sushi");
        Assert.assertEquals("sushi", xy[1]);
        Assert.assertNull(xy[2]);

        String[] xz = lm.doRegex("lets get lunch @yelp");
        Assert.assertNull(xz[1]);
        Assert.assertNull(xz[2]);

        String[] x1 = lm.doRegex("time to play @yelp i want to eat something yummy near austin, tx");
        Assert.assertEquals("i want to eat something yummy", x1[1]);
        Assert.assertEquals("austin, tx", x1[2]);

        String[] xsz = lm.doRegex("time to play @yelp1 shoes near palo alto, ca, us");
        Assert.assertTrue(1 == Integer.parseInt(xsz[0]));
        Assert.assertEquals("1", xsz[0]);
        Assert.assertEquals("shoes", xsz[1]);
        Assert.assertEquals("palo alto, ca, us", xsz[2]);
    }
}
