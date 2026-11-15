/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.property.LongPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;

public abstract class StyleableLongProperty
extends LongPropertyBase
implements StyleableProperty<Number> {
    private StyleOrigin origin = null;

    public StyleableLongProperty() {
    }

    public StyleableLongProperty(long l) {
        super(l);
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
    public void set(long l) {
        super.set(l);
        this.origin = StyleOrigin.USER;
    }

    @Override
    public StyleOrigin getStyleOrigin() {
        return this.origin;
    }
}

