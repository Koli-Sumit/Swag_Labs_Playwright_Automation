package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.Checkout_CompletePage;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCheckoutComplete extends BaseTest {

    Checkout_CompletePage checkoutCompletePage;

    @Test(description = "Verify order completion message is displayed")
    public void CHKC_001(){
        checkoutCompletePage = new Checkout_CompletePage(page);
        checkoutCompletePage.validateCompletePage();
    }

    @Test(description = "Verify Back Home button navigation")
    public void CHKC_002(){
        checkoutCompletePage = new Checkout_CompletePage(page);
        checkoutCompletePage.validateOrderSuccess();
    }
}
