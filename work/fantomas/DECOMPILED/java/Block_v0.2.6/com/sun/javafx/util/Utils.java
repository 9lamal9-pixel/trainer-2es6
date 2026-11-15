/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.util;

import com.sun.glass.utils.NativeLibLoader;
import com.sun.javafx.FXPermissions;
import com.sun.javafx.PlatformUtil;
import com.sun.prism.impl.PrismSettings;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Utils {
    public static float clamp(float f, float f2, float f3) {
        if (f2 < f) {
            return f;
        }
        if (f2 > f3) {
            return f3;
        }
        return f2;
    }

    public static int clamp(int n, int n2, int n3) {
        if (n2 < n) {
            return n;
        }
        if (n2 > n3) {
            return n3;
        }
        return n2;
    }

    public static double clamp(double d, double d2, double d3) {
        if (d2 < d) {
            return d;
        }
        if (d2 > d3) {
            return d3;
        }
        return d2;
    }

    public static double clampMin(double d, double d2) {
        if (d < d2) {
            return d2;
        }
        return d;
    }

    public static int clampMax(int n, int n2) {
        if (n > n2) {
            return n2;
        }
        return n;
    }

    public static double nearest(double d, double d2, double d3) {
        double d4 = d2 - d;
        double d5 = d3 - d2;
        if (d4 < d5) {
            return d;
        }
        return d3;
    }

    public static String stripQuotes(String string) {
        int n;
        char c;
        if (string == null) {
            return string;
        }
        if (string.length() == 0) {
            return string;
        }
        int n2 = 0;
        char c2 = string.charAt(n2);
        if (c2 == '\"' || c2 == '\'') {
            ++n2;
        }
        if ((c = string.charAt((n = string.length()) - 1)) == '\"' || c == '\'') {
            --n;
        }
        if (n - n2 < 0) {
            return string;
        }
        return string.substring(n2, n);
    }

    public static String[] split(String string, String string2) {
        if (string == null || string.length() == 0) {
            return new String[0];
        }
        if (string2 == null || string2.length() == 0) {
            return new String[0];
        }
        if (string2.length() > string.length()) {
            return new String[0];
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        int n = string.indexOf(string2);
        while (n >= 0) {
            String string3 = string.substring(0, n);
            if (string3 != null && string3.length() > 0) {
                arrayList.add(string3);
            }
            string = string.substring(n + string2.length());
            n = string.indexOf(string2);
        }
        if (string != null && string.length() > 0) {
            arrayList.add(string);
        }
        return arrayList.toArray(new String[0]);
    }

    public static boolean contains(String string, String string2) {
        if (string == null || string.length() == 0) {
            return false;
        }
        if (string2 == null || string2.length() == 0) {
            return false;
        }
        if (string2.length() > string.length()) {
            return false;
        }
        return string.indexOf(string2) > -1;
    }

    public static double calculateBrightness(Color color) {
        return 0.3 * color.getRed() + 0.59 * color.getGreen() + 0.11 * color.getBlue();
    }

    public static Color deriveColor(Color color, double d) {
        double d2 = Utils.calculateBrightness(color);
        double d3 = d;
        if (d > 0.0) {
            if (d2 > 0.85) {
                d3 *= 1.6;
            } else if (!(d2 > 0.6)) {
                d3 = d2 > 0.5 ? (d3 *= 0.9) : (d2 > 0.4 ? (d3 *= 0.8) : (d2 > 0.3 ? (d3 *= 0.7) : (d3 *= 0.6)));
            }
        } else if (d2 < 0.2) {
            d3 *= 0.6;
        }
        if (d3 < -1.0) {
            d3 = -1.0;
        } else if (d3 > 1.0) {
            d3 = 1.0;
        }
        double[] dArray = Utils.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue());
        if (d3 > 0.0) {
            dArray[1] = dArray[1] * (1.0 - d3);
            dArray[2] = dArray[2] + (1.0 - dArray[2]) * d3;
        } else {
            dArray[2] = dArray[2] * (d3 + 1.0);
        }
        if (dArray[1] < 0.0) {
            dArray[1] = 0.0;
        } else if (dArray[1] > 1.0) {
            dArray[1] = 1.0;
        }
        if (dArray[2] < 0.0) {
            dArray[2] = 0.0;
        } else if (dArray[2] > 1.0) {
            dArray[2] = 1.0;
        }
        Color color2 = Color.hsb((int)dArray[0], dArray[1], dArray[2], color.getOpacity());
        return Color.hsb((int)dArray[0], dArray[1], dArray[2], color.getOpacity());
    }

    private static Color interpolateLinear(double d, Color color, Color color2) {
        Color color3 = Utils.convertSRGBtoLinearRGB(color);
        Color color4 = Utils.convertSRGBtoLinearRGB(color2);
        return Utils.convertLinearRGBtoSRGB(Color.color(color3.getRed() + (color4.getRed() - color3.getRed()) * d, color3.getGreen() + (color4.getGreen() - color3.getGreen()) * d, color3.getBlue() + (color4.getBlue() - color3.getBlue()) * d, color3.getOpacity() + (color4.getOpacity() - color3.getOpacity()) * d));
    }

    private static Color ladder(double d, Stop[] stopArray) {
        Stop stop = null;
        for (int i = 0; i < stopArray.length; ++i) {
            Stop stop2 = stopArray[i];
            if (d <= stop2.getOffset()) {
                if (stop == null) {
                    return stop2.getColor();
                }
                return Utils.interpolateLinear((d - stop.getOffset()) / (stop2.getOffset() - stop.getOffset()), stop.getColor(), stop2.getColor());
            }
            stop = stop2;
        }
        return stop.getColor();
    }

    public static Color ladder(Color color, Stop[] stopArray) {
        return Utils.ladder(Utils.calculateBrightness(color), stopArray);
    }

    public static double[] HSBtoRGB(double d, double d2, double d3) {
        double d4 = (d % 360.0 + 360.0) % 360.0;
        d = d4 / 360.0;
        double d5 = 0.0;
        double d6 = 0.0;
        double d7 = 0.0;
        if (d2 == 0.0) {
            d6 = d7 = d3;
            d5 = d7;
        } else {
            double d8 = (d - Math.floor(d)) * 6.0;
            double d9 = d8 - Math.floor(d8);
            double d10 = d3 * (1.0 - d2);
            double d11 = d3 * (1.0 - d2 * d9);
            double d12 = d3 * (1.0 - d2 * (1.0 - d9));
            switch ((int)d8) {
                case 0: {
                    d5 = d3;
                    d6 = d12;
                    d7 = d10;
                    break;
                }
                case 1: {
                    d5 = d11;
                    d6 = d3;
                    d7 = d10;
                    break;
                }
                case 2: {
                    d5 = d10;
                    d6 = d3;
                    d7 = d12;
                    break;
                }
                case 3: {
                    d5 = d10;
                    d6 = d11;
                    d7 = d3;
                    break;
                }
                case 4: {
                    d5 = d12;
                    d6 = d10;
                    d7 = d3;
                    break;
                }
                case 5: {
                    d5 = d3;
                    d6 = d10;
                    d7 = d11;
                }
            }
        }
        double[] dArray = new double[]{d5, d6, d7};
        return dArray;
    }

    public static double[] RGBtoHSB(double d, double d2, double d3) {
        double d4;
        double d5;
        double d6;
        double[] dArray = new double[3];
        double d7 = d6 = d > d2 ? d : d2;
        if (d3 > d6) {
            d6 = d3;
        }
        double d8 = d5 = d < d2 ? d : d2;
        if (d3 < d5) {
            d5 = d3;
        }
        double d9 = d6;
        double d10 = d6 != 0.0 ? (d6 - d5) / d6 : 0.0;
        if (d10 == 0.0) {
            d4 = 0.0;
        } else {
            double d11 = (d6 - d) / (d6 - d5);
            double d12 = (d6 - d2) / (d6 - d5);
            double d13 = (d6 - d3) / (d6 - d5);
            d4 = d == d6 ? d13 - d12 : (d2 == d6 ? 2.0 + d11 - d13 : 4.0 + d12 - d11);
            if ((d4 /= 6.0) < 0.0) {
                d4 += 1.0;
            }
        }
        dArray[0] = d4 * 360.0;
        dArray[1] = d10;
        dArray[2] = d9;
        return dArray;
    }

    public static Color convertSRGBtoLinearRGB(Color color) {
        double[] dArray = new double[]{color.getRed(), color.getGreen(), color.getBlue()};
        for (int i = 0; i < dArray.length; ++i) {
            dArray[i] = dArray[i] <= 0.04045 ? dArray[i] / 12.92 : Math.pow((dArray[i] + 0.055) / 1.055, 2.4);
        }
        return Color.color(dArray[0], dArray[1], dArray[2], color.getOpacity());
    }

    public static Color convertLinearRGBtoSRGB(Color color) {
        double[] dArray = new double[]{color.getRed(), color.getGreen(), color.getBlue()};
        for (int i = 0; i < dArray.length; ++i) {
            dArray[i] = dArray[i] <= 0.0031308 ? dArray[i] * 12.92 : 1.055 * Math.pow(dArray[i], 0.4166666666666667) - 0.055;
        }
        return Color.color(dArray[0], dArray[1], dArray[2], color.getOpacity());
    }

    public static double sum(double[] dArray) {
        double d = 0.0;
        for (double d2 : dArray) {
            d += d2;
        }
        return d / (double)dArray.length;
    }

    public static Point2D pointRelativeTo(Node node, Node node2, HPos hPos, VPos vPos, double d, double d2, boolean bl) {
        double d3 = node2.getLayoutBounds().getWidth();
        double d4 = node2.getLayoutBounds().getHeight();
        return Utils.pointRelativeTo(node, d3, d4, hPos, vPos, d, d2, bl);
    }

    public static Point2D pointRelativeTo(Node node, double d, double d2, HPos hPos, VPos vPos, double d3, double d4, boolean bl) {
        Bounds bounds = Utils.getBounds(node);
        Scene scene = node.getScene();
        NodeOrientation nodeOrientation = node.getEffectiveNodeOrientation();
        if (nodeOrientation == NodeOrientation.RIGHT_TO_LEFT) {
            if (hPos == HPos.LEFT) {
                hPos = HPos.RIGHT;
            } else if (hPos == HPos.RIGHT) {
                hPos = HPos.LEFT;
            }
            d3 *= -1.0;
        }
        double d5 = Utils.positionX(bounds, d, hPos) + d3;
        double d6 = Utils.positionY(bounds, d2, vPos) + d4;
        if (nodeOrientation == NodeOrientation.RIGHT_TO_LEFT && hPos == HPos.CENTER) {
            d5 = scene.getWindow() instanceof Stage ? d5 + bounds.getWidth() - d : d5 - bounds.getWidth() - d;
        }
        if (bl) {
            return Utils.pointRelativeTo((Object)node, d, d2, d5, d6, hPos, vPos);
        }
        return new Point2D(d5, d6);
    }

    public static Point2D pointRelativeTo(Object object, double d, double d2, double d3, double d4, HPos hPos, VPos vPos) {
        Rectangle2D rectangle2D;
        double d5 = d3;
        double d6 = d4;
        Bounds bounds = Utils.getBounds(object);
        Screen screen = Utils.getScreen(object);
        Rectangle2D rectangle2D2 = rectangle2D = Utils.hasFullScreenStage(screen) ? screen.getBounds() : screen.getVisualBounds();
        if (hPos != null) {
            if (d5 + d > rectangle2D.getMaxX()) {
                d5 = Utils.positionX(bounds, d, Utils.getHPosOpposite(hPos, vPos));
            }
            if (d5 < rectangle2D.getMinX()) {
                d5 = Utils.positionX(bounds, d, Utils.getHPosOpposite(hPos, vPos));
            }
        }
        if (vPos != null) {
            if (d6 + d2 > rectangle2D.getMaxY()) {
                d6 = Utils.positionY(bounds, d2, Utils.getVPosOpposite(hPos, vPos));
            }
            if (d6 < rectangle2D.getMinY()) {
                d6 = Utils.positionY(bounds, d2, Utils.getVPosOpposite(hPos, vPos));
            }
        }
        if (d5 + d > rectangle2D.getMaxX()) {
            d5 -= d5 + d - rectangle2D.getMaxX();
        }
        if (d5 < rectangle2D.getMinX()) {
            d5 = rectangle2D.getMinX();
        }
        if (d6 + d2 > rectangle2D.getMaxY()) {
            d6 -= d6 + d2 - rectangle2D.getMaxY();
        }
        if (d6 < rectangle2D.getMinY()) {
            d6 = rectangle2D.getMinY();
        }
        return new Point2D(d5, d6);
    }

    private static double positionX(Bounds bounds, double d, HPos hPos) {
        if (hPos == HPos.CENTER) {
            return bounds.getMinX();
        }
        if (hPos == HPos.RIGHT) {
            return bounds.getMaxX();
        }
        if (hPos == HPos.LEFT) {
            return bounds.getMinX() - d;
        }
        return 0.0;
    }

    private static double positionY(Bounds bounds, double d, VPos vPos) {
        if (vPos == VPos.BOTTOM) {
            return bounds.getMaxY();
        }
        if (vPos == VPos.CENTER) {
            return bounds.getMinY();
        }
        if (vPos == VPos.TOP) {
            return bounds.getMinY() - d;
        }
        return 0.0;
    }

    private static Bounds getBounds(Object object) {
        if (object instanceof Node) {
            Node node = (Node)object;
            Bounds bounds = node.localToScreen(node.getLayoutBounds());
            return bounds != null ? bounds : new BoundingBox(0.0, 0.0, 0.0, 0.0);
        }
        if (object instanceof Window) {
            Window window = (Window)object;
            return new BoundingBox(window.getX(), window.getY(), window.getWidth(), window.getHeight());
        }
        return new BoundingBox(0.0, 0.0, 0.0, 0.0);
    }

    private static HPos getHPosOpposite(HPos hPos, VPos vPos) {
        if (vPos == VPos.CENTER) {
            if (hPos == HPos.LEFT) {
                return HPos.RIGHT;
            }
            if (hPos == HPos.RIGHT) {
                return HPos.LEFT;
            }
            if (hPos == HPos.CENTER) {
                return HPos.CENTER;
            }
            return HPos.CENTER;
        }
        return HPos.CENTER;
    }

    private static VPos getVPosOpposite(HPos hPos, VPos vPos) {
        if (hPos == HPos.CENTER) {
            if (vPos == VPos.BASELINE) {
                return VPos.BASELINE;
            }
            if (vPos == VPos.BOTTOM) {
                return VPos.TOP;
            }
            if (vPos == VPos.CENTER) {
                return VPos.CENTER;
            }
            if (vPos == VPos.TOP) {
                return VPos.BOTTOM;
            }
            return VPos.CENTER;
        }
        return VPos.CENTER;
    }

    public static boolean hasFullScreenStage(Screen screen) {
        List list = AccessController.doPrivileged(() -> Window.getWindows(), null, FXPermissions.ACCESS_WINDOW_LIST_PERMISSION);
        for (Window window : list) {
            Stage stage;
            if (!(window instanceof Stage) || !(stage = (Stage)window).isFullScreen() || Utils.getScreen(stage) != screen) continue;
            return true;
        }
        return false;
    }

    public static boolean isQVGAScreen() {
        Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
        return rectangle2D.getWidth() == 320.0 && rectangle2D.getHeight() == 240.0 || rectangle2D.getWidth() == 240.0 && rectangle2D.getHeight() == 320.0;
    }

    public static Screen getScreen(Object object) {
        Bounds bounds = Utils.getBounds(object);
        Rectangle2D rectangle2D = new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        return Utils.getScreenForRectangle(rectangle2D);
    }

    public static Screen getScreenForRectangle(Rectangle2D rectangle2D) {
        ObservableList<Screen> observableList = Screen.getScreens();
        double d = rectangle2D.getMinX();
        double d2 = rectangle2D.getMaxX();
        double d3 = rectangle2D.getMinY();
        double d4 = rectangle2D.getMaxY();
        Screen screen = null;
        double d5 = 0.0;
        for (Screen screen2 : observableList) {
            Rectangle2D rectangle2D2 = screen2.getBounds();
            double d6 = Utils.getIntersectionLength(d, d2, rectangle2D2.getMinX(), rectangle2D2.getMaxX()) * Utils.getIntersectionLength(d3, d4, rectangle2D2.getMinY(), rectangle2D2.getMaxY());
            if (!(d5 < d6)) continue;
            d5 = d6;
            screen = screen2;
        }
        if (screen != null) {
            return screen;
        }
        screen = Screen.getPrimary();
        double d7 = Double.MAX_VALUE;
        for (Screen screen3 : observableList) {
            double d8;
            Rectangle2D rectangle2D3 = screen3.getBounds();
            double d9 = Utils.getOuterDistance(d, d2, rectangle2D3.getMinX(), rectangle2D3.getMaxX());
            double d10 = d9 * d9 + (d8 = Utils.getOuterDistance(d3, d4, rectangle2D3.getMinY(), rectangle2D3.getMaxY())) * d8;
            if (!(d7 > d10)) continue;
            d7 = d10;
            screen = screen3;
        }
        return screen;
    }

    public static Screen getScreenForPoint(double d, double d2) {
        ObservableList<Screen> observableList = Screen.getScreens();
        for (Screen screen : observableList) {
            Rectangle2D rectangle2D = screen.getBounds();
            if (!(d >= rectangle2D.getMinX()) || !(d < rectangle2D.getMaxX()) || !(d2 >= rectangle2D.getMinY()) || !(d2 < rectangle2D.getMaxY())) continue;
            return screen;
        }
        Object object = Screen.getPrimary();
        double d3 = Double.MAX_VALUE;
        for (Screen screen : observableList) {
            double d4;
            Rectangle2D rectangle2D = screen.getBounds();
            double d5 = Utils.getOuterDistance(rectangle2D.getMinX(), rectangle2D.getMaxX(), d);
            double d6 = d5 * d5 + (d4 = Utils.getOuterDistance(rectangle2D.getMinY(), rectangle2D.getMaxY(), d2)) * d4;
            if (!(d3 >= d6)) continue;
            d3 = d6;
            object = screen;
        }
        return object;
    }

    private static double getIntersectionLength(double d, double d2, double d3, double d4) {
        return d <= d3 ? Utils.getIntersectionLengthImpl(d3, d4, d2) : Utils.getIntersectionLengthImpl(d, d2, d4);
    }

    private static double getIntersectionLengthImpl(double d, double d2, double d3) {
        if (d3 <= d) {
            return 0.0;
        }
        return d3 <= d2 ? d3 - d : d2 - d;
    }

    private static double getOuterDistance(double d, double d2, double d3, double d4) {
        if (d2 <= d3) {
            return d3 - d2;
        }
        if (d4 <= d) {
            return d4 - d;
        }
        return 0.0;
    }

    private static double getOuterDistance(double d, double d2, double d3) {
        if (d3 <= d) {
            return d - d3;
        }
        if (d3 >= d2) {
            return d3 - d2;
        }
        return 0.0;
    }

    public static void forceInit(Class<?> clazz) {
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        }
        catch (ClassNotFoundException classNotFoundException) {
            throw new AssertionError((Object)classNotFoundException);
        }
    }

    public static boolean assertionEnabled() {
        boolean bl = false;
        if (!$assertionsDisabled) {
            bl = true;
            if (!true) {
                throw new AssertionError();
            }
        }
        return bl;
    }

    public static boolean isWindows() {
        return PlatformUtil.isWindows();
    }

    public static boolean isMac() {
        return PlatformUtil.isMac();
    }

    public static boolean isUnix() {
        return PlatformUtil.isUnix();
    }

    public static String convertUnicode(String string) {
        int n = -1;
        char[] cArray = string.toCharArray();
        int n2 = cArray.length;
        int n3 = -1;
        char[] cArray2 = new char[n2];
        int n4 = 0;
        while (n3 < n2 - 1) {
            char c;
            if ((c = cArray[++n3]) == '\\' && n != n3) {
                if ((c = cArray[++n3]) == 'u') {
                    while ((c = cArray[++n3]) == 'u') {
                    }
                    int n5 = n3 + 3;
                    if (n5 < n2) {
                        int n6;
                        char c2 = c;
                        int n7 = Character.digit(c2, 16);
                        if (n7 >= 0 && c2 > '\u007f') {
                            c = "0123456789abcdef".charAt(n7);
                        }
                        int n8 = n6 = n7;
                        while (n3 < n5 && n6 >= 0) {
                            char c3;
                            int n9;
                            if ((n9 = Character.digit(c3 = (c = cArray[++n3]), 16)) >= 0 && c3 > '\u007f') {
                                c = "0123456789abcdef".charAt(n9);
                            }
                            n6 = n9;
                            n8 = (n8 << 4) + n6;
                        }
                        if (n6 >= 0) {
                            c = (char)n8;
                            n = n3;
                        }
                    }
                } else {
                    --n3;
                    c = '\\';
                }
            }
            cArray2[n4++] = c;
        }
        return new String(cArray2, 0, n4);
    }

    public static synchronized void loadNativeSwingLibrary() {
        AccessController.doPrivileged(() -> {
            String string = "prism_common";
            if (PrismSettings.verbose) {
                System.out.println("Loading Prism common native library ...");
            }
            NativeLibLoader.loadLibrary(string);
            if (PrismSettings.verbose) {
                System.out.println("\tsucceeded.");
            }
            return null;
        });
    }
}

