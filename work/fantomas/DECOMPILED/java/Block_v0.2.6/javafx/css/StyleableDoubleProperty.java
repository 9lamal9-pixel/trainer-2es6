/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.property.DoublePropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;

public abstract class StyleableDoubleProperty
extends DoublePropertyBase
implements StyleableProperty<Number> {
    private StyleOrigin origin = null;

    public StyleableDoubleProperty() {
    }

    public StyleableDoubleProperty(double d) {
        super(d);
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
    public void set(double d) {
        super.set(d);
        this.origin = StyleOrigin.USER;
    }

    @Override
    public StyleOrigin getStyleOrigin() {
        return this.origin;
    }
}

