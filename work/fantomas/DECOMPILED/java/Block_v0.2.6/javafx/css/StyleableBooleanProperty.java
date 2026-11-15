/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;

public abstract class StyleableBooleanProperty
extends BooleanPropertyBase
implements StyleableProperty<Boolean> {
    private StyleOrigin origin = null;

    public StyleableBooleanProperty() {
    }

    public StyleableBooleanProperty(boolean bl) {
        super(bl);
    }

    @Override
    public void applyStyle(StyleOrigin styleOrigin, Boolean bl) {
        this.set(bl);
        this.origin = styleOrigin;
    }

    @Override
    public void bind(ObservableValue<? extends Boolean> observableValue) {
        super.bind(observableValue);
        this.origin = StyleOrigin.USER;
    }

    @Override
    public void set(boolean bl) {
        super.set(bl);
        this.origin = StyleOrigin.USER;
    }

    @Override
    public StyleOrigin getStyleOrigin() {
        return this.origin;
    }
}

