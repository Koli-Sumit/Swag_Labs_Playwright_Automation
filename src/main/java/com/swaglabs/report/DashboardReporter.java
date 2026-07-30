package com.swaglabs.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.swaglabs.utils.EmailUtil;
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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * TestNG IReporter that generates the dynamic HTML dashboards.
 * <p>
 * This runs ONCE after all suites complete. It:
 * 1. Collects passed/failed/skipped counts from all test results
 * 2. Builds suite-level and individual test-level data
 * 3. Updates trend history (persisted JSON file)
 * 4. Reads the HTML templates, injects data, writes outputs
 * <p>
 * Generates TWO reports by default (so links don't 404):
 * - test-output/dashboard-report.html    (Legacy dashboard)
 * - test-output/coverage-report.html     (Coverage dashboard)
 * <p>
 * Control via -Dreport.type=both|dashboard|coverage (default: both)
 * <p>
 * Wire it in testng.xml:
 * <listeners>
 * <listener class-name="com.swaglabs.report.DashboardReporter" />
 * </listeners>
 */
public class DashboardReporter implements IReporter {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String TEMPLATE_PATH = "src/main/resources/report-template.html";
    private static final String COVERAGE_TEMPLATE_PATH = "src/main/resources/coverage-template.html";
    private static final String OUTPUT_PATH = "test-output/dashboard-report.html";
    private static final String COVERAGE_OUTPUT_PATH = "test-output/coverage-report.html";
    private static final String HISTORY_PATH = "test-output/trend-history.json";

    Data data;

    @Override
    public void generateReport(
            List<XmlSuite> xmlSuites,
            List<ISuite> suites,
            String outputDirectory
    ) {
        long runStartMs = System.currentTimeMillis();
        System.out.println("=================================================================");
        System.out.println("  Dashboard Report Generation Started");
        System.out.println("=================================================================");

        // ── 1. Aggregate all results ──
        int totalPassed = 0;
        int totalFailed = 0;
        int totalSkipped = 0;
        long totalDurationMs = 0;

        List<ReportData.SuiteData> suiteDataList = new ArrayList<>();
        List<ReportData.TestResultData> allTests = new ArrayList<>();

        for (ISuite suite : suites) {

            if (suite.getResults().isEmpty()) {
                continue;   // Skip Master
            }

            String suiteName = suite.getName();
            int sPassed = 0, sFailed = 0, sSkipped = 0;
            long sDurationMs = 0;

            Map<String, ISuiteResult> suiteResults = suite.getResults();

            for (ISuiteResult sr : suiteResults.values()) {
                ITestContext tc = sr.getTestContext();

                Map<String, ITestResult> finalResultsMap = new LinkedHashMap<>();

                List<ITestResult> allRawResults = new ArrayList<>();
                allRawResults.addAll(tc.getPassedTests().getAllResults());
                allRawResults.addAll(tc.getFailedTests().getAllResults());
                allRawResults.addAll(tc.getSkippedTests().getAllResults());

                for (ITestResult tr : allRawResults) {
                    String key = tr.getMethod().getMethodName();
                    ITestResult existing = finalResultsMap.get(key);

                    if (existing == null || tr.getEndMillis() > existing.getEndMillis()) {
                        finalResultsMap.put(key, tr);
                    }
                }

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

//         ── 2. Sort tests by end time (most recent first) ──
        allTests.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
        Collections.reverse(allTests);
        List<ReportData.TestResultData> recentTests = new ArrayList<>(allTests);

        //Module Summery
        Map<String, ReportData.ModuleData> moduleMap = new LinkedHashMap<>();

        for (ReportData.TestResultData test : allTests) {

            ReportData.ModuleData module = moduleMap.computeIfAbsent(test.module, key -> {
                ReportData.ModuleData m = new ReportData.ModuleData();
                m.module = key;
                return m;
            });

            module.total++;

            switch (test.status) {
                case "passed":
                    module.passed++;
                    break;

                case "failed":
                    module.failed++;
                    break;

                case "skipped":
                    module.skipped++;
                    break;
            }
        }

        List<ReportData.ModuleData> moduleSummary =
                new ArrayList<>(moduleMap.values());

        // ── 3. Trend history ──
        // Calculate coverages for trend tracking
        int reqTotalTemp = totalPassed + totalFailed + totalSkipped;
        int reqCovPct = reqTotalTemp > 0 ? (totalPassed * 100) / reqTotalTemp : 0;

        int tcTotalTemp = totalPassed + totalFailed + totalSkipped;
        int tcCovPct = tcTotalTemp > 0 ? (totalPassed * 100) / tcTotalTemp : 0;

        Map<String, int[]> tempModuleStats = new LinkedHashMap<>();
        for (ReportData.TestResultData t : allTests) {
            tempModuleStats.computeIfAbsent(t.module, k -> new int[3]);
            int[] stats = tempModuleStats.get(t.module);
            if ("passed".equals(t.status)) stats[0]++;
            else if ("failed".equals(t.status)) stats[1]++;
            else stats[2]++;
        }
        int featCoveredTemp = 0;
        int featTotalTemp = tempModuleStats.size();
        for (int[] stats : tempModuleStats.values()) {
            int total = stats[0] + stats[1] + stats[2];
            if (total > 0 && (int) Math.round((stats[0] * 100.0) / total) == 100) featCoveredTemp++;
        }
        int featCovPct = featTotalTemp > 0 ? (featCoveredTemp * 100) / featTotalTemp : 0;

        int browserCovPct = 100; // Assuming 100% for single browser runs

        String buildNumber = getBuildNumber();
        Path historyFile = Paths.get(HISTORY_PATH);
        TrendHistory.TrendSnapshot trend =
                TrendHistory.recordAndSnapshot(
                        "#" + buildNumber,
                        totalPassed, totalFailed, totalSkipped,
                        totalDurationSec,
                        reqCovPct, tcCovPct, featCovPct, browserCovPct, // NEW: Pass percentages
                        historyFile
                );

        // ── 4. Read templates, detect type, inject data, write output ──
        try {
            String reportType = System.getProperty("report.type",
                    System.getenv("REPORT_TYPE") != null ? System.getenv("REPORT_TYPE") : "both");

            boolean generateDashboard = "both".equalsIgnoreCase(reportType) || "dashboard".equalsIgnoreCase(reportType);
            boolean generateCoverage = "both".equalsIgnoreCase(reportType) || "coverage".equalsIgnoreCase(reportType);

            // ── Generate Legacy Dashboard ──
            if (generateDashboard) {
                Path templatePath = Paths.get(TEMPLATE_PATH);
                if (Files.exists(templatePath)) {
                    String html = Files.readString(templatePath);

                    if (html.contains("const DEFAULT_REPORT_DATA = {")) {
                        ReportData data = new ReportData();
                        data.buildNumber = Integer.parseInt(buildNumber);
                        data.branch = System.getProperty("branch", System.getenv("BRANCH") != null ? System.getenv("BRANCH") : "Master");

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
                        //String buildStartTime = LocalDateTime.now().format(formatter);
                        String buildStartTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(formatter);
                        data.runAgo = buildStartTime;

                        data.environment = System.getProperty("env", System.getenv("ENV") != null ? System.getenv("ENV") : "Production");
                        data.reportId = "RPT-" +
                                Instant.now()
                                        .atZone(ZoneId.of("Asia/Kolkata"))
                                        .format(DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss"))
                                        .replace("-", "") +
                                "-" + randomHex(4);
                        data.passed = totalPassed;
                        data.failed = totalFailed;
                        data.skipped = totalSkipped;
                        data.durationSeconds = totalDurationSec;
                        data.suites = suiteDataList;
                        data.recentTests = recentTests;
                        data.moduleSummary = moduleSummary;

                        data.trendBuilds = trend.builds;
                        data.trendPassed = trend.passed;
                        data.trendFailed = trend.failed;
                        data.trendSkipped = trend.skipped;

                        data.trendPassedDelta = trend.passedDelta;
                        data.trendFailedDelta = trend.failedDelta;
                        data.trendSkippedDelta = trend.skippedDelta;
                        data.trendDurationDelta = trend.durationDelta;


// ==========================
// Execution Information
// ==========================

                        ReportData.ExecutionInfo executionInfo = new ReportData.ExecutionInfo();

                        executionInfo.build = new ReportData.BuildInfo();
                        data.build = executionInfo.build;
                        executionInfo.environment = new ReportData.EnvironmentInfo();
                        executionInfo.execution = new ReportData.ExecutionDetails();

                        executionInfo.build.number = "#" + buildNumber;
                        executionInfo.build.branch = getGitBranch();
                        executionInfo.build.version = System.getProperty("version", "1.0");
                        executionInfo.build.commit = getGitCommit();
                        executionInfo.build.triggeredBy = getTriggeredBy();
                        executionInfo.build.suite = xmlSuites.isEmpty() ? "-" : xmlSuites.get(0).getName();

                        executionInfo.environment.name = data.environment;
                        executionInfo.environment.browser = getBrowserName();
                        executionInfo.environment.browserVersion = "";
                        executionInfo.environment.os = System.getProperty("os.name");
                        executionInfo.environment.java = System.getProperty("java.version");
                        executionInfo.environment.playwright = getPlaywrightVersion();

                        String executionMode =
                                xmlSuites.get(0).getParallel() == XmlSuite.ParallelMode.NONE
                                        ? "Sequential"
                                        : "Parallel";

                        executionInfo.execution.mode = executionMode;
                        executionInfo.execution.startTime = buildStartTime;
                        executionInfo.execution.endTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(formatter);
                        executionInfo.execution.duration = formatDuration(totalDurationSec);

                        data.executionInfo = executionInfo;

                        generateHtmlReport(data);
                    }
                } else {
                    System.err.println("[DashboardReporter] Legacy template not found at: " + templatePath.toAbsolutePath());
                }
            }

            // ── Generate Coverage Dashboard ──
            if (generateCoverage) {
                Path coverageTemplate = Paths.get(COVERAGE_TEMPLATE_PATH);
                if (Files.exists(coverageTemplate)) {
                    String html = Files.readString(coverageTemplate);

                    // --- NEW: Inject the dynamic delta badges ---
                    html = html.replace("{{REQ_DELTA_BADGE}}", buildDeltaBadgeHtml(trend.reqCoverageDelta));
                    html = html.replace("{{TC_DELTA_BADGE}}", buildDeltaBadgeHtml(trend.tcCoverageDelta));
                    html = html.replace("{{FEAT_DELTA_BADGE}}", buildDeltaBadgeHtml(trend.featCoverageDelta));
                    html = html.replace("{{BROWSER_DELTA_BADGE}}", buildDeltaBadgeHtml(trend.browserCoverageDelta));

                    CoverageData coverageData = buildCoverageData(
                            totalPassed, totalFailed, totalSkipped, totalDurationSec, allTests
                    );

                    // FIX: Pass COVERAGE_OUTPUT_PATH so it writes to coverage-report.html
                    generateCoverageHtmlReport(html, coverageData, Paths.get(COVERAGE_OUTPUT_PATH));
                } else {
                    System.err.println("[DashboardReporter] Coverage template not found at: " + coverageTemplate.toAbsolutePath());
                }
            }

        } catch (Exception e) {
            System.err.println("[DashboardReporter] FAILED to generate report:");
            e.printStackTrace();
        }

        long elapsed = System.currentTimeMillis() - runStartMs;
        System.out.println("=================================================================");
        System.out.println("  Dashboard : " + Paths.get(OUTPUT_PATH).toAbsolutePath());
        System.out.println("  Coverage  : " + Paths.get(COVERAGE_OUTPUT_PATH).toAbsolutePath());
        System.out.println("  Passed: " + totalPassed + " | Failed: " + totalFailed + " | Skipped: " + totalSkipped);
        System.out.println("  Duration: " + totalDurationSec + "s");
        System.out.println("  Generated in: " + elapsed + "ms");

        System.out.println("=================================================================");

        try {
            //EmailUtil.sendEmail();
            System.out.println("Automation report email sent successfully.");
            System.out.println("=================================================================");
        } catch (Exception e) {
            System.err.println("Failed to send automation report email.");
            //e.printStackTrace();
            System.out.println("=================================================================");
        }

    }

    // ═══════════════════════════════════════════════════════════
    //  COVERAGE DATA BUILDERS
    // ═══════════════════════════════════════════════════════════

    private CoverageData buildCoverageData(
            int totalPassed, int totalFailed, int totalSkipped,
            int totalDurationSec, List<ReportData.TestResultData> allTests
    ) {
        CoverageData cd = new CoverageData();

        cd.reportId = "RPT-" +
                Instant.now()
                        .atZone(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss"))
                        .replace("-", "") +
                "-" + randomHex(4);
        cd.durationSeconds = totalDurationSec;

        // ── 1. READ EXCEL DATA ──
        String excelPath = "src/test/resources/requirements.xlsx";
        Map<String, String> excelData = readRequirementsFromExcel(excelPath);

        // ── 2. CALCULATE ACTUAL REQUIREMENT COVERAGE ──
        int covered = 0;
        int notCovered = 0;
        int partial = 0;
        Set<String> executedReqIds = new HashSet<>();

        for (ReportData.TestResultData test : allTests) {
            String reqId = test.description;

//            System.out.println("DEBUG TEST NAME: " + test.name);
//            System.out.println("DEBUG DESCRIPTION: " + test.description);
//            System.out.println("DEBUG TEST STATUS: " + test.status); // <-- ADDED THIS

            if (reqId == null || reqId.isEmpty() || !reqId.startsWith("REQ")) continue;

            executedReqIds.add(reqId);
            String excelStatus = excelData.getOrDefault(reqId, "MAPPED");
            //System.out.println("DEBUG EXCEL STATUS FOR THIS REQ: " + excelStatus); // <-- ADDED THIS

            if ("failed".equals(test.status) || "skipped".equals(test.status)) {
                //System.out.println("DEBUG ACTION: Marking as NOT COVERED (Test failed/skipped)"); // <-- ADDED THIS
                notCovered++;
            } else if ("passed".equals(test.status)) {
                if ("PARTIAL".equalsIgnoreCase(excelStatus)) {
                    //System.out.println("DEBUG ACTION: Marking as PARTIAL"); // <-- ADDED THIS
                    partial++;
                } else {
                    //System.out.println("DEBUG ACTION: Marking as COVERED"); // <-- ADDED THIS
                    covered++;
                }
            }
        }

        // ── 3. COUNT EXCEL REQUIREMENTS THAT HAD NO TEST RUN ──
        for (String excelReqId : excelData.keySet()) {
            if (!executedReqIds.contains(excelReqId)) {
                System.out.println("DEBUG ACTION: " + excelReqId + " had no test, marking NOT COVERED"); // <-- ADDED THIS
                notCovered++;
            }
        }

        // <-- ADDED THIS FINAL PRINT
        System.out.println("=================================================================");
        System.out.println("✅ Excel_DATA mapped successfully -> Covered: " + covered + ", Not Covered: " + notCovered + ", Partial: " + partial);
        System.out.println("=================================================================");

        // ── 4. SET REQUIREMENT DATA ──
        cd.requirement = new CoverageData.RequirementData();
        cd.requirement.covered = covered;
        cd.requirement.notCovered = notCovered;
        cd.requirement.partial = partial;

        // ── 5. TEST CASE COVERAGE (Remains unchanged - based on pure execution) ──
        cd.testCase = new CoverageData.TestCaseData();
        cd.testCase.passed = totalPassed;
        cd.testCase.failed = totalFailed;
        cd.testCase.skipped = totalSkipped;
        cd.testCase.notExecuted = 0;

        // ── 6. BROWSERS, PLATFORMS, FEATURES (Keep your existing logic below) ──
        cd.browsers = buildBrowserData(totalPassed, totalFailed, totalSkipped);
        cd.platforms = buildPlatformData(totalPassed + totalFailed + totalSkipped);

        Map<String, int[]> moduleStats = new LinkedHashMap<>();
        for (ReportData.TestResultData t : allTests) {
            moduleStats.computeIfAbsent(t.module, k -> new int[3]);
            int[] stats = moduleStats.get(t.module);
            if ("passed".equals(t.status)) stats[0]++;
            else if ("failed".equals(t.status)) stats[1]++;
            else stats[2]++;
        }

        cd.features = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : moduleStats.entrySet()) {
            CoverageData.FeatureData f = new CoverageData.FeatureData();
            f.name = entry.getKey();
            int total = entry.getValue()[0] + entry.getValue()[1] + entry.getValue()[2];
            f.tests = total;
            f.coverage = total > 0 ? (int) Math.round((entry.getValue()[0] * 100.0) / total) : 0;
            cd.features.add(f);
        }

        return cd;

    }

    private List<CoverageData.BrowserData> buildBrowserData(
            int totalPassed, int totalFailed, int totalSkipped
    ) {
        List<CoverageData.BrowserData> browsers = new ArrayList<>();
        String browser = System.getProperty("browser",
                System.getenv("BROWSER") != null ? System.getenv("BROWSER") : "chrome");

        String displayName;
        switch (browser.toLowerCase().trim()) {
            case "firefox":
                displayName = "Firefox";
                break;
            case "edge":
                displayName = "Edge";
                break;
            case "safari":
                displayName = "Safari";
                break;
            default:
                displayName = "Chrome";
                break;
        }

        CoverageData.BrowserData b = new CoverageData.BrowserData();
        b.name = displayName;
        b.total = totalPassed + totalFailed + totalSkipped;
        b.pass = totalPassed;
        b.fail = totalFailed;
        b.skip = totalSkipped;
        browsers.add(b);

        return browsers;
    }

    private List<CoverageData.PlatformData> buildPlatformData(int totalTests) {
        List<CoverageData.PlatformData> platforms = new ArrayList<>();
        String os = System.getProperty("os.name", "Unknown");

        String name, icon, color;
        String osLower = os.toLowerCase();
        if (osLower.contains("win")) {
            name = os;
            icon = "fab fa-windows";
            color = "#06b6d4";
        } else if (osLower.contains("mac")) {
            name = os;
            icon = "fab fa-apple";
            color = "#a855f7";
        } else if (osLower.contains("nux") || osLower.contains("nix") || osLower.contains("aix")) {
            name = os;
            icon = "fab fa-linux";
            color = "#f59e0b";
        } else {
            name = os;
            icon = "fas fa-desktop";
            color = "#64748b";
        }

        CoverageData.PlatformData p = new CoverageData.PlatformData();
        p.name = name;
        p.icon = icon;
        p.color = color;
        p.tests = totalTests;
        platforms.add(p);

        return platforms;
    }

    // ═══════════════════════════════════════════════════════════
    //  COVERAGE HTML GENERATOR
    // ═══════════════════════════════════════════════════════════

    private void generateCoverageHtmlReport(String html, CoverageData data, Path outputPath) throws IOException {

        // Coverage Delta Badges
        // If you want a neutral dash instead of hiding it, replace "" with the span string above.
        html = html.replace("{{REQ_DELTA_BADGE}}", "");
        html = html.replace("{{TC_DELTA_BADGE}}", "");
        html = html.replace("{{FEAT_DELTA_BADGE}}", "");
        html = html.replace("{{BROWSER_DELTA_BADGE}}", "");

        String json = GSON.toJson(data);

        html = replaceDataBlock(html, json);

        // Replace descriptive sub-texts to match real data
        // Requirements
        int reqTotal = data.requirement.covered + data.requirement.notCovered + data.requirement.partial;
        html = html.replace("{{REQ_MAPPED_SUMMARY}}", data.requirement.covered + " of " + reqTotal + " requirements mapped");
        html = html.replace("{{REQ_COVERED_COUNT}}", String.valueOf(data.requirement.covered));
        html = html.replace("{{REQ_NOT_COVERED_COUNT}}", String.valueOf(data.requirement.notCovered));
        html = html.replace("{{REQ_PARTIAL_COUNT}}", String.valueOf(data.requirement.partial));


        // Test Cases
        int tcExecuted = data.testCase.passed + data.testCase.failed + data.testCase.skipped;
        int tcTotal = tcExecuted + data.testCase.notExecuted;
        html = html.replace("{{TC_EXECUTED_SUMMARY}}", tcExecuted + " of " + tcTotal + " cases executed");
        html = html.replace("{{TC_PASSED_COUNT}}", String.valueOf(data.testCase.passed));
        html = html.replace("{{TC_FAILED_COUNT}}", String.valueOf(data.testCase.failed));
        html = html.replace("{{TC_SKIPPED_COUNT}}", String.valueOf(data.testCase.skipped));
        html = html.replace("{{TC_NOT_EXECUTED_COUNT}}", String.valueOf(data.testCase.notExecuted));

// Platforms
        int platTotalTests = 0;
        for (CoverageData.PlatformData p : data.platforms) {
            platTotalTests += p.tests;
        }
        html = html.replace("{{PLATFORM_SUMMARY}}", platTotalTests + " tests across " + data.platforms.size() + " platform" + (data.platforms.size() != 1 ? "s" : ""));

// Features
        int featCovered = 0;
        for (CoverageData.FeatureData f : data.features) {
            if (f.coverage == 100) featCovered++;
        }
        int featTotal = data.features.size();
        int featRemaining = featTotal - featCovered;
        html = html.replace("{{FEATURE_SUMMARY}}", featCovered + " of " + featTotal + " features fully automated \u2014 " + featRemaining + " remaining");

// Report Meta
        String durationStr = formatDuration(data.durationSeconds);
        html = html.replace("{{REPORT_META_SUMMARY}}", "Report ID: " + data.reportId + " \u00b7 Duration: " + durationStr);

// Last Run
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
        String nowStr = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(fmt);
        html = html.replace("{{LAST_RUN_SUMMARY}}", "Last run: " + nowStr);

// Sprint/Release
        String sprint = System.getProperty("sprint", System.getenv("SPRINT") != null ? System.getenv("SPRINT") : "Sprint " + getBuildNumber());
        String release = System.getProperty("release", System.getenv("RELEASE") != null ? System.getenv("RELEASE") : "v" + getBuildNumber() + ".0");
        html = html.replace("{{SPRINT_RELEASE_SUMMARY}}", sprint + " &middot; Release " + release);

        outputPath.getParent().toFile().mkdirs();
        Files.writeString(outputPath, html);

        //System.out.println("✅ COVERAGE_DATA injected successfully (" + json.length() + " bytes) -> " + outputPath.toAbsolutePath());
        System.out.println("✅ COVERAGE_DATA injected successfully (" + json.length() + " bytes) -> " + outputPath.toRealPath());
    }

    private String replaceDataBlock(String html, String json) {
        String startMarker = "const DATA = {";
        int startIdx = html.indexOf(startMarker);
        if (startIdx == -1) {
            System.err.println("[DashboardReporter] WARNING: 'const DATA = {' not found — skipping block replacement");
            return html;
        }

        int braceStart = html.indexOf('{', startIdx);
        int depth = 0;
        int endIdx = -1;

        for (int i = braceStart; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    endIdx = i + 1;
                    if (endIdx < html.length() && html.charAt(endIdx) == ';') {
                        endIdx++;
                    }
                    break;
                }
            }
        }

        if (endIdx == -1) {
            System.err.println("[DashboardReporter] WARNING: Could not find matching '}' for DATA block");
            return html;
        }

        return html.substring(0, startIdx) + "const DATA = " + json + ";" + html.substring(endIdx);
    }

    private String formatDuration(int totalSeconds) {
        if (totalSeconds < 60) return totalSeconds + "s";
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes < 60) return minutes + "m " + seconds + "s";
        int hours = minutes / 60;
        minutes = minutes % 60;
        return hours + "h " + minutes + "m " + seconds + "s";
    }

    // ═══════════════════════════════════════════════════════════
    //  LEGACY HTML GENERATOR
    // ═══════════════════════════════════════════════════════════

    private void generateHtmlReport(ReportData data) throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        Path outputPath = Paths.get(OUTPUT_PATH);

        if (!Files.exists(templatePath)) {
            throw new FileNotFoundException("Template not found: " + templatePath.toAbsolutePath());
        }

        String html = Files.readString(templatePath);
        String json = GSON.toJson(data);

        String targetLine = "const DEFAULT_REPORT_DATA = {";

        if (!html.contains(targetLine)) {
            System.err.println("╔══════════════════════════════════════════════════════════════╗");
            System.err.println("║  FATAL: Cannot find '" + targetLine + "'           ║");
            System.err.println("║  in template: " + templatePath.toAbsolutePath());
            System.err.println("║  The HTML template appears corrupted or wrong file.    ║");
            System.err.println("╚══════════════════════════════════════════════════════════════╝");
            outputPath.getParent().toFile().mkdirs();
            Files.writeString(outputPath, html);
            return;
        }

        String injection =
                "// === INJECTED BY TEST FRAMEWORK ===\n" +
                        "window.REPORT_DATA = " + json + ";\n" +
                        "// === END INJECTION ===\n";

        html = html.replace(targetLine, injection + targetLine);

        outputPath.getParent().toFile().mkdirs();
        Files.writeString(outputPath, html);

        //System.out.println("✅ REPORT_DATA injected successfully (" + json.length() + " bytes) -> " + outputPath.toAbsolutePath());
        System.out.println("✅ REPORT_DATA injected successfully (" + json.length() + " bytes) -> " + outputPath.toRealPath());
    }

    // ═══════════════════════════════════════════════════════════
    //  TEST RESULT BUILDERS
    // ═══════════════════════════════════════════════════════════

    private ReportData.TestResultData buildTestResult(ITestResult tr, String status) {
        ReportData.TestResultData t = new ReportData.TestResultData();

        t.name = tr.getMethod().getMethodName();
        t.description = tr.getMethod().getDescription() != null ? tr.getMethod().getDescription() : "";

        ///////////////////////////
        String className = tr.getTestClass().getRealClass().getSimpleName();

// Remove "Test" prefix
        if (className.startsWith("Test")) {
            className = className.substring(4);
        }

// Optional: Remove "Tests" suffix if you use names like LoginTests
        if (className.endsWith("Tests")) {
            className = className.substring(0, className.length() - 5);
        }

        t.module = className;

        t.status = status;
        t.durationMs = tr.getEndMillis() - tr.getStartMillis();
        t.retries = RetryAnalyzer.getRetryCount(tr);
        t.timestamp = Instant.ofEpochMilli(tr.getEndMillis())
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("hh:mm:ss a"));

        if ("failed".equals(status) && tr.getThrowable() != null) {
            t.failure = buildFailureData(tr);
        }

        return t;
    }

    private ReportData.FailureData buildFailureData(ITestResult tr) {
        ReportData.FailureData f = new ReportData.FailureData();
        Throwable throwable = tr.getThrowable();

        f.id = "F-" + (10000 + new Random().nextInt(90000));
        f.severity = determineSeverity(throwable);
        f.errorType = determineErrorType(throwable);

        String msg = throwable.getMessage() != null
                ? throwable.getMessage()
                : throwable.getClass().getSimpleName();
        f.error = msg.length() > 300 ? msg.substring(0, 300) + "..." : msg;

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String stack = sw.toString();
        f.stack = stack.length() > 2000 ? stack.substring(0, 2000) + "\n... [truncated]" : stack;

        f.runId = "RUN-" + randomHex(6).toLowerCase();
        f.node = System.getenv("HOSTNAME") != null ? System.getenv("HOSTNAME") : "local-runner";

        f.timeline = buildTimeline(tr, throwable);

        Object ss = tr.getAttribute("screenshot");
        if (ss != null) {
            f.screenshot = ss.toString();
        }

        return f;
    }

    private String determineSeverity(Throwable t) {
        String name = t.getClass().getSimpleName();
        String msg = (t.getMessage() != null ? t.getMessage() : "").toLowerCase();

        if (name.contains("Timeout") || msg.contains("timed out") || name.contains("OutOfMemory")) {
            return "critical";
        }
        if (name.contains("Connect") || name.contains("Socket") || name.contains("SSL")
                || msg.contains("connection refused") || msg.contains("econnrefused")) {
            return "high";
        }
        if (name.contains("Assertion") || msg.contains("expected") || msg.contains("but was")) {
            return "medium";
        }
        return "low";
    }

    private String determineErrorType(Throwable t) {
        String name = t.getClass().getSimpleName();
        String msg = (t.getMessage() != null ? t.getMessage() : "").toLowerCase();

        if (name.contains("Timeout") || msg.contains("timed out")) return "Timeout";
        if (msg.contains("auth") || msg.contains("401") || msg.contains("403")) return "Auth";
        if (msg.contains("connection") || msg.contains("network") || msg.contains("econn")) return "Network";
        if (name.contains("Assertion") || msg.contains("expected") || msg.contains("validation")) return "Validation";
        if (name.contains("Locator") || msg.contains("locator expected") || msg.contains("strict mode violation"))
            return "Locator Not Found";
        return "Unknown";
    }

    private List<ReportData.TimelineEvent> buildTimeline(ITestResult tr, Throwable throwable) {
        List<ReportData.TimelineEvent> timeline = new ArrayList<>();
        long start = tr.getStartMillis();
        long end = tr.getEndMillis();
        int retries = RetryAnalyzer.getRetryCount(tr);

        ReportData.TimelineEvent e1 = new ReportData.TimelineEvent();
        e1.time = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        e1.text = "Test initiated — " + tr.getMethod().getMethodName();
        e1.fail = false;
        timeline.add(e1);

        if (retries > 0) {
            long interval = (end - start) / (retries + 1);
            for (int i = 1; i <= retries; i++) {
                long t = start + (interval * i);

                ReportData.TimelineEvent rs = new ReportData.TimelineEvent();
                rs.time = Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                rs.text = "Retry " + i + "/" + retries + " initiated";
                rs.fail = false;
                timeline.add(rs);

                ReportData.TimelineEvent rf = new ReportData.TimelineEvent();
                rf.time = Instant.ofEpochMilli(t + interval / 2).atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                rf.text = "Retry " + i + " failed — " + getShortError(throwable);
                rf.fail = true;
                timeline.add(rf);
            }
        }

        ReportData.TimelineEvent ef = new ReportData.TimelineEvent();
        ef.time = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        ef.text = "Test marked FAILED — " + getShortError(throwable);
        ef.fail = true;
        timeline.add(ef);

        return timeline;
    }

    private String getShortError(Throwable t) {
        if (t == null) return "Unknown error";
        String msg = t.getMessage();
        if (msg != null && msg.length() > 60) return msg.substring(0, 60) + "...";
        return msg != null ? msg : t.getClass().getSimpleName();
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════════════════

    private String getBuildNumber() {
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

    private String getGitCommit() {
        try {
            Process process = new ProcessBuilder(
                    "git", "rev-parse", "--short", "HEAD")
                    .redirectErrorStream(true)
                    .start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String commit = reader.readLine();
            process.waitFor();

            return commit != null ? commit : "-";

        } catch (Exception e) {
            return "-";
        }
    }

    private String getTriggeredBy() {

        String actor = System.getenv("GITHUB_ACTOR");

        if (actor != null && !actor.isBlank()) {
            return actor;
        }

        return System.getProperty("user.name");
    }

    private String getGitBranch() {

        String githubBranch = System.getenv("GITHUB_REF_NAME");

        if (githubBranch != null && !githubBranch.isBlank()) {
            return githubBranch;
        }

        try {
            Process process = new ProcessBuilder(
                    "git", "branch", "--show-current")
                    .redirectErrorStream(true)
                    .start();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

            String branch = reader.readLine();

            process.waitFor();

            return branch != null ? branch : "master";

        } catch (Exception e) {
            return "master";
        }
    }

    private String getBrowserName() {

        return System.getProperty(
                "browser",
                System.getenv("BROWSER") != null
                        ? System.getenv("BROWSER")
                        : "Chrome");
    }

    private String getPlaywrightVersion() {

        Package pkg =
                com.microsoft.playwright.Playwright.class.getPackage();

        return pkg.getImplementationVersion() != null
                ? pkg.getImplementationVersion()
                : "Unknown";
    }

    private String randomHex(int digits) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            sb.append(Integer.toHexString(r.nextInt(16)));
        }
        return sb.toString().toUpperCase();
    }

    // ═══════════════════════════════════════════════════════════
