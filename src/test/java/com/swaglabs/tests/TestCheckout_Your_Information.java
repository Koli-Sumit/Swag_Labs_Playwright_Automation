package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.pages.Checkout_OverviewPage;
import com.swaglabs.pages.Checkout_YourInformationPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCheckoutYourInformation extends BaseTest {

    Checkout_YourInformationPage checkout_YourInformationPage;

    /** Verifies the checkout information page loads. */
    @Test(groups = {"Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-011")
    public void CHK1_001() {
        checkout_YourInformationPage = new Checkout_YourInformationPage(page);

        assertThat(checkout_YourInformationPage.pageTitle()).hasText(AppConstants.CheckYourInfo_Page_Title);
    }

    /** Verifies required checkout fields show validation errors. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-012")
    public void CHK1_002() {
        checkout_YourInformationPage = new Checkout_YourInformationPage(page);
        checkout_YourInformationPage.clickContinue();

        assertThat(checkout_YourInformationPage.errorMessage()).hasText("Error: First Name is required");
    }

    /** Verifies valid customer details open the overview page. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-013")
    public void CHK1_003() throws IOException {
        checkout_YourInformationPage = new Checkout_YourInformationPage(page);
        Checkout_OverviewPage checkoutOverviewPage = checkout_YourInformationPage.enter_your_information();

        assertThat(checkoutOverviewPage.pageTitle()).hasText(AppConstants.CheckOverview_Page_Title);
    }
}
