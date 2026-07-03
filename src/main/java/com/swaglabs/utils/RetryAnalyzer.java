package com.swaglabs.utils;


import java.util.concurrent.ConcurrentHashMap;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Allows failed tests to retry up to maxRetries times.
 * The DashboardReporter captures the final status after all retries.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRIES = 2;

    private static final ConcurrentHashMap<String, Integer> retryCounts =
            new ConcurrentHashMap<>();

//    // ✅ ADD THIS - See when class is loaded
//    static {
//        System.out.println("========== RetryAnalyzer CLASS LOADED ==========");
//    }

    @Override
    public boolean retry(ITestResult result) {
        // ✅ ADD THIS
        String key = result.getMethod().getMethodName();
        int currentCount = retryCounts.getOrDefault(key, 0);
        //System.out.println("========== Map after update: " + retryCounts + " ==========");

        if (currentCount < MAX_RETRIES) {
            // ✅ ONLY update the map when we are actually retrying
            retryCounts.put(key, currentCount + 1);
            //System.out.println("[Retry] " + key+ " — attempt " + (currentCount + 2) + " of " + (MAX_RETRIES + 1));
            return true;
        }

        return false;
    }

    public static int getRetryCount(ITestResult result) {
        String key = result.getMethod().getMethodName();
        int count = retryCounts.getOrDefault(key, 0);

        //System.out.println("========== getRetryCount() for " + key + " = " + count + " | Map: " + retryCounts + " ==========");

        return count;
    }

    public static void clear() {
        //System.out.println("==========Retry clear() called ==========");
        retryCounts.clear();
    }
}

