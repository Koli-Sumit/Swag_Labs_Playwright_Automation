package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.pages.InventoryPage;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


@Listeners(com.swaglabs.utils.TestListener.class)
public class TestInventory extends BaseTest {

    InventoryPage inventory;

    @Test(description = "Verify Inventory page loads successfully")
    public void INV_001(){
        inventory = new InventoryPage(page);
        inventory.verifyInventory();
    }

    @Test(description = "Add product to cart")
    public void INV_002(){
        inventory = new InventoryPage(page);
        inventory.addProductToCart();
    }

    @Test(description = "Remove product from cart")
    public void INV_003() {
        inventory = new InventoryPage(page);
        inventory.removeProductFromCart();
    }

    @Test(description = "Verify product details page navigation")
    public void INV_005() {
        inventory = new InventoryPage(page);
        inventory.productDetails();
    }

    @Test(description = "Verify product sorting (Price Low to High)")
    public void INV_004() {
        inventory = new InventoryPage(page);
        inventory.sortProducts();
    }
}
