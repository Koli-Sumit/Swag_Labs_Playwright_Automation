package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.Log;
import com.swaglabs.utils.configReader;
import org.slf4j.Logger;

import java.io.IOException;


public class UserLoginPage {

    //Variables
    protected Page page;
    configReader cr;
    private static final Logger logger =
            Log.getLogger(UserLoginPage.class);

    // Locators
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator lockedUserError;
    private final Locator wrongPasswordError;

    //Constructor
    public UserLoginPage(Page page) {
        this.page = page;

        //Selectors
        usernameInput = page.locator("#user-name");
        passwordInput = page.locator("#password");
        loginButton = page.locator("#login-button");
        lockedUserError = page.locator("text = Epic sadface: Sorry, this user has been locked out.");
        wrongPasswordError = page.locator("text = Epic sadface: Username and password do not match any user in this service");
    }

    //Login with locked user
    public void lockedUser() throws IOException {
        cr = new configReader();
        usernameInput.fill(cr.getLockUesr());
        logger.info("👤 Entered locked user username");
        passwordInput.fill(cr.getPassword());
        logger.info("🔒 Entered password");
        loginButton.click();
        logger.info("🖱️ Clicked Login button with Locked credentials");

        if (lockedUserError.isVisible()){
            System.out.println(lockedUserError.textContent());
        }else {
            logger.error("❌ Expected error message is not displayed ");
        }
    }

    //Login with invalid password
    public void invalidPassword() throws IOException {
        cr = new configReader();
        usernameInput.fill(cr.getUsername());
        logger.info("❌ Entered invalid username");
        passwordInput.fill(cr.getInvalidPassword());
        logger.info("🔒 Entered password ");
        loginButton.click();
        logger.info("🖱️ Clicked Login button with invalid credentials");

        if (wrongPasswordError.isVisible()){
            System.out.println(wrongPasswordError.textContent());
        }
        else {
            logger.error("❌ Expected error message is not displayed");
        }

    }

    //Login with valid credentials
    public void validLogin() throws IOException {
        cr = new configReader();
        usernameInput.fill(cr.getUsername());
        logger.info("👤 Entered valid username");
        passwordInput.fill(cr.getPassword());
        logger.info("🔒 Entered valid password");
        loginButton.click();
        logger.info("🖱️ Clicked Login button with valid credentials");

        String products = page.locator(".title").textContent();
        if (products.equals("Products")) {
            System.out.println("Expected Title on Dashboard : " + products);
            logger.info("🔐 Login successful");
        }else  {
            logger.error("🚫 User is not Logged In Successfully");
        }
    }

}
