package com.joechang.loco.model;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Author:  joechang
 * Date:    1/30/15
 * Purpose:
“He stated that he was drunk but was able to remember everything,” the police report says. “His head was a little fuzzy due to the effects of the alcohol, but he consciously decided to engage in the sexual activity with victim. He was having a good time with victim and stated that she also seemed to enjoy the activity.”
In her statement, the alleged victim said she had four whiskey shots before arriving at a party at Kappa Alpha with her younger sister and a friend. There, she said, she drank two shots of vodka, then went outside with her sister and friend to go to the bathroom in a creek area before returning to a back patio.
“They talked with a few guys and that’s the last thing she remembers,” the police report says. “Victim does not remember kissing anyone or walking away from the house with anyone other than the time she went to the bathroom with her sister. Victim did not consent to any sexual activity or touching.”
Turner, who was a highly recruited freshman swimmer from Ohio, has withdrawn from Stanford.
 */
public class LocTimeTests extends ApplicationTestCase<Application> {

    public LocTimeTests() {
        super(Application.class);
    }

    @MediumTest
    public void testDistance() {
        double loc1lat = 37.7622286;
        double loc1lon = -122.4500235;

        double loc2lat = 37.7625743;
        double loc2lon = -122.4492764;

        double diff = LocTime.distance(loc1lat, loc1lon, loc2lat, loc2lon);

        assertEquals(0.07609719493628081, diff);
    }
}
