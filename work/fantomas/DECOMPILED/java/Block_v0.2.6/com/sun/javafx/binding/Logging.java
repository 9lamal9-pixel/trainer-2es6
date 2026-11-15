/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.binding;

import com.sun.javafx.logging.PlatformLogger;

public class Logging {
    public static ErrorLogger getLogger() {
        return ErrorLogger.INSTANCE;
    }

    public static class ErrorLogger
    extends PlatformLogger {
        private static final ErrorLogger INSTANCE = new ErrorLogger();
        private ErrorLogRecord errorLogRecord;

        ErrorLogger() {
            super(System.getLogger("javafx.beans"));
        }

        public ErrorLogRecord getErrorLogRecord() {
            return this.errorLogRecord;
        }

        public void setErrorLogRecord(ErrorLogRecord errorLogRecord) {
            this.errorLogRecord = errorLogRecord;
        }

        @Override
        public void warning(String string, Throwable throwable) {
            super.warning(string, throwable);
            this.errorLogRecord = new ErrorLogRecord(PlatformLogger.Level.WARNING, throwable);
        }

        @Override
        public void fine(String string, Throwable throwable) {
            super.fine(string, throwable);
            this.errorLogRecord = new ErrorLogRecord(PlatformLogger.Level.FINE, throwable);
        }

        public static class ErrorLogRecord {
            private final PlatformLogger.Level level;
            private final Throwable thrown;

            public ErrorLogRecord(PlatformLogger.Level level, Throwable throwable) {
                this.level = level;
                this.thrown = throwable;
            }

            public Throwable getThrown() {
                return this.thrown;
            }

            public PlatformLogger.Level getLevel() {
                return this.level;
            }
        }
    }
}

