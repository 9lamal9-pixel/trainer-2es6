/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyProperty;

public abstract class ReadOnlyIntegerProperty
extends IntegerExpression
implements ReadOnlyProperty<Number> {
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyIntegerProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }

    public static <T extends Number> ReadOnlyIntegerProperty readOnlyIntegerProperty(final ReadOnlyProperty<T> readOnlyProperty) {
        if (readOnlyProperty == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return readOnlyProperty instanceof ReadOnlyIntegerProperty ? (ReadOnlyIntegerProperty)readOnlyProperty : new ReadOnlyIntegerPropertyBase(){
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
            public int get() {
                this.valid = true;
                Number number = (Number)readOnlyProperty.getValue();
                return number == null ? 0 : number.intValue();
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

    public ReadOnlyObjectProperty<Integer> asObject() {
        return new ReadOnlyObjectPropertyBase<Integer>(){
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (this.valid) {
                    this.valid = false;
                    this.fireValueChangedEvent();
                }
            };
            {
                ReadOnlyIntegerProperty.this.addListener(new WeakInvalidationListener(this.listener));
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return ReadOnlyIntegerProperty.this.getName();
            }

            @Override
            public Integer get() {
                this.valid = true;
                return ReadOnlyIntegerProperty.this.getValue();
            }
        };
    }
}

