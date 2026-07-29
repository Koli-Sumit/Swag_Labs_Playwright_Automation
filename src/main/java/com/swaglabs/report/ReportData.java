package com.swaglabs.report;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Maps 1:1 to the window.REPORT_DATA object expected by the HTML template.
 * Every field here corresponds to a key in the JavaScript object.
 */
public class ReportData {

    // ── Build metadata ──
    @SerializedName("buildNumber")
    public int buildNumber;

    @SerializedName("branch")
    public String branch;

    @SerializedName("runAgo")
    public String runAgo;

    @SerializedName("environment")
    public String environment;

    @SerializedName("reportId")
    public String reportId;

//    @SerializedName("frameworkVersion")
//    public String frameworkVersion = "v1.0.0";
//
//    @SerializedName("frameworkName")
//    public String frameworkName = "Playwright-TestNG Automation";
//
//    @SerializedName("projectName")
//    public String projectName = "Swag Labs";
//
//    @SerializedName("userName")
//    public String userName = "QA Team";
//
//    @SerializedName("userRole")
//    public String userRole = "Automation";
//
//    @SerializedName("userInitials")
//    public String userInitials = "QA";

    // ── Current build totals ──
    @SerializedName("passed")
    public int passed;

    @SerializedName("failed")
    public int failed;

    @SerializedName("skipped")
    public int skipped;

    // ── Trend history ──
    @SerializedName("trendBuilds")
    public List<String> trendBuilds;

    @SerializedName("trendPassed")
    public List<Integer> trendPassed;

    @SerializedName("trendFailed")
    public List<Integer> trendFailed;

    @SerializedName("trendSkipped")
    public List<Integer> trendSkipped;

    // ── KPI trend deltas (% change from previous build) ──
    @SerializedName("trendPassedDelta")
    public double trendPassedDelta;

    @SerializedName("trendFailedDelta")
    public double trendFailedDelta;

    @SerializedName("trendSkippedDelta")
    public double trendSkippedDelta;

    @SerializedName("trendDurationDelta")
    public double trendDurationDelta;

    // ── Duration ──
    @SerializedName("durationSeconds")
    public int durationSeconds;

    //Execution info
    @SerializedName("executionInfo")
    public ExecutionInfo executionInfo;

    // ── Suites ──
    @SerializedName("suites")
    public List<SuiteData> suites;

    // ── Recent tests ──
    @SerializedName("recentTests")
    public List<TestResultData> recentTests;

    // ── Module Summery ──
    public List<ModuleData> moduleSummary;

    // ── Inner classes ──

    public static class SuiteData {
        @SerializedName("name")
        public String name;
        @SerializedName("total")
        public int total;
        @SerializedName("passed")
        public int passed;
        @SerializedName("failed")
        public int failed;
        @SerializedName("skipped")
        public int skipped;
        @SerializedName("durationSeconds")
        public int durationSeconds;
    }

    public static class ModuleData {

        @SerializedName("module")
        public String module;

        @SerializedName("total")
        public int total;

        @SerializedName("passed")
        public int passed;

        @SerializedName("failed")
        public int failed;

        @SerializedName("skipped")
        public int skipped;

    }

    public static class TestResultData {
        @SerializedName("name")
        public String name;
        @SerializedName("description") // <-- ADD THIS
        public String description;      // <-- ADD THIS
        @SerializedName("module")
        public String module;
        @SerializedName("status")
        public String status;  // "passed" | "failed" | "skipped"
        @SerializedName("durationMs")
        public long durationMs;
        @SerializedName("retries")
        public int retries;
        @SerializedName("timestamp")
        public String timestamp;

        // ✅ ADD THIS — links to failure details when status is "failed"
        @SerializedName("failure")
        public FailureData failure;
    }

    // ✅ ADD THIS CLASS — holds all failure-specific data for the detail panel
    public static class FailureData {
        @SerializedName("id")
        public String id;              // e.g. "F-10421"

        @SerializedName("severity")
        public String severity;        // "critical" | "high" | "medium" | "low"

        @SerializedName("errorType")
        public String errorType;       // "Timeout" | "Network" | "Validation" | "Auth" | "Unknown"

        @SerializedName("error")
        public String error;           // Short error message

        @SerializedName("stack")
        public String stack;           // Full stack trace

        @SerializedName("runId")
        public String runId;           // e.g. "RUN-88a3f2"

        @SerializedName("node")
        public String node;

        @SerializedName("screenshot")
        public String screenshot;           // Take screenshot

        @SerializedName("timeline")
        public List<TimelineEvent> timeline;
    }

    // ✅ ADD THIS CLASS — individual events in the failure timeline
    public static class TimelineEvent {
        @SerializedName("time")
        public String time;            // e.g. "14:31:51"

        @SerializedName("text")
        public String text;            // e.g. "Test initiated — connecting to payment API"

        @SerializedName("fail")
        public boolean fail;           // true = red dot, false = grey dot
    }

    //Execution info

    public static class ExecutionInfo {

        @SerializedName("build")
        public BuildInfo build;

        @SerializedName("environment")
        public EnvironmentInfo environment;

        @SerializedName("execution")
        public ExecutionDetails execution;
    }

    public static class BuildInfo {

        @SerializedName("number")
        public String number;

        @SerializedName("branch")
        public String branch;

        @SerializedName("version")
        public String version;

        @SerializedName("commit")
        public String commit;

        @SerializedName("triggeredBy")
        public String triggeredBy;

        @SerializedName("suite")
        public String suite;
    }

    public static class EnvironmentInfo {

        @SerializedName("name")
        public String name;

        @SerializedName("browser")
        public String browser;

        @SerializedName("browserVersion")
        public String browserVersion;

        @SerializedName("os")
        public String os;

        @SerializedName("java")
        public String java;

        @SerializedName("playwright")
        public String playwright;
    }

    public static class ExecutionDetails {

        @SerializedName("mode")
        public String mode;

        @SerializedName("startTime")
        public String startTime;

        @SerializedName("endTime")
        public String endTime;

        @SerializedName("duration")
        public String duration;
    }


}
