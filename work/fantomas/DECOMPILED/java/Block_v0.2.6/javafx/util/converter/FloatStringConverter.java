/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import javafx.util.StringConverter;

public class FloatStringConverter
extends StringConverter<Float> {
    @Override
    public Float fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return Float.valueOf(string);
    }

    @Override
    public String toString(Float f) {
        if (f == null) {
            return "";
        }
        return Float.toString(f.floatValue());
    }
}

