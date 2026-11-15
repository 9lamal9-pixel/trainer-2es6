/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.SizeUnits;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;

public final class DeriveSizeConverter
extends StyleConverter<ParsedValue<Size, Size>[], Size> {
    public static DeriveSizeConverter getInstance() {
        return Holder.INSTANCE;
    }

    private DeriveSizeConverter() {
    }

    @Override
    public Size convert(ParsedValue<ParsedValue<Size, Size>[], Size> parsedValue, Font font) {
        ParsedValue<Size, Size>[] parsedValueArray = parsedValue.getValue();
        double d = parsedValueArray[0].convert(font).pixels(font);
        double d2 = parsedValueArray[1].convert(font).pixels(font);
        return new Size(d + d2, SizeUnits.PX);
    }

    public String toString() {
        return "DeriveSizeConverter";
    }

    private static class Holder {
        static final DeriveSizeConverter INSTANCE = new DeriveSizeConverter();

        private Holder() {
        }
    }
}

