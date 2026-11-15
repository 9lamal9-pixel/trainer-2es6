/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;

public interface Property<T>
extends ReadOnlyProperty<T>,
WritableValue<T> {
    public void bind(ObservableValue<? extends T> var1);

    public void unbind();

    public boolean isBound();

    public void bindBidirectional(Property<T> var1);

    public void unbindBidirectional(Property<T> var1);
}

