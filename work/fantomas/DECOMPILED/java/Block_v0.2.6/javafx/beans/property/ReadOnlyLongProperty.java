/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.LongExpression;
import javafx.beans.property.ReadOnlyLongPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyProperty;

public abstract class ReadOnlyLongProperty
extends LongExpression
implements ReadOnlyProperty<Number> {
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyLongProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }

    public static <T extends Number> ReadOnlyLongProperty readOnlyLongProperty(final ReadOnlyProperty<T> readOnlyProperty) {
        if (readOnlyProperty == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return readOnlyProperty instanceof ReadOnlyLongProperty ? (ReadOnlyLongProperty)readOnlyProperty : new ReadOnlyLongPropertyBase(){
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (this.valid) {
                    this.valid = false;
                    this.fireValueChangedEvent();
                }
            };
            {
                readOnlyProperty.addListener(new WeakInvalidationListener(this.listener));
            }

            @Override
            public long get() {
                this.valid = true;
                Number number = (Number)readOnlyProperty.getValue();
                return number == null ? 0L : number.longValue();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return readOnlyProperty.getName();
            }
        };
    }

    public ReadOnlyObjectProperty<Long> asObject() {
        return new ReadOnlyObjectPropertyBase<Long>(){
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (this.valid) {
                    this.valid = false;
                    this.fireValueChangedEvent();
                }
            };
            {
                ReadOnlyLongProperty.this.addListener(new WeakInvalidationListener(this.listener));
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return ReadOnlyLongProperty.this.getName();
            }

            @Override
            public Long get() {
                this.valid = true;
                return ReadOnlyLongProperty.this.getValue();
            }
        };
    }
}

