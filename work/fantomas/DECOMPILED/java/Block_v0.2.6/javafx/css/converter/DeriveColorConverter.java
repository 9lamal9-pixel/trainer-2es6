/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import com.sun.javafx.util.Utils;
import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.StyleConverter;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class DeriveColorConverter
extends StyleConverter<ParsedValue[], Color> {
    public static DeriveColorConverter getInstance() {
        return Holder.INSTANCE;
    }

    private DeriveColorConverter() {
    }

    @Override
    public Color convert(ParsedValue<ParsedValue[], Color> parsedValue, Font font) {
        ParsedValue[] parsedValueArray = parsedValue.getValue();
        Color color = (Color)parsedValueArray[0].convert(font);
        Size size = (Size)parsedValueArray[1].convert(font);
        return Utils.deriveColor(color, size.pixels(font));
    }

    public String toString() {
        return "DeriveColorConverter";
    }

    private static class Holder {
        static final DeriveColorConverter INSTANCE = new DeriveColorConverter();

        private Holder() {
        }
    }
}

