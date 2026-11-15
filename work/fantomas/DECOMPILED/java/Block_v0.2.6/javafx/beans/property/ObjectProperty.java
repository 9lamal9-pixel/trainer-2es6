/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.WritableObjectValue;

public abstract class ObjectProperty<T>
extends ReadOnlyObjectProperty<T>
implements Property<T>,
WritableObjectValue<T> {
    @Override
    public void setValue(T t) {
        this.set(t);
    }

    @Override
    public void bindBidirectional(Property<T> property) {
        Bindings.bindBidirectional(this, property);
    }

    @Override
    public void unbindBidirectional(Property<T> property) {
        Bindings.unbindBidirectional(this, property);
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ObjectProperty [");
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

