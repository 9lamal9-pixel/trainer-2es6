/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.text.Font;

public final class BorderStrokeStyleSequenceConverter
extends StyleConverter<ParsedValue<ParsedValue[], BorderStrokeStyle>[], BorderStrokeStyle[]> {
    private static final BorderStrokeStyleSequenceConverter BORDER_STYLE_SEQUENCE_CONVERTER = new BorderStrokeStyleSequenceConverter();

    public static BorderStrokeStyleSequenceConverter getInstance() {
        return BORDER_STYLE_SEQUENCE_CONVERTER;
    }

    private BorderStrokeStyleSequenceConverter() {
    }

    @Override
    public BorderStrokeStyle[] convert(ParsedValue<ParsedValue<ParsedValue[], BorderStrokeStyle>[], BorderStrokeStyle[]> parsedValue, Font font) {
        BorderStrokeStyle[] borderStrokeStyleArray;
        ParsedValue<ParsedValue[], BorderStrokeStyle>[] parsedValueArray = parsedValue.getValue();
        borderStrokeStyleArray = new BorderStrokeStyle[]{parsedValueArray.length > 0 ? parsedValueArray[0].convert(font) : BorderStrokeStyle.SOLID, parsedValueArray.length > 1 ? parsedValueArray[1].convert(font) : borderStrokeStyleArray[0], parsedValueArray.length > 2 ? parsedValueArray[2].convert(font) : borderStrokeStyleArray[0], parsedValueArray.length > 3 ? parsedValueArray[3].convert(font) : borderStrokeStyleArray[1]};
        return borderStrokeStyleArray;
    }

    public String toString() {
        return "BorderStrokeStyleSequenceConverter";
    }
}

