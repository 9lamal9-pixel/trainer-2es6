/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import javafx.util.StringConverter;

public class DefaultStringConverter
extends StringConverter<String> {
    @Override
    public String toString(String string) {
        return string != null ? string : "";
    }

    @Override
    public String fromString(String string) {
        return string;
    }
}

