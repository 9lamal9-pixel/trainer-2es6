/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.logging;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PlatformLogger
implements System.Logger {
    private static final Map<String, WeakReference<PlatformLogger>> loggers = new HashMap<String, WeakReference<PlatformLogger>>();
    private final System.Logger loggerProxy;
    private boolean loggingEnabled = true;

    private System.Logger.Level getSystemLoggerLevel(Level level) {
        switch (level) {
            case ALL: {
                return System.Logger.Level.ALL;
            }
            case FINEST: {
                return System.Logger.Level.TRACE;
            }
            case FINER: {
                return System.Logger.Level.TRACE;
            }
            case FINE: {
                return System.Logger.Level.DEBUG;
            }
            case INFO: {
                return System.Logger.Level.INFO;
            }
            case WARNING: {
                return System.Logger.Level.WARNING;
            }
            case SEVERE: {
                return System.Logger.Level.ERROR;
            }
            case OFF: {
                return System.Logger.Level.OFF;
            }
        }
        return System.Logger.Level.ALL;
    }

    public static synchronized PlatformLogger getLogger(String string) {
        PlatformLogger platformLogger = null;
        WeakReference<PlatformLogger> weakReference = loggers.get(string);
        if (weakReference != null) {
            platformLogger = (PlatformLogger)weakReference.get();
        }
        if (platformLogger == null) {
            platformLogger = new PlatformLogger(System.getLogger(string));
            loggers.put(string, new WeakReference<PlatformLogger>(platformLogger));
        }
        return platformLogger;
    }

    protected PlatformLogger(System.Logger logger) {
        this.loggerProxy = logger;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isLoggable(System.Logger.Level level) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void log(System.Logger.Level level, ResourceBundle resourceBundle, String string, Object ... objectArray) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void log(System.Logger.Level level, ResourceBundle resourceBundle, String string, Throwable throwable) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean isLoggable(Level level) {
        if (level == null) {
            throw new NullPointerException();
        }
        return this.loggerProxy.isLoggable(this.getSystemLoggerLevel(level));
    }

    public void severe(String string) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.ERROR, string, (Object[])null);
    }

    public void severe(String string, Throwable throwable) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.ERROR, string, throwable);
    }

    public void severe(String string, Object ... objectArray) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.ERROR, string, objectArray);
    }

    public void warning(String string) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.WARNING, string, (Object[])null);
    }

    public void warning(String string, Throwable throwable) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.WARNING, string, throwable);
    }

    public void warning(String string, Object ... objectArray) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.WARNING, string, objectArray);
    }

    public void info(String string) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.INFO, string, (Object[])null);
    }

    public void info(String string, Throwable throwable) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.INFO, string, throwable);
    }

    public void info(String string, Object ... objectArray) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.INFO, string, objectArray);
    }

    public void fine(String string) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.DEBUG, string, (Object[])null);
    }

    public void fine(String string, Throwable throwable) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.DEBUG, string, throwable);
    }

    public void fine(String string, Object ... objectArray) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.DEBUG, string, objectArray);
    }

    public void finer(String string) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.TRACE, string, (Object[])null);
    }

    public void finer(String string, Throwable throwable) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.TRACE, string, throwable);
    }

    public void finer(String string, Object ... objectArray) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.TRACE, string, objectArray);
    }

    public void finest(String string) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.TRACE, string, (Object[])null);
    }

    public void finest(String string, Throwable throwable) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.TRACE, string, throwable);
    }

    public void finest(String string, Object ... objectArray) {
        if (!this.loggingEnabled) {
            return;
        }
        this.loggerProxy.log(System.Logger.Level.TRACE, string, objectArray);
    }

    public void enableLogging() {
        this.loggingEnabled = true;
    }

    public void disableLogging() {
        this.loggingEnabled = false;
    }

    public static enum Level {
        ALL(System.Logger.Level.ALL),
        FINEST(System.Logger.Level.TRACE),
        FINER(System.Logger.Level.TRACE),
        FINE(System.Logger.Level.DEBUG),
        INFO(System.Logger.Level.INFO),
        WARNING(System.Logger.Level.WARNING),
        SEVERE(System.Logger.Level.ERROR),
        OFF(System.Logger.Level.OFF);

        final System.Logger.Level systemLevel;

        private Level(System.Logger.Level level) {
            this.systemLevel = level;
        }
    }
}

