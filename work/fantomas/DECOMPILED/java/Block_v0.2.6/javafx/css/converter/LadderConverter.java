/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import com.sun.javafx.util.Utils;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

public final class LadderConverter
extends StyleConverter<ParsedValue[], Color> {
    public static LadderConverter getInstance() {
        return Holder.INSTANCE;
    }

    private LadderConverter() {
    }

    @Override
    public Color convert(ParsedValue<ParsedValue[], Color> parsedValue, Font font) {
        ParsedValue[] parsedValueArray = parsedValue.getValue();
        Color color = (Color)parsedValueArray[0].convert(font);
        Stop[] stopArray = new Stop[parsedValueArray.length - 1];
        for (int i = 1; i < parsedValueArray.length; ++i) {
            stopArray[i - 1] = (Stop)parsedValueArray[i].convert(font);
        }
        return Utils.ladder(color, stopArray);
    }

    public String toString() {
        return "LadderConverter";
    }

    private static class Holder {
        static final LadderConverter INSTANCE = new LadderConverter();

        private Holder() {
        }
    }
}

