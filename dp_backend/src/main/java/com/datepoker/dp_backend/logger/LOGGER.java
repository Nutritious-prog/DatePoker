package com.datepoker.dp_backend.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LOGGER {
    private static final Logger logger = LoggerFactory.getLogger("DatePokerApp");

    private LOGGER() {
        // Private constructor to prevent instantiation
    }

    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public static void error(String message, Object... args) {
        logger.error(message, args);
    }

    public static void debug(String message, Object... args) {
        logger.debug(message, args);
    }
}

