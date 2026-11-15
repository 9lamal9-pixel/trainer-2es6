/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;

public final class SizeConverter
extends StyleConverter<ParsedValue<?, Size>, Number> {
    public static StyleConverter<ParsedValue<?, Size>, Number> getInstance() {
        return Holder.INSTANCE;
    }

    private SizeConverter() {
    }

    @Override
    public Number convert(ParsedValue<ParsedValue<?, Size>, Number> parsedValue, Font font) {
        ParsedValue<?, Size> parsedValue2 = parsedValue.getValue();
        return parsedValue2.convert(font).pixels(font);
    }

    public String toString() {
        return "SizeConverter";
    }

    public static final class SequenceConverter
    extends StyleConverter<ParsedValue[], Number[]> {
        public static SequenceConverter getInstance() {
            return Holder.SEQUENCE_INSTANCE;
        }

        private SequenceConverter() {
        }

        @Override
        public Number[] convert(ParsedValue<ParsedValue[], Number[]> parsedValue, Font font) {
            ParsedValue[] parsedValueArray = parsedValue.getValue();
            Number[] numberArray = new Number[parsedValueArray.length];
            for (int i = 0; i < parsedValueArray.length; ++i) {
                numberArray[i] = ((Size)parsedValueArray[i].convert(font)).pixels(font);
            }
            return numberArray;
        }

        public String toString() {
            return "Size.SequenceConverter";
        }
    }

    private static class Holder {
        static final SizeConverter INSTANCE = new SizeConverter();
        static final SequenceConverter SEQUENCE_INSTANCE = new SequenceConverter();

        private Holder() {
        }
    }
}

