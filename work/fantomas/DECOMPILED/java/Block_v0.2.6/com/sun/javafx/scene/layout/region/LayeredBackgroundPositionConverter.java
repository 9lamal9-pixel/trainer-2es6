/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.text.Font;

public final class LayeredBackgroundPositionConverter
extends StyleConverter<ParsedValue<ParsedValue[], BackgroundPosition>[], BackgroundPosition[]> {
    private static final LayeredBackgroundPositionConverter LAYERED_BACKGROUND_POSITION_CONVERTER = new LayeredBackgroundPositionConverter();

    public static LayeredBackgroundPositionConverter getInstance() {
        return LAYERED_BACKGROUND_POSITION_CONVERTER;
    }

    private LayeredBackgroundPositionConverter() {
    }

    @Override
    public BackgroundPosition[] convert(ParsedValue<ParsedValue<ParsedValue[], BackgroundPosition>[], BackgroundPosition[]> parsedValue, Font font) {
        ParsedValue<ParsedValue[], BackgroundPosition>[] parsedValueArray = parsedValue.getValue();
        BackgroundPosition[] backgroundPositionArray = new BackgroundPosition[parsedValueArray.length];
        for (int i = 0; i < parsedValueArray.length; ++i) {
            backgroundPositionArray[i] = parsedValueArray[i].convert(font);
        }
        return backgroundPositionArray;
    }

    public String toString() {
        return "LayeredBackgroundPositionConverter";
    }
}

