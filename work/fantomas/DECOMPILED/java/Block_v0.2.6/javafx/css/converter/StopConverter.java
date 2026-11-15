/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.StyleConverter;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

public final class StopConverter
extends StyleConverter<ParsedValue[], Stop> {
    public static StopConverter getInstance() {
        return Holder.INSTANCE;
    }

    private StopConverter() {
    }

    @Override
    public Stop convert(ParsedValue<ParsedValue[], Stop> parsedValue, Font font) {
        ParsedValue[] parsedValueArray = parsedValue.getValue();
        Double d = ((Size)parsedValueArray[0].convert(font)).pixels(font);
        Color color = (Color)parsedValueArray[1].convert(font);
        return new Stop(d, color);
    }

    public String toString() {
        return "StopConverter";
    }

    private static class Holder {
        static final StopConverter INSTANCE = new StopConverter();

        private Holder() {
        }
    }
}

