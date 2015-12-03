package com.joechang.loco.user;

import com.joechang.loco.firebase.FirebaseManager;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Author:    joechang
 * Created:   10/2/15 12:23 PM
 * Purpose:
 */
public class UserManagerTest {

    @Before
    public void setUp() throws Exception {
        FirebaseManager.init();
    }

    @Test
    public void testAddNewUsers() throws Exception {
        String[] newNumbers = { "4153104000", "7185913213", "66" };
        UserManager um = new UserManager();
        um.addNewUsers(newNumbers);
        TimeUnit.SECONDS.sleep(100);
    }
}