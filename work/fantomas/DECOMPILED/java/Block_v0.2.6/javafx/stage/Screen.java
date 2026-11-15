/*
 * Decompiled with CFR 0.152.
 */
package javafx.stage;

import com.sun.javafx.tk.ScreenConfigurationAccessor;
import com.sun.javafx.tk.Toolkit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;

public final class Screen {
    private static final AtomicBoolean configurationDirty = new AtomicBoolean(true);
    private static final ScreenConfigurationAccessor accessor;
    private static Screen primary;
    private static final ObservableList<Screen> screens;
    private static final ObservableList<Screen> unmodifiableScreens;
    private Rectangle2D bounds = Rectangle2D.EMPTY;
    private Rectangle2D visualBounds = Rectangle2D.EMPTY;
    private double dpi;
    private float outputScaleX;
    private float outputScaleY;

    private Screen() {
    }

    private static void checkDirty() {
        if (configurationDirty.compareAndSet(true, false)) {
            Screen.updateConfiguration();
        }
    }

    private static void updateConfiguration() {
        Object object = Toolkit.getToolkit().getPrimaryScreen();
        Screen screen = Screen.nativeToScreen(object, primary);
        if (screen != null) {
            primary = screen;
        }
        List<?> list = Toolkit.getToolkit().getScreens();
        ObservableList observableList = FXCollections.observableArrayList();
        boolean bl = screens.size() == list.size();
        for (int i = 0; i < list.size(); ++i) {
            Screen screen2;
            Object obj = list.get(i);
            Screen screen3 = null;
            if (bl) {
                screen3 = (Screen)screens.get(i);
            }
            if ((screen2 = Screen.nativeToScreen(obj, screen3)) == null) continue;
            if (bl) {
                bl = false;
                observableList.clear();
                observableList.addAll(screens.subList(0, i));
            }
            observableList.add(screen2);
        }
        if (!bl) {
            screens.clear();
            screens.addAll(observableList);
        }
        configurationDirty.set(false);
    }

    private static Screen nativeToScreen(Object object, Screen screen) {
        int n = accessor.getMinX(object);
        int n2 = accessor.getMinY(object);
        int n3 = accessor.getWidth(object);
        int n4 = accessor.getHeight(object);
        int n5 = accessor.getVisualMinX(object);
        int n6 = accessor.getVisualMinY(object);
        int n7 = accessor.getVisualWidth(object);
        int n8 = accessor.getVisualHeight(object);
        double d = accessor.getDPI(object);
        float f = accessor.getRecommendedOutputScaleX(object);
        float f2 = accessor.getRecommendedOutputScaleY(object);
        if (screen == null || screen.bounds.getMinX() != (double)n || screen.bounds.getMinY() != (double)n2 || screen.bounds.getWidth() != (double)n3 || screen.bounds.getHeight() != (double)n4 || screen.visualBounds.getMinX() != (double)n5 || screen.visualBounds.getMinY() != (double)n6 || screen.visualBounds.getWidth() != (double)n7 || screen.visualBounds.getHeight() != (double)n8 || screen.dpi != d || screen.outputScaleX != f || screen.outputScaleY != f2) {
            Screen screen2 = new Screen();
            screen2.bounds = new Rectangle2D(n, n2, n3, n4);
            screen2.visualBounds = new Rectangle2D(n5, n6, n7, n8);
            screen2.dpi = d;
            screen2.outputScaleX = f;
            screen2.outputScaleY = f2;
            return screen2;
        }
        return null;
    }

    static Screen getScreenForNative(Object object) {
        double d = accessor.getMinX(object);
        double d2 = accessor.getMinY(object);
        double d3 = accessor.getWidth(object);
        double d4 = accessor.getHeight(object);
        Screen screen = null;
        for (int i = 0; i < screens.size(); ++i) {
            Screen screen2 = (Screen)screens.get(i);
            if (screen2.bounds.contains(d, d2, d3, d4)) {
                return screen2;
            }
            if (screen != null || !screen2.bounds.intersects(d, d2, d3, d4)) continue;
            screen = screen2;
        }
        return screen == null ? Screen.getPrimary() : screen;
    }

    public static Screen getPrimary() {
        Screen.checkDirty();
        return primary;
    }

    public static ObservableList<Screen> getScreens() {
        Screen.checkDirty();
        return unmodifiableScreens;
    }

    public static ObservableList<Screen> getScreensForRectangle(double d, double d2, double d3, double d4) {
        Screen.checkDirty();
        ObservableList<Screen> observableList = FXCollections.observableArrayList();
        for (Screen screen : screens) {
            if (!screen.bounds.intersects(d, d2, d3, d4)) continue;
            observableList.add(screen);
        }
        return observableList;
    }

    public static ObservableList<Screen> getScreensForRectangle(Rectangle2D rectangle2D) {
        Screen.checkDirty();
        return Screen.getScreensForRectangle(rectangle2D.getMinX(), rectangle2D.getMinY(), rectangle2D.getWidth(), rectangle2D.getHeight());
    }

    public final Rectangle2D getBounds() {
        return this.bounds;
    }

    public final Rectangle2D getVisualBounds() {
        return this.visualBounds;
    }

    public final double getDpi() {
        return this.dpi;
    }

    public final double getOutputScaleX() {
        return this.outputScaleX;
    }

    public final double getOutputScaleY() {
        return this.outputScaleY;
    }

    public int hashCode() {
        long l = 7L;
        l = 37L * l + (long)this.bounds.hashCode();
        l = 37L * l + (long)this.visualBounds.hashCode();
        l = 37L * l + Double.doubleToLongBits(this.dpi);
        l = 37L * l + (long)Float.floatToIntBits(this.outputScaleX);
        l = 37L * l + (long)Float.floatToIntBits(this.outputScaleY);
        return (int)(l ^ l >> 32);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Screen) {
            Screen screen = (Screen)object;
            return (this.bounds == null ? screen.bounds == null : this.bounds.equals(screen.bounds)) && (this.visualBounds == null ? screen.visualBounds == null : this.visualBounds.equals(screen.visualBounds)) && screen.dpi == this.dpi && screen.outputScaleX == this.outputScaleX && screen.outputScaleY == this.outputScaleY;
        }
        return false;
    }

    public String toString() {
        return super.toString() + " bounds:" + this.bounds + " visualBounds:" + this.visualBounds + " dpi:" + this.dpi + " outputScale:(" + this.outputScaleX + "," + this.outputScaleY + ")";
    }

    static {
        screens = FXCollections.observableArrayList();
        unmodifiableScreens = FXCollections.unmodifiableObservableList(screens);
        accessor = Toolkit.getToolkit().setScreenConfigurationListener(() -> Screen.updateConfiguration());
    }
}

