/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import com.sun.javafx.css.ParsedValueImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.StyleConverter;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;

public class BorderStyleConverter
extends StyleConverter<ParsedValue[], BorderStrokeStyle> {
    public static final ParsedValueImpl<ParsedValue[], Number[]> NONE = new ParsedValueImpl(null, null);
    public static final ParsedValueImpl<ParsedValue[], Number[]> HIDDEN = new ParsedValueImpl(null, null);
    public static final ParsedValueImpl<ParsedValue[], Number[]> DOTTED = new ParsedValueImpl(null, null);
    public static final ParsedValueImpl<ParsedValue[], Number[]> DASHED = new ParsedValueImpl(null, null);
    public static final ParsedValueImpl<ParsedValue[], Number[]> SOLID = new ParsedValueImpl(null, null);
    private static final BorderStyleConverter BORDER_STYLE_CONVERTER = new BorderStyleConverter();

    public static BorderStyleConverter getInstance() {
        return BORDER_STYLE_CONVERTER;
    }

    private BorderStyleConverter() {
    }

    @Override
    public BorderStrokeStyle convert(ParsedValue<ParsedValue[], BorderStrokeStyle> parsedValue, Font font) {
        double d;
        List<Double> list;
        boolean bl;
        ParsedValue[] parsedValueArray = parsedValue.getValue();
        ParsedValue parsedValue2 = parsedValueArray[0];
        boolean bl2 = bl = parsedValueArray[1] == null && parsedValueArray[2] == null && parsedValueArray[3] == null && parsedValueArray[4] == null && parsedValueArray[5] == null;
        if (NONE == parsedValue2) {
            return BorderStrokeStyle.NONE;
        }
        if (DOTTED == parsedValue2 && bl) {
            return BorderStrokeStyle.DOTTED;
        }
        if (DASHED == parsedValue2 && bl) {
            return BorderStrokeStyle.DASHED;
        }
        if (SOLID == parsedValue2 && bl) {
            return BorderStrokeStyle.SOLID;
        }
        ParsedValue[] parsedValueArray2 = (ParsedValue[])parsedValueArray[0].getValue();
        if (parsedValueArray2 == null) {
            list = DOTTED == parsedValue2 ? BorderStrokeStyle.DOTTED.getDashArray() : (DASHED == parsedValue2 ? BorderStrokeStyle.DASHED.getDashArray() : (SOLID == parsedValue2 ? BorderStrokeStyle.SOLID.getDashArray() : Collections.emptyList()));
        } else {
            list = new ArrayList<Double>(parsedValueArray2.length);
            for (int i = 0; i < parsedValueArray2.length; ++i) {
                Size size = (Size)parsedValueArray2[i].convert(font);
                list.add(size.pixels(font));
            }
        }
        double d2 = parsedValueArray[1] != null ? (Double)parsedValueArray[1].convert(font) : 0.0;
        StrokeType strokeType = parsedValueArray[2] != null ? (StrokeType)((Object)parsedValueArray[2].convert(font)) : StrokeType.INSIDE;
        StrokeLineJoin strokeLineJoin = parsedValueArray[3] != null ? (StrokeLineJoin)((Object)parsedValueArray[3].convert(font)) : StrokeLineJoin.MITER;
        double d3 = d = parsedValueArray[4] != null ? (Double)parsedValueArray[4].convert(font) : 10.0;
        StrokeLineCap strokeLineCap = parsedValueArray[5] != null ? (StrokeLineCap)((Object)parsedValueArray[5].convert(font)) : (DOTTED == parsedValue2 ? StrokeLineCap.ROUND : StrokeLineCap.BUTT);
        BorderStrokeStyle borderStrokeStyle = new BorderStrokeStyle(strokeType, strokeLineJoin, strokeLineCap, d, d2, list);
        if (BorderStrokeStyle.SOLID.equals(borderStrokeStyle)) {
            return BorderStrokeStyle.SOLID;
        }
        return borderStrokeStyle;
    }

    public String toString() {
        return "BorderStyleConverter";
    }
}

