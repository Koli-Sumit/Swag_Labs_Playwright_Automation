package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.Checkout_OverviewPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCheckoutOverview extends BaseTest {

    Checkout_OverviewPage checkout_OverviewPage;

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Verify order summary is displayed correctly")
    public void CHK2_001() {

        checkout_OverviewPage = new Checkout_OverviewPage(page);
        checkout_OverviewPage.validateOverviewPage();
        checkout_OverviewPage.validateProductDescription();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Verify Finish button completes checkout")
    public void CHK2_002() {

        checkout_OverviewPage = new Checkout_OverviewPage(page);
        checkout_OverviewPage.finishButton();
    }
}
