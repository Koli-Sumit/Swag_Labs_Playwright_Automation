package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.pages.CartPage;
import com.swaglabs.pages.InventoryPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Listeners(com.swaglabs.report.DashboardReporter.class)
public class TestInventory extends BaseTest {

    InventoryPage inventory;

    /** Verifies the inventory page title. */
    @Test(groups = {"Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-004")
    public void INV_001() {
        inventory = new InventoryPage(page);

        assertThat(inventory.pageTitle()).hasText(AppConstants.Inventory_Page_Title);
    }

    /** Verifies products can be added to the cart. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-005")
    public void INV_002() {
        inventory = new InventoryPage(page);
        inventory.addProductToCart();

        assertThat(inventory.backpackRemoveFromCartButton()).isVisible();
    }

    /** Verifies a product can be removed from the cart. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-006")
    public void INV_003() {
        inventory = new InventoryPage(page);
        inventory.removeProductFromCart();

        assertThat(inventory.bikeLightAddToCartButton()).isVisible();
    }

    /** Verifies product details can be viewed and closed. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-007")
    public void INV_004() {
        inventory = new InventoryPage(page);
        inventory.productDetails();

        assertThat(inventory.pageTitle()).hasText(AppConstants.Inventory_Page_Title);
    }

    /** Verifies sorting products and navigating to the cart. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-008")
    public void INV_005() {
        inventory = new InventoryPage(page);
        inventory.sortProducts();
        assertThat(inventory.sortDropdown()).hasValue("lohi");

        CartPage cartPage = inventory.goToCart();
        assertThat(cartPage.pageTitle()).hasText(AppConstants.Cart_Page_Title);

    }
}
