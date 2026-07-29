package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.pages.Checkout_CompletePage;
import com.swaglabs.pages.InventoryPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCheckoutComplete extends BaseTest {

    Checkout_CompletePage checkoutCompletePage;

    /** Verifies the order completion message is displayed. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-016")
    public void CHKC_001() {
        checkoutCompletePage = new Checkout_CompletePage(page);

        assertThat(checkoutCompletePage.pageTitle()).hasText(AppConstants.CheckComplete_Page_Title);
        assertThat(checkoutCompletePage.orderSuccessMessage()).hasText("Thank you for your order!");
    }

    /** Verifies Back Home opens the inventory page. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-017")
    public void CHKC_002() {
        checkoutCompletePage = new Checkout_CompletePage(page);
        InventoryPage inventoryPage = checkoutCompletePage.backToHome();

        assertThat(inventoryPage.pageTitle()).hasText(AppConstants.Inventory_Page_Title);
    }
}
