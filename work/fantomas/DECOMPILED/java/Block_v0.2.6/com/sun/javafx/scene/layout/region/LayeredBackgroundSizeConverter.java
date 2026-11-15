/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.text.Font;

public final class LayeredBackgroundSizeConverter
extends StyleConverter<ParsedValue<ParsedValue[], BackgroundSize>[], BackgroundSize[]> {
    private static final LayeredBackgroundSizeConverter LAYERED_BACKGROUND_SIZE_CONVERTER = new LayeredBackgroundSizeConverter();

    public static LayeredBackgroundSizeConverter getInstance() {
        return LAYERED_BACKGROUND_SIZE_CONVERTER;
    }

    private LayeredBackgroundSizeConverter() {
    }

    @Override
    public BackgroundSize[] convert(ParsedValue<ParsedValue<ParsedValue[], BackgroundSize>[], BackgroundSize[]> parsedValue, Font font) {
        ParsedValue<ParsedValue[], BackgroundSize>[] parsedValueArray = parsedValue.getValue();
        BackgroundSize[] backgroundSizeArray = new BackgroundSize[parsedValueArray.length];
        for (int i = 0; i < parsedValueArray.length; ++i) {
            backgroundSizeArray[i] = parsedValueArray[i].convert(font);
        }
        return backgroundSizeArray;
    }
}

