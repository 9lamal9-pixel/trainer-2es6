/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import javafx.util.StringConverter;

public class CharacterStringConverter
extends StringConverter<Character> {
    @Override
    public Character fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return Character.valueOf(string.charAt(0));
    }

    @Override
    public String toString(Character c) {
        if (c == null) {
            return "";
        }
        return c.toString();
    }
}

