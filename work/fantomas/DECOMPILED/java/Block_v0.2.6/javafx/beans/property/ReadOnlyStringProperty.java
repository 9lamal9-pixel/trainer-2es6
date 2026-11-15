/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyProperty;

public abstract class ReadOnlyStringProperty
extends StringExpression
implements ReadOnlyProperty<String> {
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyStringProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        stringBuilder.append("value: ").append((String)this.get()).append("]");
        return stringBuilder.toString();
    }
}

