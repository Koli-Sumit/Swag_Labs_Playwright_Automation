package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.InventoryPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestInventory extends BaseTest {

    InventoryPage inventory;

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Verify Inventory page loads successfully")
    public void INV_001(){
        inventory = new InventoryPage(page);
        inventory.verifyInventory();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Add product to cart")
    public void INV_002(){
        inventory = new InventoryPage(page);
        inventory.addProductToCart();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Remove product from cart")
    public void INV_003() {
        inventory = new InventoryPage(page);
        inventory.removeProductFromCart();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Verify product details page navigation")
    public void INV_004() {
        inventory = new InventoryPage(page);
        inventory.productDetails();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,description = "Verify product sorting (Price Low to High)")
    public void INV_005() {
        inventory = new InventoryPage(page);
        inventory.sortProducts();
    }
}
