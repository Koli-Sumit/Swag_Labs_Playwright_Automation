package com.swaglabs.base;

import com.microsoft.playwright.*;
import com.swaglabs.utils.configReader;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AuthSetup {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    public configReader cfgReader;
    BrowserType.LaunchOptions options;


    @BeforeSuite()
    public void setUp() throws Exception {

        cfgReader = new configReader();
        options = new BrowserType.LaunchOptions();
        options.setHeadless(false);
        options.setArgs(List.of("--start-maximized"));
        options.setSlowMo(700);


        playwright = Playwright.create();

        browser = null;

        String browserName = cfgReader.getBrowser();

        switch (browserName.toLowerCase()) {
            case "chromium":
                browser = playwright.chromium().launch(options);
                break;
            case "firefox":
                browser = playwright.firefox().launch(options);
                break;
            case "webkit":
                browser = playwright.webkit().launch(options);
                break;
            default:
                throw new IllegalArgumentException("Invalid browser");
        }

        //context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));

        //Auto load Auth login data
        Path authFile = Paths.get("auth.json");
        if (Files.exists(authFile)) {
            context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setViewportSize(null)
                            .setStorageStatePath(Paths.get("auth.json"))
            );
        } else {
            System.out.println("Auth file not found");
        }

        page = context.newPage();
        page.navigate(cfgReader.getURL());
        System.out.println(page.title());
    }

    @AfterSuite
    public void teardown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        if (context != null) {
            context.close();
        }
    }
    }
