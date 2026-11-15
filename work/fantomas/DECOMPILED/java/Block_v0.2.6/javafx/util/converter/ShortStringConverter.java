/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import javafx.util.StringConverter;

public class ShortStringConverter
extends StringConverter<Short> {
    @Override
    public Short fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return Short.valueOf(string);
    }

    @Override
    public String toString(Short s) {
        if (s == null) {
            return "";
        }
        return Short.toString(s);
    }
}

