/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.MapExpression;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableMap;

public abstract class ReadOnlyMapProperty<K, V>
extends MapExpression<K, V>
implements ReadOnlyProperty<ObservableMap<K, V>> {
    public void bindContentBidirectional(ObservableMap<K, V> observableMap) {
        Bindings.bindContentBidirectional(this, observableMap);
    }

    public void unbindContentBidirectional(Object object) {
        Bindings.unbindContentBidirectional(this, object);
    }

    public void bindContent(ObservableMap<K, V> observableMap) {
        Bindings.bindContent(this, observableMap);
    }

    public void unbindContent(Object object) {
        Bindings.unbindContent(this, object);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Map)) {
            return false;
        }
        Map map = (Map)object;
        if (map.size() != this.size()) {
            return false;
        }
        try {
            for (Map.Entry entry : this.entrySet()) {
                Object k = entry.getKey();
                Object v = entry.getValue();
                if (!(v == null ? map.get(k) != null || !map.containsKey(k) : !v.equals(map.get(k)))) continue;
                return false;
            }
        }
        catch (ClassCastException classCastException) {
            return false;
        }
        catch (NullPointerException nullPointerException) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int n = 0;
        for (Map.Entry entry : this.entrySet()) {
            n += entry.hashCode();
        }
        return n;
    }

    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyMapProperty [");
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

