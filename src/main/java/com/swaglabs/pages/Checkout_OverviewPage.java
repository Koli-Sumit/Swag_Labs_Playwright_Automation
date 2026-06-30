package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class Checkout_OverviewPage {

    //Variables
    protected Page page;
    private static final Logger logger =
            Log.getLogger(Checkout_OverviewPage.class);

    //Locators
    protected Locator overviewPage;
    protected Locator productSummary;
    protected Locator finishButton;


    //Constructor
    public  Checkout_OverviewPage(Page page) {
        this.page = page;

        //Selectors
        overviewPage = page.locator(".title");
        productSummary = page.locator(".summary_info");
        finishButton = page.locator("#finish");
    }

    public void validateOverviewPage() {
       String pageTitle =  overviewPage.textContent();
       if (pageTitle.equalsIgnoreCase("Checkout: Overview")) {
           logger.info("🏷️ Page Title  : {}", pageTitle);
       }else {
           logger.error("🏷️ Page Title : {}", pageTitle);
       }
    }

    public void validateProductDescription() {

        try {
            assertThat(page.locator(".inventory_item_name"))
                    .hasText("Sauce Labs Backpack");
            logger.info("☑️ Product Verified");
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }

        try {
            assertThat(page.locator("data-test=payment-info-label"))
            .hasText("Payment Information:");
            logger.info("☑️ Payment Verified");
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void finishButton() {
        finishButton.click();
    }

}
