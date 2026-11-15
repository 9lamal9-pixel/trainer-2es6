/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import com.sun.javafx.scene.layout.region.BorderImageWidthConverter;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.layout.BorderWidths;
import javafx.scene.text.Font;

public class BorderImageWidthsSequenceConverter
extends StyleConverter<ParsedValue<ParsedValue[], BorderWidths>[], BorderWidths[]> {
    private static final BorderImageWidthsSequenceConverter CONVERTER = new BorderImageWidthsSequenceConverter();

    public static BorderImageWidthsSequenceConverter getInstance() {
        return CONVERTER;
    }

    @Override
    public BorderWidths[] convert(ParsedValue<ParsedValue<ParsedValue[], BorderWidths>[], BorderWidths[]> parsedValue, Font font) {
        ParsedValue<ParsedValue[], BorderWidths>[] parsedValueArray = parsedValue.getValue();
        BorderWidths[] borderWidthsArray = new BorderWidths[parsedValueArray.length];
        for (int i = 0; i < parsedValueArray.length; ++i) {
            borderWidthsArray[i] = BorderImageWidthConverter.getInstance().convert((ParsedValue)parsedValueArray[i], font);
        }
        return borderWidthsArray;
    }

    public String toString() {
        return "BorderImageWidthsSequenceConverter";
    }
}

