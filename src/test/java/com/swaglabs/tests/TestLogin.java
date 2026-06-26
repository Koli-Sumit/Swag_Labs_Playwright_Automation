package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.userLogin;
import com.swaglabs.utils.RetryAnalyzer;
import io.qameta.allure.Story;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.io.IOException;


@Listeners(com.swaglabs.utils.TestListener.class)
public class TestLogin extends BaseTest {

    //Login with locked user
    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"NEGATIVE"},description = "Verify locked_out_user login shows error")
    public void LOGIN_001() throws IOException {
        userLogin ul = new userLogin(page);
        ul.lockedUser();
    }

    //Login with invalid password
    @Test(retryAnalyzer = RetryAnalyzer.class,groups = {"NEGATIVE"},description = "Verify invalid login shows error")
    public void LOGIN_002() throws IOException {
        userLogin ul = new userLogin(page);
        ul.invalidPassword();
    }

    //Login with valid credentials
    @Test(retryAnalyzer = RetryAnalyzer.class,groups = {"SMOKE", "REGRESSION"},alwaysRun = true,description = "Verify valid login functionality")
    public void LOGIN_003() throws IOException {
        userLogin ul = new userLogin(page);
        ul.login();
    }

}
