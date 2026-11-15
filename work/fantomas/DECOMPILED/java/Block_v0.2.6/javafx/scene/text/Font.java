/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.text;

import com.sun.javafx.scene.text.FontHelper;
import com.sun.javafx.tk.Toolkit;
import java.io.File;
import java.io.FilePermission;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public final class Font {
    private static final String DEFAULT_FAMILY = "System";
    private static final String DEFAULT_FULLNAME = "System Regular";
    private static float defaultSystemFontSize;
    private static Font DEFAULT;
    private String name;
    private String family;
    private String style;
    private double size;
    private int hash = 0;
    private Object nativeFont;

    private static float getDefaultSystemFontSize() {
        if (defaultSystemFontSize == -1.0f) {
            defaultSystemFontSize = Toolkit.getToolkit().getFontLoader().getSystemFontSize();
        }
        return defaultSystemFontSize;
    }

    public static synchronized Font getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Font(DEFAULT_FULLNAME, Font.getDefaultSystemFontSize());
        }
        return DEFAULT;
    }

    public static List<String> getFamilies() {
        return Toolkit.getToolkit().getFontLoader().getFamilies();
    }

    public static List<String> getFontNames() {
        return Toolkit.getToolkit().getFontLoader().getFontNames();
    }

    public static List<String> getFontNames(String string) {
        return Toolkit.getToolkit().getFontLoader().getFontNames(string);
    }

    public static Font font(String string, FontWeight fontWeight, FontPosture fontPosture, double d) {
        String string2 = string == null || "".equals(string) ? DEFAULT_FAMILY : string;
        double d2 = d < 0.0 ? (double)Font.getDefaultSystemFontSize() : d;
        return Toolkit.getToolkit().getFontLoader().font(string2, fontWeight, fontPosture, (float)d2);
    }

    public static Font font(String string, FontWeight fontWeight, double d) {
        return Font.font(string, fontWeight, null, d);
    }

    public static Font font(String string, FontPosture fontPosture, double d) {
        return Font.font(string, null, fontPosture, d);
    }

    public static Font font(String string, double d) {
        return Font.font(string, null, null, d);
    }

    public static Font font(String string) {
        return Font.font(string, null, null, -1.0);
    }

    public static Font font(double d) {
        return Font.font(null, null, null, d);
    }

    public final String getName() {
        return this.name;
    }

    public final String getFamily() {
        return this.family;
    }

    public final String getStyle() {
        return this.style;
    }

    public final double getSize() {
        return this.size;
    }

    public Font(@NamedArg(value="size") double d) {
        this(null, d);
    }

    public Font(@NamedArg(value="name") String string, @NamedArg(value="size") double d) {
        this.name = string;
        this.size = d;
        if (string == null || "".equals(string)) {
            this.name = DEFAULT_FULLNAME;
        }
        if (d < 0.0) {
            this.size = Font.getDefaultSystemFontSize();
        }
        Toolkit.getToolkit().getFontLoader().loadFont(this);
    }

    private Font(Object object, String string, String string2, String string3, double d) {
        this.nativeFont = object;
        this.family = string;
        this.name = string2;
        this.style = string3;
        this.size = d;
    }

    public static Font loadFont(String string, double d) {
        Font[] fontArray = Font.loadFontInternal(string, d, false);
        return fontArray == null ? null : fontArray[0];
    }

    public static Font[] loadFonts(String string, double d) {
        return Font.loadFontInternal(string, d, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Font[] loadFontInternal(String string, double d, boolean bl) {
        URL uRL = null;
        try {
            uRL = new URL(string);
        }
        catch (Exception exception) {
            return null;
        }
        if (d <= 0.0) {
            d = Font.getDefaultSystemFontSize();
        }
        if (uRL.getProtocol().equals("file")) {
            String string2 = uRL.getFile();
            string2 = new File(string2).getPath();
            try {
                SecurityManager securityManager = System.getSecurityManager();
                if (securityManager != null) {
                    FilePermission filePermission = new FilePermission(string2, "read");
                    securityManager.checkPermission(filePermission);
                }
            }
            catch (Exception exception) {
                return null;
            }
            return Toolkit.getToolkit().getFontLoader().loadFont(string2, d, bl);
        }
        Font[] fontArray = null;
        URLConnection uRLConnection = null;
        InputStream inputStream = null;
        try {
            uRLConnection = uRL.openConnection();
            inputStream = uRLConnection.getInputStream();
            fontArray = Toolkit.getToolkit().getFontLoader().loadFont(inputStream, d, bl);
        }
        catch (Exception exception) {
            Font[] fontArray2 = null;
            return fontArray2;
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (Exception exception) {}
        }
        return fontArray;
    }

    public static Font loadFont(InputStream inputStream, double d) {
        Font[] fontArray;
        if (d <= 0.0) {
            d = Font.getDefaultSystemFontSize();
        }
        return (fontArray = Toolkit.getToolkit().getFontLoader().loadFont(inputStream, d, false)) == null ? null : fontArray[0];
    }

    public static Font[] loadFonts(InputStream inputStream, double d) {
        Font[] fontArray;
        if (d <= 0.0) {
            d = Font.getDefaultSystemFontSize();
        }
        return (fontArray = Toolkit.getToolkit().getFontLoader().loadFont(inputStream, d, true)) == null ? null : fontArray;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Font[name=");
        stringBuilder = stringBuilder.append(this.name);
        stringBuilder = stringBuilder.append(", family=").append(this.family);
        stringBuilder = stringBuilder.append(", style=").append(this.style);
        stringBuilder = stringBuilder.append(", size=").append(this.size);
        stringBuilder = stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Font) {
            Font font = (Font)object;
            return (this.name == null ? font.name == null : this.name.equals(font.name)) && this.size == font.size;
        }
        return false;
    }

    public int hashCode() {
        if (this.hash == 0) {
            long l = 17L;
            l = 37L * l + (long)this.name.hashCode();
            l = 37L * l + Double.doubleToLongBits(this.size);
            this.hash = (int)(l ^ l >> 32);
        }
        return this.hash;
    }

    Object getNativeFont() {
        return this.nativeFont;
    }

    void setNativeFont(Object object, String string, String string2, String string3) {
        this.nativeFont = object;
        this.name = string;
        this.family = string2;
        this.style = string3;
    }

    static Font nativeFont(Object object, String string, String string2, String string3, double d) {
        Font font = new Font(object, string2, string, string3, d);
        return font;
    }

    static {
        FontHelper.setFontAccessor(new FontHelper.FontAccessor(){

            @Override
            public Object getNativeFont(Font font) {
                return font.getNativeFont();
            }

            @Override
            public void setNativeFont(Font font, Object object, String string, String string2, String string3) {
                font.setNativeFont(object, string, string2, string3);
            }

            @Override
            public Font nativeFont(Object object, String string, String string2, String string3, double d) {
                return Font.nativeFont(object, string, string2, string3, d);
            }
        });
        defaultSystemFontSize = -1.0f;
    }
}

