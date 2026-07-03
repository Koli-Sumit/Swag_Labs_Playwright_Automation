package com.swaglabs.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.swaglabs.utils.RetryAnalyzer;
import org.testng.*;
import org.testng.xml.XmlSuite;

import javax.xml.crypto.Data;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;

/**
 * TestNG IReporter that generates the dynamic HTML dashboard.
 * <p>
 * This runs ONCE after all suites complete. It:
 * 1. Collects passed/failed/skipped counts from all test results
 * 2. Builds suite-level and individual test-level data
 * 3. Updates trend history (persisted JSON file)
 * 4. Reads the HTML template, injects window.REPORT_DATA, writes output
 * <p>
 * Wire it in testng.xml:
 * <listeners>
 * <listener class-name="com.yourproject.report.DashboardReporter" />
 * </listeners>
 */
public class DashboardReporter implements IReporter {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Override this path in testng.xml via <parameter> if needed
     */
    private static final String TEMPLATE_PATH = "src/main/resources/report-template.html";
    private static final String OUTPUT_PATH = "test-output/dashboard-report.html";
    private static final String HISTORY_PATH = "test-output/trend-history.json";

    // Store the start time globally or in your test base class
    Data data;

    @Override
    public void generateReport(
            List<XmlSuite> xmlSuites,
            List<ISuite> suites,
            String outputDirectory
    ) {
        long runStartMs = System.currentTimeMillis();
        System.out.println("========================================");
        System.out.println("  Dashboard Report Generation Started");
        System.out.println("========================================");

        // ── 1. Aggregate all results ──
        int totalPassed = 0;
        int totalFailed = 0;
        int totalSkipped = 0;
        long totalDurationMs = 0;

        List<ReportData.SuiteData> suiteDataList = new ArrayList<>();
        List<ReportData.TestResultData> allTests = new ArrayList<>();

        for (ISuite suite : suites) {
            String suiteName = suite.getName();
            int sPassed = 0, sFailed = 0, sSkipped = 0;
            long sDurationMs = 0;

            // Get results grouped by test class
            Map<String, ISuiteResult> suiteResults = suite.getResults();

            for (ISuiteResult sr : suiteResults.values()) {
                ITestContext tc = sr.getTestContext();

                // ✅ Global Deduplication: Combine all lists, keep only the LATEST attempt
                Map<String, ITestResult> finalResultsMap = new LinkedHashMap<>();

                // Combine all results into one place
                List<ITestResult> allRawResults = new ArrayList<>();
                allRawResults.addAll(tc.getPassedTests().getAllResults());
                allRawResults.addAll(tc.getFailedTests().getAllResults());
                allRawResults.addAll(tc.getSkippedTests().getAllResults());

                // Keep only the latest ITestResult for each method name
                for (ITestResult tr : allRawResults) {
                    String key = tr.getMethod().getMethodName();
                    ITestResult existing = finalResultsMap.get(key);

                    // If no existing result, OR this one finished later -> keep it
                    if (existing == null || tr.getEndMillis() > existing.getEndMillis()) {
                        finalResultsMap.put(key, tr);
                    }
                }

                // Now iterate over the single, deduplicated list
                for (ITestResult tr : finalResultsMap.values()) {
                    int status = tr.getStatus();

                    if (status == ITestResult.SUCCESS) {
                        sPassed++;
                        allTests.add(buildTestResult(tr, "passed"));
                    } else if (status == ITestResult.FAILURE) {
                        sFailed++;
                        allTests.add(buildTestResult(tr, "failed"));
                    } else {
                        sSkipped++;
                        allTests.add(buildTestResult(tr, "skipped"));
                    }

                    sDurationMs += tr.getEndMillis() - tr.getStartMillis();
                }
            }

            totalPassed += sPassed;
            totalFailed += sFailed;
            totalSkipped += sSkipped;
            totalDurationMs += sDurationMs;

            ReportData.SuiteData sd = new ReportData.SuiteData();
            sd.name = suiteName;
            sd.total = sPassed + sFailed + sSkipped;
            sd.passed = sPassed;
            sd.failed = sFailed;
            sd.skipped = sSkipped;
            sd.durationSeconds = (int) (sDurationMs / 1000);
            suiteDataList.add(sd);
        }

        int totalDurationSec = (int) (totalDurationMs / 1000);

        // ── 2. Sort tests by end time (most recent first) ──
        allTests.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
        // Send ALL tests — the HTML dashboard handles pagination
        List<ReportData.TestResultData> recentTests = new ArrayList<>(allTests);


        // ── 3. Trend history ──
        String buildNumber = getBuildNumber();
        Path historyFile = Paths.get(HISTORY_PATH);
        TrendHistory.TrendSnapshot trend =
                TrendHistory.recordAndSnapshot(
                        "#" + buildNumber,
                        totalPassed, totalFailed, totalSkipped,
                        totalDurationSec,
                        historyFile
                );

        // ── 4. Build the ReportData object ──
        ReportData data = new ReportData();
        data.buildNumber = Integer.parseInt(buildNumber);
        data.branch = System.getProperty("branch", System.getenv("BRANCH") != null ? System.getenv("BRANCH") : "Master Branch");

        // 1. Define how you want the date/time to look
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

        // 2. Get the current time at the exact moment the build starts
        String buildStartTime = LocalDateTime.now().format(formatter);

        // 3. Assign it to your data object
        data.runAgo = buildStartTime;
        // Output will look like: "25-Oct-2023 14:30:05"

        data.environment = System.getProperty("env", System.getenv("ENV") != null ? System.getenv("ENV") : "Production");
        //data.reportId = "RPT-" + Instant.now().atZone(ZoneId.UTC()).format(DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss")).replace("-", "") + "-" + randomHex(4);
        data.reportId = "RPT-" +
                Instant.now()
                        .atZone(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss"))
                        .replace("-", "") +
                "-" + randomHex(4);
        data.passed = totalPassed;
        data.failed = totalFailed;
        data.skipped = totalSkipped;
        data.durationSeconds = totalDurationSec;
        data.suites = suiteDataList;
        data.recentTests = recentTests;

        // Trend arrays
        data.trendBuilds = trend.builds;
        data.trendPassed = trend.passed;
        data.trendFailed = trend.failed;
        data.trendSkipped = trend.skipped;

        // Trend deltas
        data.trendPassedDelta = trend.passedDelta;
        data.trendFailedDelta = trend.failedDelta;
        data.trendSkippedDelta = trend.skippedDelta;
        data.trendDurationDelta = trend.durationDelta;

        // ── 5. Read template, inject data, write output ──
        try {
            generateHtmlReport(data);
        } catch (Exception e) {
            System.err.println("[DashboardReporter] FAILED to generate report:");
            e.printStackTrace();
        }

        long elapsed = System.currentTimeMillis() - runStartMs;
        System.out.println("----------------------------------------");
        System.out.println("  Report: " + Paths.get(OUTPUT_PATH).toAbsolutePath());
        System.out.println("  Passed: " + totalPassed + " | Failed: " + totalFailed + " | Skipped: " + totalSkipped);
        System.out.println("  Duration: " + totalDurationSec + "s");
        System.out.println("  Generated in: " + elapsed + "ms");
        System.out.println("========================================");
    }

    /**
     * Reads the HTML template, injects window.REPORT_DATA as JSON,
     * and writes the final report file.
     */

    private void generateHtmlReport(ReportData data) throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        Path outputPath = Paths.get(OUTPUT_PATH);

        if (!Files.exists(templatePath)) {
            throw new FileNotFoundException("Template not found: " + templatePath.toAbsolutePath());
        }

        String html = Files.readString(templatePath);
        String json = GSON.toJson(data);

        // ── NO MARKER NEEDED — find the actual JS line that must exist ──
        String targetLine = "const DEFAULT_REPORT_DATA = {";

        if (!html.contains(targetLine)) {
            System.err.println("╔══════════════════════════════════════════════════════════════╗");
            System.err.println("║  FATAL: Cannot find '" + targetLine + "'           ║");
            System.err.println("║  in template: " + templatePath.toAbsolutePath());
            System.err.println("║  The HTML template appears corrupted or wrong file.    ║");
            System.err.println("╚══════════════════════════════════════════════════════════════╝");
            // Still write the file so you can inspect it
            outputPath.getParent().toFile().mkdirs();
            Files.writeString(outputPath, html);
            return;
        }

        // Build injection block
        String injection =
                "// === INJECTED BY TEST FRAMEWORK ===\n" +
                        "window.REPORT_DATA = " + json + ";\n" +
                        "// === END INJECTION ===\n";

        // Inject RIGHT BEFORE the line "const DEFAULT_REPORT_DATA = {"
        html = html.replace(targetLine, injection + targetLine);

        // Write output
        outputPath.getParent().toFile().mkdirs();
        Files.writeString(outputPath, html);

        System.out.println("✅ REPORT_DATA injected successfully (" + json.length() + " bytes)");

        // Print the injected section so you can see it in terminal
        int startIdx = html.indexOf("// === INJECTED BY TEST FRAMEWORK ===");
        if (startIdx >= 0) {
            int endIdx = html.indexOf("// === END INJECTION ===", startIdx) + "// === END INJECTION ===".length();
//            System.out.println("--- Injected block ---");
//            System.out.println(html.substring(startIdx, endIdx));
//            System.out.println("--- End injected block ---");
        }
    }


