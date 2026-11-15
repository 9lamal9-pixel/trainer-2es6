/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import java.util.Map;
import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.StyleConverter;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class EffectConverter
extends StyleConverter<ParsedValue[], Effect> {
    private static Map<ParsedValue<ParsedValue[], Effect>, Effect> cache;

    public static StyleConverter<ParsedValue[], Effect> getInstance() {
        return Holder.EFFECT_CONVERTER;
    }

    @Override
    public Effect convert(ParsedValue<ParsedValue[], Effect> parsedValue, Font font) {
        throw new IllegalArgumentException("Parsed value is not an Effect");
    }

    protected EffectConverter() {
    }

    public String toString() {
        return "EffectConverter";
    }

    public static void clearCache() {
        if (cache != null) {
            cache.clear();
        }
    }

    public static final class InnerShadowConverter
    extends EffectConverter {
        public static InnerShadowConverter getInstance() {
            return Holder.INNER_SHADOW_INSTANCE;
        }

        private InnerShadowConverter() {
        }

        @Override
        public Effect convert(ParsedValue<ParsedValue[], Effect> parsedValue, Font font) {
            Effect effect = (Effect)super.getCachedValue(parsedValue);
            if (effect != null) {
                return effect;
            }
            ParsedValue[] parsedValueArray = parsedValue.getValue();
            BlurType blurType = (BlurType)((Object)parsedValueArray[0].convert(font));
            Color color = (Color)parsedValueArray[1].convert(font);
            Double d = ((Size)parsedValueArray[2].convert(font)).pixels(font);
            Double d2 = ((Size)parsedValueArray[3].convert(font)).pixels(font);
            Double d3 = ((Size)parsedValueArray[4].convert(font)).pixels(font);
            Double d4 = ((Size)parsedValueArray[5].convert(font)).pixels(font);
            InnerShadow innerShadow = new InnerShadow();
            if (blurType != null) {
                innerShadow.setBlurType(blurType);
            }
            if (color != null) {
                innerShadow.setColor(color);
            }
            if (d != null) {
                innerShadow.setRadius(d);
            }
            if (d2 != null) {
                innerShadow.setChoke(d2);
            }
            if (d3 != null) {
                innerShadow.setOffsetX(d3);
            }
            if (d4 != null) {
                innerShadow.setOffsetY(d4);
            }
            super.cacheValue(parsedValue, innerShadow);
            return innerShadow;
        }

        @Override
        public String toString() {
            return "InnerShadowConverter";
        }
    }

    public static final class DropShadowConverter
    extends EffectConverter {
        public static DropShadowConverter getInstance() {
            return Holder.DROP_SHADOW_INSTANCE;
        }

        private DropShadowConverter() {
        }

        @Override
        public Effect convert(ParsedValue<ParsedValue[], Effect> parsedValue, Font font) {
            Effect effect = (Effect)super.getCachedValue(parsedValue);
            if (effect != null) {
                return effect;
            }
            ParsedValue[] parsedValueArray = parsedValue.getValue();
            BlurType blurType = (BlurType)((Object)parsedValueArray[0].convert(font));
            Color color = (Color)parsedValueArray[1].convert(font);
            Double d = ((Size)parsedValueArray[2].convert(font)).pixels(font);
            Double d2 = ((Size)parsedValueArray[3].convert(font)).pixels(font);
            Double d3 = ((Size)parsedValueArray[4].convert(font)).pixels(font);
            Double d4 = ((Size)parsedValueArray[5].convert(font)).pixels(font);
            DropShadow dropShadow = new DropShadow();
            if (blurType != null) {
                dropShadow.setBlurType(blurType);
            }
            if (color != null) {
                dropShadow.setColor(color);
            }
            if (d2 != null) {
                dropShadow.setSpread(d2);
            }
            if (d != null) {
                dropShadow.setRadius(d);
            }
            if (d3 != null) {
                dropShadow.setOffsetX(d3);
            }
            if (d4 != null) {
                dropShadow.setOffsetY(d4);
            }
            super.cacheValue(parsedValue, dropShadow);
            return dropShadow;
        }

        @Override
        public String toString() {
            return "DropShadowConverter";
        }
    }

    private static class Holder {
        static final EffectConverter EFFECT_CONVERTER = new EffectConverter();
        static final DropShadowConverter DROP_SHADOW_INSTANCE = new DropShadowConverter();
        static final InnerShadowConverter INNER_SHADOW_INSTANCE = new InnerShadowConverter();

        private Holder() {
        }
    }
}

