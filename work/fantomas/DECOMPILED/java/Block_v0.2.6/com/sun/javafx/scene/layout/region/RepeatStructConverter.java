/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout.region;

import com.sun.javafx.scene.layout.region.RepeatStruct;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.css.converter.EnumConverter;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.text.Font;

public final class RepeatStructConverter
extends StyleConverter<ParsedValue<String, BackgroundRepeat>[][], RepeatStruct[]> {
    private static final RepeatStructConverter REPEAT_STRUCT_CONVERTER = new RepeatStructConverter();
    private final EnumConverter<BackgroundRepeat> repeatConverter = new EnumConverter<BackgroundRepeat>(BackgroundRepeat.class);

    public static RepeatStructConverter getInstance() {
        return REPEAT_STRUCT_CONVERTER;
    }

    private RepeatStructConverter() {
    }

    @Override
    public RepeatStruct[] convert(ParsedValue<ParsedValue<String, BackgroundRepeat>[][], RepeatStruct[]> parsedValue, Font font) {
        ParsedValue<String, BackgroundRepeat>[][] parsedValueArray = parsedValue.getValue();
        RepeatStruct[] repeatStructArray = new RepeatStruct[parsedValueArray.length];
        for (int i = 0; i < parsedValueArray.length; ++i) {
            ParsedValue<String, BackgroundRepeat>[] parsedValueArray2 = parsedValueArray[i];
            BackgroundRepeat backgroundRepeat = (BackgroundRepeat)((Object)this.repeatConverter.convert((ParsedValue)parsedValueArray2[0], (Font)null));
            BackgroundRepeat backgroundRepeat2 = (BackgroundRepeat)((Object)this.repeatConverter.convert((ParsedValue)parsedValueArray2[1], (Font)null));
            repeatStructArray[i] = new RepeatStruct(backgroundRepeat, backgroundRepeat2);
        }
        return repeatStructArray;
    }

    public String toString() {
        return "RepeatStructConverter";
    }
}

