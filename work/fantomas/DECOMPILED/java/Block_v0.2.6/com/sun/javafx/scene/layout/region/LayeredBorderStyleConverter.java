/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.text.Font;

public final class LayeredBorderStyleConverter
extends StyleConverter<ParsedValue<ParsedValue<ParsedValue[], BorderStrokeStyle>[], BorderStrokeStyle[]>[], BorderStrokeStyle[][]> {
    private static final LayeredBorderStyleConverter LAYERED_BORDER_STYLE_CONVERTER = new LayeredBorderStyleConverter();

    public static LayeredBorderStyleConverter getInstance() {
        return LAYERED_BORDER_STYLE_CONVERTER;
    }

    private LayeredBorderStyleConverter() {
    }

    @Override
    public BorderStrokeStyle[][] convert(ParsedValue<ParsedValue<ParsedValue<ParsedValue[], BorderStrokeStyle>[], BorderStrokeStyle[]>[], BorderStrokeStyle[][]> parsedValue, Font font) {
        ParsedValue<ParsedValue<ParsedValue[], BorderStrokeStyle>[], BorderStrokeStyle[]>[] parsedValueArray = parsedValue.getValue();
        BorderStrokeStyle[][] borderStrokeStyleArray = new BorderStrokeStyle[parsedValueArray.length][0];
        for (int i = 0; i < parsedValueArray.length; ++i) {
            borderStrokeStyleArray[i] = parsedValueArray[i].convert(font);
        }
        return borderStrokeStyleArray;
    }

    public String toString() {
        return "LayeredBorderStyleConverter";
    }
}

