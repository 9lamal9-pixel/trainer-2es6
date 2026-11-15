/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class ColorConverter
extends StyleConverter<String, Color> {
    public static StyleConverter<String, Color> getInstance() {
        return Holder.COLOR_INSTANCE;
    }

    private ColorConverter() {
    }

    @Override
    public Color convert(ParsedValue<String, Color> parsedValue, Font font) {
        String string = parsedValue.getValue();
        if (string == null) {
            return null;
        }
        if (string instanceof Color) {
            return (Color)((Object)string);
        }
        if (string instanceof String) {
            String string2 = string;
            if (string2.isEmpty() || "null".equals(string2)) {
                return null;
            }
            try {
                return Color.web(string);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        System.err.println("not a color: " + parsedValue);
        return Color.BLACK;
    }

    public String toString() {
        return "ColorConverter";
    }

    private static class Holder {
        static final ColorConverter COLOR_INSTANCE = new ColorConverter();

        private Holder() {
        }
    }
}

