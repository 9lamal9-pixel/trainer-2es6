/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.css.StyleConverter;
import javafx.scene.text.Font;

public class ParsedValue<V, T> {
    protected final V value;
    protected final StyleConverter<V, T> converter;

    public final V getValue() {
        return this.value;
    }

    public final StyleConverter<V, T> getConverter() {
        return this.converter;
    }

    public T convert(Font font) {
        return (T)(this.converter != null ? this.converter.convert(this, font) : this.value);
    }

    public boolean isContainsLookups() {
        return false;
    }

    public boolean isLookup() {
        return false;
    }

    protected ParsedValue(V v, StyleConverter<V, T> styleConverter) {
        this.value = v;
        this.converter = styleConverter;
    }
}

