/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import java.text.Format;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.WritableStringValue;
import javafx.util.StringConverter;

public abstract class StringProperty
extends ReadOnlyStringProperty
implements Property<String>,
WritableStringValue {
    @Override
    public void setValue(String string) {
        this.set(string);
    }

    @Override
    public void bindBidirectional(Property<String> property) {
        Bindings.bindBidirectional(this, property);
    }

    public void bindBidirectional(Property<?> property, Format format) {
        Bindings.bindBidirectional((Property<String>)this, property, format);
    }

    public <T> void bindBidirectional(Property<T> property, StringConverter<T> stringConverter) {
        Bindings.bindBidirectional((Property<String>)this, property, stringConverter);
    }

    @Override
    public void unbindBidirectional(Property<String> property) {
        Bindings.unbindBidirectional(this, property);
    }

    public void unbindBidirectional(Object object) {
        Bindings.unbindBidirectional((Object)this, object);
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("StringProperty [");
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

