/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.ReadOnlyProperty;

public abstract class ReadOnlyObjectProperty<T>
extends ObjectExpression<T>
implements ReadOnlyProperty<T> {
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyObjectProperty [");
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

