/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import java.util.List;
import java.util.ListIterator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListExpression;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableList;

public abstract class ReadOnlyListProperty<E>
extends ListExpression<E>
implements ReadOnlyProperty<ObservableList<E>> {
    public void bindContentBidirectional(ObservableList<E> observableList) {
        Bindings.bindContentBidirectional(this, observableList);
    }

    public void unbindContentBidirectional(Object object) {
        Bindings.unbindContentBidirectional(this, object);
    }

    public void bindContent(ObservableList<E> observableList) {
        Bindings.bindContent(this, observableList);
    }

    public void unbindContent(Object object) {
        Bindings.unbindContent(this, object);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof List)) {
            return false;
        }
        List list = (List)object;
        if (this.size() != list.size()) {
            return false;
        }
        ListIterator listIterator = this.listIterator();
        ListIterator listIterator2 = list.listIterator();
        while (listIterator.hasNext() && listIterator2.hasNext()) {
            Object e = listIterator.next();
            Object e2 = listIterator2.next();
            if (e != null ? e.equals(e2) : e2 == null) continue;
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int n = 1;
        for (Object e : this) {
            n = 31 * n + (e == null ? 0 : e.hashCode());
        }
        return n;
    }

    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyListProperty [");
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

