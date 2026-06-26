package com.swaglabs.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.swaglabs.utils.configReader;
import java.io.IOException;


public class userLogin {

    //Variables
    protected Page page;
    configReader cr;

    // Locators
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator lockedUserError;
    private final Locator wrongPasswordError;

    //Constructor
    public userLogin(Page page) {
        this.page = page;

        //Selectors
        usernameInput = page.locator("#user-name");
        passwordInput = page.locator("#password");
        loginButton = page.locator("#login-button");
        lockedUserError = page.locator("text = Epic sadface: Sorry, this user has been locked out.");
        wrongPasswordError = page.locator("text = Epic sadface: Username and password do not match any user in this service");
    }

    //Login with locked user
    public void lockedUser() throws IOException {
        cr = new configReader();
        usernameInput.fill(cr.getLockUesr());
        System.out.println("Entered UserName : " + cr.getLockUesr());
        passwordInput.fill(cr.getPassword());
        System.out.println("Entered Password : " + cr.getPassword());
        loginButton.click();
        System.out.println("Clicked on Login button");

        if (lockedUserError.isVisible()){
            System.out.println(lockedUserError.textContent());
        }else {
            System.out.println("Error Message Is Not Displayed!");
        }
    }

    //Login with invalid password
    public void invalidPassword() throws IOException {
        cr = new configReader();
        usernameInput.fill(cr.getUsername());
        System.out.println("Entered UserName : " + cr.getUsername());
        passwordInput.fill(cr.getInvalidPassword());
        System.out.println("Entered Password : " + cr.getInvalidPassword());
        loginButton.click();
        System.out.println("Clicked on Login button");

        if (wrongPasswordError.isVisible()){
            System.out.println(wrongPasswordError.textContent());
        }
        else {
            System.out.println("Error Message Is Not Displayed!");
        }

    }

    //Login with valid credentials
    public void login() throws IOException {
        cr = new configReader();
        usernameInput.fill(cr.getUsername());
        System.out.println("Entered UserName : " + cr.getUsername());
        passwordInput.fill(cr.getPassword());
        System.out.println("Entered Password : " + cr.getPassword());
        loginButton.click();
        System.out.println("Clicked on Login button");

        String products = page.locator(".title").textContent();
        if (products.equals("Products")) {
            System.out.println("Expected Title on Dashboard : " + products);
            System.out.println("User Logged In Successfully");
        }else  {
            System.out.println("Not landed on Product page");
        }
    }

}
