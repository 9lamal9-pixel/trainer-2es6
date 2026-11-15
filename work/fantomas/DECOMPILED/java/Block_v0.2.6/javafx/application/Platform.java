/*
 * Decompiled with CFR 0.152.
 */
package javafx.application;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.tk.Toolkit;
import javafx.application.ConditionalFeature;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public final class Platform {
    private static ReadOnlyBooleanWrapper accessibilityActiveProperty;

    private Platform() {
    }

    public static void startup(Runnable runnable) {
        PlatformImpl.startup(runnable, true);
    }

    public static void runLater(Runnable runnable) {
        PlatformImpl.runLater(runnable);
    }

    public static void requestNextPulse() {
        Toolkit.getToolkit().requestNextPulse();
    }

    public static boolean isFxApplicationThread() {
        return PlatformImpl.isFxApplicationThread();
    }

    public static void exit() {
        PlatformImpl.exit();
    }

    public static void setImplicitExit(boolean bl) {
        PlatformImpl.setImplicitExit(bl);
    }

    public static boolean isImplicitExit() {
        return PlatformImpl.isImplicitExit();
    }

    public static boolean isSupported(ConditionalFeature conditionalFeature) {
        return PlatformImpl.isSupported(conditionalFeature);
    }

    public static Object enterNestedEventLoop(Object object) {
        return Toolkit.getToolkit().enterNestedEventLoop(object);
    }

    public static void exitNestedEventLoop(Object object, Object object2) {
        Toolkit.getToolkit().exitNestedEventLoop(object, object2);
    }

    public static boolean isNestedLoopRunning() {
        return Toolkit.getToolkit().isNestedLoopRunning();
    }

    public static boolean isAccessibilityActive() {
        return accessibilityActiveProperty == null ? false : accessibilityActiveProperty.get();
    }

    public static ReadOnlyBooleanProperty accessibilityActiveProperty() {
        if (accessibilityActiveProperty == null) {
            accessibilityActiveProperty = new ReadOnlyBooleanWrapper(Platform.class, "accessibilityActive");
            accessibilityActiveProperty.bind(PlatformImpl.accessibilityActiveProperty());
        }
        return accessibilityActiveProperty.getReadOnlyProperty();
    }
}

