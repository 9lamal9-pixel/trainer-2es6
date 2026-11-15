/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.property.FloatPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;

public abstract class StyleableFloatProperty
extends FloatPropertyBase
implements StyleableProperty<Number> {
    private StyleOrigin origin = null;

    public StyleableFloatProperty() {
    }

    public StyleableFloatProperty(float f) {
        super(f);
    }

    @Override
    public void applyStyle(StyleOrigin styleOrigin, Number number) {
        this.setValue(number);
        this.origin = styleOrigin;
    }

    @Override
    public void bind(ObservableValue<? extends Number> observableValue) {
        super.bind(observableValue);
        this.origin = StyleOrigin.USER;
    }

    @Override
    public void set(float f) {
        super.set(f);
        this.origin = StyleOrigin.USER;
    }

    @Override
    public StyleOrigin getStyleOrigin() {
        return this.origin;
    }
}

