/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import javafx.util.StringConverter;

public class ByteStringConverter
extends StringConverter<Byte> {
    @Override
    public Byte fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return Byte.valueOf(string);
    }

    @Override
    public String toString(Byte by) {
        if (by == null) {
            return "";
        }
        return Byte.toString(by);
    }
}

