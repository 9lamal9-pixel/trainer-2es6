/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import com.sun.javafx.util.Utils;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;

public final class StringConverter
extends StyleConverter<String, String> {
    public static StyleConverter<String, String> getInstance() {
        return Holder.INSTANCE;
    }

    private StringConverter() {
    }

    @Override
    public String convert(ParsedValue<String, String> parsedValue, Font font) {
        String string = parsedValue.getValue();
        if (string == null) {
            return null;
        }
        return Utils.convertUnicode(string);
    }

    public String toString() {
        return "StringConverter";
    }

    public static final class SequenceConverter
    extends StyleConverter<ParsedValue<String, String>[], String[]> {
        public static SequenceConverter getInstance() {
            return Holder.SEQUENCE_INSTANCE;
        }

        private SequenceConverter() {
        }

        @Override
        public String[] convert(ParsedValue<ParsedValue<String, String>[], String[]> parsedValue, Font font) {
            ParsedValue<String, String>[] parsedValueArray = parsedValue.getValue();
            String[] stringArray = new String[parsedValueArray.length];
            for (int i = 0; i < parsedValueArray.length; ++i) {
                stringArray[i] = StringConverter.getInstance().convert(parsedValueArray[i], font);
            }
            return stringArray;
        }

        public String toString() {
            return "String.SequenceConverter";
        }
    }

    private static class Holder {
        static final StringConverter INSTANCE = new StringConverter();
        static final SequenceConverter SEQUENCE_INSTANCE = new SequenceConverter();

        private Holder() {
        }
    }
}

