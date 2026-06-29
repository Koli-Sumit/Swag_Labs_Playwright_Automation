package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

public class CartPage {

    //Variables
    protected Page page;
    private static final Logger logger =
            Log.getLogger(CartPage.class);

    //Locators
    protected Locator addToCartButton;
    protected Locator checkCartProduct;
    protected Locator checkout;

    //Constructor
    public CartPage(Page page) {
        this.page = page;

        //Selectors
        addToCartButton = page.locator("#shopping_cart_container");
        checkCartProduct = page.locator(".inventory_item_name");
        checkout = page.locator("#checkout");
    }

    public void productCart(){
        addToCartButton.click();
        logger.info("🛍️ Opened Product Cart");
        String prodInCart = checkCartProduct.textContent();

        if (prodInCart.equals("Sauce Labs Backpack")) {
            logger.info("🛒 Product is added in cart : {}", prodInCart);
        }else  {
            logger.error("❌ Wrong product in cart: {}", prodInCart);
        }
    }

    public void checkout(){
        checkout.click();
        logger.info("💳 Opened Checkout Page");
    }
}
