/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.WritableListValue;
import javafx.collections.ObservableList;

public abstract class ListProperty<E>
extends ReadOnlyListProperty<E>
implements Property<ObservableList<E>>,
WritableListValue<E> {
    @Override
    public void setValue(ObservableList<E> observableList) {
        this.set(observableList);
    }

    @Override
    public void bindBidirectional(Property<ObservableList<E>> property) {
        Bindings.bindBidirectional(this, property);
    }

    @Override
    public void unbindBidirectional(Property<ObservableList<E>> property) {
        Bindings.unbindBidirectional(this, property);
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ListProperty [");
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

