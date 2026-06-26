package com.swaglabs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

    private Log() {
        // Prevent object creation
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
