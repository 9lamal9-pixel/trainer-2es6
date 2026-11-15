/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.logging;

import com.sun.javafx.logging.Logger;
import com.sun.javafx.logging.PrintLogger;
import java.util.ArrayList;

public class PulseLogger {
    public static final boolean PULSE_LOGGING_ENABLED;
    private static final Logger[] loggers;

    public static void pulseStart() {
        for (Logger logger : loggers) {
            logger.pulseStart();
        }
    }

    public static void pulseEnd() {
        for (Logger logger : loggers) {
            logger.pulseEnd();
        }
    }

    public static void renderStart() {
        for (Logger logger : loggers) {
            logger.renderStart();
        }
    }

    public static void renderEnd() {
        for (Logger logger : loggers) {
            logger.renderEnd();
        }
    }

    public static void addMessage(String string) {
        for (Logger logger : loggers) {
            logger.addMessage(string);
        }
    }

    public static void incrementCounter(String string) {
        for (Logger logger : loggers) {
            logger.incrementCounter(string);
        }
    }

    public static void newPhase(String string) {
        for (Logger logger : loggers) {
            logger.newPhase(string);
        }
    }

    public static void newInput(String string) {
        for (Logger logger : loggers) {
            logger.newInput(string);
        }
    }

    static {
        ArrayList<Logger> arrayList = new ArrayList<Logger>();
        Logger logger = PrintLogger.getInstance();
        if (logger != null) {
            arrayList.add(logger);
        }
        PULSE_LOGGING_ENABLED = (loggers = arrayList.toArray(new Logger[arrayList.size()])).length > 0;
    }
}

