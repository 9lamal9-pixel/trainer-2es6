/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import com.sun.javafx.util.Utils;
import java.util.Locale;
import java.util.Map;
import javafx.css.CssMetaData;
import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public final class FontConverter
extends StyleConverter<ParsedValue[], Font> {
    public static StyleConverter<ParsedValue[], Font> getInstance() {
        return Holder.INSTANCE;
    }

    private FontConverter() {
    }

    @Override
    public Font convert(ParsedValue<ParsedValue[], Font> parsedValue, Font font) {
        Object object;
        Object object2;
        ParsedValue[] parsedValueArray = parsedValue.getValue();
        Font font2 = font != null ? font : Font.getDefault();
        String string = parsedValueArray[0] != null ? Utils.stripQuotes((String)parsedValueArray[0].convert(font2)) : font2.getFamily();
        double d = font2.getSize();
        if (parsedValueArray[1] != null) {
            object2 = (ParsedValue)parsedValueArray[1].getValue();
            object = (Size)((ParsedValue)object2).convert(font2);
            d = object.pixels(font2.getSize(), font2);
        }
        object2 = parsedValueArray[2] != null ? (FontWeight)((Object)parsedValueArray[2].convert(font2)) : FontWeight.NORMAL;
        object = parsedValueArray[3] != null ? (FontPosture)((Object)parsedValueArray[3].convert(font2)) : FontPosture.REGULAR;
        Font font3 = Font.font(string, (FontWeight)((Object)object2), (FontPosture)((Object)object), d);
        return font3;
    }

    @Override
    public Font convert(Map<CssMetaData<? extends Styleable, ?>, Object> map) {
        Font font = Font.getDefault();
        double d = font.getSize();
        String string = font.getFamily();
        FontWeight fontWeight = FontWeight.NORMAL;
        FontPosture fontPosture = FontPosture.REGULAR;
        for (Map.Entry<CssMetaData<Styleable, ?>, Object> entry : map.entrySet()) {
            Object object = entry.getValue();
            if (object == null) continue;
            String string2 = entry.getKey().getProperty();
            if (string2.endsWith("font-size")) {
                d = ((Number)object).doubleValue();
                continue;
            }
            if (string2.endsWith("font-family")) {
                string = Utils.stripQuotes((String)object);
                continue;
            }
            if (string2.endsWith("font-weight")) {
                fontWeight = (FontWeight)((Object)object);
                continue;
            }
            if (!string2.endsWith("font-style")) continue;
            fontPosture = (FontPosture)((Object)object);
        }
        Font font2 = Font.font(string, fontWeight, fontPosture, d);
        return font2;
    }

    public String toString() {
        return "FontConverter";
    }

    public static final class FontSizeConverter
    extends StyleConverter<ParsedValue<?, Size>, Number> {
        public static FontSizeConverter getInstance() {
            return Holder.INSTANCE;
        }

        private FontSizeConverter() {
        }

        @Override
        public Number convert(ParsedValue<ParsedValue<?, Size>, Number> parsedValue, Font font) {
            ParsedValue<?, Size> parsedValue2 = parsedValue.getValue();
            return parsedValue2.convert(font).pixels(font.getSize(), font);
        }

        public String toString() {
            return "FontConverter.FontSizeConverter";
        }

        private static class Holder {
            static final FontSizeConverter INSTANCE = new FontSizeConverter();

            private Holder() {
            }
        }
    }

    public static final class FontWeightConverter
    extends StyleConverter<String, FontWeight> {
        public static FontWeightConverter getInstance() {
            return Holder.INSTANCE;
        }

        private FontWeightConverter() {
        }

        @Override
        public FontWeight convert(ParsedValue<String, FontWeight> parsedValue, Font font) {
            String string = parsedValue.getValue();
            FontWeight fontWeight = null;
            if (string instanceof String) {
                try {
                    String string2 = string.toUpperCase(Locale.ROOT);
                    fontWeight = Enum.valueOf(FontWeight.class, string2);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    fontWeight = FontWeight.NORMAL;
                }
                catch (NullPointerException nullPointerException) {
                    fontWeight = FontWeight.NORMAL;
                }
            } else if (string instanceof FontWeight) {
                fontWeight = (FontWeight)((Object)string);
            }
            return fontWeight;
        }

        public String toString() {
            return "FontConverter.WeightConverter";
        }

        private static class Holder {
            static final FontWeightConverter INSTANCE = new FontWeightConverter();

            private Holder() {
            }
        }
    }

    public static final class FontStyleConverter
    extends StyleConverter<String, FontPosture> {
        public static FontStyleConverter getInstance() {
            return Holder.INSTANCE;
        }

        private FontStyleConverter() {
        }

        @Override
        public FontPosture convert(ParsedValue<String, FontPosture> parsedValue, Font font) {
            String string = parsedValue.getValue();
            FontPosture fontPosture = null;
            if (string instanceof String) {
                try {
                    String string2 = string.toUpperCase(Locale.ROOT);
                    fontPosture = Enum.valueOf(FontPosture.class, string2);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    fontPosture = FontPosture.REGULAR;
                }
                catch (NullPointerException nullPointerException) {
                    fontPosture = FontPosture.REGULAR;
                }
            } else if (string instanceof FontPosture) {
                fontPosture = (FontPosture)((Object)string);
            }
            return fontPosture;
        }

        public String toString() {
            return "FontConverter.StyleConverter";
        }

        private static class Holder {
            static final FontStyleConverter INSTANCE = new FontStyleConverter();

            private Holder() {
            }
        }
    }

    private static class Holder {
        static final FontConverter INSTANCE = new FontConverter();

        private Holder() {
        }
    }
}

