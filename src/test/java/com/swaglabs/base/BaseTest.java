package com.swaglabs.base;

import com.microsoft.playwright.*;
import com.swaglabs.constants.AppConstants;
import com.swaglabs.utils.ConfigReader;
import com.swaglabs.utils.Log;
import org.slf4j.Logger;
import org.testng.annotations.*;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BaseTest {

    protected static Playwright playwright;
    protected static Browser browser;
    protected static BrowserContext context;
    protected static Page page;

    BrowserType.LaunchOptions options;
    Logger logger;

    @BeforeSuite(alwaysRun = true)
    public void setUp() throws Exception {
        logger = Log.getLogger(BaseTest.class);

        options = new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of("--start-maximized"))
                .setSlowMo(1000);

        playwright = Playwright.create();

        String browserName = ConfigReader.get(AppConstants.BROWSER);

        switch (browserName.toLowerCase()) {

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
        page.navigate(ConfigReader.get(AppConstants.URL));
    }

    @AfterSuite(alwaysRun = true)
    public void tearDown() {

        try {
            context.tracing().stop(
                    new Tracing.StopOptions()
                            .setPath(Paths.get("trace.zip"))
            );
        } finally {

            if (context != null) {
                context.close();
            }

            if (browser != null) {
                browser.close();
            }

            if (playwright != null) {
                playwright.close();
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void captureScreenshotOnFailure(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.info("Capturing screenshot for failed test: {}", result.getMethod().getMethodName());

            String screenshot = screenShot(page, result);
            // Store the path in ITestResult
            result.setAttribute("screenshot", screenshot);
        }

        if (page != null) {
            page.waitForTimeout(600);
        }
    }

    public static String screenShot(Page page, ITestResult result) {

        String methodName = result.getMethod().getMethodName();
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm a"));

        String fileName = methodName + "_" + timestamp + ".png";

        Path screenshotPath = Paths.get("test-output", "screenshots", fileName);

        try {
            Files.createDirectories(screenshotPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create screenshot directory", e);
        }

        page.screenshot(new Page.ScreenshotOptions()
                .setPath(screenshotPath)
                .setFullPage(true));

        // This is what your HTML should receive
        return "screenshots/" + fileName;
    }

}
