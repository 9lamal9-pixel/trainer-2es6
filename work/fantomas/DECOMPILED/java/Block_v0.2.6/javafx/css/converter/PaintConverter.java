/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import com.sun.javafx.css.StyleManager;
import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.SizeUnits;
import javafx.css.StyleConverter;
import javafx.scene.image.Image;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

public final class PaintConverter
extends StyleConverter<ParsedValue<?, Paint>, Paint> {
    public static StyleConverter<ParsedValue<?, Paint>, Paint> getInstance() {
        return Holder.INSTANCE;
    }

    private PaintConverter() {
    }

    @Override
    public Paint convert(ParsedValue<ParsedValue<?, Paint>, Paint> parsedValue, Font font) {
        ParsedValue<?, Paint> parsedValue2 = parsedValue.getValue();
        if (parsedValue2 instanceof Paint) {
            return (Paint)((Object)parsedValue2);
        }
        return parsedValue.getValue().convert(font);
    }

    public String toString() {
        return "PaintConverter";
    }

    public static final class RadialGradientConverter
    extends StyleConverter<ParsedValue[], Paint> {
        public static RadialGradientConverter getInstance() {
            return Holder.RADIAL_GRADIENT_INSTANCE;
        }

        private RadialGradientConverter() {
        }

        @Override
        public Paint convert(ParsedValue<ParsedValue[], Paint> parsedValue, Font font) {
            boolean bl;
            Paint paint = (Paint)super.getCachedValue(parsedValue);
            if (paint != null) {
                return paint;
            }
            ParsedValue[] parsedValueArray = parsedValue.getValue();
            int n = 0;
            Size size = parsedValueArray[n++] != null ? (Size)parsedValueArray[n - 1].convert(font) : null;
            Size size2 = parsedValueArray[n++] != null ? (Size)parsedValueArray[n - 1].convert(font) : null;
            Size size3 = parsedValueArray[n++] != null ? (Size)parsedValueArray[n - 1].convert(font) : null;
            Size size4 = parsedValueArray[n++] != null ? (Size)parsedValueArray[n - 1].convert(font) : null;
            Size size5 = (Size)parsedValueArray[n++].convert(font);
            boolean bl2 = size5.getUnits().equals((Object)SizeUnits.PERCENT);
            boolean bl3 = size3 != null ? bl2 == size3.getUnits().equals((Object)SizeUnits.PERCENT) : (bl = true);
            boolean bl4 = bl && size4 != null ? bl2 == size4.getUnits().equals((Object)SizeUnits.PERCENT) : (bl = true);
            if (!bl) {
                throw new IllegalArgumentException("units do not agree");
            }
            CycleMethod cycleMethod = (CycleMethod)((Object)parsedValueArray[n++].convert(font));
            Stop[] stopArray = new Stop[parsedValueArray.length - n];
            for (int i = n; i < parsedValueArray.length; ++i) {
                stopArray[i - n] = (Stop)parsedValueArray[i].convert(font);
            }
            double d = 0.0;
            if (size != null) {
                d = size.pixels(font);
                if (size.getUnits().equals((Object)SizeUnits.PERCENT)) {
                    d = d * 360.0 % 360.0;
                }
            }
            paint = new RadialGradient(d, size2 != null ? size2.pixels() : 0.0, size3 != null ? size3.pixels() : 0.0, size4 != null ? size4.pixels() : 0.0, size5 != null ? size5.pixels() : 1.0, bl2, cycleMethod, stopArray);
            super.cacheValue(parsedValue, paint);
            return paint;
        }

        public String toString() {
            return "RadialGradientConverter";
        }
    }

    public static final class RepeatingImagePatternConverter
    extends StyleConverter<ParsedValue[], Paint> {
        public static RepeatingImagePatternConverter getInstance() {
            return Holder.REPEATING_IMAGE_PATTERN_INSTANCE;
        }

        private RepeatingImagePatternConverter() {
        }

        @Override
        public Paint convert(ParsedValue<ParsedValue[], Paint> parsedValue, Font font) {
            Paint paint = (Paint)super.getCachedValue(parsedValue);
            if (paint != null) {
                return paint;
            }
            ParsedValue[] parsedValueArray = parsedValue.getValue();
            ParsedValue parsedValue2 = parsedValueArray[0];
            String string = (String)parsedValue2.convert(font);
            if (string == null) {
                return null;
            }
            Image image = new Image(string);
            paint = new ImagePattern(image, 0.0, 0.0, image.getWidth(), image.getHeight(), false);
            super.cacheValue(parsedValue, paint);
            return paint;
        }

        public String toString() {
            return "RepeatingImagePatternConverter";
        }
    }

    public static final class ImagePatternConverter
    extends StyleConverter<ParsedValue[], Paint> {
        public static ImagePatternConverter getInstance() {
            return Holder.IMAGE_PATTERN_INSTANCE;
        }

        private ImagePatternConverter() {
        }

        @Override
        public Paint convert(ParsedValue<ParsedValue[], Paint> parsedValue, Font font) {
            Paint paint = (Paint)super.getCachedValue(parsedValue);
            if (paint != null) {
                return paint;
            }
            ParsedValue[] parsedValueArray = parsedValue.getValue();
            ParsedValue parsedValue2 = parsedValueArray[0];
            String string = (String)parsedValue2.convert(font);
            if (parsedValueArray.length == 1) {
                return new ImagePattern(StyleManager.getInstance().getCachedImage(string));
            }
            Size size = (Size)parsedValueArray[1].convert(font);
            Size size2 = (Size)parsedValueArray[2].convert(font);
            Size size3 = (Size)parsedValueArray[3].convert(font);
            Size size4 = (Size)parsedValueArray[4].convert(font);
            boolean bl = parsedValueArray.length < 6 ? true : (Boolean)parsedValueArray[5].getValue();
            paint = new ImagePattern(new Image(string), size.getValue(), size2.getValue(), size3.getValue(), size4.getValue(), bl);
            super.cacheValue(parsedValue, paint);
            return paint;
        }

        public String toString() {
            return "ImagePatternConverter";
        }
    }

    public static final class LinearGradientConverter
    extends StyleConverter<ParsedValue[], Paint> {
        public static LinearGradientConverter getInstance() {
            return Holder.LINEAR_GRADIENT_INSTANCE;
        }

        private LinearGradientConverter() {
        }

        @Override
        public Paint convert(ParsedValue<ParsedValue[], Paint> parsedValue, Font font) {
            Paint paint = (Paint)super.getCachedValue(parsedValue);
            if (paint != null) {
                return paint;
            }
            ParsedValue[] parsedValueArray = parsedValue.getValue();
            int n = 0;
            Size size = (Size)parsedValueArray[n++].convert(font);
            Size size2 = (Size)parsedValueArray[n++].convert(font);
            Size size3 = (Size)parsedValueArray[n++].convert(font);
            Size size4 = (Size)parsedValueArray[n++].convert(font);
            boolean bl = size.getUnits() == SizeUnits.PERCENT && size.getUnits() == size2.getUnits() && size.getUnits() == size3.getUnits() && size.getUnits() == size4.getUnits();
            CycleMethod cycleMethod = (CycleMethod)((Object)parsedValueArray[n++].convert(font));
            Stop[] stopArray = new Stop[parsedValueArray.length - n];
            for (int i = n; i < parsedValueArray.length; ++i) {
                stopArray[i - n] = (Stop)parsedValueArray[i].convert(font);
            }
            paint = new LinearGradient(size.pixels(font), size2.pixels(font), size3.pixels(font), size4.pixels(font), bl, cycleMethod, stopArray);
            super.cacheValue(parsedValue, paint);
            return paint;
        }

        public String toString() {
            return "LinearGradientConverter";
        }
    }

    public static final class SequenceConverter
    extends StyleConverter<ParsedValue<?, Paint>[], Paint[]> {
        public static SequenceConverter getInstance() {
            return Holder.SEQUENCE_INSTANCE;
        }

        private SequenceConverter() {
        }

        @Override
        public Paint[] convert(ParsedValue<ParsedValue<?, Paint>[], Paint[]> parsedValue, Font font) {
            ParsedValue<?, Paint>[] parsedValueArray = parsedValue.getValue();
            Paint[] paintArray = new Paint[parsedValueArray.length];
            for (int i = 0; i < parsedValueArray.length; ++i) {
                paintArray[i] = parsedValueArray[i].convert(font);
            }
            return paintArray;
        }

        public String toString() {
            return "Paint.SequenceConverter";
        }
    }

    private static class Holder {
        static final PaintConverter INSTANCE = new PaintConverter();
        static final SequenceConverter SEQUENCE_INSTANCE = new SequenceConverter();
        static final LinearGradientConverter LINEAR_GRADIENT_INSTANCE = new LinearGradientConverter();
        static final ImagePatternConverter IMAGE_PATTERN_INSTANCE = new ImagePatternConverter();
        static final RepeatingImagePatternConverter REPEATING_IMAGE_PATTERN_INSTANCE = new RepeatingImagePatternConverter();
        static final RadialGradientConverter RADIAL_GRADIENT_INSTANCE = new RadialGradientConverter();

        private Holder() {
        }
    }
}

