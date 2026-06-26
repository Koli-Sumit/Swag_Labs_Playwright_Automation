package com.swaglabs.utils;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.util.Arrays;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("\n───────────────────────────────────");
        System.out.println("▶ STARTING: " + result.getMethod().getMethodName());
        System.out.println("  Priority: " + result.getMethod().getPriority());
        System.out.println("  Groups: " + Arrays.toString(result.getMethod().getGroups()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        System.out.println("✅ PASSED: " + result.getMethod().getMethodName());
        System.out.println("  Time: " + duration + " ms");
        System.out.println("───────────────────────────────────");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        System.out.println("❌ FAILED: " + result.getMethod().getMethodName());
        System.out.println("  Time: " + duration + " ms");

        if (result.getThrowable() != null) {
            System.out.println("  Error: " + result.getThrowable().getMessage());
        }
        System.out.println("───────────────────────────────────");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("⏭️ SKIPPED: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            System.out.println("  Reason: " + result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        System.out.println("⚠️ PARTIAL: " + result.getMethod().getMethodName() +
                " (" + result.getMethod().getSuccessPercentage() + "%)");
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("\n═══════════════════════════════════");
        System.out.println("TEST STARTED: " + context.getName());
        System.out.println("═══════════════════════════════════");
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total = passed + failed + skipped;

        System.out.println("\n═══════════════════════════════════");
        System.out.println("SUMMARY: " + context.getName());
        System.out.println("  Total: " + total);
        System.out.println("  Passed: " + passed);
        System.out.println("  Failed: " + failed);
        System.out.println("  Skipped: " + skipped);
        System.out.println("═══════════════════════════════════\n");
    }
}