package com.swaglabs.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores trend data in a simple JSON file so the chart shows history
 * across multiple test runs, not just the current build.
 * <p>
 * File format (trend-history.json):
 * [
 * { "build": "#100", "passed": 120, "failed": 5, "skipped": 2, "duration": 89 },
 * { "build": "#101", "passed": 125, "failed": 3, "skipped": 1, "duration": 82 },
 * ...
 * ]
 */
public class TrendHistory {

    private static final int MAX_HISTORY = 14; // Keep last 14 builds
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class TrendPoint {
        public String build;
        public int passed;
        public int failed;
        public int skipped;
        public int duration;
    }

    /**
     * Append current run results to history and return all data
     * needed to populate the dashboard's trend arrays.
     */
    public static TrendSnapshot recordAndSnapshot(
            String buildLabel,
            int passed, int failed, int skipped,
            int durationSeconds,
            Path historyFile
    ) {
        List<TrendPoint> history = loadHistory(historyFile);

        // Append current run
        TrendPoint current = new TrendPoint();
        current.build = buildLabel;
        current.passed = passed;
        current.failed = failed;
        current.skipped = skipped;
        current.duration = durationSeconds;
        history.add(current);

        // Trim to max
        if (history.size() > MAX_HISTORY) {
            history = history.subList(history.size() - MAX_HISTORY, history.size());
        }

        // Save
        saveHistory(history, historyFile);

        // Build snapshot
        TrendSnapshot snap = new TrendSnapshot();
        for (TrendPoint p : history) {
            snap.builds.add(p.build);
            snap.passed.add(p.passed);
            snap.failed.add(p.failed);
            snap.skipped.add(p.skipped);
            snap.durations.add(p.duration);
        }

        // Compute deltas (current vs previous)
        if (history.size() >= 2) {
            TrendPoint prev = history.get(history.size() - 2);
            snap.passedDelta = safeDelta(current.passed, prev.passed);
            snap.failedDelta = safeDelta(current.failed, prev.failed);
            snap.skippedDelta = safeDelta(current.skipped, prev.skipped);
            snap.durationDelta = safeDelta(current.duration, prev.duration);
        } else {
            snap.passedDelta = 0;
            snap.failedDelta = 0;
            snap.skippedDelta = 0;
            snap.durationDelta = 0;
        }

        return snap;
    }

    /**
     * Percentage change: ((current - previous) / previous) * 100
     * Returns 0 if previous was 0 (avoid division by zero).
     */
    private static double safeDelta(int current, int previous) {
        if (previous == 0) return 0;
        return ((double) (current - previous) / previous) * 100.0;
    }

    public static class TrendSnapshot {
        public List<String> builds = new ArrayList<>();
        public List<Integer> passed = new ArrayList<>();
        public List<Integer> failed = new ArrayList<>();
        public List<Integer> skipped = new ArrayList<>();
        public List<Integer> durations = new ArrayList<>();
        public double passedDelta;
        public double failedDelta;
        public double skippedDelta;
        public double durationDelta;
    }

    private static List<TrendPoint> loadHistory(Path file) {
        if (!Files.exists(file)) return new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file)) {
            Type type = new TypeToken<List<TrendPoint>>() {
            }.getType();
            List<TrendPoint> list = GSON.fromJson(reader, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[TrendHistory] Failed to load history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void saveHistory(List<TrendPoint> history, Path file) {
        try {
            file.getParent().toFile().mkdirs();
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(history, writer);
            }
        } catch (Exception e) {
            System.err.println("[TrendHistory] Failed to save history: " + e.getMessage());
        }
    }
}
