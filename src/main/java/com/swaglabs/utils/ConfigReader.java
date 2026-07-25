package com.swaglabs.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class ConfigReader {

    private static final Properties prop = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
            prop.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    // Prevent object creation
    private ConfigReader() {
    }

    /**
     * Returns the value of the specified key from config.properties.
     *
     * @param key Property key (e.g. URL, USERNAME)
     * @return Property value
     */
    public static String get(String key) {
        return prop.getProperty(key);
    }
}