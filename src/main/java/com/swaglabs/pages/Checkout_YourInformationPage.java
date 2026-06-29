package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.Log;
import com.swaglabs.utils.configReader;
import org.slf4j.Logger;

import java.io.IOException;

public class Checkout_YourInformationPage {

    //Variables
    protected Page page;
    configReader cr;
    private static final Logger logger =
            Log.getLogger(Checkout_YourInformationPage.class);

    //Locators
    protected Locator expectedPage;
    protected Locator continueButton;
    protected Locator errorMessageValidation;
    protected Locator errorMessageCloseButton;
    protected Locator firstNameInput;
    protected Locator lastNameInput;
    protected Locator postalCodeInput;

    public Checkout_YourInformationPage(Page page) {
        this.page = page;

        //Selectors
        expectedPage = page.locator(".title");
        continueButton = page.locator("#continue");
        errorMessageValidation = page.locator("data-test=error");
        errorMessageCloseButton = page.locator(".error-button");
        firstNameInput = page.locator("#first-name");
        lastNameInput = page.locator("#last-name");
        postalCodeInput = page.locator("#postal-code");
    }

    public void validate_YourInformationPage() {
        String pageTitle = expectedPage.textContent();
        if (pageTitle.equalsIgnoreCase("Checkout Your Information")) {
            logger.info("🏷️ Page Title  : {}", pageTitle);
        }else {
            logger.error("🏷️ Page Title : {}", pageTitle);
        }
    }

    public void validate_fields(){
        continueButton.click();
        String errorCode = errorMessageValidation.textContent();
        if (errorCode.equalsIgnoreCase("Error: First Name is required")) {
            logger.info("❌ Error Message is displayed  : {}", errorCode);
            errorMessageCloseButton.click();
        }else  {
            logger.error("❌ Error Message is displayed : {}", errorCode);
            errorMessageCloseButton.click();
        }
    }

    public void enter_your_information() throws IOException {
        cr = new configReader();
        firstNameInput.clear();
        firstNameInput.fill(cr.getFirstName());
        logger.info("📝 Entered First Name on Information Page: {}", firstNameInput.textContent());
        lastNameInput.clear();
        lastNameInput.fill(cr.getLastName());
        logger.info("📝 Entered Last Name on Information Page: {}", lastNameInput.textContent());
        postalCodeInput.clear();
        postalCodeInput.fill(cr.getPostalCode());
        logger.info("📮 Entered ZIP/Postal Code on Information Page: {}", postalCodeInput.inputValue());
        continueButton.click();
        logger.info("▶️ Clicked on Continue Button");
    }
}
