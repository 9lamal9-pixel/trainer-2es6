/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.CursorType;
import com.sun.javafx.cursor.StandardCursorFrame;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;

public abstract class Cursor {
    public static final Cursor DEFAULT = new StandardCursor("DEFAULT", CursorType.DEFAULT);
    public static final Cursor CROSSHAIR = new StandardCursor("CROSSHAIR", CursorType.CROSSHAIR);
    public static final Cursor TEXT = new StandardCursor("TEXT", CursorType.TEXT);
    public static final Cursor WAIT = new StandardCursor("WAIT", CursorType.WAIT);
    public static final Cursor SW_RESIZE = new StandardCursor("SW_RESIZE", CursorType.SW_RESIZE);
    public static final Cursor SE_RESIZE = new StandardCursor("SE_RESIZE", CursorType.SE_RESIZE);
    public static final Cursor NW_RESIZE = new StandardCursor("NW_RESIZE", CursorType.NW_RESIZE);
    public static final Cursor NE_RESIZE = new StandardCursor("NE_RESIZE", CursorType.NE_RESIZE);
    public static final Cursor N_RESIZE = new StandardCursor("N_RESIZE", CursorType.N_RESIZE);
    public static final Cursor S_RESIZE = new StandardCursor("S_RESIZE", CursorType.S_RESIZE);
    public static final Cursor W_RESIZE = new StandardCursor("W_RESIZE", CursorType.W_RESIZE);
    public static final Cursor E_RESIZE = new StandardCursor("E_RESIZE", CursorType.E_RESIZE);
    public static final Cursor OPEN_HAND = new StandardCursor("OPEN_HAND", CursorType.OPEN_HAND);
    public static final Cursor CLOSED_HAND = new StandardCursor("CLOSED_HAND", CursorType.CLOSED_HAND);
    public static final Cursor HAND = new StandardCursor("HAND", CursorType.HAND);
    public static final Cursor MOVE = new StandardCursor("MOVE", CursorType.MOVE);
    public static final Cursor DISAPPEAR = new StandardCursor("DISAPPEAR", CursorType.DISAPPEAR);
    public static final Cursor H_RESIZE = new StandardCursor("H_RESIZE", CursorType.H_RESIZE);
    public static final Cursor V_RESIZE = new StandardCursor("V_RESIZE", CursorType.V_RESIZE);
    public static final Cursor NONE = new StandardCursor("NONE", CursorType.NONE);
    private String name = "CUSTOM";

    Cursor() {
    }

    Cursor(String string) {
        this.name = string;
    }

    abstract CursorFrame getCurrentFrame();

    void activate() {
    }

    void deactivate() {
    }

    public String toString() {
        return this.name;
    }

    public static Cursor cursor(String string) {
        if (string == null) {
            throw new NullPointerException("The cursor identifier must not be null");
        }
        if (Cursor.isUrl(string)) {
            return new ImageCursor(new Image(string));
        }
        String string2 = string.toUpperCase(Locale.ROOT);
        if (string2.equals(Cursor.DEFAULT.name)) {
            return DEFAULT;
        }
        if (string2.equals(Cursor.CROSSHAIR.name)) {
            return CROSSHAIR;
        }
        if (string2.equals(Cursor.TEXT.name)) {
            return TEXT;
        }
        if (string2.equals(Cursor.WAIT.name)) {
            return WAIT;
        }
        if (string2.equals(Cursor.MOVE.name)) {
            return MOVE;
        }
        if (string2.equals(Cursor.SW_RESIZE.name)) {
            return SW_RESIZE;
        }
        if (string2.equals(Cursor.SE_RESIZE.name)) {
            return SE_RESIZE;
        }
        if (string2.equals(Cursor.NW_RESIZE.name)) {
            return NW_RESIZE;
        }
        if (string2.equals(Cursor.NE_RESIZE.name)) {
            return NE_RESIZE;
        }
        if (string2.equals(Cursor.N_RESIZE.name)) {
            return N_RESIZE;
        }
        if (string2.equals(Cursor.S_RESIZE.name)) {
            return S_RESIZE;
        }
        if (string2.equals(Cursor.W_RESIZE.name)) {
            return W_RESIZE;
        }
        if (string2.equals(Cursor.E_RESIZE.name)) {
            return E_RESIZE;
        }
        if (string2.equals(Cursor.OPEN_HAND.name)) {
            return OPEN_HAND;
        }
        if (string2.equals(Cursor.CLOSED_HAND.name)) {
            return CLOSED_HAND;
        }
        if (string2.equals(Cursor.HAND.name)) {
            return HAND;
        }
        if (string2.equals(Cursor.H_RESIZE.name)) {
            return H_RESIZE;
        }
        if (string2.equals(Cursor.V_RESIZE.name)) {
            return V_RESIZE;
        }
        if (string2.equals(Cursor.DISAPPEAR.name)) {
            return DISAPPEAR;
        }
        if (string2.equals(Cursor.NONE.name)) {
            return NONE;
        }
        throw new IllegalArgumentException("Invalid cursor specification");
    }

    private static boolean isUrl(String string) {
        try {
            new URL(string);
        }
        catch (MalformedURLException malformedURLException) {
            return false;
        }
        return true;
    }

    private static final class StandardCursor
    extends Cursor {
        private final CursorFrame singleFrame;

        public StandardCursor(String string, CursorType cursorType) {
            super(string);
            this.singleFrame = new StandardCursorFrame(cursorType);
        }

        @Override
        CursorFrame getCurrentFrame() {
            return this.singleFrame;
        }
    }
}

