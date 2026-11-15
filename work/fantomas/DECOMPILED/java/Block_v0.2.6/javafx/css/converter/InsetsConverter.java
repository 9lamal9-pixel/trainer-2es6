/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.StyleConverter;
import javafx.geometry.Insets;
import javafx.scene.text.Font;

public final class InsetsConverter
extends StyleConverter<ParsedValue[], Insets> {
    public static StyleConverter<ParsedValue[], Insets> getInstance() {
        return Holder.INSTANCE;
    }

    private InsetsConverter() {
    }

    @Override
    public Insets convert(ParsedValue<ParsedValue[], Insets> parsedValue, Font font) {
        ParsedValue[] parsedValueArray = parsedValue.getValue();
        double d = ((Size)parsedValueArray[0].convert(font)).pixels(font);
        double d2 = parsedValueArray.length > 1 ? ((Size)parsedValueArray[1].convert(font)).pixels(font) : d;
        double d3 = parsedValueArray.length > 2 ? ((Size)parsedValueArray[2].convert(font)).pixels(font) : d;
        double d4 = parsedValueArray.length > 3 ? ((Size)parsedValueArray[3].convert(font)).pixels(font) : d2;
        return new Insets(d, d2, d3, d4);
    }

    public String toString() {
        return "InsetsConverter";
    }

    public static final class SequenceConverter
    extends StyleConverter<ParsedValue<ParsedValue[], Insets>[], Insets[]> {
        public static SequenceConverter getInstance() {
            return Holder.SEQUENCE_INSTANCE;
        }

        private SequenceConverter() {
        }

        @Override
        public Insets[] convert(ParsedValue<ParsedValue<ParsedValue[], Insets>[], Insets[]> parsedValue, Font font) {
            ParsedValue<ParsedValue[], Insets>[] parsedValueArray = parsedValue.getValue();
            Insets[] insetsArray = new Insets[parsedValueArray.length];
            for (int i = 0; i < parsedValueArray.length; ++i) {
                insetsArray[i] = InsetsConverter.getInstance().convert(parsedValueArray[i], font);
            }
            return insetsArray;
        }

        public String toString() {
            return "InsetsSequenceConverter";
        }
    }

    private static class Holder {
        static final InsetsConverter INSTANCE = new InsetsConverter();
        static final SequenceConverter SEQUENCE_INSTANCE = new SequenceConverter();

        private Holder() {
        }
    }
}

