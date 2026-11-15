/*
 * Decompiled with CFR 0.152.
 */
package javafx.application;

import java.security.AccessController;
import javafx.application.Application;

public abstract class Preloader
extends Application {
    private static final String lineSeparator;

    public void handleProgressNotification(ProgressNotification progressNotification) {
    }

    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
    }

    public void handleApplicationNotification(PreloaderNotification preloaderNotification) {
    }

    public boolean handleErrorNotification(ErrorNotification errorNotification) {
        return false;
    }

    static {
        String string = AccessController.doPrivileged(() -> System.getProperty("line.separator"));
        lineSeparator = string != null ? string : "\n";
    }

    public static class StateChangeNotification
    implements PreloaderNotification {
        private final Type notificationType;
        private final Application application;

        public StateChangeNotification(Type type) {
            this.notificationType = type;
            this.application = null;
        }

        public StateChangeNotification(Type type, Application application) {
            this.notificationType = type;
            this.application = application;
        }

        public Type getType() {
            return this.notificationType;
        }

        public Application getApplication() {
            return this.application;
        }

        public static enum Type {
            BEFORE_LOAD,
            BEFORE_INIT,
            BEFORE_START;

        }
    }

    public static class ProgressNotification
    implements PreloaderNotification {
        private final double progress;
        private final String details;

        public ProgressNotification(double d) {
            this(d, "");
        }

        private ProgressNotification(double d, String string) {
            this.progress = d;
            this.details = string;
        }

        public double getProgress() {
            return this.progress;
        }

        private String getDetails() {
            return this.details;
        }
    }

    public static class ErrorNotification
    implements PreloaderNotification {
        private String location;
        private String details = "";
        private Throwable cause;

        public ErrorNotification(String string, String string2, Throwable throwable) {
            if (string2 == null) {
                throw new NullPointerException();
            }
            this.location = string;
            this.details = string2;
            this.cause = throwable;
        }

        public String getLocation() {
            return this.location;
        }

        public String getDetails() {
            return this.details;
        }

        public Throwable getCause() {
            return this.cause;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("Preloader.ErrorNotification: ");
            stringBuilder.append(this.details);
            if (this.cause != null) {
                stringBuilder.append(lineSeparator).append("Caused by: ").append(this.cause.toString());
            }
            if (this.location != null) {
                stringBuilder.append(lineSeparator).append("Location: ").append(this.location);
            }
            return stringBuilder.toString();
        }
    }

    public static interface PreloaderNotification {
    }
}

