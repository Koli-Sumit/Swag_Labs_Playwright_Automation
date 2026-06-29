package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.Checkout_YourInformationPage;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCheckoutYourInformation extends BaseTest {

    Checkout_YourInformationPage checkout_YourInformationPage;

    @Test(description = "Verify Checkout Step One page loads correctly")
    public void CHK1_001() {

        checkout_YourInformationPage = new Checkout_YourInformationPage(page);
        checkout_YourInformationPage.validate_YourInformationPage();
    }

    @Test(description = "Verify mandatory field validation")
    public void CHK1_002() {
        checkout_YourInformationPage = new Checkout_YourInformationPage(page);
        checkout_YourInformationPage.validate_fields();
    }

    @Test(description = "Verify successful submission with valid information")
    public void CHK1_003() throws IOException {
        checkout_YourInformationPage = new Checkout_YourInformationPage(page);
        checkout_YourInformationPage.enter_your_information();
    }
}