    /**
     * Build a TestResultData from a TestNG ITestResult.
     */
    private ReportData.TestResultData buildTestResult(ITestResult tr, String status) {

//        System.out.println("[DEBUG buildTestResult] Method=" + tr.getMethod().getMethodName()
//                + " Status=" + status
//                + " Retries=" + RetryAnalyzer.getRetryCount(tr));

        ReportData.TestResultData t = new ReportData.TestResultData();

        // Test name: method name
        t.name = tr.getMethod().getMethodName();

        // Module: use the class simple name, or group if available
        String[] groups = tr.getMethod().getGroups();
        if (groups != null && groups.length > 0) {
            t.module = groups[0];
        } else {
            t.module = tr.getTestClass().getName();
            // Extract just the class name without package
            int lastDot = t.module.lastIndexOf('.');
            if (lastDot >= 0) {
                t.module = t.module.substring(lastDot + 1);
            }
            // Remove "Test" suffix if present
            if (t.module.endsWith("Test")) {
                t.module = t.module.substring(0, t.module.length() - 4);
            }
        }

        t.status = status;
        t.durationMs = tr.getEndMillis() - tr.getStartMillis();

        // Retries: TestNG stores this in getMethod().getInvocationCount()
        // but for actual retries, check if there's a retry analyzer result.
        // Simple approach: count from the failed tests that were retried
        t.retries = RetryAnalyzer.getRetryCount(tr); // Set to actual retry count if you use IRetryAnalyzer

        // Timestamp: when this test finished
        t.timestamp = Instant.ofEpochMilli(tr.getEndMillis())
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        //System.out.println(" **************************************** retries " + t.retries);
        return t;

    }

