/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class StrokeBorderPaintConverter
extends StyleConverter<ParsedValue<?, Paint>[], Paint[]> {
    private static final StrokeBorderPaintConverter STROKE_BORDER_PAINT_CONVERTER = new StrokeBorderPaintConverter();

    public static StrokeBorderPaintConverter getInstance() {
        return STROKE_BORDER_PAINT_CONVERTER;
    }

    private StrokeBorderPaintConverter() {
    }

    @Override
    public Paint[] convert(ParsedValue<ParsedValue<?, Paint>[], Paint[]> parsedValue, Font font) {
        Paint[] paintArray;
        ParsedValue<?, Paint>[] parsedValueArray = parsedValue.getValue();
        paintArray = new Paint[]{parsedValueArray.length > 0 ? parsedValueArray[0].convert(font) : Color.BLACK, parsedValueArray.length > 1 ? parsedValueArray[1].convert(font) : paintArray[0], parsedValueArray.length > 2 ? parsedValueArray[2].convert(font) : paintArray[0], parsedValueArray.length > 3 ? parsedValueArray[3].convert(font) : paintArray[1]};
        return paintArray;
    }

    public String toString() {
        return "StrokeBorderPaintConverter";
    }
}

