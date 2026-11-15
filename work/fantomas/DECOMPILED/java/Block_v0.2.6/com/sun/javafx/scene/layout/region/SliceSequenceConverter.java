/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import com.sun.javafx.scene.layout.region.BorderImageSliceConverter;
import com.sun.javafx.scene.layout.region.BorderImageSlices;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;

public final class SliceSequenceConverter
extends StyleConverter<ParsedValue<ParsedValue[], BorderImageSlices>[], BorderImageSlices[]> {
    private static final SliceSequenceConverter BORDER_IMAGE_SLICE_SEQUENCE_CONVERTER = new SliceSequenceConverter();

    public static SliceSequenceConverter getInstance() {
        return BORDER_IMAGE_SLICE_SEQUENCE_CONVERTER;
    }

    @Override
    public BorderImageSlices[] convert(ParsedValue<ParsedValue<ParsedValue[], BorderImageSlices>[], BorderImageSlices[]> parsedValue, Font font) {
        ParsedValue<ParsedValue[], BorderImageSlices>[] parsedValueArray = parsedValue.getValue();
        BorderImageSlices[] borderImageSlicesArray = new BorderImageSlices[parsedValueArray.length];
        for (int i = 0; i < parsedValueArray.length; ++i) {
            borderImageSlicesArray[i] = BorderImageSliceConverter.getInstance().convert((ParsedValue)parsedValueArray[i], font);
        }
        return borderImageSlicesArray;
    }

    public String toString() {
        return "BorderImageSliceSequenceConverter";
    }
}

