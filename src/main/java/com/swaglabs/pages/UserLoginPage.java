package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.utils.ConfigReader;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;


public class UserLoginPage {

    //Variables
    protected Page page;
    //configReader cr;
    private static final Logger logger = Log.getLogger(UserLoginPage.class);

    // Locators
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;


    /** Initializes the login page with the active browser page. */
    public UserLoginPage(Page page) {
        this.page = page;

        //Selectors
        usernameInput = page.locator("#user-name");
        passwordInput = page.locator("#password");
        loginButton = page.locator("#login-button");
        errorMessage = page.locator("[data-test='error']");
    }

    /** Attempts login using the configured locked user. */
    public void lockedUser() throws IOException {
        usernameInput.fill(ConfigReader.get(AppConstants.LOCKED_USER));
        logger.info("👤 Entered locked user username");
        passwordInput.fill(ConfigReader.get(AppConstants.PASSWORD));
        logger.info("🔒 Entered password");
        loginButton.click();
        logger.info("🖱️ Clicked Login button with Locked credentials");
    }

    /** Attempts login using an invalid password. */
    public void invalidPassword() throws IOException {
        usernameInput.fill(ConfigReader.get(AppConstants.USERNAME));
        logger.info("❌ Entered invalid username");
        passwordInput.fill(ConfigReader.get(AppConstants.INVALID_PASSWORD));
        logger.info("🔒 Entered password ");
        loginButton.click();
        logger.info("🖱️ Clicked Login button with invalid credentials");

    }

    /** Logs in using configured valid credentials. */
    public InventoryPage validLogin() throws IOException {
        usernameInput.fill(ConfigReader.get(AppConstants.USERNAME));
        logger.info("👤 Entered valid username");
        passwordInput.fill(ConfigReader.get(AppConstants.PASSWORD));
        logger.info("🔒 Entered valid password");
        loginButton.click();
        logger.info("🖱️ Clicked Login button with valid credentials");

        return new InventoryPage(page);
    }

    /** Returns the login error message locator. */
    public Locator errorMessage() {
        return errorMessage;
    }
}
