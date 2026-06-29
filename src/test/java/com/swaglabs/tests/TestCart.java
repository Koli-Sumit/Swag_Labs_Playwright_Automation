package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.CartPage;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(com.swaglabs.utils.TestListener.class)
public class TestCart extends BaseTest {

    CartPage cartPage;

    @Test(description = "Verify added product is displayed in cart")
    public void CART_001() {
        cartPage = new CartPage(page);
        cartPage.productCart();
    }

    @Test(description = "Verify checkout navigation from cart")
    public void CART_002() {
        cartPage = new CartPage(page);
        cartPage.checkout();
    }
}
