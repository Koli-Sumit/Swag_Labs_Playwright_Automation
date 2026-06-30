package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

public class Checkout_CompletePage {

    //Variables
    protected Page page;
    private static final Logger logger =
            Log.getLogger(Checkout_CompletePage.class);

    //Locators
    protected Locator completePage;
    protected Locator orderSuccess;
    protected Locator orderInfo;
    protected Locator backToHomePage;

    //Constructor
    public Checkout_CompletePage(Page page) {
        this.page = page;

        //Selectors
        completePage = page.locator(".title");
        orderSuccess = page.locator(".complete-header");
        orderInfo = page.locator(".complete-text");
        backToHomePage = page.locator("#back-to-products");
    }

    public void validateCompletePage() {
        String pageTitle = completePage.textContent();
        if (pageTitle.equalsIgnoreCase("Checkout: Complete!")) {
            logger.info("🏷️ Page Title  : {}", pageTitle);
        } else {
            logger.error("🏷️ Page Title : {}", pageTitle);
        }
    }

    public void validateOrderSuccess() {
        String successMessage = orderSuccess.textContent();

        if (successMessage.equalsIgnoreCase("Thank you for your order!")) {
            logger.info("✅ Order Placed Successfully : {}", successMessage);
            logger.info("📦 Order Information: {}", orderInfo.textContent());
            backToHomePage.click();
        } else {
            logger.error("❌ Order Failed : ");
        }
    }


}
