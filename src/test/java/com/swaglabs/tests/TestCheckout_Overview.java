package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.pages.Checkout_CompletePage;
import com.swaglabs.pages.Checkout_OverviewPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCheckoutOverview extends BaseTest {

    Checkout_OverviewPage checkout_OverviewPage;

    /** Verifies the checkout overview displays order details. */
    @Test(groups = {"Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-014")
    public void CHK2_001() {
        checkout_OverviewPage = new Checkout_OverviewPage(page);

        assertThat(checkout_OverviewPage.pageTitle()).hasText(AppConstants.CheckOverview_Page_Title);
        assertThat(checkout_OverviewPage.productName()).hasText("Sauce Labs Backpack");
        assertThat(checkout_OverviewPage.paymentInfoLabel()).hasText("Payment Information:");
    }

    /** Verifies Finish completes checkout successfully. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-015")
    public void CHK2_002() {
        checkout_OverviewPage = new Checkout_OverviewPage(page);
        Checkout_CompletePage checkoutCompletePage = checkout_OverviewPage.finishButton();

        assertThat(checkoutCompletePage.pageTitle()).hasText(AppConstants.CheckComplete_Page_Title);
    }
}
