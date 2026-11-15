/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.Cursor;
import javafx.scene.text.Font;

public final class CursorConverter
extends StyleConverter<String, Cursor> {
    public static StyleConverter<String, Cursor> getInstance() {
        return Holder.INSTANCE;
    }

    private CursorConverter() {
    }

    @Override
    public Cursor convert(ParsedValue<String, Cursor> parsedValue, Font font) {
        String string = parsedValue.getValue();
        if (string != null) {
            int n = string.indexOf("Cursor.");
            if (n > -1) {
                string = string.substring(n + "Cursor.".length());
            }
            string = string.replace('-', '_').toUpperCase();
        }
        try {
            return Cursor.cursor(string);
        }
        catch (IllegalArgumentException | NullPointerException runtimeException) {
            return Cursor.DEFAULT;
        }
    }

    public String toString() {
        return "CursorConverter";
    }

    private static class Holder {
        static final CursorConverter INSTANCE = new CursorConverter();

        private Holder() {
        }
    }
}

