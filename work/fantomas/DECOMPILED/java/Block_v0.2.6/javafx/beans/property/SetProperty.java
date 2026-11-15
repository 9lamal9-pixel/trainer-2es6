/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.value.WritableSetValue;
import javafx.collections.ObservableSet;

public abstract class SetProperty<E>
extends ReadOnlySetProperty<E>
implements Property<ObservableSet<E>>,
WritableSetValue<E> {
    @Override
    public void setValue(ObservableSet<E> observableSet) {
        this.set(observableSet);
    }

    @Override
    public void bindBidirectional(Property<ObservableSet<E>> property) {
        Bindings.bindBidirectional(this, property);
    }

    @Override
    public void unbindBidirectional(Property<ObservableSet<E>> property) {
        Bindings.unbindBidirectional(this, property);
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("SetProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }
}

