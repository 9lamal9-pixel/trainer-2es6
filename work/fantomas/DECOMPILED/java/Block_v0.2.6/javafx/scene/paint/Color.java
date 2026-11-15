/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.paint;

import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Utils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javafx.animation.Interpolatable;
import javafx.beans.NamedArg;
import javafx.scene.paint.Paint;

public final class Color
extends Paint
implements Interpolatable<Color> {
    private static final double DARKER_BRIGHTER_FACTOR = 0.7;
    private static final double SATURATE_DESATURATE_FACTOR = 0.7;
    private static final int PARSE_COMPONENT = 0;
    private static final int PARSE_PERCENT = 1;
    private static final int PARSE_ANGLE = 2;
    private static final int PARSE_ALPHA = 3;
    public static final Color TRANSPARENT = new Color(0.0, 0.0, 0.0, 0.0);
    public static final Color ALICEBLUE = new Color(0.9411765f, 0.972549f, 1.0f);
    public static final Color ANTIQUEWHITE = new Color(0.98039216f, 0.92156863f, 0.84313726f);
    public static final Color AQUA = new Color(0.0f, 1.0f, 1.0f);
    public static final Color AQUAMARINE = new Color(0.49803922f, 1.0f, 0.83137256f);
    public static final Color AZURE = new Color(0.9411765f, 1.0f, 1.0f);
    public static final Color BEIGE = new Color(0.9607843f, 0.9607843f, 0.8627451f);
    public static final Color BISQUE = new Color(1.0f, 0.89411765f, 0.76862746f);
    public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f);
    public static final Color BLANCHEDALMOND = new Color(1.0f, 0.92156863f, 0.8039216f);
    public static final Color BLUE = new Color(0.0f, 0.0f, 1.0f);
    public static final Color BLUEVIOLET = new Color(0.5411765f, 0.16862746f, 0.8862745f);
    public static final Color BROWN = new Color(0.64705884f, 0.16470589f, 0.16470589f);
    public static final Color BURLYWOOD = new Color(0.87058824f, 0.72156864f, 0.5294118f);
    public static final Color CADETBLUE = new Color(0.37254903f, 0.61960787f, 0.627451f);
    public static final Color CHARTREUSE = new Color(0.49803922f, 1.0f, 0.0f);
    public static final Color CHOCOLATE = new Color(0.8235294f, 0.4117647f, 0.11764706f);
    public static final Color CORAL = new Color(1.0f, 0.49803922f, 0.3137255f);
    public static final Color CORNFLOWERBLUE = new Color(0.39215687f, 0.58431375f, 0.92941177f);
    public static final Color CORNSILK = new Color(1.0f, 0.972549f, 0.8627451f);
    public static final Color CRIMSON = new Color(0.8627451f, 0.078431375f, 0.23529412f);
    public static final Color CYAN = new Color(0.0f, 1.0f, 1.0f);
    public static final Color DARKBLUE = new Color(0.0f, 0.0f, 0.54509807f);
    public static final Color DARKCYAN = new Color(0.0f, 0.54509807f, 0.54509807f);
    public static final Color DARKGOLDENROD = new Color(0.72156864f, 0.5254902f, 0.043137256f);
    public static final Color DARKGRAY = new Color(0.6627451f, 0.6627451f, 0.6627451f);
    public static final Color DARKGREEN = new Color(0.0f, 0.39215687f, 0.0f);
    public static final Color DARKGREY = DARKGRAY;
    public static final Color DARKKHAKI = new Color(0.7411765f, 0.7176471f, 0.41960785f);
    public static final Color DARKMAGENTA = new Color(0.54509807f, 0.0f, 0.54509807f);
    public static final Color DARKOLIVEGREEN = new Color(0.33333334f, 0.41960785f, 0.18431373f);
    public static final Color DARKORANGE = new Color(1.0f, 0.54901963f, 0.0f);
    public static final Color DARKORCHID = new Color(0.6f, 0.19607843f, 0.8f);
    public static final Color DARKRED = new Color(0.54509807f, 0.0f, 0.0f);
    public static final Color DARKSALMON = new Color(0.9137255f, 0.5882353f, 0.47843137f);
    public static final Color DARKSEAGREEN = new Color(0.56078434f, 0.7372549f, 0.56078434f);
    public static final Color DARKSLATEBLUE = new Color(0.28235295f, 0.23921569f, 0.54509807f);
    public static final Color DARKSLATEGRAY;
    public static final Color DARKSLATEGREY;
    public static final Color DARKTURQUOISE;
    public static final Color DARKVIOLET;
    public static final Color DEEPPINK;
    public static final Color DEEPSKYBLUE;
    public static final Color DIMGRAY;
    public static final Color DIMGREY;
    public static final Color DODGERBLUE;
    public static final Color FIREBRICK;
    public static final Color FLORALWHITE;
    public static final Color FORESTGREEN;
    public static final Color FUCHSIA;
    public static final Color GAINSBORO;
    public static final Color GHOSTWHITE;
    public static final Color GOLD;
    public static final Color GOLDENROD;
    public static final Color GRAY;
    public static final Color GREEN;
    public static final Color GREENYELLOW;
    public static final Color GREY;
    public static final Color HONEYDEW;
    public static final Color HOTPINK;
    public static final Color INDIANRED;
    public static final Color INDIGO;
    public static final Color IVORY;
    public static final Color KHAKI;
    public static final Color LAVENDER;
    public static final Color LAVENDERBLUSH;
    public static final Color LAWNGREEN;
    public static final Color LEMONCHIFFON;
    public static final Color LIGHTBLUE;
    public static final Color LIGHTCORAL;
    public static final Color LIGHTCYAN;
    public static final Color LIGHTGOLDENRODYELLOW;
    public static final Color LIGHTGRAY;
    public static final Color LIGHTGREEN;
    public static final Color LIGHTGREY;
    public static final Color LIGHTPINK;
    public static final Color LIGHTSALMON;
    public static final Color LIGHTSEAGREEN;
    public static final Color LIGHTSKYBLUE;
    public static final Color LIGHTSLATEGRAY;
    public static final Color LIGHTSLATEGREY;
    public static final Color LIGHTSTEELBLUE;
    public static final Color LIGHTYELLOW;
    public static final Color LIME;
    public static final Color LIMEGREEN;
    public static final Color LINEN;
    public static final Color MAGENTA;
    public static final Color MAROON;
    public static final Color MEDIUMAQUAMARINE;
    public static final Color MEDIUMBLUE;
    public static final Color MEDIUMORCHID;
    public static final Color MEDIUMPURPLE;
    public static final Color MEDIUMSEAGREEN;
    public static final Color MEDIUMSLATEBLUE;
    public static final Color MEDIUMSPRINGGREEN;
    public static final Color MEDIUMTURQUOISE;
    public static final Color MEDIUMVIOLETRED;
    public static final Color MIDNIGHTBLUE;
    public static final Color MINTCREAM;
    public static final Color MISTYROSE;
    public static final Color MOCCASIN;
    public static final Color NAVAJOWHITE;
    public static final Color NAVY;
    public static final Color OLDLACE;
    public static final Color OLIVE;
    public static final Color OLIVEDRAB;
    public static final Color ORANGE;
    public static final Color ORANGERED;
    public static final Color ORCHID;
    public static final Color PALEGOLDENROD;
    public static final Color PALEGREEN;
    public static final Color PALETURQUOISE;
    public static final Color PALEVIOLETRED;
    public static final Color PAPAYAWHIP;
    public static final Color PEACHPUFF;
    public static final Color PERU;
    public static final Color PINK;
    public static final Color PLUM;
    public static final Color POWDERBLUE;
    public static final Color PURPLE;
    public static final Color RED;
    public static final Color ROSYBROWN;
    public static final Color ROYALBLUE;
    public static final Color SADDLEBROWN;
    public static final Color SALMON;
    public static final Color SANDYBROWN;
    public static final Color SEAGREEN;
    public static final Color SEASHELL;
    public static final Color SIENNA;
    public static final Color SILVER;
    public static final Color SKYBLUE;
    public static final Color SLATEBLUE;
    public static final Color SLATEGRAY;
    public static final Color SLATEGREY;
    public static final Color SNOW;
    public static final Color SPRINGGREEN;
    public static final Color STEELBLUE;
    public static final Color TAN;
    public static final Color TEAL;
    public static final Color THISTLE;
    public static final Color TOMATO;
    public static final Color TURQUOISE;
    public static final Color VIOLET;
    public static final Color WHEAT;
    public static final Color WHITE;
    public static final Color WHITESMOKE;
    public static final Color YELLOW;
    public static final Color YELLOWGREEN;
    private float red;
    private float green;
    private float blue;
    private float opacity = 1.0f;
    private Object platformPaint;

    public static Color color(double d, double d2, double d3, double d4) {
        return new Color(d, d2, d3, d4);
    }

    public static Color color(double d, double d2, double d3) {
        return new Color(d, d2, d3, 1.0);
    }

    public static Color rgb(int n, int n2, int n3, double d) {
        Color.checkRGB(n, n2, n3);
        return new Color((double)n / 255.0, (double)n2 / 255.0, (double)n3 / 255.0, d);
    }

    public static Color rgb(int n, int n2, int n3) {
        Color.checkRGB(n, n2, n3);
        return new Color((double)n / 255.0, (double)n2 / 255.0, (double)n3 / 255.0, 1.0);
    }

    public static Color grayRgb(int n) {
        return Color.rgb(n, n, n);
    }

    public static Color grayRgb(int n, double d) {
        return Color.rgb(n, n, n, d);
    }

    public static Color gray(double d, double d2) {
        return new Color(d, d, d, d2);
    }

    public static Color gray(double d) {
        return Color.gray(d, 1.0);
    }

    private static void checkRGB(int n, int n2, int n3) {
        if (n < 0 || n > 255) {
            throw new IllegalArgumentException("Color.rgb's red parameter (" + n + ") expects color values 0-255");
        }
        if (n2 < 0 || n2 > 255) {
            throw new IllegalArgumentException("Color.rgb's green parameter (" + n2 + ") expects color values 0-255");
        }
        if (n3 < 0 || n3 > 255) {
            throw new IllegalArgumentException("Color.rgb's blue parameter (" + n3 + ") expects color values 0-255");
        }
    }

    public static Color hsb(double d, double d2, double d3, double d4) {
        Color.checkSB(d2, d3);
        double[] dArray = Utils.HSBtoRGB(d, d2, d3);
        Color color = new Color(dArray[0], dArray[1], dArray[2], d4);
        return color;
    }

    public static Color hsb(double d, double d2, double d3) {
        return Color.hsb(d, d2, d3, 1.0);
    }

    private static void checkSB(double d, double d2) {
        if (d < 0.0 || d > 1.0) {
            throw new IllegalArgumentException("Color.hsb's saturation parameter (" + d + ") expects values 0.0-1.0");
        }
        if (d2 < 0.0 || d2 > 1.0) {
            throw new IllegalArgumentException("Color.hsb's brightness parameter (" + d2 + ") expects values 0.0-1.0");
        }
    }

    public static Color web(String string, double d) {
        if (string == null) {
            throw new NullPointerException("The color components or name must be specified");
        }
        if (string.isEmpty()) {
            throw new IllegalArgumentException("Invalid color specification");
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        if (string2.startsWith("#")) {
            string2 = string2.substring(1);
        } else if (string2.startsWith("0x")) {
            string2 = string2.substring(2);
        } else if (string2.startsWith("rgb")) {
            if (string2.startsWith("(", 3)) {
                return Color.parseRGBColor(string2, 4, false, d);
            }
            if (string2.startsWith("a(", 3)) {
                return Color.parseRGBColor(string2, 5, true, d);
            }
        } else if (string2.startsWith("hsl")) {
            if (string2.startsWith("(", 3)) {
                return Color.parseHSLColor(string2, 4, false, d);
            }
            if (string2.startsWith("a(", 3)) {
                return Color.parseHSLColor(string2, 5, true, d);
            }
        } else {
            Color color = NamedColors.get(string2);
            if (color != null) {
                if (d == 1.0) {
                    return color;
                }
                return Color.color(color.red, color.green, color.blue, d);
            }
        }
        int n = string2.length();
        try {
            if (n == 3) {
                int n2 = Integer.parseInt(string2.substring(0, 1), 16);
                int n3 = Integer.parseInt(string2.substring(1, 2), 16);
                int n4 = Integer.parseInt(string2.substring(2, 3), 16);
                return Color.color((double)n2 / 15.0, (double)n3 / 15.0, (double)n4 / 15.0, d);
            }
            if (n == 4) {
                int n5 = Integer.parseInt(string2.substring(0, 1), 16);
                int n6 = Integer.parseInt(string2.substring(1, 2), 16);
                int n7 = Integer.parseInt(string2.substring(2, 3), 16);
                int n8 = Integer.parseInt(string2.substring(3, 4), 16);
                return Color.color((double)n5 / 15.0, (double)n6 / 15.0, (double)n7 / 15.0, d * (double)n8 / 15.0);
            }
            if (n == 6) {
                int n9 = Integer.parseInt(string2.substring(0, 2), 16);
                int n10 = Integer.parseInt(string2.substring(2, 4), 16);
                int n11 = Integer.parseInt(string2.substring(4, 6), 16);
                return Color.rgb(n9, n10, n11, d);
            }
            if (n == 8) {
                int n12 = Integer.parseInt(string2.substring(0, 2), 16);
                int n13 = Integer.parseInt(string2.substring(2, 4), 16);
                int n14 = Integer.parseInt(string2.substring(4, 6), 16);
                int n15 = Integer.parseInt(string2.substring(6, 8), 16);
                return Color.rgb(n12, n13, n14, d * (double)n15 / 255.0);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        throw new IllegalArgumentException("Invalid color specification");
    }

    private static Color parseRGBColor(String string, int n, boolean bl, double d) {
        try {
            int n2;
            int n3;
            int n4;
            int n5 = string.indexOf(44, n);
            int n6 = n4 = n5 < 0 ? -1 : string.indexOf(44, n5 + 1);
            int n7 = n4 < 0 ? -1 : (n3 = string.indexOf(bl ? 44 : 41, n4 + 1));
            int n8 = bl ? (n3 < 0 ? -1 : string.indexOf(41, n3 + 1)) : (n2 = n3);
            if (n2 >= 0) {
                double d2 = Color.parseComponent(string, n, n5, 0);
                double d3 = Color.parseComponent(string, n5 + 1, n4, 0);
                double d4 = Color.parseComponent(string, n4 + 1, n3, 0);
                if (bl) {
                    d *= Color.parseComponent(string, n3 + 1, n2, 3);
                }
                return new Color(d2, d3, d4, d);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        throw new IllegalArgumentException("Invalid color specification");
    }

    private static Color parseHSLColor(String string, int n, boolean bl, double d) {
        try {
            int n2;
            int n3;
            int n4;
            int n5 = string.indexOf(44, n);
            int n6 = n4 = n5 < 0 ? -1 : string.indexOf(44, n5 + 1);
            int n7 = n4 < 0 ? -1 : (n3 = string.indexOf(bl ? 44 : 41, n4 + 1));
            int n8 = bl ? (n3 < 0 ? -1 : string.indexOf(41, n3 + 1)) : (n2 = n3);
            if (n2 >= 0) {
                double d2 = Color.parseComponent(string, n, n5, 2);
                double d3 = Color.parseComponent(string, n5 + 1, n4, 1);
                double d4 = Color.parseComponent(string, n4 + 1, n3, 1);
                if (bl) {
                    d *= Color.parseComponent(string, n3 + 1, n2, 3);
                }
                return Color.hsb(d2, d3, d4, d);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        throw new IllegalArgumentException("Invalid color specification");
    }

    private static double parseComponent(String string, int n, int n2, int n3) {
        if ((string = string.substring(n, n2).trim()).endsWith("%")) {
            if (n3 > 1) {
                throw new IllegalArgumentException("Invalid color specification");
            }
            n3 = 1;
            string = string.substring(0, string.length() - 1).trim();
        } else if (n3 == 1) {
            throw new IllegalArgumentException("Invalid color specification");
        }
        double d = n3 == 0 ? (double)Integer.parseInt(string) : Double.parseDouble(string);
        switch (n3) {
            case 3: {
                return d < 0.0 ? 0.0 : (d > 1.0 ? 1.0 : d);
            }
            case 1: {
                return d <= 0.0 ? 0.0 : (d >= 100.0 ? 1.0 : d / 100.0);
            }
            case 0: {
                return d <= 0.0 ? 0.0 : (d >= 255.0 ? 1.0 : d / 255.0);
            }
            case 2: {
                return d < 0.0 ? d % 360.0 + 360.0 : (d > 360.0 ? d % 360.0 : d);
            }
        }
        throw new IllegalArgumentException("Invalid color specification");
    }

    public static Color web(String string) {
        return Color.web(string, 1.0);
    }

    public static Color valueOf(String string) {
        if (string == null) {
            throw new NullPointerException("color must be specified");
        }
        return Color.web(string);
    }

    private static int to32BitInteger(int n, int n2, int n3, int n4) {
        int n5 = n;
        n5 <<= 8;
        n5 |= n2;
        n5 <<= 8;
        n5 |= n3;
        n5 <<= 8;
        return n5 |= n4;
    }

    public double getHue() {
        return Utils.RGBtoHSB(this.red, this.green, this.blue)[0];
    }

    public double getSaturation() {
        return Utils.RGBtoHSB(this.red, this.green, this.blue)[1];
    }

    public double getBrightness() {
        return Utils.RGBtoHSB(this.red, this.green, this.blue)[2];
    }

    public Color deriveColor(double d, double d2, double d3, double d4) {
        double[] dArray = Utils.RGBtoHSB(this.red, this.green, this.blue);
        double d5 = dArray[2];
        if (d5 == 0.0 && d3 > 1.0) {
            d5 = 0.05;
        }
        double d6 = ((dArray[0] + d) % 360.0 + 360.0) % 360.0;
        double d7 = Math.max(Math.min(dArray[1] * d2, 1.0), 0.0);
        d5 = Math.max(Math.min(d5 * d3, 1.0), 0.0);
        double d8 = Math.max(Math.min((double)this.opacity * d4, 1.0), 0.0);
        return Color.hsb(d6, d7, d5, d8);
    }

    public Color brighter() {
        return this.deriveColor(0.0, 1.0, 1.4285714285714286, 1.0);
    }

    public Color darker() {
        return this.deriveColor(0.0, 1.0, 0.7, 1.0);
    }

    public Color saturate() {
        return this.deriveColor(0.0, 1.4285714285714286, 1.0, 1.0);
    }

    public Color desaturate() {
        return this.deriveColor(0.0, 0.7, 1.0, 1.0);
    }

    public Color grayscale() {
        double d = 0.21 * (double)this.red + 0.71 * (double)this.green + 0.07 * (double)this.blue;
        return Color.color(d, d, d, this.opacity);
    }

    public Color invert() {
        return Color.color(1.0 - (double)this.red, 1.0 - (double)this.green, 1.0 - (double)this.blue, this.opacity);
    }

    public final double getRed() {
        return this.red;
    }

    public final double getGreen() {
        return this.green;
    }

    public final double getBlue() {
        return this.blue;
    }

    public final double getOpacity() {
        return this.opacity;
    }

    @Override
    public final boolean isOpaque() {
        return this.opacity >= 1.0f;
    }

    public Color(@NamedArg(value="red") double d, @NamedArg(value="green") double d2, @NamedArg(value="blue") double d3, @NamedArg(value="opacity", defaultValue="1") double d4) {
        if (d < 0.0 || d > 1.0) {
            throw new IllegalArgumentException("Color's red value (" + d + ") must be in the range 0.0-1.0");
        }
        if (d2 < 0.0 || d2 > 1.0) {
            throw new IllegalArgumentException("Color's green value (" + d2 + ") must be in the range 0.0-1.0");
        }
        if (d3 < 0.0 || d3 > 1.0) {
            throw new IllegalArgumentException("Color's blue value (" + d3 + ") must be in the range 0.0-1.0");
        }
        if (d4 < 0.0 || d4 > 1.0) {
            throw new IllegalArgumentException("Color's opacity value (" + d4 + ") must be in the range 0.0-1.0");
        }
        this.red = (float)d;
        this.green = (float)d2;
        this.blue = (float)d3;
        this.opacity = (float)d4;
    }

    private Color(float f, float f2, float f3) {
        this.red = f;
        this.green = f2;
        this.blue = f3;
    }

    @Override
    Object acc_getPlatformPaint() {
        if (this.platformPaint == null) {
            this.platformPaint = Toolkit.getToolkit().getPaint(this);
        }
        return this.platformPaint;
    }

    @Override
    public Color interpolate(Color color, double d) {
        if (d <= 0.0) {
            return this;
        }
        if (d >= 1.0) {
            return color;
        }
        float f = (float)d;
        return new Color(this.red + (color.red - this.red) * f, this.green + (color.green - this.green) * f, this.blue + (color.blue - this.blue) * f, this.opacity + (color.opacity - this.opacity) * f);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Color) {
            Color color = (Color)object;
            return this.red == color.red && this.green == color.green && this.blue == color.blue && this.opacity == color.opacity;
        }
        return false;
    }

    public int hashCode() {
        int n = (int)Math.round((double)this.red * 255.0);
        int n2 = (int)Math.round((double)this.green * 255.0);
        int n3 = (int)Math.round((double)this.blue * 255.0);
        int n4 = (int)Math.round((double)this.opacity * 255.0);
        return Color.to32BitInteger(n, n2, n3, n4);
    }

    public String toString() {
        int n = (int)Math.round((double)this.red * 255.0);
        int n2 = (int)Math.round((double)this.green * 255.0);
        int n3 = (int)Math.round((double)this.blue * 255.0);
        int n4 = (int)Math.round((double)this.opacity * 255.0);
        return String.format("0x%02x%02x%02x%02x", n, n2, n3, n4);
    }

    static {
        DARKSLATEGREY = DARKSLATEGRAY = new Color(0.18431373f, 0.30980393f, 0.30980393f);
        DARKTURQUOISE = new Color(0.0f, 0.80784315f, 0.81960785f);
        DARKVIOLET = new Color(0.5803922f, 0.0f, 0.827451f);
        DEEPPINK = new Color(1.0f, 0.078431375f, 0.5764706f);
        DEEPSKYBLUE = new Color(0.0f, 0.7490196f, 1.0f);
        DIMGREY = DIMGRAY = new Color(0.4117647f, 0.4117647f, 0.4117647f);
        DODGERBLUE = new Color(0.11764706f, 0.5647059f, 1.0f);
        FIREBRICK = new Color(0.69803923f, 0.13333334f, 0.13333334f);
        FLORALWHITE = new Color(1.0f, 0.98039216f, 0.9411765f);
        FORESTGREEN = new Color(0.13333334f, 0.54509807f, 0.13333334f);
        FUCHSIA = new Color(1.0f, 0.0f, 1.0f);
        GAINSBORO = new Color(0.8627451f, 0.8627451f, 0.8627451f);
        GHOSTWHITE = new Color(0.972549f, 0.972549f, 1.0f);
        GOLD = new Color(1.0f, 0.84313726f, 0.0f);
        GOLDENROD = new Color(0.85490197f, 0.64705884f, 0.1254902f);
        GRAY = new Color(0.5019608f, 0.5019608f, 0.5019608f);
        GREEN = new Color(0.0f, 0.5019608f, 0.0f);
        GREENYELLOW = new Color(0.6784314f, 1.0f, 0.18431373f);
        GREY = GRAY;
        HONEYDEW = new Color(0.9411765f, 1.0f, 0.9411765f);
        HOTPINK = new Color(1.0f, 0.4117647f, 0.7058824f);
        INDIANRED = new Color(0.8039216f, 0.36078432f, 0.36078432f);
        INDIGO = new Color(0.29411766f, 0.0f, 0.50980395f);
        IVORY = new Color(1.0f, 1.0f, 0.9411765f);
        KHAKI = new Color(0.9411765f, 0.9019608f, 0.54901963f);
        LAVENDER = new Color(0.9019608f, 0.9019608f, 0.98039216f);
        LAVENDERBLUSH = new Color(1.0f, 0.9411765f, 0.9607843f);
        LAWNGREEN = new Color(0.4862745f, 0.9882353f, 0.0f);
        LEMONCHIFFON = new Color(1.0f, 0.98039216f, 0.8039216f);
        LIGHTBLUE = new Color(0.6784314f, 0.84705883f, 0.9019608f);
        LIGHTCORAL = new Color(0.9411765f, 0.5019608f, 0.5019608f);
        LIGHTCYAN = new Color(0.8784314f, 1.0f, 1.0f);
        LIGHTGOLDENRODYELLOW = new Color(0.98039216f, 0.98039216f, 0.8235294f);
        LIGHTGRAY = new Color(0.827451f, 0.827451f, 0.827451f);
        LIGHTGREEN = new Color(0.5647059f, 0.93333334f, 0.5647059f);
        LIGHTGREY = LIGHTGRAY;
        LIGHTPINK = new Color(1.0f, 0.7137255f, 0.75686276f);
        LIGHTSALMON = new Color(1.0f, 0.627451f, 0.47843137f);
        LIGHTSEAGREEN = new Color(0.1254902f, 0.69803923f, 0.6666667f);
        LIGHTSKYBLUE = new Color(0.5294118f, 0.80784315f, 0.98039216f);
        LIGHTSLATEGREY = LIGHTSLATEGRAY = new Color(0.46666667f, 0.53333336f, 0.6f);
        LIGHTSTEELBLUE = new Color(0.6901961f, 0.76862746f, 0.87058824f);
        LIGHTYELLOW = new Color(1.0f, 1.0f, 0.8784314f);
        LIME = new Color(0.0f, 1.0f, 0.0f);
        LIMEGREEN = new Color(0.19607843f, 0.8039216f, 0.19607843f);
        LINEN = new Color(0.98039216f, 0.9411765f, 0.9019608f);
        MAGENTA = new Color(1.0f, 0.0f, 1.0f);
        MAROON = new Color(0.5019608f, 0.0f, 0.0f);
        MEDIUMAQUAMARINE = new Color(0.4f, 0.8039216f, 0.6666667f);
        MEDIUMBLUE = new Color(0.0f, 0.0f, 0.8039216f);
        MEDIUMORCHID = new Color(0.7294118f, 0.33333334f, 0.827451f);
        MEDIUMPURPLE = new Color(0.5764706f, 0.4392157f, 0.85882354f);
        MEDIUMSEAGREEN = new Color(0.23529412f, 0.7019608f, 0.44313726f);
        MEDIUMSLATEBLUE = new Color(0.48235294f, 0.40784314f, 0.93333334f);
        MEDIUMSPRINGGREEN = new Color(0.0f, 0.98039216f, 0.6039216f);
        MEDIUMTURQUOISE = new Color(0.28235295f, 0.81960785f, 0.8f);
        MEDIUMVIOLETRED = new Color(0.78039217f, 0.08235294f, 0.52156866f);
        MIDNIGHTBLUE = new Color(0.09803922f, 0.09803922f, 0.4392157f);
        MINTCREAM = new Color(0.9607843f, 1.0f, 0.98039216f);
        MISTYROSE = new Color(1.0f, 0.89411765f, 0.88235295f);
        MOCCASIN = new Color(1.0f, 0.89411765f, 0.70980394f);
        NAVAJOWHITE = new Color(1.0f, 0.87058824f, 0.6784314f);
        NAVY = new Color(0.0f, 0.0f, 0.5019608f);
        OLDLACE = new Color(0.99215686f, 0.9607843f, 0.9019608f);
        OLIVE = new Color(0.5019608f, 0.5019608f, 0.0f);
        OLIVEDRAB = new Color(0.41960785f, 0.5568628f, 0.13725491f);
        ORANGE = new Color(1.0f, 0.64705884f, 0.0f);
        ORANGERED = new Color(1.0f, 0.27058825f, 0.0f);
        ORCHID = new Color(0.85490197f, 0.4392157f, 0.8392157f);
        PALEGOLDENROD = new Color(0.93333334f, 0.9098039f, 0.6666667f);
        PALEGREEN = new Color(0.59607846f, 0.9843137f, 0.59607846f);
        PALETURQUOISE = new Color(0.6862745f, 0.93333334f, 0.93333334f);
        PALEVIOLETRED = new Color(0.85882354f, 0.4392157f, 0.5764706f);
        PAPAYAWHIP = new Color(1.0f, 0.9372549f, 0.8352941f);
        PEACHPUFF = new Color(1.0f, 0.85490197f, 0.7254902f);
        PERU = new Color(0.8039216f, 0.52156866f, 0.24705882f);
        PINK = new Color(1.0f, 0.7529412f, 0.79607844f);
        PLUM = new Color(0.8666667f, 0.627451f, 0.8666667f);
        POWDERBLUE = new Color(0.6901961f, 0.8784314f, 0.9019608f);
        PURPLE = new Color(0.5019608f, 0.0f, 0.5019608f);
        RED = new Color(1.0f, 0.0f, 0.0f);
        ROSYBROWN = new Color(0.7372549f, 0.56078434f, 0.56078434f);
        ROYALBLUE = new Color(0.25490198f, 0.4117647f, 0.88235295f);
        SADDLEBROWN = new Color(0.54509807f, 0.27058825f, 0.07450981f);
        SALMON = new Color(0.98039216f, 0.5019608f, 0.44705883f);
        SANDYBROWN = new Color(0.95686275f, 0.6431373f, 0.3764706f);
        SEAGREEN = new Color(0.18039216f, 0.54509807f, 0.34117648f);
        SEASHELL = new Color(1.0f, 0.9607843f, 0.93333334f);
        SIENNA = new Color(0.627451f, 0.32156864f, 0.1764706f);
        SILVER = new Color(0.7529412f, 0.7529412f, 0.7529412f);
        SKYBLUE = new Color(0.5294118f, 0.80784315f, 0.92156863f);
        SLATEBLUE = new Color(0.41568628f, 0.3529412f, 0.8039216f);
        SLATEGREY = SLATEGRAY = new Color(0.4392157f, 0.5019608f, 0.5647059f);
        SNOW = new Color(1.0f, 0.98039216f, 0.98039216f);
        SPRINGGREEN = new Color(0.0f, 1.0f, 0.49803922f);
        STEELBLUE = new Color(0.27450982f, 0.50980395f, 0.7058824f);
        TAN = new Color(0.8235294f, 0.7058824f, 0.54901963f);
        TEAL = new Color(0.0f, 0.5019608f, 0.5019608f);
        THISTLE = new Color(0.84705883f, 0.7490196f, 0.84705883f);
        TOMATO = new Color(1.0f, 0.3882353f, 0.2784314f);
        TURQUOISE = new Color(0.2509804f, 0.8784314f, 0.8156863f);
        VIOLET = new Color(0.93333334f, 0.50980395f, 0.93333334f);
        WHEAT = new Color(0.9607843f, 0.87058824f, 0.7019608f);
        WHITE = new Color(1.0f, 1.0f, 1.0f);
        WHITESMOKE = new Color(0.9607843f, 0.9607843f, 0.9607843f);
        YELLOW = new Color(1.0f, 1.0f, 0.0f);
        YELLOWGREEN = new Color(0.6039216f, 0.8039216f, 0.19607843f);
    }

    private static final class NamedColors {
        private static final Map<String, Color> namedColors = NamedColors.createNamedColors();

        private NamedColors() {
        }

        private static Color get(String string) {
            return namedColors.get(string);
        }

        private static Map<String, Color> createNamedColors() {
            HashMap<String, Color> hashMap = new HashMap<String, Color>(256);
            hashMap.put("aliceblue", ALICEBLUE);
            hashMap.put("antiquewhite", ANTIQUEWHITE);
            hashMap.put("aqua", AQUA);
            hashMap.put("aquamarine", AQUAMARINE);
            hashMap.put("azure", AZURE);
            hashMap.put("beige", BEIGE);
            hashMap.put("bisque", BISQUE);
            hashMap.put("black", BLACK);
            hashMap.put("blanchedalmond", BLANCHEDALMOND);
            hashMap.put("blue", BLUE);
            hashMap.put("blueviolet", BLUEVIOLET);
            hashMap.put("brown", BROWN);
            hashMap.put("burlywood", BURLYWOOD);
            hashMap.put("cadetblue", CADETBLUE);
            hashMap.put("chartreuse", CHARTREUSE);
            hashMap.put("chocolate", CHOCOLATE);
            hashMap.put("coral", CORAL);
            hashMap.put("cornflowerblue", CORNFLOWERBLUE);
            hashMap.put("cornsilk", CORNSILK);
            hashMap.put("crimson", CRIMSON);
            hashMap.put("cyan", CYAN);
            hashMap.put("darkblue", DARKBLUE);
            hashMap.put("darkcyan", DARKCYAN);
            hashMap.put("darkgoldenrod", DARKGOLDENROD);
            hashMap.put("darkgray", DARKGRAY);
            hashMap.put("darkgreen", DARKGREEN);
            hashMap.put("darkgrey", DARKGREY);
            hashMap.put("darkkhaki", DARKKHAKI);
            hashMap.put("darkmagenta", DARKMAGENTA);
            hashMap.put("darkolivegreen", DARKOLIVEGREEN);
            hashMap.put("darkorange", DARKORANGE);
            hashMap.put("darkorchid", DARKORCHID);
            hashMap.put("darkred", DARKRED);
            hashMap.put("darksalmon", DARKSALMON);
            hashMap.put("darkseagreen", DARKSEAGREEN);
            hashMap.put("darkslateblue", DARKSLATEBLUE);
            hashMap.put("darkslategray", DARKSLATEGRAY);
            hashMap.put("darkslategrey", DARKSLATEGREY);
            hashMap.put("darkturquoise", DARKTURQUOISE);
            hashMap.put("darkviolet", DARKVIOLET);
            hashMap.put("deeppink", DEEPPINK);
            hashMap.put("deepskyblue", DEEPSKYBLUE);
            hashMap.put("dimgray", DIMGRAY);
            hashMap.put("dimgrey", DIMGREY);
            hashMap.put("dodgerblue", DODGERBLUE);
            hashMap.put("firebrick", FIREBRICK);
            hashMap.put("floralwhite", FLORALWHITE);
            hashMap.put("forestgreen", FORESTGREEN);
            hashMap.put("fuchsia", FUCHSIA);
            hashMap.put("gainsboro", GAINSBORO);
            hashMap.put("ghostwhite", GHOSTWHITE);
            hashMap.put("gold", GOLD);
            hashMap.put("goldenrod", GOLDENROD);
            hashMap.put("gray", GRAY);
            hashMap.put("green", GREEN);
            hashMap.put("greenyellow", GREENYELLOW);
            hashMap.put("grey", GREY);
            hashMap.put("honeydew", HONEYDEW);
            hashMap.put("hotpink", HOTPINK);
            hashMap.put("indianred", INDIANRED);
            hashMap.put("indigo", INDIGO);
            hashMap.put("ivory", IVORY);
            hashMap.put("khaki", KHAKI);
            hashMap.put("lavender", LAVENDER);
            hashMap.put("lavenderblush", LAVENDERBLUSH);
            hashMap.put("lawngreen", LAWNGREEN);
            hashMap.put("lemonchiffon", LEMONCHIFFON);
            hashMap.put("lightblue", LIGHTBLUE);
            hashMap.put("lightcoral", LIGHTCORAL);
            hashMap.put("lightcyan", LIGHTCYAN);
            hashMap.put("lightgoldenrodyellow", LIGHTGOLDENRODYELLOW);
            hashMap.put("lightgray", LIGHTGRAY);
            hashMap.put("lightgreen", LIGHTGREEN);
            hashMap.put("lightgrey", LIGHTGREY);
            hashMap.put("lightpink", LIGHTPINK);
            hashMap.put("lightsalmon", LIGHTSALMON);
            hashMap.put("lightseagreen", LIGHTSEAGREEN);
            hashMap.put("lightskyblue", LIGHTSKYBLUE);
            hashMap.put("lightslategray", LIGHTSLATEGRAY);
            hashMap.put("lightslategrey", LIGHTSLATEGREY);
            hashMap.put("lightsteelblue", LIGHTSTEELBLUE);
            hashMap.put("lightyellow", LIGHTYELLOW);
            hashMap.put("lime", LIME);
            hashMap.put("limegreen", LIMEGREEN);
            hashMap.put("linen", LINEN);
            hashMap.put("magenta", MAGENTA);
            hashMap.put("maroon", MAROON);
            hashMap.put("mediumaquamarine", MEDIUMAQUAMARINE);
            hashMap.put("mediumblue", MEDIUMBLUE);
            hashMap.put("mediumorchid", MEDIUMORCHID);
            hashMap.put("mediumpurple", MEDIUMPURPLE);
            hashMap.put("mediumseagreen", MEDIUMSEAGREEN);
            hashMap.put("mediumslateblue", MEDIUMSLATEBLUE);
            hashMap.put("mediumspringgreen", MEDIUMSPRINGGREEN);
            hashMap.put("mediumturquoise", MEDIUMTURQUOISE);
            hashMap.put("mediumvioletred", MEDIUMVIOLETRED);
            hashMap.put("midnightblue", MIDNIGHTBLUE);
            hashMap.put("mintcream", MINTCREAM);
            hashMap.put("mistyrose", MISTYROSE);
            hashMap.put("moccasin", MOCCASIN);
            hashMap.put("navajowhite", NAVAJOWHITE);
            hashMap.put("navy", NAVY);
            hashMap.put("oldlace", OLDLACE);
            hashMap.put("olive", OLIVE);
            hashMap.put("olivedrab", OLIVEDRAB);
            hashMap.put("orange", ORANGE);
            hashMap.put("orangered", ORANGERED);
            hashMap.put("orchid", ORCHID);
            hashMap.put("palegoldenrod", PALEGOLDENROD);
            hashMap.put("palegreen", PALEGREEN);
            hashMap.put("paleturquoise", PALETURQUOISE);
            hashMap.put("palevioletred", PALEVIOLETRED);
            hashMap.put("papayawhip", PAPAYAWHIP);
            hashMap.put("peachpuff", PEACHPUFF);
            hashMap.put("peru", PERU);
            hashMap.put("pink", PINK);
            hashMap.put("plum", PLUM);
            hashMap.put("powderblue", POWDERBLUE);
            hashMap.put("purple", PURPLE);
            hashMap.put("red", RED);
            hashMap.put("rosybrown", ROSYBROWN);
            hashMap.put("royalblue", ROYALBLUE);
            hashMap.put("saddlebrown", SADDLEBROWN);
            hashMap.put("salmon", SALMON);
            hashMap.put("sandybrown", SANDYBROWN);
            hashMap.put("seagreen", SEAGREEN);
            hashMap.put("seashell", SEASHELL);
            hashMap.put("sienna", SIENNA);
            hashMap.put("silver", SILVER);
            hashMap.put("skyblue", SKYBLUE);
            hashMap.put("slateblue", SLATEBLUE);
            hashMap.put("slategray", SLATEGRAY);
            hashMap.put("slategrey", SLATEGREY);
            hashMap.put("snow", SNOW);
            hashMap.put("springgreen", SPRINGGREEN);
            hashMap.put("steelblue", STEELBLUE);
            hashMap.put("tan", TAN);
            hashMap.put("teal", TEAL);
            hashMap.put("thistle", THISTLE);
            hashMap.put("tomato", TOMATO);
            hashMap.put("transparent", TRANSPARENT);
            hashMap.put("turquoise", TURQUOISE);
            hashMap.put("violet", VIOLET);
            hashMap.put("wheat", WHEAT);
            hashMap.put("white", WHITE);
            hashMap.put("whitesmoke", WHITESMOKE);
            hashMap.put("yellow", YELLOW);
            hashMap.put("yellowgreen", YELLOWGREEN);
            return hashMap;
        }
    }
}

