package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.UserLoginPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.io.IOException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestLogin extends BaseTest {

    //Verify locked_out_user login shows error
    @Test(groups = {"smoke","login"},retryAnalyzer = RetryAnalyzer.class,description = "REQ-001")
    public void LOGIN_001() throws IOException {
        UserLoginPage ul = new UserLoginPage(page);
        ul.lockedUser();

        assertThat(page.locator("[data-test='error']"))
                .hasText("Epic sadface: Sorry, this user has been locked out.");
    }

    //Verify invalid login shows error
    @Test(retryAnalyzer = RetryAnalyzer.class,description = "REQ-002")
    public void LOGIN_002() throws IOException {
        UserLoginPage ul = new UserLoginPage(page);
        ul.invalidPassword();

        assertThat(page.locator("[data-test='error']"))
                .hasText("Epic sadface: Username and password do not match any user in this service");
    }

    //Verify Login with valid credentials
    @Test(retryAnalyzer = RetryAnalyzer.class, description = "REQ-003")
    public void LOGIN_003() throws IOException {
        UserLoginPage ul = new UserLoginPage(page);
        ul.validLogin();

        assertThat(page.locator(".title")).hasText("Products");
    }

//    //Verify Failed login functionality
//    @Test(retryAnalyzer = RetryAnalyzer.class,description = "REQ-004")
//    public void LOGIN_004() throws IOException {
//        throw  new FileNotFoundException("Failed test");
//
//    }
//
//    //Verify Intentional Skip
//    @Test(retryAnalyzer = RetryAnalyzer.class, description = "REQ-005")
//    public void LOGIN_005() throws IOException {
//        throw new SkipException("Intentional Skip");
//    }

}
