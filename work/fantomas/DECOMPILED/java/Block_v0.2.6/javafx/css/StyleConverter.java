/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.layout.region.BackgroundPositionConverter;
import com.sun.javafx.scene.layout.region.BackgroundSizeConverter;
import com.sun.javafx.scene.layout.region.BorderImageSliceConverter;
import com.sun.javafx.scene.layout.region.BorderImageWidthConverter;
import com.sun.javafx.scene.layout.region.BorderImageWidthsSequenceConverter;
import com.sun.javafx.scene.layout.region.BorderStrokeStyleSequenceConverter;
import com.sun.javafx.scene.layout.region.BorderStyleConverter;
import com.sun.javafx.scene.layout.region.CornerRadiiConverter;
import com.sun.javafx.scene.layout.region.LayeredBackgroundPositionConverter;
import com.sun.javafx.scene.layout.region.LayeredBackgroundSizeConverter;
import com.sun.javafx.scene.layout.region.LayeredBorderPaintConverter;
import com.sun.javafx.scene.layout.region.LayeredBorderStyleConverter;
import com.sun.javafx.scene.layout.region.Margins;
import com.sun.javafx.scene.layout.region.RepeatStructConverter;
import com.sun.javafx.scene.layout.region.SliceSequenceConverter;
import com.sun.javafx.scene.layout.region.StrokeBorderPaintConverter;
import com.sun.javafx.util.Logging;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javafx.css.CssMetaData;
import javafx.css.ParsedValue;
import javafx.css.Styleable;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.ColorConverter;
import javafx.css.converter.CursorConverter;
import javafx.css.converter.DeriveColorConverter;
import javafx.css.converter.DeriveSizeConverter;
import javafx.css.converter.DurationConverter;
import javafx.css.converter.EffectConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.FontConverter;
import javafx.css.converter.InsetsConverter;
import javafx.css.converter.LadderConverter;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StopConverter;
import javafx.css.converter.StringConverter;
import javafx.css.converter.URLConverter;
import javafx.geometry.Insets;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class StyleConverter<F, T> {
    private static Map<ParsedValue, Object> cache;
    private static Map<String, StyleConverter<?, ?>> tmap;

    public T convert(ParsedValue<F, T> parsedValue, Font font) {
        return (T)parsedValue.getValue();
    }

    public static StyleConverter<String, Boolean> getBooleanConverter() {
        return BooleanConverter.getInstance();
    }

    public static StyleConverter<?, Duration> getDurationConverter() {
        return DurationConverter.getInstance();
    }

    public static StyleConverter<String, Color> getColorConverter() {
        return ColorConverter.getInstance();
    }

    public static StyleConverter<ParsedValue[], Effect> getEffectConverter() {
        return EffectConverter.getInstance();
    }

    public static <E extends Enum<E>> StyleConverter<String, E> getEnumConverter(Class<E> clazz) {
        EnumConverter<E> enumConverter = new EnumConverter<E>(clazz);
        return enumConverter;
    }

    public static StyleConverter<ParsedValue[], Font> getFontConverter() {
        return FontConverter.getInstance();
    }

    public static StyleConverter<ParsedValue[], Insets> getInsetsConverter() {
        return InsetsConverter.getInstance();
    }

    public static StyleConverter<ParsedValue<?, Paint>, Paint> getPaintConverter() {
        return PaintConverter.getInstance();
    }

    public static StyleConverter<?, Number> getSizeConverter() {
        return SizeConverter.getInstance();
    }

    public static StyleConverter<String, String> getStringConverter() {
        return StringConverter.getInstance();
    }

    public static StyleConverter<ParsedValue[], String> getUrlConverter() {
        return URLConverter.getInstance();
    }

    public T convert(Map<CssMetaData<? extends Styleable, ?>, Object> map) {
        return null;
    }

    public void writeBinary(DataOutputStream dataOutputStream, StringStore stringStore) throws IOException {
        String string = this.getClass().getName();
        int n = stringStore.addString(string);
        dataOutputStream.writeShort(n);
    }

    public static void clearCache() {
        if (cache != null) {
            cache.clear();
        }
    }

    protected T getCachedValue(ParsedValue parsedValue) {
        if (cache != null) {
            return (T)cache.get(parsedValue);
        }
        return null;
    }

    protected void cacheValue(ParsedValue parsedValue, Object object) {
        if (cache == null) {
            cache = new WeakHashMap<ParsedValue, Object>();
        }
        cache.put(parsedValue, object);
    }

    public static StyleConverter<?, ?> readBinary(DataInputStream dataInputStream, String[] stringArray) throws IOException {
        short s = dataInputStream.readShort();
        Object object = stringArray[s];
        if (object == null || ((String)object).isEmpty()) {
            return null;
        }
        if (((String)object).startsWith("com.sun.javafx.css.converters.")) {
            object = "javafx.css.converter." + ((String)object).substring("com.sun.javafx.css.converters.".length());
        }
        if (((String)object).startsWith("javafx.css.converter.EnumConverter")) {
            return EnumConverter.readBinary(dataInputStream, stringArray);
        }
        if (tmap == null || !tmap.containsKey(object)) {
            PlatformLogger platformLogger;
            StyleConverter<?, ?> styleConverter = StyleConverter.getInstance((String)object);
            if (styleConverter == null && (platformLogger = Logging.getCSSLogger()).isLoggable(PlatformLogger.Level.SEVERE)) {
                platformLogger.severe("could not deserialize " + (String)object);
            }
            if (styleConverter == null) {
                System.err.println("could not deserialize " + (String)object);
            }
            if (tmap == null) {
                tmap = new HashMap();
            }
            tmap.put((String)object, styleConverter);
            return styleConverter;
        }
        return tmap.get(object);
    }

    static StyleConverter<?, ?> getInstance(String string) {
        StyleConverter styleConverter = null;
        switch (string) {
            case "javafx.css.converter.BooleanConverter": {
                styleConverter = BooleanConverter.getInstance();
                break;
            }
            case "javafx.css.converter.ColorConverter": {
                styleConverter = ColorConverter.getInstance();
                break;
            }
            case "javafx.css.converter.CursorConverter": {
                styleConverter = CursorConverter.getInstance();
                break;
            }
            case "javafx.css.converter.EffectConverter": {
                styleConverter = EffectConverter.getInstance();
                break;
            }
            case "javafx.css.converter.EffectConverter$DropShadowConverter": {
                styleConverter = EffectConverter.DropShadowConverter.getInstance();
                break;
            }
            case "javafx.css.converter.EffectConverter$InnerShadowConverter": {
                styleConverter = EffectConverter.InnerShadowConverter.getInstance();
                break;
            }
            case "javafx.css.converter.FontConverter": {
                styleConverter = FontConverter.getInstance();
                break;
            }
            case "javafx.css.converter.FontConverter$FontStyleConverter": 
            case "javafx.css.converter.FontConverter$StyleConverter": {
                styleConverter = FontConverter.FontStyleConverter.getInstance();
                break;
            }
            case "javafx.css.converter.FontConverter$FontWeightConverter": 
            case "javafx.css.converter.FontConverter$WeightConverter": {
                styleConverter = FontConverter.FontWeightConverter.getInstance();
                break;
            }
            case "javafx.css.converter.FontConverter$FontSizeConverter": 
            case "javafx.css.converter.FontConverter$SizeConverter": {
                styleConverter = FontConverter.FontSizeConverter.getInstance();
                break;
            }
            case "javafx.css.converter.InsetsConverter": {
                styleConverter = InsetsConverter.getInstance();
                break;
            }
            case "javafx.css.converter.InsetsConverter$SequenceConverter": {
                styleConverter = InsetsConverter.SequenceConverter.getInstance();
                break;
            }
            case "javafx.css.converter.PaintConverter": {
                styleConverter = PaintConverter.getInstance();
                break;
            }
            case "javafx.css.converter.PaintConverter$SequenceConverter": {
                styleConverter = PaintConverter.SequenceConverter.getInstance();
                break;
            }
            case "javafx.css.converter.PaintConverter$LinearGradientConverter": {
                styleConverter = PaintConverter.LinearGradientConverter.getInstance();
                break;
            }
            case "javafx.css.converter.PaintConverter$RadialGradientConverter": {
                styleConverter = PaintConverter.RadialGradientConverter.getInstance();
                break;
            }
            case "javafx.css.converter.SizeConverter": {
                styleConverter = SizeConverter.getInstance();
                break;
            }
            case "javafx.css.converter.SizeConverter$SequenceConverter": {
                styleConverter = SizeConverter.SequenceConverter.getInstance();
                break;
            }
            case "javafx.css.converter.StringConverter": {
                styleConverter = StringConverter.getInstance();
                break;
            }
            case "javafx.css.converter.StringConverter$SequenceConverter": {
                styleConverter = StringConverter.SequenceConverter.getInstance();
                break;
            }
            case "javafx.css.converter.URLConverter": {
                styleConverter = URLConverter.getInstance();
                break;
            }
            case "javafx.css.converter.URLConverter$SequenceConverter": {
                styleConverter = URLConverter.SequenceConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.BackgroundPositionConverter": 
            case "com.sun.javafx.scene.layout.region.BackgroundImage$BackgroundPositionConverter": {
                styleConverter = BackgroundPositionConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.BackgroundSizeConverter": 
            case "com.sun.javafx.scene.layout.region.BackgroundImage$BackgroundSizeConverter": {
                styleConverter = BackgroundSizeConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.BorderImageSliceConverter": 
            case "com.sun.javafx.scene.layout.region.BorderImage$SliceConverter": {
                styleConverter = BorderImageSliceConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.BorderImageWidthConverter": {
                styleConverter = BorderImageWidthConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.BorderImageWidthsSequenceConverter": {
                styleConverter = BorderImageWidthsSequenceConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.BorderStrokeStyleSequenceConverter": 
            case "com.sun.javafx.scene.layout.region.StrokeBorder$BorderStyleSequenceConverter": {
                styleConverter = BorderStrokeStyleSequenceConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.BorderStyleConverter": 
            case "com.sun.javafx.scene.layout.region.StrokeBorder$BorderStyleConverter": {
                styleConverter = BorderStyleConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.LayeredBackgroundPositionConverter": 
            case "com.sun.javafx.scene.layout.region.BackgroundImage$LayeredBackgroundPositionConverter": {
                styleConverter = LayeredBackgroundPositionConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.LayeredBackgroundSizeConverter": 
            case "com.sun.javafx.scene.layout.region.BackgroundImage$LayeredBackgroundSizeConverter": {
                styleConverter = LayeredBackgroundSizeConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.LayeredBorderPaintConverter": 
            case "com.sun.javafx.scene.layout.region.StrokeBorder$LayeredBorderPaintConverter": {
                styleConverter = LayeredBorderPaintConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.LayeredBorderStyleConverter": 
            case "com.sun.javafx.scene.layout.region.StrokeBorder$LayeredBorderStyleConverter": {
                styleConverter = LayeredBorderStyleConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.RepeatStructConverter": 
            case "com.sun.javafx.scene.layout.region.BackgroundImage$BackgroundRepeatConverter": 
            case "com.sun.javafx.scene.layout.region.BorderImage$RepeatConverter": {
                styleConverter = RepeatStructConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.SliceSequenceConverter": 
            case "com.sun.javafx.scene.layout.region.BorderImage$SliceSequenceConverter": {
                styleConverter = SliceSequenceConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.StrokeBorderPaintConverter": 
            case "com.sun.javafx.scene.layout.region.StrokeBorder$BorderPaintConverter": {
                styleConverter = StrokeBorderPaintConverter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.Margins$Converter": {
                styleConverter = Margins.Converter.getInstance();
                break;
            }
            case "com.sun.javafx.scene.layout.region.Margins$SequenceConverter": {
                styleConverter = Margins.SequenceConverter.getInstance();
                break;
            }
            case "javafx.scene.layout.CornerRadiiConverter": 
            case "com.sun.javafx.scene.layout.region.CornerRadiiConverter": {
                styleConverter = CornerRadiiConverter.getInstance();
                break;
            }
            case "javafx.css.converter.DeriveColorConverter": 
            case "com.sun.javafx.css.parser.DeriveColorConverter": {
                styleConverter = DeriveColorConverter.getInstance();
                break;
            }
            case "javafx.css.converter.DeriveSizeConverter": 
            case "com.sun.javafx.css.parser.DeriveSizeConverter": {
                styleConverter = DeriveSizeConverter.getInstance();
                break;
            }
            case "javafx.css.converter.LadderConverter": 
            case "com.sun.javafx.css.parser.LadderConverter": {
                styleConverter = LadderConverter.getInstance();
                break;
            }
            case "javafx.css.converter.StopConverter": 
            case "com.sun.javafx.css.parser.StopConverter": {
                styleConverter = StopConverter.getInstance();
                break;
            }
            default: {
                PlatformLogger platformLogger = Logging.getCSSLogger();
                if (!platformLogger.isLoggable(PlatformLogger.Level.SEVERE)) break;
                platformLogger.severe("StyleConverter : converter Class is null for : " + string);
            }
        }
        return styleConverter;
    }

    public static class StringStore {
        private final Map<String, Integer> stringMap = new HashMap<String, Integer>();
        public final List<String> strings = new ArrayList<String>();

        public int addString(String string) {
            Integer n = this.stringMap.get(string);
            if (n == null) {
                n = this.strings.size();
                this.strings.add(string);
                this.stringMap.put(string, n);
            }
            return n;
        }

        public void writeBinary(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeShort(this.strings.size());
            if (this.stringMap.containsKey(null)) {
                Integer n = this.stringMap.get(null);
                dataOutputStream.writeShort(n);
            } else {
                dataOutputStream.writeShort(-1);
            }
            for (int i = 0; i < this.strings.size(); ++i) {
                String string = this.strings.get(i);
                if (string == null) continue;
                dataOutputStream.writeUTF(string);
            }
        }

        public static String[] readBinary(DataInputStream dataInputStream) throws IOException {
            int n = dataInputStream.readShort();
            short s = dataInputStream.readShort();
            Object[] objectArray = new String[n];
            Arrays.fill(objectArray, null);
            for (int i = 0; i < n; ++i) {
                if (i == s) continue;
                objectArray[i] = dataInputStream.readUTF();
            }
            return objectArray;
        }
    }
}