//  EXCEL & NAMING CONVENTION UTILITIES
// ═══════════════════════════════════════════════════════════

    private Map<String, String> readRequirementsFromExcel(String filePath) {
        Map<String, String> reqStatusMap = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Requirements");
            if (sheet == null) sheet = workbook.getSheetAt(0); // Fallback to first sheet

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell idCell = row.getCell(0);
                Cell statusCell = row.getCell(2); // Assuming Column C (index 2) is Status

                if (idCell != null && statusCell != null) {
                    String reqId = idCell.getStringCellValue().trim();
                    String status = statusCell.getStringCellValue().trim();
                    reqStatusMap.put(reqId, status); // e.g., "REQ-001" -> "MAPPED"
                }
            }
        } catch (Exception e) {
            System.err.println("[DashboardReporter] Warning: Could not read Excel file at " + filePath);
            System.err.println("Falling back to standard Pass/Fail counting.");
        }
        return reqStatusMap;
    }

    private String extractReqId(String testName) {
        // Format: REQ001_Login_FULL -> extracts "REQ001" and converts to "REQ-001"
        if (testName != null && testName.startsWith("REQ")) {
            String[] parts = testName.split("_");
            String rawId = parts[0]; // "REQ001"
            // Insert dash after REQ: REQ-001
            return rawId.replaceFirst("REQ(\\d+)", "REQ-$1");
        }
        return null;
    }

    private String extractCoverageType(String testName) {
        if (testName != null && testName.startsWith("REQ")) {
            String[] parts = testName.split("_");
            return parts[parts.length - 1]; // Returns "FULL", "PARTIAL", or "NOT_COVERED"
        }
        return "FULL";
    }

    private String buildDeltaBadgeHtml(double delta) {
        if (delta == 0) {
            return "<span class=\"badge badge-skip\"><i class=\"fas fa-minus text-[9px]\"></i> 0%</span>";
        } else if (delta > 0) {
            return String.format("<span class=\"badge badge-pass\"><i class=\"fas fa-arrow-up text-[9px]\"></i> %.1f%%</span>", delta);
        } else {
            return String.format("<span class=\"badge badge-fail\"><i class=\"fas fa-arrow-down text-[9px]\"></i> %.1f%%</span>", Math.abs(delta));
        }
    }

}
