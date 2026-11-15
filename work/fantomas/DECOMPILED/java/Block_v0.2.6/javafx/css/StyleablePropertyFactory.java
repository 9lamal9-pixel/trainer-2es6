/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import javafx.beans.property.Property;
import javafx.css.CssMetaData;
import javafx.css.ParsedValue;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.SimpleStyleableStringProperty;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.geometry.Insets;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.util.Pair;

public class StyleablePropertyFactory<S extends Styleable> {
    private final Map<String, Pair<Class, CssMetaData<S, ?>>> metaDataMap;
    private final List<CssMetaData<? extends Styleable, ?>> unmodifiableMetaDataList;
    private final List<CssMetaData<? extends Styleable, ?>> metaDataList = new ArrayList();

    public StyleablePropertyFactory(List<CssMetaData<? extends Styleable, ?>> list) {
        this.unmodifiableMetaDataList = Collections.unmodifiableList(this.metaDataList);
        if (list != null) {
            this.metaDataList.addAll(list);
        }
        this.metaDataMap = new HashMap();
    }

    public final List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return this.unmodifiableMetaDataList;
    }

    public final StyleableProperty<Boolean> createStyleableBooleanProperty(S s, String string, String string2, Function<S, StyleableProperty<Boolean>> function, boolean bl, boolean bl2) {
        CssMetaData<S, Boolean> cssMetaData = this.createBooleanCssMetaData(string2, function, bl, bl2);
        return new SimpleStyleableBooleanProperty(cssMetaData, s, string, bl);
    }

    public final StyleableProperty<Boolean> createStyleableBooleanProperty(S s, String string, String string2, Function<S, StyleableProperty<Boolean>> function, boolean bl) {
        return this.createStyleableBooleanProperty(s, string, string2, function, bl, false);
    }

    public final StyleableProperty<Boolean> createStyleableBooleanProperty(S s, String string, String string2, Function<S, StyleableProperty<Boolean>> function) {
        return this.createStyleableBooleanProperty(s, string, string2, function, false, false);
    }

    public final StyleableProperty<Boolean> createStyleableBooleanProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Boolean.class, string2);
        return new SimpleStyleableBooleanProperty(cssMetaData, s, string, (Boolean)cssMetaData.getInitialValue(s));
    }

    public final StyleableProperty<Color> createStyleableColorProperty(S s, String string, String string2, Function<S, StyleableProperty<Color>> function, Color color, boolean bl) {
        CssMetaData<S, Color> cssMetaData = this.createColorCssMetaData(string2, function, color, bl);
        return new SimpleStyleableObjectProperty<Color>(cssMetaData, s, string, color);
    }

    public final StyleableProperty<Color> createStyleableColorProperty(S s, String string, String string2, Function<S, StyleableProperty<Color>> function, Color color) {
        return this.createStyleableColorProperty(s, string, string2, function, color, false);
    }

    public final StyleableProperty<Color> createStyleableColorProperty(S s, String string, String string2, Function<S, StyleableProperty<Color>> function) {
        return this.createStyleableColorProperty(s, string, string2, function, Color.BLACK, false);
    }

    public final StyleableProperty<Color> createStyleableColorProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Color.class, string2);
        return new SimpleStyleableObjectProperty<Color>(cssMetaData, s, string, (Color)cssMetaData.getInitialValue(s));
    }

    public final StyleableProperty<Duration> createStyleableDurationProperty(S s, String string, String string2, Function<S, StyleableProperty<Duration>> function, Duration duration, boolean bl) {
        CssMetaData<S, Duration> cssMetaData = this.createDurationCssMetaData(string2, function, duration, bl);
        return new SimpleStyleableObjectProperty<Duration>(cssMetaData, s, string, duration);
    }

    public final StyleableProperty<Duration> createStyleableDurationProperty(S s, String string, String string2, Function<S, StyleableProperty<Duration>> function, Duration duration) {
        return this.createStyleableDurationProperty(s, string, string2, function, duration, false);
    }

    public final StyleableProperty<Duration> createStyleableDurationProperty(S s, String string, String string2, Function<S, StyleableProperty<Duration>> function) {
        return this.createStyleableDurationProperty(s, string, string2, function, Duration.UNKNOWN, false);
    }

    public final StyleableProperty<Duration> createStyleableDurationProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Duration.class, string2);
        return new SimpleStyleableObjectProperty<Duration>(cssMetaData, s, string, (Duration)cssMetaData.getInitialValue(s));
    }

    public final <E extends Effect> StyleableProperty<E> createStyleableEffectProperty(S s, String string, String string2, Function<S, StyleableProperty<E>> function, E e, boolean bl) {
        CssMetaData<S, E> cssMetaData = this.createEffectCssMetaData(string2, function, e, bl);
        return new SimpleStyleableObjectProperty<E>(cssMetaData, s, string, e);
    }

    public final <E extends Effect> StyleableProperty<E> createStyleableEffectProperty(S s, String string, String string2, Function<S, StyleableProperty<E>> function, E e) {
        return this.createStyleableEffectProperty(s, string, string2, function, e, false);
    }

    public final <E extends Effect> StyleableProperty<E> createStyleableEffectProperty(S s, String string, String string2, Function<S, StyleableProperty<E>> function) {
        return this.createStyleableEffectProperty(s, string, string2, function, null, false);
    }

    public final StyleableProperty<Effect> createStyleableEffectProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Effect.class, string2);
        return new SimpleStyleableObjectProperty<Effect>(cssMetaData, s, string, (Effect)cssMetaData.getInitialValue(s));
    }

    public final <E extends Enum<E>> StyleableProperty<E> createStyleableEnumProperty(S s, String string, String string2, Function<S, StyleableProperty<E>> function, Class<E> clazz, E e, boolean bl) {
        CssMetaData<S, E> cssMetaData = this.createEnumCssMetaData(clazz, string2, function, e, bl);
        return new SimpleStyleableObjectProperty<E>(cssMetaData, s, string, e);
    }

    public final <E extends Enum<E>> StyleableProperty<E> createStyleableEnumProperty(S s, String string, String string2, Function<S, StyleableProperty<E>> function, Class<E> clazz, E e) {
        return this.createStyleableEnumProperty(s, string, string2, function, clazz, e, false);
    }

    public final <E extends Enum<E>> StyleableProperty<E> createStyleableEnumProperty(S s, String string, String string2, Function<S, StyleableProperty<E>> function, Class<E> clazz) {
        return this.createStyleableEnumProperty(s, string, string2, function, clazz, null, false);
    }

    public final <E extends Enum<E>> StyleableProperty<E> createStyleableEffectProperty(S s, String string, String string2, Class<E> clazz) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(clazz, string2);
        return new SimpleStyleableObjectProperty<Enum>(cssMetaData, s, string, (Enum)cssMetaData.getInitialValue(s));
    }

    public final StyleableProperty<Font> createStyleableFontProperty(S s, String string, String string2, Function<S, StyleableProperty<Font>> function, Font font, boolean bl) {
        CssMetaData<S, Font> cssMetaData = this.createFontCssMetaData(string2, function, font, bl);
        return new SimpleStyleableObjectProperty<Font>(cssMetaData, s, string, font);
    }

    public final StyleableProperty<Font> createStyleableFontProperty(S s, String string, String string2, Function<S, StyleableProperty<Font>> function, Font font) {
        return this.createStyleableFontProperty(s, string, string2, function, font, true);
    }

    public final StyleableProperty<Font> createStyleableFontProperty(S s, String string, String string2, Function<S, StyleableProperty<Font>> function) {
        return this.createStyleableFontProperty(s, string, string2, function, Font.getDefault(), true);
    }

    public final StyleableProperty<Font> createStyleableFontProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Font.class, string2);
        return new SimpleStyleableObjectProperty<Font>(cssMetaData, s, string, (Font)cssMetaData.getInitialValue(s));
    }

    public final StyleableProperty<Insets> createStyleableInsetsProperty(S s, String string, String string2, Function<S, StyleableProperty<Insets>> function, Insets insets, boolean bl) {
        CssMetaData<S, Insets> cssMetaData = this.createInsetsCssMetaData(string2, function, insets, bl);
        return new SimpleStyleableObjectProperty<Insets>(cssMetaData, s, string, insets);
    }

    public final StyleableProperty<Insets> createStyleableInsetsProperty(S s, String string, String string2, Function<S, StyleableProperty<Insets>> function, Insets insets) {
        return this.createStyleableInsetsProperty(s, string, string2, function, insets, false);
    }

    public final StyleableProperty<Insets> createStyleableInsetsProperty(S s, String string, String string2, Function<S, StyleableProperty<Insets>> function) {
        return this.createStyleableInsetsProperty(s, string, string2, function, Insets.EMPTY, false);
    }

    public final StyleableProperty<Insets> createStyleableInsetsProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Insets.class, string2);
        return new SimpleStyleableObjectProperty<Insets>(cssMetaData, s, string, (Insets)cssMetaData.getInitialValue(s));
    }

    public final StyleableProperty<Paint> createStyleablePaintProperty(S s, String string, String string2, Function<S, StyleableProperty<Paint>> function, Paint paint, boolean bl) {
        CssMetaData<S, Paint> cssMetaData = this.createPaintCssMetaData(string2, function, paint, bl);
        return new SimpleStyleableObjectProperty<Paint>(cssMetaData, s, string, paint);
    }

    public final StyleableProperty<Paint> createStyleablePaintProperty(S s, String string, String string2, Function<S, StyleableProperty<Paint>> function, Paint paint) {
        return this.createStyleablePaintProperty(s, string, string2, function, paint, false);
    }

    public final StyleableProperty<Paint> createStyleablePaintProperty(S s, String string, String string2, Function<S, StyleableProperty<Paint>> function) {
        return this.createStyleablePaintProperty(s, string, string2, function, Color.BLACK, false);
    }

    public final StyleableProperty<Paint> createStyleablePaintProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Paint.class, string2);
        return new SimpleStyleableObjectProperty<Paint>(cssMetaData, s, string, (Paint)cssMetaData.getInitialValue(s));
    }

    public final StyleableProperty<Number> createStyleableNumberProperty(S s, String string, String string2, Function<S, StyleableProperty<Number>> function, Number number, boolean bl) {
        CssMetaData<S, Number> cssMetaData = this.createSizeCssMetaData(string2, function, number, bl);
        return new SimpleStyleableObjectProperty<Number>(cssMetaData, s, string, number);
    }

    public final StyleableProperty<Number> createStyleableNumberProperty(S s, String string, String string2, Function<S, StyleableProperty<Number>> function, Number number) {
        return this.createStyleableNumberProperty(s, string, string2, function, number, false);
    }

    public final StyleableProperty<Number> createStyleableNumberProperty(S s, String string, String string2, Function<S, StyleableProperty<Number>> function) {
        return this.createStyleableNumberProperty(s, string, string2, function, 0.0, false);
    }

    public final StyleableProperty<Number> createStyleableNumberProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Number.class, string2);
        return new SimpleStyleableObjectProperty<Number>(cssMetaData, s, string, (Number)cssMetaData.getInitialValue(s));
    }

    public final StyleableProperty<String> createStyleableStringProperty(S s, String string, String string2, Function<S, StyleableProperty<String>> function, String string3, boolean bl) {
        CssMetaData<S, String> cssMetaData = this.createStringCssMetaData(string2, function, string3, bl);
        return new SimpleStyleableStringProperty(cssMetaData, s, string, string3);
    }

    public final StyleableProperty<String> createStyleableStringProperty(S s, String string, String string2, Function<S, StyleableProperty<String>> function, String string3) {
        return this.createStyleableStringProperty(s, string, string2, function, string3, false);
    }

    public final StyleableProperty<String> createStyleableStringProperty(S s, String string, String string2, Function<S, StyleableProperty<String>> function) {
        return this.createStyleableStringProperty(s, string, string2, function, null, false);
    }

    public final StyleableProperty<String> createStyleableStringProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(String.class, string2);
        return new SimpleStyleableStringProperty(cssMetaData, s, string, (String)cssMetaData.getInitialValue(s));
    }

    public final StyleableProperty<String> createStyleableUrlProperty(S s, String string, String string2, Function<S, StyleableProperty<String>> function, String string3, boolean bl) {
        CssMetaData<S, String> cssMetaData = this.createUrlCssMetaData(string2, function, string3, bl);
        return new SimpleStyleableStringProperty(cssMetaData, s, string, string3);
    }

    public final StyleableProperty<String> createStyleableUrlProperty(S s, String string, String string2, Function<S, StyleableProperty<String>> function, String string3) {
        return this.createStyleableUrlProperty(s, string, string2, function, string3, false);
    }

    public final StyleableProperty<String> createStyleableUrlProperty(S s, String string, String string2, Function<S, StyleableProperty<String>> function) {
        return this.createStyleableUrlProperty(s, string, string2, function, null, false);
    }

    public final StyleableProperty<String> createStyleableUrlProperty(S s, String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("cssProperty cannot be null or empty string");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(String.class, string2);
        return new SimpleStyleableStringProperty(cssMetaData, s, string, (String)cssMetaData.getInitialValue(s));
    }

    public final CssMetaData<S, Boolean> createBooleanCssMetaData(String string2, Function<S, StyleableProperty<Boolean>> function, boolean bl, boolean bl2) {
        if (string2 == null || string2.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Boolean.class, string2, string -> {
            StyleConverter<String, Boolean> styleConverter = StyleConverter.getBooleanConverter();
            return new SimpleCssMetaData((String)string, function, styleConverter, Boolean.valueOf(bl), bl2);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, Boolean> createBooleanCssMetaData(String string, Function<S, StyleableProperty<Boolean>> function, boolean bl) {
        return this.createBooleanCssMetaData(string, function, bl, false);
    }

    public final CssMetaData<S, Boolean> createBooleanCssMetaData(String string, Function<S, StyleableProperty<Boolean>> function) {
        return this.createBooleanCssMetaData(string, function, false, false);
    }

    public final CssMetaData<S, Color> createColorCssMetaData(String string, Function<S, StyleableProperty<Color>> function, Color color, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Color.class, string, string2 -> {
            StyleConverter<String, Color> styleConverter = StyleConverter.getColorConverter();
            return new SimpleCssMetaData(string, function, styleConverter, color, bl);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, Color> createColorCssMetaData(String string, Function<S, StyleableProperty<Color>> function, Color color) {
        return this.createColorCssMetaData(string, function, color, false);
    }

    public final CssMetaData<S, Color> createColorCssMetaData(String string, Function<S, StyleableProperty<Color>> function) {
        return this.createColorCssMetaData(string, function, Color.BLACK, false);
    }

    public final CssMetaData<S, Duration> createDurationCssMetaData(String string, Function<S, StyleableProperty<Duration>> function, Duration duration, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Duration.class, string, string2 -> {
            StyleConverter<?, Duration> styleConverter = StyleConverter.getDurationConverter();
            return new SimpleCssMetaData(string, function, styleConverter, duration, bl);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, Duration> createDurationCssMetaData(String string, Function<S, StyleableProperty<Duration>> function, Duration duration) {
        return this.createDurationCssMetaData(string, function, duration, false);
    }

    public final CssMetaData<S, Duration> createDurationCssMetaData(String string, Function<S, StyleableProperty<Duration>> function) {
        return this.createDurationCssMetaData(string, function, Duration.UNKNOWN, false);
    }

    public final <E extends Effect> CssMetaData<S, E> createEffectCssMetaData(String string, Function<S, StyleableProperty<E>> function, E e, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Effect.class, string, string2 -> {
            StyleConverter<ParsedValue[], Effect> styleConverter = StyleConverter.getEffectConverter();
            return new SimpleCssMetaData(string, function, styleConverter, e, bl);
        });
        return cssMetaData;
    }

    public final <E extends Effect> CssMetaData<S, E> createEffectCssMetaData(String string, Function<S, StyleableProperty<E>> function, E e) {
        return this.createEffectCssMetaData(string, function, e, false);
    }

    public final <E extends Effect> CssMetaData<S, E> createEffectCssMetaData(String string, Function<S, StyleableProperty<E>> function) {
        return this.createEffectCssMetaData(string, function, null, false);
    }

    public final <E extends Enum<E>> CssMetaData<S, E> createEnumCssMetaData(Class<? extends Enum> clazz, String string, Function<S, StyleableProperty<E>> function, E e, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(clazz, string, string2 -> {
            EnumConverter enumConverter = new EnumConverter(clazz);
            return new SimpleCssMetaData(string, function, enumConverter, e, bl);
        });
        return cssMetaData;
    }

    public final <E extends Enum<E>> CssMetaData<S, E> createEnumCssMetaData(Class<? extends Enum> clazz, String string, Function<S, StyleableProperty<E>> function, E e) {
        return this.createEnumCssMetaData(clazz, string, function, e, false);
    }

    public final <E extends Enum<E>> CssMetaData<S, E> createEnumCssMetaData(Class<? extends Enum> clazz, String string, Function<S, StyleableProperty<E>> function) {
        return this.createEnumCssMetaData(clazz, string, function, null, false);
    }

    public final CssMetaData<S, Font> createFontCssMetaData(String string, Function<S, StyleableProperty<Font>> function, Font font, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Font.class, string, string2 -> {
            StyleConverter<ParsedValue[], Font> styleConverter = StyleConverter.getFontConverter();
            return new SimpleCssMetaData(string, function, styleConverter, font, bl);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, Font> createFontCssMetaData(String string, Function<S, StyleableProperty<Font>> function, Font font) {
        return this.createFontCssMetaData(string, function, font, true);
    }

    public final CssMetaData<S, Font> createFontCssMetaData(String string, Function<S, StyleableProperty<Font>> function) {
        return this.createFontCssMetaData(string, function, Font.getDefault(), true);
    }

    public final CssMetaData<S, Insets> createInsetsCssMetaData(String string, Function<S, StyleableProperty<Insets>> function, Insets insets, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Insets.class, string, string2 -> {
            StyleConverter<ParsedValue[], Insets> styleConverter = StyleConverter.getInsetsConverter();
            return new SimpleCssMetaData(string, function, styleConverter, insets, bl);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, Insets> createInsetsCssMetaData(String string, Function<S, StyleableProperty<Insets>> function, Insets insets) {
        return this.createInsetsCssMetaData(string, function, insets, false);
    }

    public final CssMetaData<S, Insets> createInsetsCssMetaData(String string, Function<S, StyleableProperty<Insets>> function) {
        return this.createInsetsCssMetaData(string, function, Insets.EMPTY, false);
    }

    public final CssMetaData<S, Paint> createPaintCssMetaData(String string, Function<S, StyleableProperty<Paint>> function, Paint paint, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Paint.class, string, string2 -> {
            StyleConverter<ParsedValue<?, Paint>, Paint> styleConverter = StyleConverter.getPaintConverter();
            return new SimpleCssMetaData(string, function, styleConverter, paint, bl);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, Paint> createPaintCssMetaData(String string, Function<S, StyleableProperty<Paint>> function, Paint paint) {
        return this.createPaintCssMetaData(string, function, paint, false);
    }

    public final CssMetaData<S, Paint> createPaintCssMetaData(String string, Function<S, StyleableProperty<Paint>> function) {
        return this.createPaintCssMetaData(string, function, Color.BLACK, false);
    }

    public final CssMetaData<S, Number> createSizeCssMetaData(String string, Function<S, StyleableProperty<Number>> function, Number number, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(Number.class, string, string2 -> {
            StyleConverter<?, Number> styleConverter = StyleConverter.getSizeConverter();
            return new SimpleCssMetaData(string, function, styleConverter, number, bl);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, Number> createSizeCssMetaData(String string, Function<S, StyleableProperty<Number>> function, Number number) {
        return this.createSizeCssMetaData(string, function, number, false);
    }

    public final CssMetaData<S, Number> createSizeCssMetaData(String string, Function<S, StyleableProperty<Number>> function) {
        return this.createSizeCssMetaData(string, function, 0.0, false);
    }

    public final CssMetaData<S, String> createStringCssMetaData(String string, Function<S, StyleableProperty<String>> function, String string2, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(String.class, string, string3 -> {
            StyleConverter<String, String> styleConverter = StyleConverter.getStringConverter();
            return new SimpleCssMetaData(string, function, styleConverter, string2, bl);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, String> createStringCssMetaData(String string, Function<S, StyleableProperty<String>> function, String string2) {
        return this.createStringCssMetaData(string, function, string2, false);
    }

    public final CssMetaData<S, String> createStringCssMetaData(String string, Function<S, StyleableProperty<String>> function) {
        return this.createStringCssMetaData(string, function, null, false);
    }

    public final CssMetaData<S, String> createUrlCssMetaData(String string, Function<S, StyleableProperty<String>> function, String string2, boolean bl) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("property cannot be null or empty string");
        }
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        CssMetaData<S, ?> cssMetaData = this.getCssMetaData(URL.class, string, string3 -> {
            StyleConverter<ParsedValue[], String> styleConverter = StyleConverter.getUrlConverter();
            return new SimpleCssMetaData(string, function, styleConverter, string2, bl);
        });
        return cssMetaData;
    }

    public final CssMetaData<S, String> createUrlCssMetaData(String string, Function<S, StyleableProperty<String>> function, String string2) {
        return this.createUrlCssMetaData(string, function, string2, false);
    }

    public final CssMetaData<S, String> createUrlCssMetaData(String string, Function<S, StyleableProperty<String>> function) {
        return this.createUrlCssMetaData(string, function, null, false);
    }

    void clearDataForTesting() {
        this.metaDataMap.clear();
        this.metaDataList.clear();
    }

    private CssMetaData<S, ?> getCssMetaData(Class clazz, String string) {
        return this.getCssMetaData(clazz, string, null);
    }

    private CssMetaData<S, ?> getCssMetaData(Class clazz, String string, Function<String, CssMetaData<S, ?>> function) {
        String string2 = string.toLowerCase();
        Pair<Class, CssMetaData<S, ?>> pair = this.metaDataMap.get(string2);
        if (pair != null) {
            if (pair.getKey() == clazz) {
                return pair.getValue();
            }
            throw new ClassCastException("CssMetaData value is not " + clazz + ": " + pair.getValue());
        }
        if (function == null) {
            throw new NoSuchElementException("No CssMetaData for " + string2);
        }
        CssMetaData<S, ?> cssMetaData = function.apply(string2);
        this.metaDataMap.put(string2, new Pair(clazz, cssMetaData));
        this.metaDataList.add(cssMetaData);
        return cssMetaData;
    }

    private static class SimpleCssMetaData<S extends Styleable, V>
    extends CssMetaData<S, V> {
        private final Function<S, StyleableProperty<V>> function;

        SimpleCssMetaData(String string, Function<S, StyleableProperty<V>> function, StyleConverter<?, V> styleConverter, V v, boolean bl) {
            super(string, styleConverter, v, bl);
            this.function = function;
        }

        @Override
        public final boolean isSettable(S s) {
            StyleableProperty<V> styleableProperty = this.getStyleableProperty(s);
            if (styleableProperty instanceof Property) {
                return !((Property)((Object)styleableProperty)).isBound();
            }
            return styleableProperty != null;
        }

        @Override
        public final StyleableProperty<V> getStyleableProperty(S s) {
            if (s != null) {
                StyleableProperty<V> styleableProperty = this.function.apply(s);
                return styleableProperty;
            }
            return null;
        }
    }
}

