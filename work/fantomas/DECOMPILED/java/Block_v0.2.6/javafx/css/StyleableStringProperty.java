/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.property.StringPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;

public abstract class StyleableStringProperty
extends StringPropertyBase
implements StyleableProperty<String> {
    private StyleOrigin origin = null;

    public StyleableStringProperty() {
    }

    public StyleableStringProperty(String string) {
        super(string);
    }

    @Override
    public void applyStyle(StyleOrigin styleOrigin, String string) {
        this.set(string);
        this.origin = styleOrigin;
    }

    @Override
    public void bind(ObservableValue<? extends String> observableValue) {
        super.bind(observableValue);
        this.origin = StyleOrigin.USER;
    }

    @Override
    public void set(String string) {
        super.set(string);
        this.origin = StyleOrigin.USER;
    }

    @Override
    public StyleOrigin getStyleOrigin() {
        return this.origin;
    }
}

