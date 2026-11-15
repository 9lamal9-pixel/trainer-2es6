/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;
import javafx.util.Duration;

public final class DurationConverter
extends StyleConverter<ParsedValue<?, Size>, Duration> {
    public static StyleConverter<ParsedValue<?, Size>, Duration> getInstance() {
        return Holder.INSTANCE;
    }

    private DurationConverter() {
    }

    @Override
    public Duration convert(ParsedValue<ParsedValue<?, Size>, Duration> parsedValue, Font font) {
        ParsedValue<?, Size> parsedValue2 = parsedValue.getValue();
        Size size = parsedValue2.convert(font);
        double d = size.getValue();
        Duration duration = null;
        if (d < Double.POSITIVE_INFINITY) {
            switch (size.getUnits()) {
                case S: {
                    duration = Duration.seconds(d);
                    break;
                }
                case MS: {
                    duration = Duration.millis(d);
                    break;
                }
                default: {
                    duration = Duration.UNKNOWN;
                    break;
                }
            }
        } else {
            duration = Duration.INDEFINITE;
        }
        return duration;
    }

    public String toString() {
        return "DurationConverter";
    }

    private static class Holder {
        static final DurationConverter INSTANCE = new DurationConverter();

        private Holder() {
        }
    }
}

