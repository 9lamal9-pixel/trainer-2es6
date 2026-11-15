/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;

public interface StyleableProperty<T>
extends WritableValue<T> {
    public void applyStyle(StyleOrigin var1, T var2);

    public StyleOrigin getStyleOrigin();

    public CssMetaData<? extends Styleable, T> getCssMetaData();
}