    /**
     * Get build number from CI environment or generate a sequential one.
     */
    private String getBuildNumber() {
        // CI environments
        String jenkins = System.getenv("BUILD_NUMBER");
        String github = System.getenv("GITHUB_RUN_NUMBER");
        String gitlab = System.getenv("CI_PIPELINE_ID");
        String circle = System.getenv("CIRCLE_BUILD_NUM");
        String azure = System.getenv("BUILD_ID");

        if (jenkins != null) return jenkins;
        if (github != null) return github;
        if (gitlab != null) return gitlab;
        if (circle != null) return circle;
        if (azure != null) return azure;

        // Local run: increment from a counter file
        Path counterFile = Paths.get("test-output/.build-counter");
        try {
            if (Files.exists(counterFile)) {
                int count = Integer.parseInt(Files.readString(counterFile).trim());
                count++;
                Files.writeString(counterFile, String.valueOf(count));
                return String.valueOf(count);
            } else {
                counterFile.getParent().toFile().mkdirs();
                Files.writeString(counterFile, "1");
                return "1";
            }
        } catch (Exception e) {
            return "1";
        }
    }

    private String randomHex(int digits) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            sb.append(Integer.toHexString(r.nextInt(16)));
        }
        return sb.toString().toUpperCase();
    }

}
