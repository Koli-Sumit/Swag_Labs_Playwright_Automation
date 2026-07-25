package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.utils.ConfigReader;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;

public class Checkout_YourInformationPage {

    protected Page page;
    private static final Logger logger = Log.getLogger(Checkout_YourInformationPage.class);

    protected Locator expectedPage;
    protected Locator continueButton;
    protected Locator errorMessageValidation;
    protected Locator firstNameInput;
    protected Locator lastNameInput;
    protected Locator postalCodeInput;

    /** Initializes the information page with the active browser page. */
    public Checkout_YourInformationPage(Page page) {
        this.page = page;
        expectedPage = page.locator(".title");
        continueButton = page.locator("#continue");
        errorMessageValidation = page.locator("data-test=error");
        firstNameInput = page.locator("#first-name");
        lastNameInput = page.locator("#last-name");
        postalCodeInput = page.locator("#postal-code");
    }

    /** Returns the information page title locator. */
    public Locator pageTitle() {
        return expectedPage;
    }

    /** Returns the checkout validation error locator. */
    public Locator errorMessage() {
        return errorMessageValidation;
    }

    /** Clicks Continue to validate required checkout fields. */
    public void clickContinue() {
        continueButton.click();
    }

    /** Enters configured customer details and proceeds to overview. */
    public Checkout_OverviewPage enter_your_information() throws IOException {
        firstNameInput.clear();
        firstNameInput.fill(ConfigReader.get(AppConstants.FIRST_NAME));
        logger.info("Entered First Name on Information Page: {}", firstNameInput.inputValue());
        lastNameInput.clear();
        lastNameInput.fill(ConfigReader.get(AppConstants.LAST_NAME));
        logger.info("Entered Last Name on Information Page: {}", lastNameInput.inputValue());
        postalCodeInput.clear();
        postalCodeInput.fill(ConfigReader.get(AppConstants.POSTAL_CODE));
        logger.info("Entered ZIP/Postal Code on Information Page: {}", postalCodeInput.inputValue());
        continueButton.click();
        logger.info("Clicked on Continue Button");

        return new Checkout_OverviewPage(page);
    }
}
