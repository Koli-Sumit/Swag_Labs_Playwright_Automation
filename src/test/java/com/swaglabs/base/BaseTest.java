package com.swaglabs.base;

import com.microsoft.playwright.*;
import com.swaglabs.utils.Log;
import com.swaglabs.utils.configReader;
import org.slf4j.Logger;
import org.testng.annotations.*;

import java.nio.file.Paths;
import java.util.List;

public class BaseTest {

    protected static Playwright playwright;
    protected static Browser browser;
    protected static BrowserContext context;
    protected static Page page;

    protected configReader cfgReader;
    BrowserType.LaunchOptions options;
    Logger logger;

    @BeforeSuite(alwaysRun = true)
    public void setUp() throws Exception {
        logger = Log.getLogger(BaseTest.class);
        cfgReader = new configReader();

        options = new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(List.of("--start-maximized"))
                .setSlowMo(1000);

        playwright = Playwright.create();

        switch (cfgReader.getBrowser().toLowerCase()) {

            case "chromium":
                browser = playwright.chromium().launch(options);
                logger.info("🌐 Chromium browser launched");
                break;

            case "firefox":
                browser = playwright.firefox().launch(options);
                logger.info("🌐 Firefox browser launched");
                break;

            case "webkit":
                browser = playwright.webkit().launch(options);
                logger.info("🌐 Webkit browser launched");
                break;

            default:
                throw new RuntimeException("Invalid Browser");
        }

        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(null)
        );

        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );

        page = context.newPage();
        page.navigate(cfgReader.getURL());
    }

    @AfterSuite(alwaysRun = true)
    public void tearDown() {

        try {
            context.tracing().stop(
                    new Tracing.StopOptions()
                            .setPath(Paths.get("trace.zip"))
            );
        } finally {

            context.close();
            browser.close();
            playwright.close();
        }
    }

    public static Page getPage() {
        return page;
    }
}