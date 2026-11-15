/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import javafx.util.StringConverter;

public class LongStringConverter
extends StringConverter<Long> {
    @Override
    public Long fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return Long.valueOf(string);
    }

    @Override
    public String toString(Long l) {
        if (l == null) {
            return "";
        }
        return Long.toString(l);
    }
}

