/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import com.sun.javafx.scene.layout.region.StrokeBorderPaintConverter;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public final class LayeredBorderPaintConverter
extends StyleConverter<ParsedValue<ParsedValue<?, Paint>[], Paint[]>[], Paint[][]> {
    private static final LayeredBorderPaintConverter LAYERED_BORDER_PAINT_CONVERTER = new LayeredBorderPaintConverter();

    public static LayeredBorderPaintConverter getInstance() {
        return LAYERED_BORDER_PAINT_CONVERTER;
    }

    private LayeredBorderPaintConverter() {
    }

    @Override
    public Paint[][] convert(ParsedValue<ParsedValue<ParsedValue<?, Paint>[], Paint[]>[], Paint[][]> parsedValue, Font font) {
        ParsedValue<ParsedValue<?, Paint>[], Paint[]>[] parsedValueArray = parsedValue.getValue();
        Paint[][] paintArray = new Paint[parsedValueArray.length][0];
        for (int i = 0; i < parsedValueArray.length; ++i) {
            paintArray[i] = StrokeBorderPaintConverter.getInstance().convert(parsedValueArray[i], font);
        }
        return paintArray;
    }

    public String toString() {
        return "LayeredBorderPaintConverter";
    }
}

