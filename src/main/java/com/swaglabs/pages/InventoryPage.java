package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;

public class InventoryPage {

    protected Page page;
    private static final Logger logger = Log.getLogger(InventoryPage.class);

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
    protected Locator shoppingCart;

    /** Initializes the inventory page with the active browser page. */
    public InventoryPage(Page page) {
        this.page = page;
        products = page.locator(".title");
        addToCart_1 = page.locator("#add-to-cart-sauce-labs-backpack");
        addToCart_2 = page.locator("#add-to-cart-sauce-labs-bike-light");
        removeFromCart = page.locator("#remove-sauce-labs-bike-light");
        productDetails = page.locator("text = Sauce Labs Backpack");
        productName = page.locator(".inventory_details_name.large_size");
        productPrice = page.locator(".inventory_details_price");
        productDescription = page.locator(".inventory_details_desc.large_size");
        backToProducts = page.locator("#back-to-products");
        sortProducts = page.locator(".product_sort_container");
        shoppingCart = page.locator("#shopping_cart_container");
    }

    /** Verifies the inventory page title. */
    public void verifyInventory() {
        String prod = products.textContent();
        if (prod.equals(AppConstants.Inventory_Page_Title)) {
            logger.info("Inventory page verification passed. Page title: {}", prod);
        }
    }

    /** Returns the inventory page title locator. */
    public Locator pageTitle() {
        return products;
    }

    /** Returns the bike light add-to-cart locator. */
    public Locator bikeLightAddToCartButton() {
        return addToCart_2;
    }

    /** Returns the backpack remove-from-cart locator. */
    public Locator backpackRemoveFromCartButton() {
        return page.locator("#remove-sauce-labs-backpack");
    }

    /** Returns the product sorting dropdown locator. */
    public Locator sortDropdown() {
        return sortProducts;
    }

    /** Adds the configured inventory products to the cart. */
    public void addProductToCart() {
        addToCart_1.click();
        logger.info("Sauce Labs Backpack product added to cart");
        addToCart_2.click();
        logger.info("Sauce Labs Bike Light product added to cart");
    }

    /** Removes the bike light from the cart. */
    public void removeProductFromCart() {
        removeFromCart.click();
        logger.info("Removed product from cart: Sauce Labs Bike Light");
    }

    /** Opens, logs, and closes backpack product details. */
    public void productDetails() {
        productDetails.click();
        logger.info("Product Name: {}", productName.textContent());
        logger.info("Product Price: {}", productPrice.textContent());
        logger.info("Product Description: {}", productDescription.textContent());
        backToProducts.click();
    }

    /** Sorts products by price from low to high. */
    public void sortProducts() {
        sortProducts.selectOption(new SelectOption().setLabel("Price (low to high)"));
        logger.info("Sorted products by price (low to high)");
    }

    /** Opens the shopping cart page. */
    public CartPage goToCart() {
        shoppingCart.click();
        logger.info("Opened Product Cart");
        return new CartPage(page);
    }
}
