/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.text.Format;
import java.text.ParsePosition;
import javafx.beans.NamedArg;
import javafx.util.StringConverter;

public class FormatStringConverter<T>
extends StringConverter<T> {
    final Format format;

    public FormatStringConverter(@NamedArg(value="format") Format format) {
        this.format = format;
    }

    @Override
    public T fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        Format format = this.getFormat();
        ParsePosition parsePosition = new ParsePosition(0);
        Object object = format.parseObject(string, parsePosition);
        if (parsePosition.getIndex() != string.length()) {
            throw new RuntimeException("Parsed string not according to the format");
        }
        return (T)object;
    }

    @Override
    public String toString(T t) {
        if (t == null) {
            return "";
        }
        Format format = this.getFormat();
        return format.format(t);
    }

    protected Format getFormat() {
        return this.format;
    }
}

