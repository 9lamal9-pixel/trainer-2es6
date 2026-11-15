/*
 * Decompiled with CFR 0.152.
 */
package javafx.application;

import com.sun.javafx.application.LauncherImpl;
import com.sun.javafx.application.ParametersImpl;
import com.sun.javafx.application.PlatformImpl;
import java.util.List;
import java.util.Map;
import javafx.application.HostServices;
import javafx.application.Preloader;
import javafx.stage.Stage;

public abstract class Application {
    public static final String STYLESHEET_CASPIAN = "CASPIAN";
    public static final String STYLESHEET_MODENA = "MODENA";
    private HostServices hostServices = null;
    private static String userAgentStylesheet = null;

    public static void launch(Class<? extends Application> clazz, String ... stringArray) {
        LauncherImpl.launchApplication(clazz, stringArray);
    }

    public static void launch(String ... stringArray) {
        StackTraceElement[] stackTraceElementArray = Thread.currentThread().getStackTrace();
        boolean bl = false;
        String string = null;
        for (StackTraceElement stackTraceElement : stackTraceElementArray) {
            String string2 = stackTraceElement.getClassName();
            String string3 = stackTraceElement.getMethodName();
            if (bl) {
                string = string2;
                break;
            }
            if (!Application.class.getName().equals(string2) || !"launch".equals(string3)) continue;
            bl = true;
        }
        if (string == null) {
            throw new RuntimeException("Error: unable to determine Application class");
        }
        try {
            Class<?> runtimeException = Class.forName(string, false, Thread.currentThread().getContextClassLoader());
            if (!Application.class.isAssignableFrom(runtimeException)) {
                throw new RuntimeException("Error: " + runtimeException + " is not a subclass of javafx.application.Application");
            }
            Class<?> clazz = runtimeException;
            LauncherImpl.launchApplication(clazz, stringArray);
        }
        catch (RuntimeException exception) {
            throw exception;
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void init() throws Exception {
    }

    public abstract void start(Stage var1) throws Exception;

    public void stop() throws Exception {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final HostServices getHostServices() {
        Application application = this;
        synchronized (application) {
            if (this.hostServices == null) {
                this.hostServices = new HostServices(this);
            }
            return this.hostServices;
        }
    }

    public final Parameters getParameters() {
        return ParametersImpl.getParameters(this);
    }

    public final void notifyPreloader(Preloader.PreloaderNotification preloaderNotification) {
        LauncherImpl.notifyPreloader(this, preloaderNotification);
    }

    public static String getUserAgentStylesheet() {
        return userAgentStylesheet;
    }

    public static void setUserAgentStylesheet(String string) {
        userAgentStylesheet = string;
        if (string == null) {
            PlatformImpl.setDefaultPlatformUserAgentStylesheet();
        } else {
            PlatformImpl.setPlatformUserAgentStylesheet(string);
        }
    }

    public static abstract class Parameters {
        public abstract List<String> getRaw();

        public abstract List<String> getUnnamed();

        public abstract Map<String, String> getNamed();
    }
}

