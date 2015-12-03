package com.joechang.kursor.commands;

import com.joechang.kursor.commands.OpenTableCliBodyProcessor;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Author:    joechang
 * Created:   9/1/15 12:42 PM
 * Purpose:
 */
public class OpenTableCliBodyProcessorTest {
    OpenTableCliBodyProcessor obp = new OpenTableCliBodyProcessor(null);

    //Positive cases
    String str1 = "@rez5 dinner in financial district for 2 on tuesday";
    String str2 = "@rez5 dinner for 2 on tuesday in financial district";
    String str3 = "@rez5 dinner on tuesday in financial district for 2";
    String str = str3;

    //Negative cases
    String n1 = "@rez dinner in financial district";
    String n2 = "@rez dinner on April 4 in financial district";

    String n = n1;

    @Test
    public void testRegexForSearchString() throws Exception {
        String[] r = obp.regexForSearchString(str);
        Assert.assertEquals("5", r[0]);
        Assert.assertEquals("dinner", r[1]);

        r = obp.regexForSearchString(n);
        Assert.assertEquals("", r[0]);
        Assert.assertEquals("dinner", r[1]);
    }

    @Test
    public void testRegexForWhere() throws Exception {
        String[] r = obp.regexForWhere(str);
        Assert.assertEquals("financial district", r[0]);

        r = obp.regexForWhere(n);
        Assert.assertEquals("financial district", r[0]);
    }

    @Test
    public void testRegexForWhen() throws Exception {
        String[] r = obp.regexForWhen(str);
        Assert.assertEquals("tuesday", r[0]);

        r = obp.regexForWhen(n1);
        Assert.assertEquals(null, r[0]);

        String tricky = "@rez dinner for two tomorrow at 6pm";
        r = obp.regexForWhen(tricky);
        Assert.assertEquals("tomorrow at 6pm", r[0]);

        String whattonight = "@rez italian in cow hollow for 2 tonight";
        r = obp.regexForWhen(whattonight);
        Assert.assertEquals("tonight", r[0]);

        String tom12 = "@rez italian in mission for 2 tomorrow 12pm";
        r = obp.regexForWhen(tom12);
        Assert.assertEquals("tomorrow 12pm", r[0]);

    }

    @Test
    public void testRegexForPartySize() throws Exception {
        String[] ss = {
                "@rez 1 for 3 this Thursday 5pm",
                "lets go to dinner @rez barbaco for 3 next Friday 6pm",
                "@rez dinner for 3 tonight",
                "@rez lunch for 3 tomorrow",
                "@rez sushi for 3 next friday"
        };

        for (String s : ss) {
            Integer r = obp.regexForPartySize(s);
            Assert.assertTrue(3 == r);
        }
    }
}