/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import javafx.util.StringConverter;

public class BooleanStringConverter
extends StringConverter<Boolean> {
    @Override
    public Boolean fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return Boolean.valueOf(string);
    }

    @Override
    public String toString(Boolean bl) {
        if (bl == null) {
            return "";
        }
        return bl.toString();
    }
}

