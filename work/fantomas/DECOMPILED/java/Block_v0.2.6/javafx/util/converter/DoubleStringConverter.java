/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import javafx.util.StringConverter;

public class DoubleStringConverter
extends StringConverter<Double> {
    @Override
    public Double fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return Double.valueOf(string);
    }

    @Override
    public String toString(Double d) {
        if (d == null) {
            return "";
        }
        return Double.toString(d);
    }
}

