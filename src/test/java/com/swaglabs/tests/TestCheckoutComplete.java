package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.Checkout_CompletePage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCheckoutComplete extends BaseTest {

    Checkout_CompletePage checkoutCompletePage;

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Verify order completion message is displayed")
    public void CHKC_001(){
        checkoutCompletePage = new Checkout_CompletePage(page);
        checkoutCompletePage.validateCompletePage();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Verify Back Home button navigation")
    public void CHKC_002(){
        checkoutCompletePage = new Checkout_CompletePage(page);
        checkoutCompletePage.validateOrderSuccess();
    }
}
