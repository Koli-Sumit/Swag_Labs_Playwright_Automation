package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

public class InventoryPage{

    //Variables
    protected Page page;
    private static final Logger logger =
            Log.getLogger(InventoryPage.class);

    //Locators
    protected Locator products;
    protected Locator addToCart_1;
    protected Locator addToCart_2;
    protected Locator removeFromCart;
    protected Locator sortProducts;
    protected Locator productDetails;
    protected Locator productName;
    protected Locator productPrice;
    protected Locator productDescription;
    protected Locator backToProducts;

    //Constructor
    public InventoryPage(Page page) {
        this.page = page;

        //Selectors
        products = page.locator("text = Products");
        addToCart_1 = page.locator("#add-to-cart-sauce-labs-backpack");
        addToCart_2 = page.locator("#add-to-cart-sauce-labs-bike-light");
        removeFromCart = page.locator("#remove-sauce-labs-bike-light");
        productDetails = page.locator("text = Sauce Labs Backpack");
        productName = page.locator(".inventory_details_name.large_size");
        productPrice = page.locator(".inventory_details_price");
        productDescription = page.locator(".inventory_details_desc.large_size");
        backToProducts = page.locator("#back-to-products");
        sortProducts = page.locator(".product_sort_container");
    }

    public void verifyInventory() {
        String prod = products.textContent();
        if (prod.equals("Products")) {
            logger.info("🔍 Inventory page verification passed. Page title: {}", prod);
        }
    }

    public void addProductToCart() {
        addToCart_1.click();
        logger.info("🛒 Sauce Labs Backpack Product added to cart");
        addToCart_2.click();
        logger.info("🛒 Sauce Labs Bike Light Product added to cart");
    }

    public void removeProductFromCart() {
        removeFromCart.click();
        logger.info("🛒 Removed product from cart: Sauce Labs Bike Light");
    }

    public void productDetails() {
        productDetails.click();
        String prodName = productName.textContent();
        logger.info("Product Name : {}", prodName);
        String prodPrice = productPrice.textContent();
        logger.info("Product Price : {}", prodPrice);
        String prodDesc = productDescription.textContent();
        logger.info("Product Description : {}", prodDesc);
        backToProducts.click();
    }

    public void sortProducts() {
        sortProducts.click();
        sortProducts.selectOption(new SelectOption().setLabel("Price (low to high)"));
        logger.info("Sorted Products to Price (low to high) ");
    }

}
