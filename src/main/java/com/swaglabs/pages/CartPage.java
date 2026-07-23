package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

public class CartPage {

    //Variables
    protected Page page;
    private static final Logger logger = Log.getLogger(CartPage.class);

    //Locators
    protected Locator cartTitle;
    protected Locator addToCartButton;
    protected Locator checkCartProduct;
    protected Locator checkout;

    /** Initializes the cart page with the active browser page. */
    public CartPage(Page page) {
        this.page = page;

        //Selectors
        cartTitle = page.locator(".title");
        addToCartButton = page.locator("#shopping_cart_container");
        checkCartProduct = page.locator(".inventory_item_name");
        checkout = page.locator("#checkout");

    }

    /** Returns the cart page title locator. */
    public Locator pageTitle() {

        return cartTitle;
    }

    /** Opens the shopping cart from the current page. */
    public void productCart(){
        addToCartButton.click();
        logger.info("🛍️ Opened Product Cart");
        //String prodInCart = checkCartProduct.textContent();
    }

    /** Returns the cart product locator. */
    public Locator cartProduct() {

        return checkCartProduct;
    }

    /** Proceeds from the cart to checkout information. */
    public Checkout_YourInformationPage checkout(){
        checkout.click();
        logger.info("💳 Opened Checkout Page");
        return new Checkout_YourInformationPage(page);
    }
}
