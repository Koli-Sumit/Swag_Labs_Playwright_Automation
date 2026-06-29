package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.UserLoginPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.io.IOException;


@Listeners(com.swaglabs.utils.TestListener.class)
public class TestLogin extends BaseTest {

    //Login with locked user
    @Test(enabled = false,retryAnalyzer = RetryAnalyzer.class, groups = {"NEGATIVE"},description = "Verify locked_out_user login shows error")
    public void LOGIN_001() throws IOException {
        UserLoginPage ul = new UserLoginPage(page);
        ul.lockedUser();
    }

    //Login with invalid password
    @Test(enabled = false, retryAnalyzer = RetryAnalyzer.class,groups = {"NEGATIVE"},description = "Verify invalid login shows error")
    public void LOGIN_002() throws IOException {
        UserLoginPage ul = new UserLoginPage(page);
        ul.invalidPassword();
    }

    //Login with valid credentials
    @Test(retryAnalyzer = RetryAnalyzer.class,groups = {"SMOKE", "REGRESSION"},alwaysRun = true,description = "Verify valid login functionality")
    public void LOGIN_003() throws IOException {
        UserLoginPage ul = new UserLoginPage(page);
        ul.validLogin();
    }

}
