package com.swaglabs.base;

import com.microsoft.playwright.*;
import com.swaglabs.utils.configReader;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BaseTest {

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

        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));

        // 1. START THE TRACE HERE
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        page = context.newPage();
        page.navigate(cfgReader.getURL());
        System.out.println(page.title());
        System.out.println("===================================================================================");
    }

    @AfterSuite
    public void teardown() {
        // 2. STOP THE TRACE AND SAVE IT HERE
        // Placing this in a finally block ensures the trace is saved even if the test fails
        try {
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get("trace.zip")));
        } finally {
            context.close();
            browser.close();
        }
    }
}
