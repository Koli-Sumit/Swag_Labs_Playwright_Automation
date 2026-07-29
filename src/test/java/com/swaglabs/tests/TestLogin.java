package com.swaglabs.tests;

import com.swaglabs.base.BaseTest;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.pages.InventoryPage;
import com.swaglabs.pages.UserLoginPage;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Listeners(com.swaglabs.report.DashboardReporter.class)
//@Listeners(com.swaglabs.utils.TestListener.class)
public class TestLogin extends BaseTest {

    UserLoginPage ul;

    /** Verifies locked-user login displays the expected error.! */
    @Test(groups = {"Smoke"}, retryAnalyzer = RetryAnalyzer.class, description = "REQ-001")
    public void LOGIN_001() throws IOException {
        ul = new UserLoginPage(page);
        ul.lockedUser();

        assertThat(page).hasTitle(AppConstants.Login_Page_Title);
        assertThat(ul.errorMessage()).hasText(AppConstants.LOCKED_OUT_ERROR);

    }

    /** Verifies invalid login displays the expected error. */
    @Test(groups = {"Smoke"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-002")
    public void LOGIN_002() throws IOException {
        ul = new UserLoginPage(page);
        ul.invalidPassword();

        assertThat(ul.errorMessage()).hasText(AppConstants.INVALID_CREDENTIALS_ERROR);
    }

    /** Verifies valid credentials open the inventory page. */
    @Test(groups = {"Smoke","Regression"},retryAnalyzer = RetryAnalyzer.class, description = "REQ-003")
    public void LOGIN_003() throws IOException {
        ul = new UserLoginPage(page);
        InventoryPage inventoryPage = ul.validLogin();
        assertThat(inventoryPage.pageTitle()).hasText(AppConstants.Inventory_Page_Title);
    }

}
