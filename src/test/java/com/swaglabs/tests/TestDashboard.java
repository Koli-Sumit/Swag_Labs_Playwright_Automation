package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.userLogin;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestDashboard extends BaseTest {

    @Test
    public void testDashboard() throws IOException {

        //page.navigate("https://www.saucedemo.com/inventory.html");

        userLogin ul = new userLogin(page);
        ul.login();

    }

}
