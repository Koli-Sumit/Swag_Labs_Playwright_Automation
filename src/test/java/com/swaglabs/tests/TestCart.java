package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.pages.CartPage;
import com.swaglabs.pages.Checkout_YourInformationPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCart extends BaseTest {

    CartPage cartPage;

    /** Verifies the added product appears in the cart. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-009")
    public void CART_001() {
        cartPage = new CartPage(page);
        cartPage.productCart();

        assertThat(cartPage.pageTitle()).hasText(AppConstants.Cart_Page_Title);
        assertThat(cartPage.cartProduct()).isVisible();
    }

    /** Verifies cart checkout opens the information page. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-010")
    public void CART_002() {
        cartPage = new CartPage(page);
        Checkout_YourInformationPage checkoutYourInformationPage = cartPage.checkout();

        assertThat(checkoutYourInformationPage.pageTitle()).hasText(AppConstants.CheckYourInfo_Page_Title);
    }
}
