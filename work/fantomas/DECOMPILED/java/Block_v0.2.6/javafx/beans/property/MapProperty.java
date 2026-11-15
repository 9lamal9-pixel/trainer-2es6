/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.value.WritableMapValue;
import javafx.collections.ObservableMap;

public abstract class MapProperty<K, V>
extends ReadOnlyMapProperty<K, V>
implements Property<ObservableMap<K, V>>,
WritableMapValue<K, V> {
    @Override
    public void setValue(ObservableMap<K, V> observableMap) {
        this.set(observableMap);
    }

    @Override
    public void bindBidirectional(Property<ObservableMap<K, V>> property) {
        Bindings.bindBidirectional(this, property);
    }

    @Override
    public void unbindBidirectional(Property<ObservableMap<K, V>> property) {
        Bindings.unbindBidirectional(this, property);
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("MapProperty [");
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

