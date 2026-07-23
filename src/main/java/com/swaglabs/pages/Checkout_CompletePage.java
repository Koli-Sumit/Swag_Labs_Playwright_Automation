package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;


public class Checkout_CompletePage {

    protected Page page;
    protected Locator completePage;
    protected Locator orderSuccess;
    protected Locator orderInfo;
    protected Locator backToHomePage;
    private static final Logger logger = Log.getLogger(Checkout_CompletePage.class);


    /** Initializes the completion page with the active browser page. */
    public Checkout_CompletePage(Page page) {
        this.page = page;
        completePage = page.locator(".title");
        orderSuccess = page.locator(".complete-header");
        orderInfo = page.locator(".complete-text");
        backToHomePage = page.locator("#back-to-products");
    }

    /** Returns the completion page title locator. */
    public Locator pageTitle() {
        return completePage;
    }

    /** Returns the order success message locator. */
    public Locator orderSuccessMessage() {
        logger.info("Order Placed Successfully : Thank you for your order!");
        return orderSuccess;
    }

    /** Returns the order information locator. */
    public Locator orderInformation() {
        logger.info(orderInfo.toString());
        return orderInfo;
    }

    /** Returns to the inventory page. */
    public InventoryPage backToHome() {
        backToHomePage.click();
        return new InventoryPage(page);
    }
}
