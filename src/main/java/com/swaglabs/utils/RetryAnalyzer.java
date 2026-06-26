package com.swaglabs.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    // Set the maximum number of retries you want
    private static final int maxRetryCount = 2;

    @Override
    public boolean retry(ITestResult result) {
        // Check if the test failed and hasn't reached the max retry limit
        if (retryCount < maxRetryCount) {
            retryCount++;
            System.out.println("Retrying test " + result.getName() + " for the " + retryCount + " time.");
            return true; // Tells TestNG to retry the test
        }
        return false; // Tells TestNG to stop retrying
    }
}
