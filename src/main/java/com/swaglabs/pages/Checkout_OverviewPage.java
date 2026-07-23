package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

public class Checkout_OverviewPage {

    protected Page page;
    protected Locator overviewPage;
    protected Locator productName;
    protected Locator paymentInfoLabel;
    protected Locator finishButton;
    private static final Logger logger = Log.getLogger(Checkout_OverviewPage.class);


    /** Initializes the overview page with the active browser page. */
    public Checkout_OverviewPage(Page page) {
        this.page = page;
        overviewPage = page.locator(".title");
        productName = page.locator(".inventory_item_name");
        paymentInfoLabel = page.locator("data-test=payment-info-label");
        finishButton = page.locator("#finish");
    }

    /** Returns the overview page title locator. */
    public Locator pageTitle() {
        return overviewPage;
    }

    /** Returns the checkout product name locator. */
    public Locator productName() {
        logger.info("Product Verified");
        return productName;
    }

    /** Returns the payment information label locator. */
    public Locator paymentInfoLabel() {
        logger.info("Payment Info Label Verified");
        return paymentInfoLabel;
    }

    /** Completes checkout and opens the confirmation page. */
    public Checkout_CompletePage finishButton() {
        finishButton.click();
        return new Checkout_CompletePage(page);
    }
}
