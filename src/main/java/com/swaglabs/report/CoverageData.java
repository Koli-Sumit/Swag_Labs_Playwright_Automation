package com.swaglabs.report;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Maps 1:1 to the const DATA object expected by the coverage HTML template.
 * Every field here corresponds to a key in the JavaScript DATA object.
 */
public class CoverageData {

    // ── Report metadata (for footer replacement) ──
    @SerializedName("reportId")
    public String reportId;

    @SerializedName("durationSeconds")
    public int durationSeconds;

    // ── Requirement coverage ──
    @SerializedName("requirement")
    public RequirementData requirement;

    // ── Test case coverage ──
    @SerializedName("testCase")
    public TestCaseData testCase;

    // ── Browser breakdown ──
    @SerializedName("browsers")
    public List<BrowserData> browsers;

    // ── Platform breakdown ──
    @SerializedName("platforms")
    public List<PlatformData> platforms;

    // ── Feature-level coverage ──
    @SerializedName("features")
    public List<FeatureData> features;

    // ── Inner classes ──

    public static class RequirementData {
        @SerializedName("covered")
        public int covered;

        @SerializedName("notCovered")
        public int notCovered;

        @SerializedName("partial")
        public int partial;
    }

    public static class TestCaseData {
        @SerializedName("passed")
        public int passed;

        @SerializedName("failed")
        public int failed;

        @SerializedName("skipped")
        public int skipped;

        @SerializedName("notExecuted")
        public int notExecuted;
    }

    public static class BrowserData {
        @SerializedName("name")
        public String name;

        @SerializedName("total")
        public int total;

        @SerializedName("pass")
        public int pass;

        @SerializedName("fail")
        public int fail;

        @SerializedName("skip")
        public int skip;
    }

    public static class PlatformData {
        @SerializedName("name")
        public String name;

        @SerializedName("icon")
        public String icon;

        @SerializedName("color")
        public String color;

        @SerializedName("tests")
        public int tests;
    }

    public static class FeatureData {
        @SerializedName("name")
        public String name;

        @SerializedName("coverage")
        public int coverage;

        @SerializedName("tests")
        public int tests;
    }

}
