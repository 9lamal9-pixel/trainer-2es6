/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.SetExpression;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableSet;

public abstract class ReadOnlySetProperty<E>
extends SetExpression<E>
implements ReadOnlyProperty<ObservableSet<E>> {
    public void bindContentBidirectional(ObservableSet<E> observableSet) {
        Bindings.bindContentBidirectional(this, observableSet);
    }

    public void unbindContentBidirectional(Object object) {
        Bindings.unbindContentBidirectional(this, object);
    }

    public void bindContent(ObservableSet<E> observableSet) {
        Bindings.bindContent(this, observableSet);
    }

    public void unbindContent(Object object) {
        Bindings.unbindContent(this, object);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Set)) {
            return false;
        }
        Set set = (Set)object;
        if (set.size() != this.size()) {
            return false;
        }
        try {
            return this.containsAll(set);
        }
        catch (ClassCastException classCastException) {
            return false;
        }
        catch (NullPointerException nullPointerException) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int n = 0;
        for (Object e : this) {
            if (e == null) continue;
            n += e.hashCode();
        }
        return n;
    }

    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlySetProperty [");
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

