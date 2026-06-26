package com.swaglabs.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class configReader {

    private final Properties prop;

    public configReader() throws IOException {

        prop = new Properties();
        FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
        prop.load(fis);
    }

    public String getURL() {
        return prop.getProperty("URL");
    }

    public String getUsername() {
        return prop.getProperty("USERNAME");
    }

    public String getLockUesr() {

        return prop.getProperty("LOCKED_USER");
    }

    public String getPassword() {

        return prop.getProperty("PASSWORD");
    }

    public String getInvalidPassword() {

        return prop.getProperty("INVALID_PASSWORD");
    }

    public String getBrowser() {

        return prop.getProperty("BROWSER");
    }

}
